package logistics;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logistics.core.LogisticsCore;
import logistics.enums.*;
import logistics.model.*;

public class LogisticsDemo {
    public static void main(String[] args) {
        LogisticsCore core = new LogisticsCore();

        // 建立一些客戶 [1,2,3]
        Customer c1 = core.createCustomer(
                "ABC Corp.", "Taipei City", "02-1234-5678", "abc@example.com",
                CustomerType.CONTRACT, BillingPreference.MONTHLY
        );
        Customer c2 = core.createCustomer(
                "Walk-in User", "New Taipei City", "02-8765-4321", "user@example.com",
                CustomerType.NON_CONTRACT, BillingPreference.CASH_ON_DELIVERY
        );

        // 建立使用者帳號 [20,21]
        User admin = new User("admin", Role.ADMIN, null);
        User customerUser = new User("user1", Role.CUSTOMER, c2);

        // 建立服務類型 [4,5,6]
        ServiceType st1 = core.createServiceType(
                "Overnight Small Box",
                PackageType.SMALL_BOX,
                0, 5,
                DeliverySpeed.OVERNIGHT,
                100, 5, 10, 50,
                200, 50, 100
        );

        // 建立包裹（由管理員建立給合約客戶） [7,8]
        Parcel p1 = core.createParcel(
                admin,
                c1,
                st1,
                2.5, 30, 20, 10,
                5000,
                "Documents",
                false,
                true,
                false
        );

        // 增加幾個追蹤事件 [9,10,11]
        User driver = new User("driverA", Role.DRIVER, null);
        core.addTrackingEvent(driver, p1.trackingNumber,
                TrackingEventType.LOADED_TO_TRUCK,
                "Taipei Hub", "TRUCK-001", null,
                "Loaded to truck", null);

        core.addTrackingEvent(driver, p1.trackingNumber,
                TrackingEventType.IN_TRANSIT,
                "Highway 1", "TRUCK-001", null,
                "On the way", null);

        core.addTrackingEvent(driver, p1.trackingNumber,
                TrackingEventType.OUT_FOR_DELIVERY,
                "Taichung City", "TRUCK-001", null,
                "Out for delivery", null);

        core.addTrackingEvent(driver, p1.trackingNumber,
                TrackingEventType.DELIVERED,
                "Taichung City", "TRUCK-001", null,
                "Delivered at door", null);

        // 顯示目前狀態 [12]
        System.out.println("=== Current status ===");
        System.out.println(core.getCurrentStatus(admin, p1.trackingNumber));

        // 顯示完整歷史 [13]
        System.out.println("\n=== History ===");
        for (TrackingEvent e : core.getHistory(admin, p1.trackingNumber)) {
            System.out.println("  " + e);
        }

        // 產生計費紀錄 [16,17,18,19]
        Map<String, Double> distanceMap = new HashMap<>();
        distanceMap.put(p1.trackingNumber, 200.0); // 例如 200 km

        BillingRecord br = core.generateBillingRecord(
                c1,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                distanceMap,
                PaymentMethod.CREDIT_CARD
        );
    System.out.println("\n=== Billing ===");
    System.out.println(br);
    for (BillingItem item : br.getItems()) {
    System.out.println("  Parcel " + item.getParcel().trackingNumber +
            " amount=" + item.getAmount() +
            " pm=" + item.getPaymentMethod());
}


        // 客戶角色查詢自己包裹 [21]
        Parcel p2 = core.createParcel(
            customerUser,
            c2,
            st1,
            1.0, 20, 20, 10,
            1000,
            "Books",
            false,
            false,
            false
        );
        System.out.println("\n=== Customer user checking own parcel ===");
        System.out.println(core.searchByTrackingNumber(customerUser, p2.trackingNumber));

        // 示範客戶無法看別人包裹 [21]
        try {
            System.out.println("\nCustomer user tries to view p1 (other customer's parcel):");
            core.searchByTrackingNumber(customerUser, p1.trackingNumber);
        } catch (SecurityException ex) {
            System.out.println("Access denied as expected: " + ex.getMessage());
        }

        // 示範搜尋功能 [14]
        System.out.println("\n=== Search by customer c1 ===");
        List<Parcel> c1Parcels = core.searchByCustomer(admin, c1.id);
        c1Parcels.forEach(System.out::println);
    }
}