package taidn.project.payment.app.services;

import taidn.project.payment.app.daos.PaymentDAO;
import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.Payment;
import taidn.project.payment.app.entities.BillState;
import taidn.project.payment.app.entities.PaymentState;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PaymentService {
    public static PaymentService INSTANCE = new PaymentService();
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    private final PaymentDAO paymentDao = PaymentDAO.INSTANCE;
    private final BillService billService = BillService.INSTANCE;
    private final AccountService accountService = AccountService.INSTANCE;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(8);

    private PaymentService() {}

    public List<Payment> listAll(){
        return paymentDao.getAll();
    }

    public void payBills(List<Integer> billIds){
        try {
            if (billIds.isEmpty()) {
                throw new RuntimeException("Pay failed. billIds is empty");
            }
            List<Bill> bills = billIds.stream()
                    .map(billService::getById)
                    .sorted(Comparator.comparingLong(b -> b.getDueDate().toEpochDay()))
                    .collect(Collectors.toList());
            List<Bill> validBills = bills.stream()
                    .filter(bill -> bill.getState() != BillState.PAID).collect(Collectors.toList());
            List<Bill> invalidBills = bills.stream()
                    .filter(bill -> bill.getState() == BillState.PAID).collect(Collectors.toList());
            for (Bill invalidBill : invalidBills) {
                System.out.printf("Bill with id %s has already paid%n", invalidBill.getId());
            }
            Integer totalBillAmount = validBills.stream().map(Bill::getAmount).reduce(Integer::sum).orElse(0);
            Integer currentBalance = accountService.getCurrentBalance();
            if (currentBalance < totalBillAmount) {
                throw new RuntimeException("Sorry! Not enough fund to proceed with payment");
            }

            for (Bill bill : validBills) {
                payBill(bill.getId());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Pay bills failed. Something went wrong, please try again!";
            throw new RuntimeException(msg, e);
        }
    }

    public void payBill(Integer billId){
        Bill bill = billService.getById(billId);
        if (bill.getState() == BillState.PAID) {
            System.out.println("Bill has already paid.");
            return;
        }
        Payment payment = new Payment(idGenerator.getAndIncrement(), bill.getAmount(), LocalDate.now(), PaymentState.PENDING, billId);
        paymentDao.create(payment);
        try {
            // Transaction
            accountService.pay(bill.getAmount());
            bill.setState(BillState.PAID);
            payment.setState(PaymentState.PROCESSED);
        } catch (Throwable e) {
            String msg = e instanceof RuntimeException ? e.getMessage() : "Pay bill failed. Something went wrong, please try again!";
            payment.setState(PaymentState.FAILED);
            bill.setState(BillState.NOT_PAID);
            throw new RuntimeException(msg, e);
        } finally {
            updateState(payment.getId(), payment.getState());
            billService.updateState(bill.getId(), bill.getState());
        }
        System.out.printf("Payment has been completed for Bill with id %s.%n", billId);
        System.out.printf("Your current balance is: %s%n", accountService.getCurrentBalance());
    }

    public Payment updateState(Integer id, PaymentState state) {
        Payment payment = paymentDao.getById(id);
        payment.setState(state);
        return paymentDao.update(payment);
    }

    public void schedule(Integer scheduleBillId, LocalDate scheduleDate) {
        LocalDate now = LocalDate.now();
        if (scheduleDate.isBefore(now)) {
            throw new RuntimeException("Invalid date");
        }
        Bill bill = billService.getById(scheduleBillId);
        if (bill.getState() == BillState.PAID) {
            throw new RuntimeException("Bill has already paid");
        }
        int delayedDays = now.until(scheduleDate).getDays();
        System.out.printf("Payment for bill id %s is scheduled on %s%n", scheduleBillId, scheduleDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        scheduledExecutorService.schedule(() -> {
            try {
                payBill(bill.getId());
            } catch (RuntimeException e) {
                System.err.printf("%n%s%n", e.getMessage());
            } catch (Throwable throwable ){
                System.err.printf("Something went wrong by error: %s%n", throwable.getMessage());
            }
        }, delayedDays, TimeUnit.DAYS);
    }

    public List<Runnable> shutdownAllScheduleTask(){
        return scheduledExecutorService.shutdownNow();
    }
}
