package logistics.api;

import logistics.enums.BillingPreference;
import logistics.enums.CustomerType;
import logistics.model.Customer;

public interface CustomerApi {
    Customer createCustomer(String name, String address, String phone, String email,
                            CustomerType type, BillingPreference pref);
    Customer getCustomer(long customerId);
}
