package taidn.project.payment.app.daos;

import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.BillState;
import taidn.project.payment.app.entities.Payment;
import taidn.project.payment.app.entities.PaymentState;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PaymentDAO extends BaseDAO<Payment> {
    public static PaymentDAO INSTANCE = new PaymentDAO();
    private final Map<Integer, Payment> map = new HashMap<>();
    private PaymentDAO() {}

    @Override
    public List<Payment> getAll() {
        return new ArrayList<>(map.values());
    }

    @Override
    public Payment getById(Integer id) {
        if (!map.containsKey(id)) {
            throw new RuntimeException(String.format("Not exist payment with id %s", id));
        }
        return map.get(id);
    }

    @Override
    public Payment create(Payment payment) {
        if (payment.getId() == null) {
            throw new RuntimeException("Create failed. Missing payment Id");
        }
        if (map.containsKey(payment.getId())) {
            throw new RuntimeException(String.format("Existed payment %s", payment));
        }
        map.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public Payment delete(Integer id) {
        if (!map.containsKey(id)) {
            throw new RuntimeException(String.format("Not exist payment with id %s", id));
        }
        Payment payment = map.get(id);
        map.remove(id);
        return payment;
    }

    @Override
    public Payment update(Payment payment) {
        if (payment.getId() == null) {
            throw new RuntimeException("Update failed. Missing paymentId");
        }
        if (!map.containsKey(payment.getId())) {
            throw new RuntimeException(String.format("Not exist payment with id %s", payment.getId()));
        }
        map.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public Boolean isExist(Integer id) {
        return map.containsKey(id);
    }
}
