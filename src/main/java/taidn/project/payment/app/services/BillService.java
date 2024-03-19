package taidn.project.payment.app.services;

import taidn.project.payment.app.daos.BillDAO;
import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.BillState;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BillService {
    public static BillService INSTANCE = new BillService();
    private final BillDAO billDAO = BillDAO.INSTANCE;
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    private BillService() {
    }

    public List<Bill> getAllBills() {
        return billDAO.getAll();
    }

    public Bill getBillById(Integer id) {
        return billDAO.getById(id);
    }

    public Bill createBill(Bill param) {
        if (param.getAmount() < 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }
        if (param.getDueDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Due date must be greater or equal than now");
        }
        param.setId(idGenerator.getAndIncrement());
        param.setState(BillState.NOT_PAID);
        return billDAO.create(param);
    }

    public Bill deleteBill(Integer id) {
        return billDAO.delete(id);
    }

    public Bill updateBill(Bill param) {
        return billDAO.update(param);

    }

    public void updateState(Integer billId, BillState state) {
        Bill bill = billDAO.getById(billId);
        bill.setState(state);
        billDAO.update(bill);
    }

    public List<Bill> searchByProvider(String provider) {
        return billDAO.searchByProvider(provider);
    }

}
