package taidn.project.payment.app;

import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.Command;
import taidn.project.payment.app.services.AccountService;
import taidn.project.payment.app.services.BillService;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * Hello world!
 */
public class MainApp {
    private final static AccountService accountService = new AccountService();
    private final static BillService billService = new BillService();

    public static void main(String[] args) {
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

                    case EXIT:
                        System.out.println("Good bye!");
                        break mainFlow;
                }
            } catch (RuntimeException e) {
                System.err.printf("Command failed by error: %n%s%n", e.getMessage());
            }
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
}
