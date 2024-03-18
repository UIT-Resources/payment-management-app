package taidn.project.payment.app.services;

import java.util.concurrent.atomic.AtomicInteger;

public class AccountService {
    private final AtomicInteger accountBalance = new AtomicInteger(0);

    public Integer cashIn(Integer amount) {
        if (amount == null) {
            return accountBalance.get();
        }
        return accountBalance.addAndGet(amount);
    }
}
