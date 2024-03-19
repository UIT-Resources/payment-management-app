package taidn.project.payment.app;

import org.junit.jupiter.api.DisplayName;
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
public class MainAppTest
{
    public static BillService billService = BillService.INSTANCE;
    public static PaymentService paymentService = PaymentService.INSTANCE;
    public static AccountService accountService = AccountService.INSTANCE;


    @Test
    public void testAccountServiceFunction(){
        // Init test
        assertEquals(0, (int) accountService.getCurrentBalance());

        // CashIn test
        accountService.cashIn(10000);
        assertEquals(10000, (int) accountService.getCurrentBalance());

        // Pay test
        accountService.pay(1000);
        assertEquals(9000, (int) accountService.getCurrentBalance());
    }

    @Test
    public void testBillServiceFunctions()
    {
        // Create & List Test
        Bill bill = new Bill(null, "type1", 10000, LocalDate.now(), null, "VNPT");
        billService.createBill(bill);
        assertEquals(1, (int) billService.getAllBills().size());
        Bill createdBill = billService.getBillById(0);
        assertEquals(BillState.NOT_PAID, createdBill.getState());
        assertEquals("type1", createdBill.getType());
        assertEquals(LocalDate.now(), createdBill.getDueDate());
        assertEquals("VNPT", createdBill.getProvider());
        assertEquals(10000, (int) createdBill.getAmount());

        // Update Test
        createdBill.setAmount(2000);
        Bill updatedBill = billService.updateBill(createdBill);
        assertEquals(2000, (int) updatedBill.getAmount());

        // Search By Provider
        List<Bill> foundedBills = billService.searchByProvider("VNPT");
        assertEquals(1, (int) foundedBills.size());
        List<Bill> foundedBills2 = billService.searchByProvider("abc");
        assertEquals(0, (int) foundedBills2.size());

        // Delete Test
        billService.deleteBill(updatedBill.getId());
        assertEquals(0, (int) billService.getAllBills().size());
    }

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
