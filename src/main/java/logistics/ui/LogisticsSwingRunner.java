package logistics.ui;

import logistics.core.LogisticsCore;
import logistics.enums.*;
import logistics.model.*;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;

public class LogisticsSwingRunner {

    private final LogisticsCore core = new LogisticsCore();

  
    private final Map<Long, Customer> customersCreated = new LinkedHashMap<>();
    private final Map<Long, ServiceType> serviceTypesCreated = new LinkedHashMap<>();
    private final Set<String> trackingNumbersCreated = new LinkedHashSet<>();

    // 目前登入身份（預設 管理員）
    private User currentUser = new User("admin", Role.ADMIN, null);

    
    private static final String[] MAIN_OPTIONS = {
            "教學/操作說明",
            "1.1 客戶管理",
            "1.2 包裹服務分類",
            "1.3 包裹收件與準備",
            "1.4 追蹤與物流",
            "1.5 計費與付款",
            "1.6 安全與權限",
            "離開"
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LogisticsSwingRunner().run());
    }

    private void run() {
        while (true) {
            int main = JOptionPane.showOptionDialog(
                    null,
                    buildHeaderText(),
                    "物流系統（Swing 介面）",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    MAIN_OPTIONS,
                    MAIN_OPTIONS[0]
            );

            if (main == -1 || main == 7) {
                showMessage("已離開系統。");
                return;
            }

            try {
                switch (main) {
                    case 0 -> showTutorial();
                    case 1 -> menuCustomer_1_1();
                    case 2 -> menuServiceType_1_2();
                    case 3 -> menuParcelCreate_1_3();
                    case 4 -> menuTracking_1_4();
                    case 5 -> menuBilling_1_5();
                    case 6 -> menuSecurity_1_6();
                    default -> showMessage("未知選項。");
                }
            } catch (SecurityException se) {
                error("權限不足", kvBlock(
                        "原因", se.getMessage(),
                        "登入帳號", currentUser.getUsername(),
                        "角色", roleZh(currentUser.getRole())
                ));
            } catch (IllegalArgumentException iae) {
                error("輸入/資料錯誤", kvBlock("原因", iae.getMessage()));
            } catch (Exception e) {
                error("系統例外", kvBlock(
                        "例外類型", e.getClass().getSimpleName(),
                        "訊息", e.getMessage()
                ));
            }
        }
    }

    /* ===================== 教學/操作說明 ===================== */

    private void showTutorial() {
        String text =
                h1("教學/操作說明") + "\n" +
                "建議操作順序（最常用流程）：\n" +
                "1) 到【1.6 安全與權限】切換角色（建議先用「管理員」或「客服人員」）。\n" +
                "2) 到【1.1 客戶管理】建立客戶檔案（姓名/地址/電話/Email + 客戶類型 + 帳單偏好）。\n" +
                "3) 到【1.2 包裹服務分類】建立服務類型（包裹類型/重量級距/配送時效 + 定價與附加費）。\n" +
                "4) 到【1.3 包裹收件與準備】建立包裹（系統會自動分配追蹤編號）。\n" +
                "5) 到【1.4 追蹤與物流】新增追蹤事件、查目前狀態、查歷史、依條件搜尋。\n" +
                "6) 到【1.5 計費與付款】計算運費、產生帳單、查看帳單歷史。\n\n" +
                hr() + "\n" +
                "角色/權限概要（系統核心會做限制）：\n" +
                "• 管理員 / 客服人員：可建立包裹、可新增多數追蹤事件、可做查詢與帳單操作。\n" +
                "• 倉儲人員：追蹤事件通常限於「入倉/出倉/分揀」。\n" +
                "• 駕駛員：追蹤事件通常限於「進/出貨車、運送中、外送、投遞、簽收、異常」。\n" +
                "• 客戶：只能查詢/檢視「自己的」貨件；建立包裹通常也限於自己的帳號。\n\n" +
                "如需進一步協助，請聯絡系統管理員或參閱系統文件。";
        report("教學/操作說明", text);
    }

    /* ===================== 1.1 客戶管理 ===================== */

    private void menuCustomer_1_1() {
        String[] opts = {
                "建立客戶檔案（姓名/地址/電話/Email + 客戶類型 + 帳單偏好）",
                "查看客戶檔案（依 customerId）",
                "列出所有客戶",
                "返回"
        };

        int c = JOptionPane.showOptionDialog(
                null, "1.1 客戶管理", "客戶管理",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opts, opts[0]
        );

        switch (c) {
            case 0 -> createCustomerUI();
            case 1 -> viewCustomerUI();
            case 2 -> listCustomersUI();
            default -> { }
        }
    }

    private void createCustomerUI() {
        String name = askNonEmpty("輸入客戶姓名：");
        if (name == null) return;

        String address = askNonEmpty("輸入地址：");
        if (address == null) return;

        String phone = askOptional("輸入電話（可空白）：");
        if (phone == null) return;

        String email = askOptional("輸入Email（可空白）：");
        if (email == null) return;

        CustomerType type = pickEnum(
                "選擇客戶類型",
                "請選擇客戶類型：",
                CustomerType.values(),
                this::customerTypeZh,
                CustomerType.NON_CONTRACT
        );
        if (type == null) return;

        BillingPreference pref = pickEnum(
                "選擇帳單偏好",
                "請選擇帳單偏好：",
                BillingPreference.values(),
                this::billingPrefZh,
                BillingPreference.CASH_ON_DELIVERY
        );
        if (pref == null) return;

        Customer c = core.createCustomer(name, address, phone, email, type, pref);
        customersCreated.put(c.id, c);

        info("建立客戶成功",
                h1("客戶建立完成") + "\n" +
                        kvBlock(
                                "客戶編號", c.id,
                                "姓名", c.name,
                                "地址", c.address,
                                "電話", c.phone,
                                "Email", c.email,
                                "客戶類型", customerTypeZh(c.type),
                                "帳單偏好", billingPrefZh(c.billingPreference)
                        ) +
                        "\n" + hr() + "\n" +
                        kv("目前客戶數", customersCreated.size())
        );
    }

    private void viewCustomerUI() {
        Long id = askLongOrPickCustomerId("輸入 customerId（或從清單選擇）：");
        if (id == null) return;

        Customer c = core.getCustomer(id);
        if (c == null) {
            info("客戶檔案", h1("查無客戶") + "\n" + kv("customerId", id));
            return;
        }

        report("客戶檔案",
                h1("客戶資料") + "\n" +
                        kvBlock(
                                "客戶編號", c.id,
                                "姓名", c.name,
                                "地址", c.address,
                                "電話", c.phone,
                                "Email", c.email,
                                "客戶類型", customerTypeZh(c.type),
                                "帳單偏好", billingPrefZh(c.billingPreference)
                        )
        );
    }

    private void listCustomersUI() {
        if (customersCreated.isEmpty()) {
            info("客戶清單", "目前尚未建立任何客戶。");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(h1("客戶清單")).append('\n');
        sb.append(kv("總數", customersCreated.size())).append('\n');
        sb.append(hr()).append('\n');

        int i = 1;
        for (Customer c : customersCreated.values()) {
            sb.append("[").append(i++).append("]\n");
            sb.append(kv("客戶編號", c.id)).append('\n');
            sb.append(kv("姓名", c.name)).append('\n');
            sb.append(kv("客戶類型", customerTypeZh(c.type))).append('\n');
            sb.append(kv("帳單偏好", billingPrefZh(c.billingPreference))).append('\n');
            sb.append(hr()).append('\n');
        }

        report("客戶清單", sb.toString());
    }

    /* ===================== 1.2 包裹服務分類 ===================== */

    private void menuServiceType_1_2() {
        String[] opts = {
                "建立服務類型（包裹類型/重量分級/配送時效 + 定價規則 + 附加費）",
                "列出所有服務類型",
                "返回"
        };

        int c = JOptionPane.showOptionDialog(
                null, "1.2 包裹服務分類", "服務類型",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opts, opts[0]
        );

        switch (c) {
            case 0 -> createServiceTypeUI();
            case 1 -> listServiceTypesUI();
            default -> { }
        }
    }

    private void createServiceTypeUI() {
        String name = askNonEmpty("輸入服務類型名稱：");
        if (name == null) return;

        PackageType pt = pickEnum(
                "選擇包裹類型",
                "請選擇包裹類型：",
                PackageType.values(),
                this::packageTypeZh,
                PackageType.SMALL_BOX
        );
        if (pt == null) return;

        DeliverySpeed speed = pickEnum(
                "選擇配送時效",
                "請選擇配送時效：",
                DeliverySpeed.values(),
                this::deliverySpeedZh,
                DeliverySpeed.STANDARD
        );
        if (speed == null) return;

        double minW = askDouble("最小重量(kg)：");
        double maxW = askDouble("最大重量(kg)：");

        double base = askDouble("基本費用 basePrice：");
        double perKm = askDouble("每公里 pricePerKm：");
        double perKg = askDouble("每公斤 pricePerKg：");
        double perM3 = askDouble("每立方公尺 pricePerCubicMeter：");

        double dangerousFee = askDouble("危險品附加費 dangerousSurcharge：");
        double fragileFee = askDouble("易碎品附加費 fragileSurcharge：");
        double oversizeFee = askDouble("超大件附加費 oversizeSurcharge：");

        ServiceType st = core.createServiceType(
                name, pt, minW, maxW, speed,
                base, perKm, perKg, perM3,
                dangerousFee, fragileFee, oversizeFee
        );
        serviceTypesCreated.put(st.getId(), st);

        info("建立服務類型成功",
                h1("服務類型建立完成") + "\n" +
                        kvBlock(
                                "服務類型編號", st.getId(),
                                "名稱", st.getName(),
                                "包裹類型", packageTypeZh(st.getPackageType()),
                                "配送時效", deliverySpeedZh(st.getSpeed()),
                                "重量範圍(kg)", st.getMinWeightKg() + " ~ " + st.getMaxWeightKg(),
                                "基本費用", st.getBasePrice(),
                                "每公里", st.getPricePerKm(),
                                "每公斤", st.getPricePerKg(),
                                "每立方公尺", st.getPricePerCubicMeter(),
                                "危險品附加費", st.getDangerousSurcharge(),
                                "易碎品附加費", st.getFragileSurcharge(),
                                "超大件附加費", st.getOversizeSurcharge()
                        )
        );
    }

    private void listServiceTypesUI() {
        if (serviceTypesCreated.isEmpty()) {
            info("服務類型清單", "目前尚未建立任何服務類型。");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(h1("服務類型清單")).append('\n');
        sb.append(kv("總數", serviceTypesCreated.size())).append('\n');
        sb.append(hr()).append('\n');

        int i = 1;
        for (ServiceType st : serviceTypesCreated.values()) {
            sb.append("[").append(i++).append("]\n");
            sb.append(kv("服務類型編號", st.getId())).append('\n');
            sb.append(kv("名稱", st.getName())).append('\n');
            sb.append(kv("包裹類型", packageTypeZh(st.getPackageType()))).append('\n');
            sb.append(kv("配送時效", deliverySpeedZh(st.getSpeed()))).append('\n');
            sb.append(kv("重量範圍(kg)", st.getMinWeightKg() + " ~ " + st.getMaxWeightKg())).append('\n');
            sb.append(hr()).append('\n');
        }

        report("服務類型清單", sb.toString());
    }

    /* ===================== 1.3 包裹收件與準備 ===================== */

    private void menuParcelCreate_1_3() {
        String[] opts = {
                "建立包裹（分配追蹤編號 + 記錄重量/尺寸/申報價值/描述 + 特殊服務標示）",
                "返回"
        };

        int c = JOptionPane.showOptionDialog(
                null, "1.3 包裹收件與準備", "包裹建立",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opts, opts[0]
        );

        if (c == 0) createParcelUI();
    }

    private void createParcelUI() {
        Customer sender = pickCustomerOrAskToCreate();
        if (sender == null) return;

        ServiceType st = pickServiceTypeOrAskToCreate();
        if (st == null) return;

        double weightKg = askDouble("重量(kg)：");
        double lengthCm = askDouble("長(cm)：");
        double widthCm = askDouble("寬(cm)：");
        double heightCm = askDouble("高(cm)：");
        double declaredValue = askDouble("申報價值：");

        String desc = askOptional("內容物描述（可空白）：");
        if (desc == null) return;

        boolean dangerous = askYesNo("特殊服務：危險物品？");
        boolean fragile = askYesNo("特殊服務：易碎品？");
        boolean international = askYesNo("特殊服務：國際貨件？");

        Parcel p = core.createParcel(
                currentUser, sender, st,
                weightKg, lengthCm, widthCm, heightCm,
                declaredValue, desc,
                dangerous, fragile, international
        );

        trackingNumbersCreated.add(p.trackingNumber);

        info("建立包裹成功",
                h1("包裹建立完成") + "\n" +
                        kvBlock(
                                "追蹤編號", p.trackingNumber,
                                "寄件客戶", sender.name + "（#" + sender.id + "）",
                                "服務類型", st.getName() + "（#" + st.getId() + "）",
                                "包裹類型", packageTypeZh(st.getPackageType()),
                                "配送時效", deliverySpeedZh(st.getSpeed()),
                                "重量(kg)", p.weightKg,
                                "尺寸(cm)", p.lengthCm + " x " + p.widthCm + " x " + p.heightCm,
                                "申報價值", p.declaredValue,
                                "描述", p.description,
                                "危險物品", p.dangerousGoods ? "是" : "否",
                                "易碎品", p.fragile ? "是" : "否",
                                "國際件", p.international ? "是" : "否"
                        )
        );
    }

    /* ===================== 1.4 追蹤與物流 ===================== */

    private void menuTracking_1_4() {
        String[] opts = {
                "新增追蹤事件（含異常：遺失/延誤/損毀）",
                "查詢目前狀態",
                "查詢歷史追蹤",
                "搜尋：依追蹤編號",
                "搜尋：依客戶帳號",
                "搜尋：依運送日期範圍",
                "搜尋：依貨車識別碼",
                "搜尋：依倉儲地點",
                "返回"
        };

        int c = JOptionPane.showOptionDialog(
                null, "1.4 追蹤與物流", "追蹤與物流",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opts, opts[0]
        );

        switch (c) {
            case 0 -> addTrackingEventUI();
            case 1 -> getCurrentStatusUI();
            case 2 -> getHistoryUI();
            case 3 -> searchByTrackingNoUI();
            case 4 -> searchByCustomerUI();
            case 5 -> searchByDateRangeUI();
            case 6 -> searchByTruckIdUI();
            case 7 -> searchByWarehouseIdUI();
            default -> { }
        }
    }

    private void addTrackingEventUI() {
        String trackingNo = pickTrackingNumberOrAsk();
        if (trackingNo == null) return;

        TrackingEventType type = pickEnum(
                "選擇追蹤事件類型",
                "請選擇事件類型：",
                TrackingEventType.values(),
                this::trackingTypeZh,
                TrackingEventType.IN_TRANSIT
        );
        if (type == null) return;

        String location = askNonEmpty("輸入所在地點 location（不可空白）：");
        if (location == null) return;

        String truckId = askOptional("輸入貨車識別碼 truckId（可空白）：");
        if (truckId == null) return;
        truckId = normalizeBlankToNull(truckId);

        String warehouseId = askOptional("輸入倉儲地點 warehouseId（可空白）：");
        if (warehouseId == null) return;
        warehouseId = normalizeBlankToNull(warehouseId);

        String description = askOptional("輸入描述 description（可空白）：");
        if (description == null) return;
        description = normalizeBlankToNull(description);

        ExceptionType ex = null;
        if (type == TrackingEventType.EXCEPTION) {
            ex = pickEnum(
                    "選擇異常類型",
                    "請選擇異常類型：",
                    ExceptionType.values(),
                    this::exceptionZh,
                    ExceptionType.DELAYED
            );
            if (ex == null) return;
        }

        core.addTrackingEvent(currentUser, trackingNo, type, location, truckId, warehouseId, description, ex);

        info("新增追蹤事件成功",
                h1("追蹤事件已新增") + "\n" +
                        kvBlock(
                                "追蹤編號", trackingNo,
                                "事件類型", trackingTypeZh(type),
                                "時間戳記", "（由系統在事件建立時記錄）",
                                "所在地點", location,
                                "貨車識別碼", truckId,
                                "倉儲地點", warehouseId,
                                "異常類型", ex == null ? "-" : exceptionZh(ex),
                                "描述", description == null ? "-" : description
                        )
        );
    }

    private void getCurrentStatusUI() {
        String trackingNo = pickTrackingNumberOrAsk();
        if (trackingNo == null) return;

        TrackingEvent e = core.getCurrentStatus(currentUser, trackingNo);

        info("目前狀態",
                h1("目前狀態") + "\n" +
                        kvBlock(
                                "追蹤編號", trackingNo,
                                "事件", e
                        )
        );
    }

    private void getHistoryUI() {
        String trackingNo = pickTrackingNumberOrAsk();
        if (trackingNo == null) return;

        List<TrackingEvent> list = core.getHistory(currentUser, trackingNo);
        if (list.isEmpty()) {
            info("歷史追蹤", h1("歷史追蹤") + "\n" + kv("追蹤編號", trackingNo) + "\n\n（無任何事件）");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(h1("歷史追蹤")).append('\n');
        sb.append(kv("追蹤編號", trackingNo)).append('\n');
        sb.append(kv("事件數量", list.size())).append('\n');
        sb.append(hr()).append('\n');

        int i = 1;
        for (TrackingEvent e : list) {
            sb.append("[").append(i++).append("]\n");
            sb.append("  ").append(e).append('\n');
            sb.append(hr()).append('\n');
        }

        report("歷史追蹤（可捲動）", sb.toString());
    }

    private void searchByTrackingNoUI() {
        String trackingNo = askNonEmpty("輸入追蹤編號：");
        if (trackingNo == null) return;

        Parcel p = core.searchByTrackingNumber(currentUser, trackingNo);
        if (p == null) {
            info("搜尋結果（追蹤編號）", h1("查無資料") + "\n" + kv("追蹤編號", trackingNo));
            return;
        }

        report("搜尋結果（追蹤編號）",
                h1("查詢成功") + "\n" +
                        kvBlock(
                                "追蹤編號", p.trackingNumber,
                                "寄件客戶", p.sender == null ? "-" : p.sender.name,
                                "重量(kg)", p.weightKg,
                                "國際件", p.international ? "是" : "否"
                        ) +
                        "\n" + hr() + "\n" +
                        "Parcel:\n" + p
        );
    }

    private void searchByCustomerUI() {
        Customer c = pickCustomerOrAskToCreate();
        if (c == null) return;

        List<Parcel> list = core.searchByCustomer(currentUser, c.id);

        report("搜尋結果（依客戶帳號）",
                formatParcelList(
                        h1("依客戶帳號搜尋") + "\n" +
                                kvBlock(
                                        "客戶編號", c.id,
                                        "客戶姓名", c.name,
                                        "筆數", (list == null ? 0 : list.size())
                                ) +
                                "\n" + hr() + "\n",
                        list
                )
        );
    }

    private void searchByDateRangeUI() {
        LocalDate start = askDate("輸入開始日期（YYYY-MM-DD）：");
        if (start == null) return;

        LocalDate end = askDate("輸入結束日期（YYYY-MM-DD）：");
        if (end == null) return;

        List<Parcel> list = core.searchByDateRange(currentUser, start, end);

        report("搜尋結果（依日期範圍）",
                formatParcelList(
                        h1("依運送日期範圍搜尋") + "\n" +
                                kvBlock(
                                        "開始日期", start,
                                        "結束日期", end,
                                        "筆數", (list == null ? 0 : list.size())
                                ) +
                                "\n" + hr() + "\n",
                        list
                )
        );
    }

    private void searchByTruckIdUI() {
        String truckId = askNonEmpty("輸入貨車識別碼 truckId：");
        if (truckId == null) return;

        List<Parcel> list = core.searchByTruckId(currentUser, truckId);

        report("搜尋結果（依貨車識別碼）",
                formatParcelList(
                        h1("依貨車識別碼搜尋") + "\n" +
                                kvBlock(
                                        "貨車識別碼", truckId,
                                        "筆數", (list == null ? 0 : list.size())
                                ) +
                                "\n" + hr() + "\n",
                        list
                )
        );
    }

    private void searchByWarehouseIdUI() {
        String warehouseId = askNonEmpty("輸入倉儲地點 warehouseId：");
        if (warehouseId == null) return;

        List<Parcel> list = core.searchByWarehouseId(currentUser, warehouseId);

        report("搜尋結果（依倉儲地點）",
                formatParcelList(
                        h1("依倉儲地點搜尋") + "\n" +
                                kvBlock(
                                        "倉儲地點", warehouseId,
                                        "筆數", (list == null ? 0 : list.size())
                                ) +
                                "\n" + hr() + "\n",
                        list
                )
        );
    }

    /* ===================== 1.5 計費與付款 ===================== */

    private void menuBilling_1_5() {
        String[] opts = {
                "計算運費（服務類型 + 距離 + 重量/體積 + 特殊處理費）",
                "產生帳單（計費週期內所有貨件）",
                "查帳單歷史（依客戶帳號）",
                "返回"
        };

        int c = JOptionPane.showOptionDialog(
                null, "1.5 計費與付款", "計費與付款",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opts, opts[0]
        );

        switch (c) {
            case 0 -> calculateChargeUI();
            case 1 -> generateBillingRecordUI();
            case 2 -> showBillingHistoryUI();
            default -> { }
        }
    }

    private void calculateChargeUI() {
        String trackingNo = pickTrackingNumberOrAsk();
        if (trackingNo == null) return;

        double distanceKm = askDouble("輸入距離 distanceKm：");
        Parcel p = core.searchByTrackingNumber(currentUser, trackingNo);

        double amount = core.calculateCharge(p, distanceKm);

        info("運費計算完成",
                h1("運費計算結果") + "\n" +
                        kvBlock(
                                "追蹤編號", trackingNo,
                                "距離(km)", distanceKm,
                                "金額", amount
                        )
        );
    }

    private void generateBillingRecordUI() {
        Customer c = pickCustomerOrAskToCreate();
        if (c == null) return;

        LocalDate start = askDate("帳單期間開始（YYYY-MM-DD）：");
        if (start == null) return;

        LocalDate end = askDate("帳單期間結束（YYYY-MM-DD）：");
        if (end == null) return;

        PaymentMethod pm = pickEnum(
                "選擇付款方式",
                "請選擇付款方式：",
                PaymentMethod.values(),
                this::paymentZh,
                PaymentMethod.CASH
        );
        if (pm == null) return;

        Map<String, Double> distanceMap = new HashMap<>();
        if (askYesNo("要針對某些追蹤編號自訂距離嗎？")) {
            while (true) {
                String tn = askOptional("輸入追蹤編號（空白=結束；取消=中止整個流程）：");
                if (tn == null) return;
                tn = normalizeBlankToNull(tn);
                if (tn == null) break;

                double d = askDouble("輸入 " + tn + " 的距離 distanceKm：");
                distanceMap.put(tn, d);
            }
        }

        BillingRecord br = core.generateBillingRecord(c, start, end, distanceMap, pm);

        report("已產生帳單（可捲動）",
                h1("帳單已產生") + "\n" +
                        kvBlock(
                                "客戶編號", c.id,
                                "客戶姓名", c.name,
                                "計費期間", start + " ~ " + end,
                                "付款方式", paymentZh(pm)
                        ) +
                        "\n" + hr() + "\n" +
                        "Record:\n" + br
        );
    }

    private void showBillingHistoryUI() {
        Customer c = pickCustomerOrAskToCreate();
        if (c == null) return;

        List<BillingRecord> list = core.getBillingHistoryForCustomer(c.id);
        if (list.isEmpty()) {
            info("帳單歷史",
                    h1("帳單歷史") + "\n" +
                            kvBlock(
                                    "客戶編號", c.id,
                                    "客戶姓名", c.name
                            ) +
                            "\n\n（目前無帳單紀錄）"
            );
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(h1("帳單歷史")).append('\n');
        sb.append(kv("客戶編號", c.id)).append('\n');
        sb.append(kv("客戶姓名", c.name)).append('\n');
        sb.append(kv("帳單筆數", list.size())).append('\n');
        sb.append(hr()).append('\n');

        int i = 1;
        for (BillingRecord br : list) {
            sb.append("[").append(i++).append("]\n");
            sb.append(br).append('\n');
            sb.append(hr()).append('\n');
        }

        report("帳單歷史（可捲動）", sb.toString());
    }

    /* ===================== 1.6 安全與權限 ===================== */

    private void menuSecurity_1_6() {
        String[] opts = {
                "切換登入身份/角色（客服/倉儲/駕駛/管理員/客戶）",
                "返回"
        };

        int c = JOptionPane.showOptionDialog(
                null, "1.6 安全與權限", "安全與權限",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opts, opts[0]
        );

        if (c == 0) switchUser();
    }

    private void switchUser() {
        Role role = pickEnum(
                "切換角色",
                "請選擇角色：",
                Role.values(),
                this::roleZh,
                currentUser.getRole()
        );
        if (role == null) return;

        if (role == Role.CUSTOMER) {
            Customer c = pickCustomerOrAskToCreate();
            if (c == null) return;

            currentUser = new User("customer#" + c.id, Role.CUSTOMER, c);

            info("切換身份成功",
                    h1("已切換角色") + "\n" +
                            kvBlock(
                                    "新角色", roleZh(Role.CUSTOMER),
                                    "綁定客戶編號", c.id,
                                    "綁定客戶姓名", c.name
                            )
            );
        } else {
            currentUser = new User(role.name().toLowerCase(), role, null);

            info("切換身份成功",
                    h1("已切換角色") + "\n" +
                            kvBlock(
                                    "新角色", roleZh(role),
                                    "登入帳號", currentUser.getUsername()
                            )
            );
        }
    }

    /* ===================== Common Pick / Input Helpers ===================== */

    private Customer pickCustomerOrAskToCreate() {
        if (customersCreated.isEmpty()) {
            info("提示", "目前尚未建立任何客戶，請先到 1.1 建立客戶。");
            return null;
        }

        String[] options = customersCreated.values().stream()
                .map(c -> c.id + " - " + c.name + "（" + customerTypeZh(c.type) + "）")
                .toArray(String[]::new);

        String pick = (String) JOptionPane.showInputDialog(
                null, "選擇客戶：", "選擇客戶",
                JOptionPane.QUESTION_MESSAGE, null,
                options, options[0]
        );
        if (pick == null) return null;

        long id = Long.parseLong(pick.split(" - ")[0].trim());
        return customersCreated.get(id);
    }

    private ServiceType pickServiceTypeOrAskToCreate() {
        if (serviceTypesCreated.isEmpty()) {
            info("提示", "目前尚未建立任何服務類型，請先到 1.2 建立服務類型。");
            return null;
        }

        String[] options = serviceTypesCreated.values().stream()
                .map(st -> st.getId() + " - " + st.getName()
                        + "（" + packageTypeZh(st.getPackageType()) + " / " + deliverySpeedZh(st.getSpeed()) + "）")
                .toArray(String[]::new);

        String pick = (String) JOptionPane.showInputDialog(
                null, "選擇服務類型：", "選擇服務類型",
                JOptionPane.QUESTION_MESSAGE, null,
                options, options[0]
        );
        if (pick == null) return null;

        long id = Long.parseLong(pick.split(" - ")[0].trim());
        return serviceTypesCreated.get(id);
    }

    private String pickTrackingNumberOrAsk() {
        if (!trackingNumbersCreated.isEmpty()) {
            String[] options = trackingNumbersCreated.toArray(new String[0]);
            return (String) JOptionPane.showInputDialog(
                    null, "選擇追蹤編號：", "選擇追蹤編號",
                    JOptionPane.QUESTION_MESSAGE, null,
                    options, options[0]
            );
        }
        return askNonEmpty("輸入追蹤編號：");
    }

    private Long askLongOrPickCustomerId(String prompt) {
        if (!customersCreated.isEmpty()) {
            String[] opts = {"手動輸入 customerId", "從已建立客戶清單選擇", "取消"};
            int c = JOptionPane.showOptionDialog(
                    null, prompt, "CustomerId",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, opts, opts[0]
            );
            if (c == 2 || c == -1) return null;

            if (c == 1) {
                Customer picked = pickCustomerOrAskToCreate();
                return picked == null ? null : picked.id;
            }
        }

        while (true) {
            String s = JOptionPane.showInputDialog(null, prompt, "CustomerId", JOptionPane.QUESTION_MESSAGE);
            if (s == null) return null;
            s = s.trim();
            if (s.isEmpty()) return null;
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                info("輸入錯誤", "請輸入整數 customerId。");
            }
        }
    }

    private String askNonEmpty(String prompt) {
        String s = JOptionPane.showInputDialog(null, prompt, "輸入", JOptionPane.QUESTION_MESSAGE);
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) {
            info("輸入錯誤", "輸入不可空白。");
            return null;
        }
        return s;
    }

    private String askOptional(String prompt) {
        String s = JOptionPane.showInputDialog(null, prompt, "輸入", JOptionPane.QUESTION_MESSAGE);
        if (s == null) return null;
        return s.trim();
    }

    private double askDouble(String prompt) {
        while (true) {
            String s = JOptionPane.showInputDialog(null, prompt, "數值", JOptionPane.QUESTION_MESSAGE);
            if (s == null) throw new IllegalArgumentException("使用者取消輸入");
            s = s.trim();
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                info("輸入錯誤", "請輸入數字（可含小數）。");
            }
        }
    }

    private LocalDate askDate(String prompt) {
        while (true) {
            String s = JOptionPane.showInputDialog(null, prompt, "日期", JOptionPane.QUESTION_MESSAGE);
            if (s == null) return null;
            s = s.trim();
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                info("日期格式錯誤", "請用 YYYY-MM-DD，例如 2025-12-26");
            }
        }
    }

    private boolean askYesNo(String prompt) {
        int r = JOptionPane.showConfirmDialog(null, prompt, "確認", JOptionPane.YES_NO_OPTION);
        return r == JOptionPane.YES_OPTION;
    }

    private String normalizeBlankToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private String formatParcelList(String titlePrefix, List<Parcel> list) {
        if (list == null || list.isEmpty()) {
            return titlePrefix + "（查無資料）";
        }

        StringBuilder sb = new StringBuilder(titlePrefix);
        int i = 1;
        for (Parcel p : list) {
            sb.append("[").append(i++).append("]\n");
            sb.append(kv("追蹤編號", p.trackingNumber)).append('\n');
            sb.append(kv("寄件客戶", p.sender == null ? "-" : p.sender.name)).append('\n');
            sb.append(kv("重量(kg)", p.weightKg)).append('\n');
            sb.append(hr()).append('\n');
        }
        return sb.toString();
    }

    /* ===================== Enum Picker  ===================== */

    private <E> E pickEnum(String title, String prompt, E[] values, Function<E, String> labeler, E defaultValue) {
        String[] labels = new String[values.length];
        int defaultIndex = 0;

        for (int i = 0; i < values.length; i++) {
            labels[i] = labeler.apply(values[i]);
            if (values[i].equals(defaultValue)) defaultIndex = i;
        }

        String pick = (String) JOptionPane.showInputDialog(
                null, prompt, title,
                JOptionPane.QUESTION_MESSAGE,
                null,
                labels,
                labels[defaultIndex]
        );
        if (pick == null) return null;

        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(pick)) return values[i];
        }
        return null;
    }

   
    private String roleZh(Role r) {
        if (r == null) return "-";
        return switch (r) {
            case CUSTOMER_SERVICE -> "客服人員";
            case WAREHOUSE -> "倉儲人員";
            case DRIVER -> "駕駛員";
            case ADMIN -> "管理員/系統管理員";
            case CUSTOMER -> "客戶";
        };
    }

    private String customerTypeZh(CustomerType t) {
        if (t == null) return "-";
        return switch (t) {
            case CONTRACT -> "合約客戶（月結帳戶）";
            case NON_CONTRACT -> "非合約客戶（現金/信用卡）";
            case PREPAID -> "預付客戶（第三方/商家支付）";
        };
    }

    private String billingPrefZh(BillingPreference p) {
        if (p == null) return "-";
        return switch (p) {
            case MONTHLY -> "月結帳單";
            case CASH_ON_DELIVERY -> "貨到付款";
            case PREPAID -> "預付";
        };
    }

    private String packageTypeZh(PackageType p) {
        if (p == null) return "-";
        return switch (p) {
            case ENVELOPE -> "平郵信封";
            case SMALL_BOX -> "小型箱";
            case MEDIUM_BOX -> "中型箱";
            case LARGE_BOX -> "大型箱";
        };
    }

    private String deliverySpeedZh(DeliverySpeed d) {
        if (d == null) return "-";
        return switch (d) {
            case OVERNIGHT -> "隔夜達";
            case TWO_DAY -> "兩日達";
            case STANDARD -> "標準速遞";
            case ECONOMY -> "經濟速遞";
        };
    }

    private String trackingTypeZh(TrackingEventType t) {
        if (t == null) return "-";
        return switch (t) {
            case PICKED_UP -> "起運地收件";
            case LOADED_TO_TRUCK -> "進貨車";
            case UNLOADED_FROM_TRUCK -> "出貨車";
            case ENTER_WAREHOUSE -> "入倉";
            case EXIT_WAREHOUSE -> "出倉";
            case SORTED -> "分揀";
            case IN_TRANSIT -> "運送中";
            case OUT_FOR_DELIVERY -> "外送中";
            case DELIVERED -> "已投遞";
            case SIGNED -> "已簽收";
            case EXCEPTION -> "異常";
        };
    }

    private String exceptionZh(ExceptionType e) {
        if (e == null) return "-";
        return switch (e) {
            case LOST -> "遺失包裹";
            case DELAYED -> "延誤";
            case DAMAGED -> "損毀";
        };
    }

    private String paymentZh(PaymentMethod p) {
        if (p == null) return "-";
        return switch (p) {
            case MONTHLY_ACCOUNT -> "月結";
            case CASH -> "現金";
            case CREDIT_CARD -> "信用卡";
            case MOBILE_PAY -> "行動支付";
            case PREPAID -> "預付（交件免收）";
        };
    }

    /* ===================== Readable Message / Formatting ===================== */

    private String buildHeaderText() {
        return h1("物流系統操作介面") + "\n"
                + kvBlock(
                "登入帳號", currentUser.getUsername(),
                "角色", roleZh(currentUser.getRole()),
                "已建立客戶數", customersCreated.size(),
                "已建立服務類型數", serviceTypesCreated.size(),
                "已建立貨件數", trackingNumbersCreated.size()
        );
    }

    private void info(String title, String body) {
        JOptionPane.showMessageDialog(null, wrap(body), title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String title, String body) {
        JOptionPane.showMessageDialog(null, wrap(body), title, JOptionPane.ERROR_MESSAGE);
    }

    private void report(String title, String body) {
        JTextArea area = new JTextArea(body, 22, 60);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);

        JScrollPane pane = new JScrollPane(area);
        JOptionPane.showMessageDialog(null, pane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // 保留你原本呼叫 showMessage 的習慣：自動判斷長短
    private void showMessage(String msg) {
        if (msg == null) {
            info("訊息", "(no content)");
            return;
        }
        String s = msg.strip();
        int lines = s.isEmpty() ? 0 : s.split("\n", -1).length;

        if (s.length() >= 450 || lines >= 16) {
            report("訊息（可捲動）", s);
        } else {
            info("訊息", s);
        }
    }

    private String wrap(String s) {
        if (s == null) return "";
        s = s.strip();
        return s.isEmpty() ? "(no content)" : s;
    }

    private String hr() {
        return "------------------------------------------------------------";
    }

    private String h1(String title) {
        return title + "\n" + hr();
    }

    private String kv(String key, Object value) {
        int pad = 18;
        String k = (key == null) ? "" : key;
        if (k.length() > pad) k = k.substring(0, pad);
        return String.format("%-" + pad + "s : %s", k, String.valueOf(value));
    }

    private String kvBlock(Object... pairs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            sb.append(kv(String.valueOf(pairs[i]), pairs[i + 1])).append('\n');
        }
        return sb.toString();
    }
}
