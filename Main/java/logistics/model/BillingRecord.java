package logistics.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BillingRecord {
    private String id;
    private Customer customer;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private List<BillingItem> items = new ArrayList<>();

    public BillingRecord(String id, Customer customer, LocalDate periodStart, LocalDate periodEnd) {
        this.id = id;
        this.customer = customer;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // ==== Getter ====
    public String getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public List<BillingItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(BillingItem item) {
        items.add(item);
    }

    // 這裡改成用 item.getAmount()
    public double getTotalAmount() {
        return items.stream()
                .mapToDouble(i -> i.getAmount())
                .sum();
    }

    @Override
    public String toString() {
        return "BillingRecord{" +
                "id='" + id + '\'' +
                ", customer=" + customer.name +
                ", period=" + periodStart + " ~ " + periodEnd +
                ", total=" + getTotalAmount() +
                ", shipments=" + items.size() +
                '}';
    }
}
