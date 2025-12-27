package logistics.api;

import logistics.enums.DeliverySpeed;
import logistics.enums.PackageType;
import logistics.model.ServiceType;

public interface ServiceTypeApi {
    ServiceType createServiceType(String name, PackageType packageType,
                                  double minWeightKg, double maxWeightKg,
                                  DeliverySpeed speed,
                                  double basePrice, double pricePerKm,
                                  double pricePerKg, double pricePerCubicMeter,
                                  double dangerousSurcharge, double fragileSurcharge, double oversizeSurcharge);

    ServiceType getServiceType(long serviceTypeId);
}
