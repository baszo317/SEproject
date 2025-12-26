package logistics.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import logistics.enums.*;
import logistics.model.*;

/**
 * 系統核心：負責客戶管理、服務類型、包裹、追蹤、計費、查詢與權限控制
 */
public class LogisticsCore {

    private long customerSeq = 1;
    private long serviceTypeSeq = 1;
    private long billingSeq = 1;
    private long trackingSeq = 100000000L;

    // 簡單 in-memory 儲存
    Map<Long, Customer> customers = new HashMap<>();
    Map<Long, ServiceType> serviceTypes = new HashMap<>();
    Map<String, Parcel> parcels = new HashMap<>();
    List<BillingRecord> billingRecords = new ArrayList<>();

    /* ========= 客戶管理 [1,2,3] ========= */
    public Customer createCustomer(String name, String address, String phone, String email,
                                   CustomerType type, BillingPreference preference) {
        long id = customerSeq++;
        Customer c = new Customer(id, name, address, phone, email, type, preference);
        customers.put(id, c);
        return c;
    }

    public Customer getCustomer(long id) {
        return customers.get(id);
    }

    /* ========= 服務類型管理 [4,5,6] ========= */
    public ServiceType createServiceType(String name, PackageType packageType, double minWeightKg,
                                         double maxWeightKg, DeliverySpeed speed,
                                         double basePrice, double pricePerKm, double pricePerKg,
                                         double pricePerCubicMeter,
                                         double dangerousSurcharge, double fragileSurcharge,
                                         double oversizeSurcharge) {
        long id = serviceTypeSeq++;
        ServiceType st = new ServiceType(id, name, packageType, minWeightKg, maxWeightKg, speed,
                basePrice, pricePerKm, pricePerKg, pricePerCubicMeter,
                dangerousSurcharge, fragileSurcharge, oversizeSurcharge);
        serviceTypes.put(id, st);
        return st;
    }

    /* ========= 包裹建立 [7,8] ========= */
    private String generateTrackingNumber() {
        return "T" + (trackingSeq++);
    }

    public Parcel createParcel(User operator, Customer sender, ServiceType st,
                               double weightKg, double lengthCm, double widthCm, double heightCm,
                               double declaredValue, String description,
                               boolean dangerous, boolean fragile, boolean international) {

        if (!canCreateParcel(operator, sender)) {
            throw new SecurityException("沒有建立包裹的權限");
        }

        String trackingNo = generateTrackingNumber();
        Parcel p = new Parcel(trackingNo, sender, st,
                weightKg, lengthCm, widthCm, heightCm,
                declaredValue, description,
                dangerous, fragile, international);

        TrackingEvent event = new TrackingEvent(
                TrackingEventType.PICKED_UP,
                LocalDateTime.now(),
                sender.address,
                null,
                null,
                "Package created and picked up",
                null
        );
        p.addEvent(event);   // 改用方法

        parcels.put(trackingNo, p);
        return p;
    }

    private boolean canCreateParcel(User operator, Customer sender) {
        if (operator == null) return false;
        switch (operator.getRole()) {
            case ADMIN:
            case CUSTOMER_SERVICE:
            case WAREHOUSE:
                return true;
            case CUSTOMER:
                return operator.getCustomerProfile() != null
                        && operator.getCustomerProfile().id == sender.id;
            default:
                return false;
        }
    }

    /* ========= 追蹤事件管理 [9,10,11,15] ========= */
    public void addTrackingEvent(User operator, String trackingNo,
                                 TrackingEventType type,
                                 String location, String truckId, String warehouseId,
                                 String description, ExceptionType exceptionType) {
        Parcel p = parcels.get(trackingNo);
        if (p == null) throw new IllegalArgumentException("找不到包裹：" + trackingNo);

        if (!canUpdateTracking(operator, type)) {
            throw new SecurityException("沒有新增此類追蹤事件的權限");
        }

        TrackingEvent e = new TrackingEvent(
                type,
                LocalDateTime.now(),
                location,
                truckId,
                warehouseId,
                description,
                exceptionType
        );
        p.addEvent(e);   // 改用方法
    }

    private boolean canUpdateTracking(User user, TrackingEventType type) {
        if (user == null) return false;
        Role role = user.getRole();

        if (role == Role.ADMIN || role == Role.CUSTOMER_SERVICE) return true;
        if (role == Role.WAREHOUSE) {
            return type == TrackingEventType.ENTER_WAREHOUSE
                    || type == TrackingEventType.EXIT_WAREHOUSE
                    || type == TrackingEventType.SORTED;
        }
        if (role == Role.DRIVER) {
            return type == TrackingEventType.LOADED_TO_TRUCK
                    || type == TrackingEventType.UNLOADED_FROM_TRUCK
                    || type == TrackingEventType.IN_TRANSIT
                    || type == TrackingEventType.OUT_FOR_DELIVERY
                    || type == TrackingEventType.DELIVERED
                    || type == TrackingEventType.SIGNED
                    || type == TrackingEventType.EXCEPTION;
        }
        return false;
    }

    /* ========= 追蹤查詢 [12,13,14,21] ========= */

    public TrackingEvent getCurrentStatus(User viewer, String trackingNo) {
        Parcel p = getParcelWithAccessCheck(viewer, trackingNo);
        return p.getCurrentStatus();
    }

    public List<TrackingEvent> getHistory(User viewer, String trackingNo) {
        Parcel p = getParcelWithAccessCheck(viewer, trackingNo);
        return p.getEvents();
    }

