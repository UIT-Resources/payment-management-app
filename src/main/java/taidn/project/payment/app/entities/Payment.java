package taidn.project.payment.app.entities;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public class Payment {
    private Integer id;
    private Integer amount;
    private LocalDate paymentDate;
    private PaymentState state;
    private Integer billId;

    public Payment() {
    }

    public Payment(Integer id, Integer amount, LocalDate paymentDate, PaymentState state, Integer billId) {
        this.id = id;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.state = state;
        this.billId = billId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void printAsRowSeparateByTab() {
        Field[] fields = Payment.class.getDeclaredFields();
        StringJoiner result = new StringJoiner("\t");
        for (Field field : fields) {
            try {
                if (field.getName().equals("paymentDate")) {
                    LocalDate dueDate = (LocalDate) field.get(this);
                    result.add(dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    result.add(String.valueOf(field.get(this)));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(result);
    }
}
