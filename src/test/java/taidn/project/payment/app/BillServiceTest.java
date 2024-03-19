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
public class BillServiceTest
{
    public static BillService billService = BillService.INSTANCE;

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
}
