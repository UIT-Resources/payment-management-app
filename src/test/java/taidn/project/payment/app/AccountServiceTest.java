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
public class AccountServiceTest
{
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
}
