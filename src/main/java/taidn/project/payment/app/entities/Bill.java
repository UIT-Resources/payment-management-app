package taidn.project.payment.app.entities;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public class Bill {
    private Integer id;
    private String type;
    private Integer amount;
    private LocalDate dueDate;
    private BillState state;
    private String provider;

    public Bill() {
    }

    public Bill(Integer id, String type, Integer amount, LocalDate dueDate, BillState state, String provider) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.dueDate = dueDate;
        this.state = state;
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", dueDate=" + dueDate +
                ", state=" + state +
                ", provider='" + provider + '\'' +
                '}';
    }

    public void printAsRowSeparateByTab() {
        Field[] fields = Bill.class.getDeclaredFields();
        StringJoiner result = new StringJoiner("\t");
        for (Field field : fields) {
            try {
                if (field.getName().equals("dueDate")) {
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BillState getState() {
        return state;
    }

    public void setState(BillState state) {
        this.state = state;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
