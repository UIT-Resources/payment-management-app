package taidn.project.payment.app;

import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.BillState;
import taidn.project.payment.app.entities.Command;
import taidn.project.payment.app.entities.Payment;
import taidn.project.payment.app.services.AccountService;
import taidn.project.payment.app.services.BillService;
import taidn.project.payment.app.services.PaymentService;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
public class MainApp {
    private final static AccountService accountService = AccountService.INSTANCE;
    private final static BillService billService = BillService.INSTANCE;
    private final static PaymentService paymentService = PaymentService.INSTANCE;

    public static void main(String[] args) {
        try {

            System.out.println("--------- Payment Management App ---------");
            String commands = Arrays.stream(Command.values()).map(Command::name).collect(Collectors.joining(", "));
            System.out.printf("[Supported commands: %s]%n", commands);
            Scanner sc = new Scanner(System.in);
            mainFlow:
            while (sc.hasNext()) {
                List<String> lineParts = Arrays.asList(sc.nextLine().split(" "));
                if (lineParts.isEmpty()) {
                    continue;
                }
                Command cmd;
                try {
                    cmd = Command.valueOf(lineParts.get(0));
                } catch (IllegalArgumentException e) {
                    System.err.println("Command is invalid. Please try again !");
                    continue;
                }
                try {
                    switch (cmd) {
                        case CASH_IN:
                            processCashIn(lineParts);
                            break;
                        case LIST_BILL:
                            processListBill();
                            break;
                        case DUE_DATE:
                            processDueDate();
                            break;
                        case CREATE_BILL:
                            processCreateBill();
                            break;
                        case DELETE_BILL:
                            processDeleteBill(lineParts);
                            break;
                        case SEARCH_BILL_BY_PROVIDER:
                            processSearchBillByProvider(lineParts);
                            break;
                        case UPDATE_BILL:
                            processUpdateBill();
                            break;
                        case LIST_PAYMENT:
                            processListPayment();
                            break;
                        case PAY:
                            processPay(lineParts);
                            break;
                        case SCHEDULE:
                            processSchedule(lineParts);
                            break;
                        case EXIT:
                            releaseResources();
                            break mainFlow;
                    }
                } catch (RuntimeException e) {
                    System.err.printf("%s%n", e.getMessage());
                }
            }
        } catch (Throwable throwable) {
            System.err.printf("Something went wrong by error: %s%n", throwable.getMessage());
        }
    }

    private static void processListBill() {
        List<Bill> bills = billService.listAll();
        printListBillAsRows(bills);
    }

    private static void processDueDate() {
        List<Bill> allBills = billService.listAll();
        List<Bill> notPaidBills = allBills.stream().filter(b -> b.getState() == BillState.NOT_PAID).collect(Collectors.toList());
        printListBillAsRows(notPaidBills);
    }

    private static void processCreateBill() {
        Bill createdBill = billService.create();
        System.out.printf("Created. %s%n", createdBill);
    }

    private static void releaseResources() {
        List<Runnable> tasks = paymentService.shutdownAllScheduleTask();
        if (!tasks.isEmpty()) {
            System.out.printf("Shutdown %s pending/running schedule tasks%n", tasks.size());
        }
        System.out.println("Good bye!");
    }

    private static void processSchedule(List<String> lineParts) {
        if (lineParts.size() != 3) {
            throw new RuntimeException(String.format("Command expect 2 argument, but received %s.", lineParts.size() - 1));
        }
        Integer scheduleBillId = Integer.parseInt(lineParts.get(1));
        LocalDate scheduleDate = LocalDate.parse(lineParts.get(2), DateTimeFormatter.ofPattern("d/M/y"));
        paymentService.schedule(scheduleBillId, scheduleDate);
    }

    private static void processPay(List<String> lineParts) {
        if (lineParts.size() < 2) {
            throw new RuntimeException(String.format("Command expect > 1 argument, but received %s.", lineParts.size() - 1));
        }
        List<Integer> billIds = lineParts.subList(1, lineParts.size())
                .stream()
                .map(Integer::parseInt).collect(Collectors.toList());
        paymentService.payBills(billIds);
    }

    private static void processListPayment() {
        List<Payment> transactions = paymentService.listAll();
        printListPaymentAsRows(transactions);
    }

    private static void processUpdateBill() {
        Bill updatedBill = billService.update();
        System.out.printf("Updated.%s%n", updatedBill);
    }

    private static void processSearchBillByProvider(List<String> lineParts) {
        if (lineParts.size() != 2) {
            throw new RuntimeException(String.format("Command expect 1 argument, but received %s.", lineParts.size() - 1));
        }
        String provider = lineParts.get(1);
        List<Bill> foundBills = billService.searchByProvider(provider);
        printListBillAsRows(foundBills);
    }

    private static void processDeleteBill(List<String> lineParts) {
        if (lineParts.size() != 2) {
            throw new RuntimeException(String.format("Command expect 1 argument, but received %s.", lineParts.size() - 1));
        }
        Integer billId = Integer.parseInt(lineParts.get(1));
        Bill deletedBill = billService.delete(billId);
        System.out.printf("Deleted bill: %s%n", deletedBill);
    }

    private static void processCashIn(List<String> lineParts) {
        if (lineParts.size() != 2) {
            throw new RuntimeException(String.format("Command expect 1 argument, but received %s.", lineParts.size() - 1));
        }
        Integer amount = Integer.parseInt(lineParts.get(1));
        Integer currentBalance = accountService.cashIn(amount);
        System.out.printf("Your available balance: %s%n", currentBalance);
    }

    private static void printListBillAsRows(List<Bill> bills) {
        Field[] fields = Bill.class.getDeclaredFields();
        StringJoiner header = new StringJoiner("\t");
        for (Field field : fields) {
            header.add(field.getName());
        }
        System.out.println(header);
        for (Bill bill : bills) {
            bill.printAsRowSeparateByTab();
        }
    }

    private static void printListPaymentAsRows(List<Payment> payments) {
        Field[] fields = Payment.class.getDeclaredFields();
        StringJoiner header = new StringJoiner("\t");
        for (Field field : fields) {
            header.add(field.getName());
        }
        System.out.println(header);
        for (Payment bill : payments) {
            bill.printAsRowSeparateByTab();
        }
    }
}
