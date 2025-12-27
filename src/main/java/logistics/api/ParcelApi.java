package logistics.api;

import logistics.model.Customer;
import logistics.model.Parcel;
import logistics.model.ServiceType;

public interface ParcelApi {
    Parcel createParcel(Customer sender, ServiceType serviceType,
                        double weightKg, double lengthCm, double widthCm, double heightCm,
                        double declaredValue, String description,
                        boolean dangerous, boolean fragile, boolean international);

    Parcel getByTrackingNumber(String trackingNumber);
}
