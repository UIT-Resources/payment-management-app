package taidn.project.payment.app;

import taidn.project.payment.app.entities.Command;
import taidn.project.payment.app.services.AccountService;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class MainApp{
    private final static AccountService accountService = new AccountService();
    public static void main( String[] args )
    {
        System.out.println( "--------- Payment Management App ---------" );
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
            } catch (IllegalArgumentException e){
                System.err.println("Command is invalid. Please try again !");
                continue;
            }
            switch (cmd) {
                case CASH_IN:
                    try {
                        if (lineParts.size() != 2) {
                            throw new RuntimeException(String.format("Command expect 1 argument, but received %s.", lineParts.size() - 1));
                        }
                        Integer amount = Integer.parseInt(lineParts.get(1));
                        Integer currentBalance = accountService.cashIn(amount);
                        System.out.printf("Your available balance: %s%n", currentBalance);
                    } catch (RuntimeException e) {
                        System.err.printf("Command failed by error: %n%s%n", e.getMessage());
                    }
                    break;
                case EXIT:
                    System.out.println("Good bye!");
                    break mainFlow;
            }
        }
    }
}
