package taidn.project.payment.app.services;

import taidn.project.payment.app.daos.PaymentDAO;
import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.BillState;
import taidn.project.payment.app.entities.Payment;
import taidn.project.payment.app.entities.PaymentState;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PaymentService {
    public static PaymentService INSTANCE = new PaymentService();
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    private final PaymentDAO paymentDao = PaymentDAO.INSTANCE;
    private final BillService billService = BillService.INSTANCE;
    private final AccountService accountService = AccountService.INSTANCE;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(8);

    private PaymentService() {
    }

    public List<Payment> getAllPayments() {
        return paymentDao.getAll();
    }

    public void payBills(List<Integer> billIds) {
        if (billIds.isEmpty()) {
            throw new RuntimeException("Pay failed. billIds is empty");
        }
        List<Bill> bills = getSortedBillsByDueDateASC(billIds);
        List<Bill> validBills = extractNotPaidBills(bills);
        List<Bill> invalidBills = extractPaidBills(bills);
        printInvalidBills(invalidBills);
        checkBalance(validBills);
        for (Bill bill : validBills) {
            payBill(bill.getId());
        }
    }

    private static void printInvalidBills(List<Bill> invalidBills) {
        for (Bill invalidBill : invalidBills) {
            System.out.printf("Bill with id %s has already paid%n", invalidBill.getId());
        }
    }

    private void checkBalance(List<Bill> validBills) {
        Integer totalBillAmount = validBills.stream().map(Bill::getAmount).reduce(Integer::sum).orElse(0);
        Integer currentBalance = accountService.getCurrentBalance();
        if (currentBalance < totalBillAmount) {
            throw new RuntimeException("Sorry! Not enough fund to proceed with payment");
        }
    }

    private static List<Bill> extractPaidBills(List<Bill> bills) {
        return bills.stream()
                .filter(bill -> bill.getState() == BillState.PAID).collect(Collectors.toList());
    }

    private static List<Bill> extractNotPaidBills(List<Bill> bills) {
        return bills.stream()
                .filter(bill -> bill.getState() != BillState.PAID).collect(Collectors.toList());
    }

    private List<Bill> getSortedBillsByDueDateASC(List<Integer> billIds) {
        return billIds.stream()
                .map(billService::getBillById)
                .sorted(Comparator.comparingLong(b -> b.getDueDate().toEpochDay()))
                .collect(Collectors.toList());
    }

    public void payBill(Integer billId) {
        Bill bill = billService.getBillById(billId);
        if (bill.getState() == BillState.PAID) {
            System.out.println("Bill has already paid.");
            return;
        }
        Payment payment = initPayment(billId, bill);
        executePaymentAndRollbackIfFailed(billId, bill, payment);
    }

    private void executePaymentAndRollbackIfFailed(Integer billId, Bill bill, Payment payment) {
        try {
            accountService.pay(bill.getAmount());
            bill.setState(BillState.PAID);
            payment.setState(PaymentState.PROCESSED);
            System.out.printf("Payment has been completed for Bill with id %s.%n", billId);
            System.out.printf("Your current balance is: %s%n", accountService.getCurrentBalance());
        } catch (Throwable e) {
            String msg = e instanceof RuntimeException ? e.getMessage() : "Pay bill failed. Something went wrong, please try again!";
            payment.setState(PaymentState.FAILED);
            bill.setState(BillState.NOT_PAID);
            throw new RuntimeException(msg, e);
        } finally {
            updateState(payment.getId(), payment.getState());
            billService.updateState(bill.getId(), bill.getState());
        }
    }

    private Payment initPayment(Integer billId, Bill bill) {
        Payment payment = new Payment(idGenerator.getAndIncrement(), bill.getAmount(), LocalDate.now(), PaymentState.PENDING, billId);
        paymentDao.create(payment);
        return payment;
    }

    public void updateState(Integer id, PaymentState state) {
        Payment payment = paymentDao.getById(id);
        payment.setState(state);
        paymentDao.update(payment);
    }

    public void schedulePayment(Integer scheduleBillId, LocalDate scheduleDate) {
        LocalDate now = LocalDate.now();
        if (scheduleDate.isBefore(now)) {
            throw new RuntimeException("Invalid date");
        }
        Bill bill = billService.getBillById(scheduleBillId);
        if (bill.getState() == BillState.PAID) {
            throw new RuntimeException("Bill has already paid");
        }
        int delayedDays = now.until(scheduleDate).getDays();
        System.out.printf("Payment for bill id %s is scheduled on %s%n", scheduleBillId, scheduleDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        scheduledExecutorService.schedule(() -> payAndPrintErrorIfRaised(bill), delayedDays, TimeUnit.DAYS);
    }

    private void payAndPrintErrorIfRaised(Bill bill) {
        try {
            payBill(bill.getId());
        } catch (RuntimeException e) {
            System.err.printf("%n%s%n", e.getMessage());
        } catch (Throwable throwable) {
            System.err.printf("Something went wrong by error: %s%n", throwable.getMessage());
        }
    }

    public List<Runnable> shutdownAllScheduleTasks() {
        return scheduledExecutorService.shutdownNow();
    }
}
