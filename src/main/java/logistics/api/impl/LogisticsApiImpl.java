package logistics.api.impl;

import logistics.api.*;
import logistics.core.LogisticsCore;
import logistics.enums.*;
import logistics.model.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogisticsApiImpl implements AuthApi, CustomerApi, ServiceTypeApi, ParcelApi, TrackingApi, BillingApi, QueryApi {

    private final LogisticsCore core;
    private User currentUser;

    /**
     * API 層的 in-memory 帳密資料（不改 Core 也能支援登入/註冊）
     * key = normalized username
     */
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    /**
     * 由於 core 目前可能沒有 getServiceType()，這裡做一份 cache 來支援 ServiceTypeApi.getServiceType()
     */
    private final Map<Long, ServiceType> serviceTypeCache = new ConcurrentHashMap<>();

    private static class Account {
        final String username;
        final String passwordHash;
        final Role role;
        final Customer customerProfileOrNull;

        Account(String username, String passwordHash, Role role, Customer customerProfileOrNull) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.role = role;
            this.customerProfileOrNull = customerProfileOrNull;
        }
    }

    public LogisticsApiImpl(LogisticsCore core) {
        this.core = core;
        this.currentUser = null;
    }

    /* =======================
     * AuthApi / Authentication
     * ======================= */

    /**
     * 舊版相容：直接指定角色登入（不檢查帳密）
     * 注意：不要加 @Override，避免 AuthApi 已移除此方法時編譯失敗
     */
    @Deprecated
    public User loginAsRole(Role role, Customer customerProfileOrNull) {
        this.currentUser = new User("demo", role, customerProfileOrNull);
        return this.currentUser;
    }

    /**
     * 新增：註冊（帳號 + 密碼）
     * 注意：不要加 @Override，避免 AuthApi 尚未更新時編譯失敗
     */
    public User register(String username, String password, Role role, Customer customerProfileOrNull) {
        String u = normalizeUsername(username);
        validatePassword(password);

        if (role == null) {
            throw new IllegalArgumentException("role is null");
        }
        if (accounts.containsKey(u)) {
            throw new IllegalArgumentException("username 已存在：" + u);
        }
        if (role == Role.CUSTOMER && customerProfileOrNull == null) {
            throw new IllegalArgumentException("CUSTOMER 角色必須綁定 customerProfile");
        }

        String hash = hashPassword(password);
        accounts.put(u, new Account(u, hash, role, customerProfileOrNull));

        // 註冊後直接登入
        this.currentUser = new User(u, role, role == Role.CUSTOMER ? customerProfileOrNull : null);
        return this.currentUser;
    }

    /**
     * 新增：登入（帳號 + 密碼）
     * 注意：不要加 @Override，避免 AuthApi 尚未更新時編譯失敗
     */
    public User login(String username, String password) {
        String u = normalizeUsername(username);
        validatePassword(password);

        Account acc = accounts.get(u);
        if (acc == null) {
            throw new SecurityException("帳號不存在");
        }

        String actual = hashPassword(password);
        if (!acc.passwordHash.equals(actual)) {
            throw new SecurityException("密碼錯誤");
        }

        this.currentUser = new User(acc.username, acc.role, acc.role == Role.CUSTOMER ? acc.customerProfileOrNull : null);
        return this.currentUser;
    }

    /**
     * 新增：登出
     * 注意：不要加 @Override，避免 AuthApi 尚未更新時編譯失敗
     */
    public void logout() {
        this.currentUser = null;
    }

    @Override
    public User currentUser() {
        return currentUser;
    }

    /* ========== CustomerApi ========== */
    @Override
    public Customer createCustomer(String name, String address, String phone, String email,
                                   CustomerType type, BillingPreference pref) {
        return core.createCustomer(name, address, phone, email, type, pref);
    }

    @Override
    public Customer getCustomer(long customerId) {
        return core.getCustomer(customerId);
    }

    /**
     * 額外提供：刪除客戶（若你 Core 有 deleteCustomer(User,long)）
     * 這不是 CustomerApi 介面方法，所以不加 @Override。
     */
    public void deleteCustomer(long customerId) {
        core.deleteCustomer(currentUser, customerId);
    }

    /* ========== ServiceTypeApi ========== */
    @Override
    public ServiceType createServiceType(String name, PackageType packageType,
                                         double minWeightKg, double maxWeightKg,
                                         DeliverySpeed speed,
                                         double basePrice, double pricePerKm,
                                         double pricePerKg, double pricePerCubicMeter,
                                         double dangerousSurcharge, double fragileSurcharge, double oversizeSurcharge) {

        ServiceType st = core.createServiceType(
                name, packageType, minWeightKg, maxWeightKg, speed,
                basePrice, pricePerKm, pricePerKg, pricePerCubicMeter,
                dangerousSurcharge, fragileSurcharge, oversizeSurcharge
        );

        // cache for getServiceType
        if (st != null) {
            serviceTypeCache.put(st.getId(), st);
        }
        return st;
    }

    @Override
    public ServiceType getServiceType(long serviceTypeId) {
        ServiceType st = serviceTypeCache.get(serviceTypeId);
        if (st == null) {
            throw new IllegalArgumentException("找不到 serviceTypeId=" + serviceTypeId + "（尚未在本 API 建立或未 cache）");
        }
        return st;
    }

    /* ========== ParcelApi ========== */
    @Override
    public Parcel createParcel(Customer sender, ServiceType serviceType,
                               double weightKg, double lengthCm, double widthCm, double heightCm,
                               double declaredValue, String description,
                               boolean dangerous, boolean fragile, boolean international) {

        return core.createParcel(
                currentUser,
                sender,
                serviceType,
                weightKg, lengthCm, widthCm, heightCm,
                declaredValue, description,
                dangerous, fragile, international
        );
    }

    @Override
    public Parcel getByTrackingNumber(String trackingNumber) {
        // 使用 Query 的 search 來取得（Core 既有：searchByTrackingNumber(User,String)）
        return core.searchByTrackingNumber(currentUser, trackingNumber);
    }

    /* ========== TrackingApi ========== */
    @Override
    public void addTrackingEvent(String trackingNo, TrackingEventType type,
                                 String location, String truckId, String warehouseId,
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

    /* =======================
     * Helpers
     * ======================= */
    private String normalizeUsername(String username) {
        if (username == null) throw new IllegalArgumentException("username is null");
        String u = username.trim();
        if (u.isEmpty()) throw new IllegalArgumentException("username is blank");
        return u;
    }

    private void validatePassword(String password) {
        if (password == null) throw new IllegalArgumentException("password is null");
        if (password.trim().length() < 6) throw new IllegalArgumentException("password 長度需至少 6");
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
