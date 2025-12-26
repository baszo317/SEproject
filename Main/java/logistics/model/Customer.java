package logistics.model;

import logistics.enums.CustomerType;
import logistics.enums.BillingPreference;

public class Customer {
    public long id;
    public String name;
    public String address;
    public String phone;
    public String email;
    public CustomerType type;
    public BillingPreference billingPreference;

    public Customer(long id, String name, String address, String phone, String email,
                    CustomerType type, BillingPreference billingPreference) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.type = type;
        this.billingPreference = billingPreference;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", billingPreference=" + billingPreference +
                '}';
    }
}
