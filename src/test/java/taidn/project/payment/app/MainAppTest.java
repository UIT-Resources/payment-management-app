package taidn.project.payment.app;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import taidn.project.payment.app.daos.BillDAO;
import taidn.project.payment.app.daos.PaymentDAO;
import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.BillState;
import taidn.project.payment.app.entities.PaymentState;
import taidn.project.payment.app.services.AccountService;
import taidn.project.payment.app.services.BillService;
import taidn.project.payment.app.services.PaymentService;

import java.time.LocalDate;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class MainAppTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MainAppTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MainAppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testBillManagement()
    {
        BillDAO billDAO = new BillDAO();
        Bill bill = new Bill(1, "type1", 10000, LocalDate.now(), BillState.PAID, "VNPT");
        billDAO.create(bill);
        assertEquals(1, (int) billDAO.getById(1).getId());

        List<Bill> bills = billDAO.getAll();
        assertTrue(bills.size() == 1 && bills.get(0).getId() == 1);

        bill.setProvider("New");
        billDAO.update(bill);
        assertEquals("New", bill.getProvider());

        billDAO.delete(1);
        assertTrue(billDAO.getAll().isEmpty());

    }

    public void testPaymentManagement()
    {
        AccountService accountService = AccountService.INSTANCE;
        accountService.cashIn(100000);
        BillDAO billDAO = BillService.INSTANCE.getBillDAO();
        Bill bill1 = new Bill(1, "type1", 10000, LocalDate.now(), BillState.PAID, "VNPT");
        Bill bill2 = new Bill(2, "type1", 10000, LocalDate.now(), BillState.NOT_PAID, "VNPT");
        billDAO.create(bill1);
        billDAO.create(bill2);

        PaymentService paymentService = PaymentService.INSTANCE;
        paymentService.payBill(2);
        assertEquals( BillState.PAID, billDAO.getById(2).getState());
        assertEquals(90000, (int) accountService.getCurrentBalance());

        paymentService.payBill(1);
        assertEquals( BillState.PAID, billDAO.getById(2).getState());
        assertEquals(90000, (int) accountService.getCurrentBalance());

    }
}
