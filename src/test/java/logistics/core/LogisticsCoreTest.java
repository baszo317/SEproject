package logistics.core;

import logistics.enums.*;
import logistics.model.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogisticsCoreTest {

    private LogisticsCore core;

    private Customer alice;     // 客戶A
    private Customer bob;       // 客戶B
    private ServiceType st;     // 服務類型

    private User admin;
    private User cs;            // 客服
    private User wh;            // 倉儲
    private User driver;
    private User aliceUser;     // 客戶角色（綁 alice）
    private User bobUser;       // 客戶角色（綁 bob）

    @BeforeEach
    void setUp() {
        core = new LogisticsCore();

        // 建客戶
        alice = core.createCustomer(
                "Alice", "Kaohsiung", "0912", "a@mail.com",
                CustomerType.NON_CONTRACT, BillingPreference.CASH_ON_DELIVERY
        );
        bob = core.createCustomer(
                "Bob", "Taipei", "0922", "b@mail.com",
                CustomerType.CONTRACT, BillingPreference.MONTHLY
        );

        // 建服務類型
        st = core.createServiceType(
                "標準小箱", PackageType.SMALL_BOX,
                0.0, 20.0, DeliverySpeed.STANDARD,
                50.0,   // base
                2.0,    // perKm
                10.0,   // perKg
                100.0,  // perM3
                30.0,   // dangerous
                20.0,   // fragile
                40.0    // oversize
        );

        // 使用者角色
        admin = new User("admin", Role.ADMIN, null);
        cs = new User("cs", Role.CUSTOMER_SERVICE, null);
        wh = new User("wh", Role.WAREHOUSE, null);
        driver = new User("driver", Role.DRIVER, null);

        aliceUser = new User("customer#" + alice.id, Role.CUSTOMER, alice);
        bobUser = new User("customer#" + bob.id, Role.CUSTOMER, bob);
    }

    @Test
    @DisplayName("1.1 客戶管理：建立後可用 id 取回")
    void createCustomer_and_getCustomer() {
        Customer c = core.createCustomer(
                "Cathy", "Tainan", "0933", "c@mail.com",
                CustomerType.PREPAID, BillingPreference.PREPAID
        );
        Customer fetched = core.getCustomer(c.id);

        assertNotNull(fetched);
        assertEquals("Cathy", fetched.name);
        assertEquals(CustomerType.PREPAID, fetched.type);
        assertEquals(BillingPreference.PREPAID, fetched.billingPreference);
    }

    @Test
    @DisplayName("1.3 建立包裹：系統分配唯一追蹤編號 Txxxx")
    void createParcel_generatesTrackingNumber() {
        Parcel p1 = core.createParcel(admin, alice, st, 3, 10, 10, 10, 1000, "book", false, false, false);
        Parcel p2 = core.createParcel(admin, alice, st, 3, 10, 10, 10, 1000, "book", false, false, false);

        assertNotNull(p1.trackingNumber);
        assertTrue(p1.trackingNumber.startsWith("T"));
        assertNotEquals(p1.trackingNumber, p2.trackingNumber);
    }

    @Test
    @DisplayName("1.6 權限：客戶只能替自己建立包裹（不能替他人）")
    void customerCannotCreateParcelForOthers() {
        assertThrows(SecurityException.class, () ->
                core.createParcel(aliceUser, bob, st, 1, 10, 10, 10, 100, "x", false, false, false)
        );

        // 但替自己可以
        assertDoesNotThrow(() ->
                core.createParcel(aliceUser, alice, st, 1, 10, 10, 10, 100, "x", false, false, false)
        );
    }

    @Test
    @DisplayName("1.4 追蹤：倉儲人員只能新增入倉/出倉/分揀，其它應拒絕")
    void warehouseRoleTrackingPermission() {
        Parcel p = core.createParcel(admin, alice, st, 1, 10, 10, 10, 100, "x", false, false, false);

        // 倉儲允許 ENTER_WAREHOUSE
        assertDoesNotThrow(() ->
                core.addTrackingEvent(wh, p.trackingNumber, TrackingEventType.ENTER_WAREHOUSE,
                        "WH-01", null, "WH-01", "arrived", null)
        );

        // 倉儲不允許 IN_TRANSIT
        assertThrows(SecurityException.class, () ->
                core.addTrackingEvent(wh, p.trackingNumber, TrackingEventType.IN_TRANSIT,
                        "On road", "TRUCK-1", null, "moving", null)
        );
    }

    @Test
    @DisplayName("1.6 權限：客戶只能看自己的貨件狀態/歷史")
    void customerCanOnlyViewOwnParcels() {
        Parcel pAlice = core.createParcel(admin, alice, st, 1, 10, 10, 10, 100, "x", false, false, false);

        // Alice 自己看 OK
        assertDoesNotThrow(() -> core.getCurrentStatus(aliceUser, pAlice.trackingNumber));

        // Bob 客戶角色去看 Alice 的貨件 → 應拒絕
        assertThrows(SecurityException.class, () -> core.getCurrentStatus(bobUser, pAlice.trackingNumber));
        assertThrows(SecurityException.class, () -> core.getHistory(bobUser, pAlice.trackingNumber));
    }

    @Test
    @DisplayName("1.4 查詢：日期區間搜尋能找到今天建立的貨件")
    void searchByDateRange() {
        Parcel p = core.createParcel(admin, alice, st, 1, 10, 10, 10, 100, "x", false, false, false);

        LocalDate today = LocalDate.now();
        List<Parcel> list = core.searchByDateRange(admin, today.minusDays(1), today.plusDays(1));

        assertTrue(list.stream().anyMatch(x -> x.trackingNumber.equals(p.trackingNumber)));
    }

    @Test
    @DisplayName("1.5 計費：calculateCharge 計算符合規則（含附加費與體積）")
    void calculateCharge_ruleCheck() {
        // oversize：length > 100 觸發
        Parcel p = core.createParcel(admin, alice, st,
                2.0, 120, 10, 10,
                100, "x",
                true,   // dangerous
                true,   // fragile
                false
        );

        double distance = 10.0;

        // 体积 m^3 = (1.20 * 0.10 * 0.10) = 0.012
        double volume = p.getVolumeCubicMeter();

        double expected =
                st.getBasePrice()
                        + st.getPricePerKm() * distance
                        + st.getPricePerKg() * p.weightKg
                        + st.getPricePerCubicMeter() * volume
                        + st.getDangerousSurcharge()
                        + st.getFragileSurcharge()
                        + st.getOversizeSurcharge();

        double actual = core.calculateCharge(p, distance);
        assertEquals(expected, actual, 1e-9);
    }

    @Test
    @DisplayName("1.5 付款方式：合約客戶帳單項目應為 MONTHLY_ACCOUNT")
    void billingPaymentMethod_contractCustomer() {
        Parcel p = core.createParcel(admin, bob, st, 1, 10, 10, 10, 100, "x", false, false, false);

        LocalDate today = LocalDate.now();
        BillingRecord br = core.generateBillingRecord(
                bob,
                today.minusDays(1),
                today.plusDays(1),
                Map.of(p.trackingNumber, 10.0),
                PaymentMethod.CREDIT_CARD
        );

        assertEquals(bob.id, br.getCustomer().id);
        assertEquals(1, br.getItems().size());
        assertEquals(PaymentMethod.MONTHLY_ACCOUNT, br.getItems().get(0).getPaymentMethod());
    }
}
