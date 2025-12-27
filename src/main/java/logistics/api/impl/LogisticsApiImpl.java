package logistics.api.impl;

import logistics.api.*;
import logistics.core.LogisticsCore;
import logistics.enums.*;
import logistics.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class LogisticsApiImpl implements AuthApi, CustomerApi, ServiceTypeApi, ParcelApi, TrackingApi, BillingApi, QueryApi {

    private final LogisticsCore core;
    private User currentUser;

    public LogisticsApiImpl(LogisticsCore core, User initialUser) {
        this.core = core;
        this.currentUser = initialUser;
    }

    /* ========== AuthApi ========== */
    @Override
    public User loginAsRole(Role role, Customer customerProfileOrNull) {
        if (role == Role.CUSTOMER && customerProfileOrNull == null) {
            throw new IllegalArgumentException("CUSTOMER 角色必須綁定 customerProfile");
        }
        this.currentUser = new User(
                role == Role.CUSTOMER ? ("customer#" + customerProfileOrNull.id) : role.name().toLowerCase(),
                role,
                customerProfileOrNull
        );
        return this.currentUser;
    }

    @Override
    public User currentUser() {
        return currentUser;
    }

    /* ========== CustomerApi ========== */
    @Override
    public Customer createCustomer(String name, String address, String phone, String email, CustomerType type, BillingPreference pref) {
        // 若你要限制只有 ADMIN/客服可建客戶，可在這裡加 role 檢查
        return core.createCustomer(name, address, phone, email, type, pref);
    }

    @Override
    public Customer getCustomer(long customerId) {
        return core.getCustomer(customerId);
    }

    /* ========== ServiceTypeApi ========== */
    @Override
    public ServiceType createServiceType(String name, PackageType packageType, double minWeightKg, double maxWeightKg,
                                         DeliverySpeed speed, double basePrice, double pricePerKm, double pricePerKg,
                                         double pricePerCubicMeter, double dangerousSurcharge, double fragileSurcharge, double oversizeSurcharge) {

        // 若你要限制只有 ADMIN 可建 ServiceType，可在這裡檢查 currentUser.getRole()
        return core.createServiceType(name, packageType, minWeightKg, maxWeightKg, speed,
                basePrice, pricePerKm, pricePerKg, pricePerCubicMeter,
                dangerousSurcharge, fragileSurcharge, oversizeSurcharge);
    }

    @Override
    public ServiceType getServiceType(long serviceTypeId) {
        // 若 core 沒有 getServiceType，可以先不提供或自行在 core 補方法
        throw new UnsupportedOperationException("getServiceType 尚未實作（看你 core 有沒有提供）");
    }

    /* ========== ParcelApi ========== */
    @Override
    public Parcel createParcel(Customer sender, ServiceType serviceType,
                              double weightKg, double lengthCm, double widthCm, double heightCm,
                              double declaredValue, String description,
                              boolean dangerous, boolean fragile, boolean international) {

        return core.createParcel(currentUser, sender, serviceType,
                weightKg, lengthCm, widthCm, heightCm,
                declaredValue, description,
                dangerous, fragile, international);
    }

    @Override
    public Parcel getByTrackingNumber(String trackingNumber) {
        return core.searchByTrackingNumber(currentUser, trackingNumber);
    }

    /* ========== TrackingApi ========== */
    @Override
    public void addTrackingEvent(String trackingNo, TrackingEventType type, String location, String truckId, String warehouseId,
                                 String description, ExceptionType exceptionTypeOrNull) {

        core.addTrackingEvent(currentUser, trackingNo, type, location, truckId, warehouseId, description, exceptionTypeOrNull);
    }

    @Override
    public TrackingEvent getCurrentStatus(String trackingNo) {
        return core.getCurrentStatus(currentUser, trackingNo);
    }

    @Override
    public List<TrackingEvent> getHistory(String trackingNo) {
        return core.getHistory(currentUser, trackingNo);
    }

    /* ========== QueryApi ========== */
    @Override
    public Parcel searchByTrackingNumber(String trackingNo) {
        return core.searchByTrackingNumber(currentUser, trackingNo);
    }

    @Override
    public List<Parcel> searchByCustomer(long customerId) {
        return core.searchByCustomer(currentUser, customerId);
    }

    @Override
    public List<Parcel> searchByDateRange(LocalDate start, LocalDate end) {
        return core.searchByDateRange(currentUser, start, end);
    }

    /* ========== BillingApi ========== */
    @Override
    public double calculateCharge(Parcel parcel, double distanceKm) {
        return core.calculateCharge(parcel, distanceKm);
    }

    @Override
    public BillingRecord generateBillingRecord(Customer customer, LocalDate start, LocalDate end,
                                               Map<String, Double> distanceByTrackingNo, PaymentMethod preferredPaymentMethod) {
        return core.generateBillingRecord(customer, start, end, distanceByTrackingNo, preferredPaymentMethod);
    }

    @Override
    public List<BillingRecord> getBillingHistoryForCustomer(long customerId) {
        return core.getBillingHistoryForCustomer(customerId);
    }
}
