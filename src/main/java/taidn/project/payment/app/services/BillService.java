package taidn.project.payment.app.services;

import taidn.project.payment.app.daos.BillDAO;
import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.BillState;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class BillService {
    public static BillService INSTANCE = new BillService();
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    private final BillDAO billDAO = new BillDAO();

    public List<Bill> listAll() {
        return billDAO.getAll();
    }

    public Bill getById(Integer id) {
        return billDAO.getById(id);
    }

    public BillDAO getBillDAO() {
        return billDAO;
    }


    public Bill create() {
        try {
            Scanner sc = new Scanner(System.in);
            Bill newBill = new Bill();
            newBill.setId(idGenerator.getAndIncrement());
            newBill.setState(BillState.NOT_PAID);

            System.out.println("Enter type: ");
            newBill.setType(sc.next());
            System.out.println("Enter amount: ");
            newBill.setAmount(sc.nextInt());
            System.out.println("Enter dueDate (day/month/year): ");
            String rawDate = sc.next();
            LocalDate dueDate = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("d/M/y"));
            newBill.setDueDate(dueDate);
            System.out.println("Enter provider: ");
            newBill.setProvider(sc.next());
            billDAO.create(newBill);
            return newBill;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Create bill failed. Something went wrong, please try again!";
            throw new RuntimeException(msg, e);
        }
    }

    public Bill delete(Integer id) {
        return billDAO.delete(id);
    }

    public List<Bill> searchByProvider(String provider) {
        return billDAO.searchByProvider(provider);
    }

    public Bill update() {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Which bill do you want to update ? Enter bill id: ");
            Integer billId = sc.nextInt();
            if (!billDAO.isExist(billId)) {
                throw new RuntimeException("Bill is not exited");
            }
            Bill bill = billDAO.getById(billId);

            System.out.println("Enter new type: ");
            bill.setType(sc.next());
            System.out.println("Enter provider: ");
            bill.setProvider(sc.next());
            if (bill.getState() == BillState.NOT_PAID) {
                System.out.println("Enter new amount: ");
                bill.setAmount(sc.nextInt());
                System.out.println("Enter new dueDate (day/month/year): ");
                String rawDate = sc.next();
                LocalDate dueDate = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("d/M/y"));
                bill.setDueDate(dueDate);
            }
            return billDAO.update(bill);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Create bill failed. Something went wrong, please try again!";
            throw new RuntimeException(msg, e);
        }
    }

    public Bill updateState(Integer billId, BillState state) {
        Bill bill = billDAO.getById(billId);
        bill.setState(state);
        return billDAO.update(bill);
    }
}
