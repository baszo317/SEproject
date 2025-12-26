package logistics.model;

import logistics.enums.DeliverySpeed;
import logistics.enums.PackageType;

public class ServiceType {
    private long id;
    private String name;
    private PackageType packageType;
    private double minWeightKg;
    private double maxWeightKg;
    private DeliverySpeed speed;

    // 定價規則
    private double basePrice;
    private double pricePerKm;
    private double pricePerKg;
    private double pricePerCubicMeter;

    // 特殊處理加價
    private double dangerousSurcharge;
    private double fragileSurcharge;
    private double oversizeSurcharge;

    public ServiceType(long id, String name, PackageType packageType,
                       double minWeightKg, double maxWeightKg,
                       DeliverySpeed speed,
                       double basePrice, double pricePerKm, double pricePerKg,
                       double pricePerCubicMeter,
                       double dangerousSurcharge, double fragileSurcharge,
                       double oversizeSurcharge) {
        this.id = id;
        this.name = name;
        this.packageType = packageType;
        this.minWeightKg = minWeightKg;
        this.maxWeightKg = maxWeightKg;
        this.speed = speed;
        this.basePrice = basePrice;
        this.pricePerKm = pricePerKm;
        this.pricePerKg = pricePerKg;
        this.pricePerCubicMeter = pricePerCubicMeter;
        this.dangerousSurcharge = dangerousSurcharge;
        this.fragileSurcharge = fragileSurcharge;
        this.oversizeSurcharge = oversizeSurcharge;
    }

    // ==== Getter（給 core 用） ====
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public double getMinWeightKg() {
        return minWeightKg;
    }

    public double getMaxWeightKg() {
        return maxWeightKg;
    }

    public DeliverySpeed getSpeed() {
        return speed;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public double getPricePerKm() {
        return pricePerKm;
    }

    public double getPricePerKg() {
        return pricePerKg;
    }

    public double getPricePerCubicMeter() {
        return pricePerCubicMeter;
    }

    public double getDangerousSurcharge() {
        return dangerousSurcharge;
    }

    public double getFragileSurcharge() {
        return fragileSurcharge;
    }

    public double getOversizeSurcharge() {
        return oversizeSurcharge;
    }

    @Override
    public String toString() {
        return "ServiceType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", packageType=" + packageType +
                ", speed=" + speed +
                '}';
    }
}
