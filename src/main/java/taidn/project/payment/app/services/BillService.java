package taidn.project.payment.app.services;

import taidn.project.payment.app.daos.BillDAO;
import taidn.project.payment.app.entities.Bill;
import taidn.project.payment.app.entities.BillState;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class BillService {
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    private final BillDAO billDAO = new BillDAO();

    public List<Bill> listAll(){
        return billDAO.getAll();
    }

    public Bill create(){
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
}
