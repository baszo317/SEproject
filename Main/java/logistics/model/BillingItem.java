package logistics.model;

import logistics.enums.PaymentMethod;

public class BillingItem {
    private Parcel parcel;
    private double amount;
    private PaymentMethod paymentMethod;

    public BillingItem(Parcel parcel, double amount, PaymentMethod paymentMethod) {
        this.parcel = parcel;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    // ===== Getter 給 core / demo 用 =====
    public Parcel getParcel() {
        return parcel;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
}
