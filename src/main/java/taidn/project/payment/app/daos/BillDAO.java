package taidn.project.payment.app.daos;

import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.BillState;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BillDAO extends BaseDAO<Bill> {
    private final Map<Integer, Bill> map = new HashMap<>();

    public BillDAO() {
        // TODO: Remove mock data
        map.put(111, new Bill(111, "type1", 10000, LocalDate.now(), BillState.NOT_PAID, "FPT"));
        map.put(112, new Bill(112, "type2", 10000, LocalDate.now().minusDays(6), BillState.PAID, "VNPT"));
    }

    @Override
    public List<Bill> getAll() {
        return new ArrayList<>(map.values());
    }

    @Override
    public Bill getById(Integer id) {
        if (!map.containsKey(id)) {
            throw new RuntimeException(String.format("Not exist bill with id %s", id));
        }
        return map.get(id);
    }

    @Override
    public Bill create(Bill bill) {
        if (bill.getId() == null) {
            throw new RuntimeException("Create failed. Missing billId");
        }
        if (map.containsKey(bill.getId())) {
            throw new RuntimeException(String.format("Existed bill %s", bill));
        }
        map.put(bill.getId(), bill);
        return bill;
    }

    @Override
    public Bill delete(Integer id) {
        if (!map.containsKey(id)) {
            throw new RuntimeException(String.format("Not exist bill with id %s", id));
        }
        Bill bill = map.get(id);
        map.remove(id);
        return bill;
    }

    @Override
    public Bill update(Bill bill) {
        if (bill.getId() == null) {
            throw new RuntimeException("Update failed. Missing billId");
        }
        if (!map.containsKey(bill.getId())) {
            throw new RuntimeException(String.format("Not exist bill with id %s", bill.getId()));
        }
        map.put(bill.getId(), bill);
        return bill;
    }

    public List<Bill> searchByProvider(String provider) {
        List<Bill> foundBills = map.values().stream()
                .filter(bill -> provider.equals(bill.getProvider()))
                .collect(Collectors.toList());
        return foundBills;
    }

    @Override
    public Boolean isExist(Integer id) {
        return map.containsKey(id);
    }
}
