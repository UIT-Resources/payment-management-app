package taidn.project.payment.app.services;

import java.util.concurrent.atomic.AtomicInteger;

public class AccountService {
    public static AccountService INSTANCE = new AccountService();
    private final AtomicInteger accountBalance = new AtomicInteger(0);

    private AccountService() {}

    public Integer cashIn(Integer amount) {
        if (amount == null) {
            return accountBalance.get();
        }
        return accountBalance.addAndGet(amount);
    }

    public Integer pay(Integer amount) {
        if (amount == null || amount > accountBalance.get()) {
            throw new RuntimeException("Sorry! Not enough fund to proceed with payment.");
        }
        return accountBalance.addAndGet(-amount);
    }

    public Integer getCurrentBalance(){
        return accountBalance.get();
    }
}
