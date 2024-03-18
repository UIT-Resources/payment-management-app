package taidn.project.payment.app.daos;

import taidn.project.payment.app.entities.Bill;

import java.util.*;
import java.util.stream.Collectors;

public class BillDAO extends BaseDAO<Bill> {
    private final Map<Integer, Bill> billMap = new HashMap<>();

    @Override
    public List<Bill> getAll() {
        return new ArrayList<>(billMap.values());
    }

    @Override
    public Bill create(Bill bill) {
        if (bill.getId() == null) {
            throw new RuntimeException("Create failed. Missing billId");
        }
        if (billMap.containsKey(bill.getId())) {
            throw new RuntimeException(String.format("Existed bill %s", bill));
        }
        billMap.put(bill.getId(), bill);
        return bill;
    }

    @Override
    public Bill delete(Integer id) {
        if (!billMap.containsKey(id)) {
            throw new RuntimeException(String.format("Not exist bill with id %s", id));
        }
        Bill bill = billMap.get(id);
        billMap.remove(id);
        return bill;
    }

    @Override
    public Bill update(Bill bill) {
        if (bill.getId() == null) {
            throw new RuntimeException("Update failed. Missing billId");
        }
        if (!billMap.containsKey(bill.getId())) {
            throw new RuntimeException(String.format("Not exist bill with id %s", bill.getId()));
        }
        billMap.put(bill.getId(), bill);
        return bill;
    }

    public List<Bill> searchByProvider(String provider) {
        List<Bill> foundBills = billMap.values().stream()
                .filter(bill -> provider.equals(bill.getProvider()))
                .collect(Collectors.toList());
        return foundBills;
    }
}
