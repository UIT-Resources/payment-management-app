package taidn.project.payment.app;

import org.junit.jupiter.api.Test;
import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.BillState;
import taidn.project.payment.app.entities.PaymentState;
import taidn.project.payment.app.services.AccountService;
import taidn.project.payment.app.services.BillService;
import taidn.project.payment.app.services.PaymentService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for simple App.
 */
public class PaymentServiceTest
{
    public static BillService billService = BillService.INSTANCE;
    public static PaymentService paymentService = PaymentService.INSTANCE;
    public static AccountService accountService = AccountService.INSTANCE;


    @Test
    public void testPaymentManagement() throws InterruptedException {
        // Create sample bills
        Bill bill = new Bill(null, "type2", 10000, LocalDate.now(), null, "FPT");
        Bill createdBill = billService.createBill(bill);
        Bill bill2 = new Bill(null, "type2", 10000, LocalDate.now().plusDays(6), null, "FPT");
        Bill createdBill2 = billService.createBill(bill2);
        List<Bill> bills = billService.getAllBills();
        System.out.println("bills = " + bills);

        // Pay test: not enough fund case
        Throwable exp = assertThrows(RuntimeException.class, () -> paymentService.payBills(Arrays.asList(1, 0)));
        assertEquals("Sorry! Not enough fund to proceed with payment", exp.getMessage());

        // Pay test: enough fund case
        accountService.cashIn(10000);
        paymentService.payBills(Collections.singletonList(1));
        assertEquals(PaymentState.PROCESSED, paymentService.getAllPayments().get(0).getState());
        assertEquals(BillState.PAID, billService.getBillById(1).getState());

        // Schedule test
        accountService.cashIn(10000);
        paymentService.schedulePayment(0, LocalDate.now());
        Thread.sleep(1000); // wait for pay process finished
        assertEquals(PaymentState.PROCESSED, paymentService.getAllPayments().get(1).getState());
        assertEquals(BillState.PAID, billService.getBillById(0).getState());
        assertEquals(0, accountService.getCurrentBalance());


    }
}
