package logistics.api;

import logistics.enums.PaymentMethod;
import logistics.model.BillingRecord;
import logistics.model.Customer;
import logistics.model.Parcel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BillingApi {
    double calculateCharge(Parcel parcel, double distanceKm);

    BillingRecord generateBillingRecord(Customer customer,
                                        LocalDate start, LocalDate end,
                                        Map<String, Double> distanceByTrackingNo,
                                        PaymentMethod preferredPaymentMethod);

    List<BillingRecord> getBillingHistoryForCustomer(long customerId);
}
