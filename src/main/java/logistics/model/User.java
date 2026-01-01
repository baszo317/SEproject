package logistics.model;

import logistics.enums.Role;

public class User {
    private String username;
    private String passwordHash; // 只保存雜湊，不保存明碼
    private Role role;
    private Customer customerProfile; // 若為客戶角色，綁定其客戶帳號

    /**
     * 建立含密碼雜湊的使用者（建議用於註冊/持久化）。
     */
    public User(String username, String passwordHash, Role role, Customer customerProfile) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.customerProfile = customerProfile;
    }

    /**
     * 舊版相容：不含密碼雜湊（例如：測試或 demo 直接指定角色登入）。
     */
    @Deprecated
    public User(String username, Role role, Customer customerProfile) {
        this(username, null, role, customerProfile);
    }

    public Role getRole() {
        return role;
    }

    public Customer getCustomerProfile() {
        return customerProfile;
    }

    public String getUsername() {
        return username;
    }

    /**
     * 給 Core 用於驗證（一般業務邏輯不需要取用）。
     */
    public String getPasswordHash() {
        return passwordHash;
    }
}