    private Parcel getParcelWithAccessCheck(User viewer, String trackingNo) {
        Parcel p = parcels.get(trackingNo);
        if (p == null) throw new IllegalArgumentException("找不到包裹：" + trackingNo);
        if (viewer == null) throw new SecurityException("未登入");

        if (viewer.getRole() == Role.CUSTOMER) {
            Customer cp = viewer.getCustomerProfile();
            if (cp == null || cp.id != p.sender.id) {
                throw new SecurityException("客戶不得存取他人貨件資料");
            }
        }
        return p;
    }

    public Parcel searchByTrackingNumber(User viewer, String trackingNo) {
        return getParcelWithAccessCheck(viewer, trackingNo);
    }

    public List<Parcel> searchByCustomer(User viewer, long customerId) {
        List<Parcel> result = new ArrayList<>();
        for (Parcel p : parcels.values()) {
            if (p.sender.id == customerId) {
                if (viewer.getRole() == Role.CUSTOMER) {
                    Customer cp = viewer.getCustomerProfile();
                    if (cp == null || cp.id != customerId) {
                        continue;
                    }
                }
                result.add(p);
            }
        }
        return result;
    }

    public List<Parcel> searchByDateRange(User viewer, LocalDate from, LocalDate to) {
        List<Parcel> result = new ArrayList<>();
        for (Parcel p : parcels.values()) {
            if (!p.hasEvents()) continue;
            List<TrackingEvent> events = p.getEvents();
            LocalDate date = events.get(0).getTimestamp().toLocalDate();  // ★ 用 getter

            if ((date.isEqual(from) || date.isAfter(from)) &&
                    (date.isEqual(to) || date.isBefore(to))) {

                if (viewer.getRole() == Role.CUSTOMER) {
                    Customer cp = viewer.getCustomerProfile();
                    if (cp == null || cp.id != p.sender.id) {
                        continue;
                    }
                }
                result.add(p);
            }
        }
        return result;
    }

    public List<Parcel> searchByTruckId(User viewer, String truckId) {
        List<Parcel> result = new ArrayList<>();
        for (Parcel p : parcels.values()) {
            for (TrackingEvent e : p.getEvents()) {
                if (truckId != null && truckId.equals(e.getTruckId())) {  // ★ 用 getter
                    if (viewer.getRole() == Role.CUSTOMER) {
                        Customer cp = viewer.getCustomerProfile();
                        if (cp == null || cp.id != p.sender.id) {
                            break;
                        }
                    }
                    result.add(p);
                    break;
                }
            }
        }
        return result;
    }

    public List<Parcel> searchByWarehouseId(User viewer, String warehouseId) {
        List<Parcel> result = new ArrayList<>();
        for (Parcel p : parcels.values()) {
            for (TrackingEvent e : p.getEvents()) {
                if (warehouseId != null && warehouseId.equals(e.getWarehouseId())) { // ★ 用 getter
                    if (viewer.getRole() == Role.CUSTOMER) {
                        Customer cp = viewer.getCustomerProfile();
                        if (cp == null || cp.id != p.sender.id) {
                            break;
                        }
                    }
                    result.add(p);
                    break;
                }
            }
        }
        return result;
    }

    /* ========= 計費 [16,17,18,19] ========= */

public double calculateCharge(Parcel p, double distanceKm) {
    ServiceType st = p.serviceType;         // 這行先暫時保留 public，之後你也可以改成 getter
    double volume = p.getVolumeCubicMeter();

    double amount = st.getBasePrice()
            + st.getPricePerKm() * distanceKm
            + st.getPricePerKg() * p.weightKg
            + st.getPricePerCubicMeter() * volume;

    if (p.dangerousGoods) {
        amount += st.getDangerousSurcharge();
    }
    if (p.fragile) {
        amount += st.getFragileSurcharge();
    }
    boolean oversize = p.lengthCm > 100 || p.widthCm > 100 || p.heightCm > 100;
    if (oversize) {
        amount += st.getOversizeSurcharge();
    }
    return amount;
}


    public BillingRecord generateBillingRecord(Customer customer,
                                               LocalDate start, LocalDate end,
                                               Map<String, Double> distanceByTracking,
                                               PaymentMethod defaultPaymentMethod) {
        String id = "B" + (billingSeq++);
        BillingRecord record = new BillingRecord(id, customer, start, end);

        for (Parcel p : parcels.values()) {
            if (p.sender.id != customer.id) continue;
            if (!p.hasEvents()) continue;

            List<TrackingEvent> events = p.getEvents();
            LocalDate shipDate = events.get(0).getTimestamp().toLocalDate(); // ★ 用 getter
            if (shipDate.isBefore(start) || shipDate.isAfter(end)) continue;

            double distance = distanceByTracking.getOrDefault(p.trackingNumber, 10.0);
            double amount = calculateCharge(p, distance);

            PaymentMethod pm = determinePaymentMethodForParcel(customer, defaultPaymentMethod);

           BillingItem item = new BillingItem(p, amount, pm);
            record.addItem(item);

        }

        billingRecords.add(record);
        return record;
    }

    private PaymentMethod determinePaymentMethodForParcel(Customer customer,
                                                          PaymentMethod defaultMethod) {
        if (customer.type == CustomerType.CONTRACT) {
            return PaymentMethod.MONTHLY_ACCOUNT;
        }
        if (customer.type == CustomerType.PREPAID) {
            return PaymentMethod.PREPAID;
        }
        if (customer.billingPreference == BillingPreference.CASH_ON_DELIVERY) {
            return PaymentMethod.CASH;
        }
        if (customer.billingPreference == BillingPreference.PREPAID) {
            return PaymentMethod.PREPAID;
        }
        return defaultMethod;
    }

   public List<BillingRecord> getBillingHistoryForCustomer(long customerId) {
    List<BillingRecord> list = new ArrayList<>();
    for (BillingRecord br : billingRecords) {
        if (br.getCustomer().id == customerId) {
            list.add(br);
        }
    }
    return list;
}
}
