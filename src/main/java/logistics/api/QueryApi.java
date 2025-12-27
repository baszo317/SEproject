package logistics.api;

import logistics.model.Parcel;

import java.time.LocalDate;
import java.util.List;

public interface QueryApi {
    Parcel searchByTrackingNumber(String trackingNo);
    List<Parcel> searchByCustomer(long customerId);
    List<Parcel> searchByDateRange(LocalDate start, LocalDate end);

}
