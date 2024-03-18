package taidn.project.payment.app;

import taidn.project.payment.app.entities.Bill;
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
                            if (lineParts.size() != 2) {
                                throw new RuntimeException(String.format("Command expect 1 argument, but received %s.", lineParts.size() - 1));
                            }
                            Integer amount = Integer.parseInt(lineParts.get(1));
                            Integer currentBalance = accountService.cashIn(amount);
                            System.out.printf("Your available balance: %s%n", currentBalance);

                            break;

                        case LIST_BILL:
                            List<Bill> bills = billService.listAll();
                            printListBillAsRows(bills);
                            break;

                        case CREATE_BILL:
                            Bill createdBill = billService.create();
                            System.out.printf("Created. %s%n", createdBill);
                            break;

                        case DELETE_BILL:
                            if (lineParts.size() != 2) {
                                throw new RuntimeException(String.format("Command expect 1 argument, but received %s.", lineParts.size() - 1));
                            }
                            Integer billId = Integer.parseInt(lineParts.get(1));
                            Bill deletedBill = billService.delete(billId);
                            System.out.printf("Deleted bill: %s%n", deletedBill);
                            break;

                        case SEARCH_BILL_BY_PROVIDER:
                            if (lineParts.size() != 2) {
                                throw new RuntimeException(String.format("Command expect 1 argument, but received %s.", lineParts.size() - 1));
                            }
                            String provider = lineParts.get(1);
                            List<Bill> foundBills = billService.searchByProvider(provider);
                            printListBillAsRows(foundBills);
                            break;
                        case UPDATE_BILL:
                            Bill updatedBill = billService.update();
                            System.out.printf("Updated.%s%n", updatedBill);
                            break;

                        case LIST_PAYMENT:
                            List<Payment> transactions = paymentService.listAll();
                            printListPaymentAsRows(transactions);
                            break;

                        case PAY:
                            if (lineParts.size() < 2) {
                                throw new RuntimeException(String.format("Command expect > 1 argument, but received %s.", lineParts.size() - 1));
                            }
                            List<Integer> billIds = lineParts.subList(1, lineParts.size())
                                    .stream()
                                    .map(Integer::parseInt).collect(Collectors.toList());
                            paymentService.payBills(billIds);
                            break;
                        case SCHEDULE:
                            if (lineParts.size() != 3) {
                                throw new RuntimeException(String.format("Command expect 2 argument, but received %s.", lineParts.size() - 1));
                            }
                            Integer scheduleBillId = Integer.parseInt(lineParts.get(1));
                            LocalDate scheduleDate = LocalDate.parse(lineParts.get(2), DateTimeFormatter.ofPattern("d/M/y"));
                            paymentService.schedule(scheduleBillId, scheduleDate);
                            break;
                        case EXIT:
                            List<Runnable> tasks = paymentService.shutdownAllScheduleTask();
                            if (!tasks.isEmpty()) {
                                System.out.printf("Shutdown %s pending/running schedule tasks%n", tasks.size());
                            }
                            System.out.println("Good bye!");
                            break mainFlow;
                    }
                } catch (RuntimeException e) {
                    System.err.printf("Command failed by error: %n%s%n", e.getMessage());
                }
            }
        } catch (Throwable throwable) {
            System.err.printf("Something went wrong by error: %s%n", throwable.getMessage());
        }
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
