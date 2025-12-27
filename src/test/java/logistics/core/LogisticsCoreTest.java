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

    /* ===================== TestReporter ===================== */

    private void info(TestReporter reporter, String message) {
        reporter.publishEntry("INFO", message);
    }

    private void step(TestReporter reporter, String stepName, String message) {
        reporter.publishEntry(stepName, message);
    }

    /* ===================== Tests ===================== */

    @Test
    @DisplayName("1.1 客戶管理：建立後可用 id 取回")
    void createCustomer_and_getCustomer(TestReporter reporter) {
        info(reporter, "開始測試：建立客戶並用 id 取回");

        Customer c = core.createCustomer(
                "Cathy", "Tainan", "0933", "c@mail.com",
                CustomerType.PREPAID, BillingPreference.PREPAID
        );
        step(reporter, "CREATE", "建立客戶完成：id=" + c.id + ", name=" + c.name + ", type=" + c.type);

        Customer fetched = core.getCustomer(c.id);
        step(reporter, "GET", "取回客戶完成：id=" + fetched.id + ", name=" + fetched.name);

        assertNotNull(fetched, "getCustomer 不應回傳 null");
        assertEquals("Cathy", fetched.name, "姓名應一致");
        assertEquals(CustomerType.PREPAID, fetched.type, "客戶類型應一致");
        assertEquals(BillingPreference.PREPAID, fetched.billingPreference, "帳單偏好應一致");

        info(reporter, "PASS：客戶建立/取回一致性驗證成功");
    }

    @Test
    @DisplayName("1.3 建立包裹：系統分配唯一追蹤編號 Txxxx")
    void createParcel_generatesTrackingNumber(TestReporter reporter) {
        info(reporter, "開始測試：建立兩個包裹，追蹤編號應唯一且以 T 開頭");

        Parcel p1 = core.createParcel(admin, alice, st, 3, 10, 10, 10, 1000, "book", false, false, false);
        step(reporter, "PARCEL#1", "trackingNumber=" + p1.trackingNumber);

        Parcel p2 = core.createParcel(admin, alice, st, 3, 10, 10, 10, 1000, "book", false, false, false);
        step(reporter, "PARCEL#2", "trackingNumber=" + p2.trackingNumber);

        assertNotNull(p1.trackingNumber, "p1 追蹤編號不可為 null");
        assertTrue(p1.trackingNumber.startsWith("T"), "追蹤編號應以 T 開頭");
        assertNotEquals(p1.trackingNumber, p2.trackingNumber, "兩筆追蹤編號必須不同");

        info(reporter, "PASS：追蹤編號生成規則/唯一性驗證成功");
    }

    @Test
    @DisplayName("1.6 權限：客戶只能替自己建立包裹（不能替他人）")
    void customerCannotCreateParcelForOthers(TestReporter reporter) {
        info(reporter, "開始測試：CUSTOMER 不能替他人建立包裹，但可替自己建立");

        step(reporter, "EXPECT_DENY", "Alice 客戶角色嘗試替 Bob 建包裹（預期拒絕）");
        assertThrows(SecurityException.class, () ->
                core.createParcel(aliceUser, bob, st, 1, 10, 10, 10, 100, "x", false, false, false)
        );

        step(reporter, "EXPECT_ALLOW", "Alice 客戶角色替自己建包裹（預期允許）");
        assertDoesNotThrow(() ->
                core.createParcel(aliceUser, alice, st, 1, 10, 10, 10, 100, "x", false, false, false)
        );

        info(reporter, "PASS：客戶建立包裹權限驗證成功");
    }

    @Test
    @DisplayName("1.4 追蹤：倉儲人員只能新增入倉/出倉/分揀，其它應拒絕")
    void warehouseRoleTrackingPermission(TestReporter reporter) {
        info(reporter, "開始測試：WAREHOUSE 追蹤事件權限");

        Parcel p = core.createParcel(admin, alice, st, 1, 10, 10, 10, 100, "x", false, false, false);
        step(reporter, "SETUP", "建立貨件 trackingNumber=" + p.trackingNumber);

        step(reporter, "EXPECT_ALLOW", "倉儲新增 ENTER_WAREHOUSE（預期允許）");
        assertDoesNotThrow(() ->
                core.addTrackingEvent(wh, p.trackingNumber, TrackingEventType.ENTER_WAREHOUSE,
                        "WH-01", null, "WH-01", "arrived", null)
        );

        step(reporter, "EXPECT_DENY", "倉儲新增 IN_TRANSIT（預期拒絕）");
        assertThrows(SecurityException.class, () ->
                core.addTrackingEvent(wh, p.trackingNumber, TrackingEventType.IN_TRANSIT,
                        "On road", "TRUCK-1", null, "moving", null)
        );

        info(reporter, "PASS：倉儲角色事件權限驗證成功");
    }

    @Test
    @DisplayName("1.6 權限：客戶只能看自己的貨件狀態/歷史")
    void customerCanOnlyViewOwnParcels(TestReporter reporter) {
        info(reporter, "開始測試：CUSTOMER 只能查詢自己貨件");

        Parcel pAlice = core.createParcel(admin, alice, st, 1, 10, 10, 10, 100, "x", false, false, false);
        step(reporter, "SETUP", "建立 Alice 貨件 trackingNumber=" + pAlice.trackingNumber);

        step(reporter, "EXPECT_ALLOW", "Alice 查看自己的 currentStatus（預期允許）");
        assertDoesNotThrow(() -> core.getCurrentStatus(aliceUser, pAlice.trackingNumber));

        step(reporter, "EXPECT_DENY", "Bob 查看 Alice 的 currentStatus/history（預期拒絕）");
        assertThrows(SecurityException.class, () -> core.getCurrentStatus(bobUser, pAlice.trackingNumber));
        assertThrows(SecurityException.class, () -> core.getHistory(bobUser, pAlice.trackingNumber));

        info(reporter, "PASS：客戶查詢權限驗證成功");
    }

    @Test
    @DisplayName("1.4 查詢：日期區間搜尋能找到今天建立的貨件")
    void searchByDateRange(TestReporter reporter) {
        info(reporter, "開始測試：日期區間查詢");

        Parcel p = core.createParcel(admin, alice, st, 1, 10, 10, 10, 100, "x", false, false, false);
        step(reporter, "SETUP", "建立貨件 trackingNumber=" + p.trackingNumber);

        LocalDate today = LocalDate.now();
        List<Parcel> list = core.searchByDateRange(admin, today.minusDays(1), today.plusDays(1));
        step(reporter, "QUERY", "查詢區間=" + today.minusDays(1) + " ~ " + today.plusDays(1) + ", count=" + list.size());

        assertTrue(list.stream().anyMatch(x -> x.trackingNumber.equals(p.trackingNumber)),
                "日期區間搜尋結果應包含剛建立的貨件");

        info(reporter, "PASS：日期區間查詢驗證成功");
    }

    @Test
    @DisplayName("1.6 權限：客服人員（CUSTOMER_SERVICE）可新增所有追蹤事件（含異常）")
    void customerServiceRoleTrackingPermission(TestReporter reporter) {
        info(reporter, "開始測試：CUSTOMER_SERVICE 可新增追蹤事件（含倉儲/運輸/外送/簽收/異常）");

        Parcel p = core.createParcel(admin, alice, st,
                1.0, 10, 10, 10,
                100, "cs-test",
                false, false, false
        );
        String tn = p.trackingNumber;
        step(reporter, "SETUP", "建立貨件 trackingNumber=" + tn);

        // 倉儲相關
        assertDoesNotThrow(() ->
                core.addTrackingEvent(cs, tn, TrackingEventType.ENTER_WAREHOUSE,
                        "WH-01", null, "WH-01", "入倉", null)
        );
        step(reporter, "OK", "客服新增 ENTER_WAREHOUSE 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(cs, tn, TrackingEventType.SORTED,
                        "WH-01", null, "WH-01", "分揀完成", null)
        );
        step(reporter, "OK", "客服新增 SORTED 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(cs, tn, TrackingEventType.EXIT_WAREHOUSE,
                        "WH-01", null, "WH-01", "出倉", null)
        );
        step(reporter, "OK", "客服新增 EXIT_WAREHOUSE 成功");

        // 運輸/外送/投遞/簽收
        assertDoesNotThrow(() ->
                core.addTrackingEvent(cs, tn, TrackingEventType.LOADED_TO_TRUCK,
                        "轉運站A", "TRUCK-1", null, "上車", null)
        );
        step(reporter, "OK", "客服新增 LOADED_TO_TRUCK 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(cs, tn, TrackingEventType.IN_TRANSIT,
                        "國道一號", "TRUCK-1", null, "運送中", null)
        );
        step(reporter, "OK", "客服新增 IN_TRANSIT 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(cs, tn, TrackingEventType.OUT_FOR_DELIVERY,
                        "高雄市前鎮區", "TRUCK-1", null, "外送中", null)
        );
        step(reporter, "OK", "客服新增 OUT_FOR_DELIVERY 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(cs, tn, TrackingEventType.DELIVERED,
                        "收件地址", "TRUCK-1", null, "已投遞", null)
        );
        step(reporter, "OK", "客服新增 DELIVERED 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(cs, tn, TrackingEventType.SIGNED,
                        "收件地址", "TRUCK-1", null, "已簽收", null)
        );
        step(reporter, "OK", "客服新增 SIGNED 成功");

        // 異常
        assertDoesNotThrow(() ->
                core.addTrackingEvent(cs, tn, TrackingEventType.EXCEPTION,
                        "路線中", "TRUCK-1", null, "延誤", ExceptionType.DELAYED)
        );
        step(reporter, "OK", "客服新增 EXCEPTION(DELAYED) 成功");

        // 追加確認可查到狀態/歷史
        assertDoesNotThrow(() -> core.getCurrentStatus(cs, tn));
        assertDoesNotThrow(() -> core.getHistory(cs, tn));
        assertFalse(core.getHistory(cs, tn).isEmpty(), "歷史紀錄不應為空");
        step(reporter, "VERIFY", "客服可查 currentStatus/history，且 history 非空");

        info(reporter, "PASS：客服角色追蹤事件權限驗證成功");
    }

    @Test
    @DisplayName("1.5 計費：calculateCharge 計算符合規則（含附加費與體積）")
    void calculateCharge_ruleCheck(TestReporter reporter) {
        info(reporter, "開始測試：運費計算（含體積與附加費）");

        Parcel p = core.createParcel(admin, alice, st,
                2.0, 120, 10, 10,     // length>100 => oversize
                100, "x",
                true,   // dangerous
                true,   // fragile
                false
        );
        step(reporter, "SETUP", "建立貨件 trackingNumber=" + p.trackingNumber + ", weightKg=" + p.weightKg);

        double distance = 10.0;
        double volume = p.getVolumeCubicMeter();
        step(reporter, "INPUT", "distanceKm=" + distance + ", volume(m^3)=" + volume);

        double expected =
                st.getBasePrice()
                        + st.getPricePerKm() * distance
                        + st.getPricePerKg() * p.weightKg
                        + st.getPricePerCubicMeter() * volume
                        + st.getDangerousSurcharge()
                        + st.getFragileSurcharge()
                        + st.getOversizeSurcharge();

        double actual = core.calculateCharge(p, distance);

        step(reporter, "CALC", "expected=" + expected + ", actual=" + actual);
        assertEquals(expected, actual, 1e-9, "計費公式計算結果應一致");

        info(reporter, "PASS：計費公式驗證成功");
    }

    @Test
    @DisplayName("1.6 權限：駕駛員（DRIVER）可新增運輸/外送/簽收/異常事件；不可做倉儲/分揀類事件")
    void driverRoleTrackingPermission(TestReporter reporter) {
        info(reporter, "開始測試：DRIVER 追蹤事件權限");

        Parcel p = core.createParcel(admin, alice, st,
                1.0, 10, 10, 10,
                100, "test",
                false, false, false
        );
        String tn = p.trackingNumber;
        step(reporter, "SETUP", "建立貨件 trackingNumber=" + tn);

        // Driver 可做
        assertDoesNotThrow(() ->
                core.addTrackingEvent(driver, tn, TrackingEventType.LOADED_TO_TRUCK,
                        "轉運站A", "TRUCK-1", null, "上車", null)
        );
        step(reporter, "OK", "駕駛新增 LOADED_TO_TRUCK 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(driver, tn, TrackingEventType.IN_TRANSIT,
                        "國道一號", "TRUCK-1", null, "運送中", null)
        );
        step(reporter, "OK", "駕駛新增 IN_TRANSIT 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(driver, tn, TrackingEventType.OUT_FOR_DELIVERY,
                        "高雄市前鎮區", "TRUCK-1", null, "外送中", null)
        );
        step(reporter, "OK", "駕駛新增 OUT_FOR_DELIVERY 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(driver, tn, TrackingEventType.DELIVERED,
                        "收件地址", "TRUCK-1", null, "已投遞", null)
        );
        step(reporter, "OK", "駕駛新增 DELIVERED 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(driver, tn, TrackingEventType.SIGNED,
                        "收件地址", "TRUCK-1", null, "已簽收", null)
        );
        step(reporter, "OK", "駕駛新增 SIGNED 成功");

        assertDoesNotThrow(() ->
                core.addTrackingEvent(driver, tn, TrackingEventType.EXCEPTION,
                        "路線中", "TRUCK-1", null, "延誤", ExceptionType.DELAYED)
        );
        step(reporter, "OK", "駕駛新增 EXCEPTION(DELAYED) 成功");

        // Driver 不應做（依你先前的測試假設：倉儲/分揀不允許）
        step(reporter, "EXPECT_DENY", "駕駛新增 ENTER_WAREHOUSE（預期拒絕）");
        assertThrows(SecurityException.class, () ->
                core.addTrackingEvent(driver, tn, TrackingEventType.ENTER_WAREHOUSE,
                        "WH-01", null, "WH-01", "入倉（駕駛不應可做）", null)
        );

        step(reporter, "EXPECT_DENY", "駕駛新增 SORTED（預期拒絕）");
        assertThrows(SecurityException.class, () ->
                core.addTrackingEvent(driver, tn, TrackingEventType.SORTED,
                        "WH-01", null, "WH-01", "分揀（駕駛不應可做）", null)
        );

        info(reporter, "PASS：駕駛角色追蹤事件權限驗證成功");
    }

    @Test
    @DisplayName("1.5 付款方式：合約客戶帳單項目應為 MONTHLY_ACCOUNT")
    void billingPaymentMethod_contractCustomer(TestReporter reporter) {
        info(reporter, "開始測試：合約客戶帳單付款方式應轉為 MONTHLY_ACCOUNT");

        Parcel p = core.createParcel(admin, bob, st, 1, 10, 10, 10, 100, "x", false, false, false);
        step(reporter, "SETUP", "建立 Bob 貨件 trackingNumber=" + p.trackingNumber);

        LocalDate today = LocalDate.now();
        BillingRecord br = core.generateBillingRecord(
                bob,
                today.minusDays(1),
                today.plusDays(1),
                Map.of(p.trackingNumber, 10.0),
                PaymentMethod.CREDIT_CARD
        );

        step(reporter, "BILLING", "產生帳單完成：items=" + br.getItems().size());

        assertEquals(bob.id, br.getCustomer().id, "帳單客戶 id 應為 Bob");
        assertEquals(1, br.getItems().size(), "此範例應只有 1 筆 item");
        assertEquals(PaymentMethod.MONTHLY_ACCOUNT, br.getItems().get(0).getPaymentMethod(),
                "合約客戶付款方式應為 MONTHLY_ACCOUNT");

        info(reporter, "PASS：合約客戶付款方式驗證成功");
    }
}
