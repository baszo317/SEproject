package logistics.api;

import logistics.enums.Role;
import logistics.model.Customer;
import logistics.model.User;

/**
 * 認證/授權 API。
 *
 * 本專案目前採用簡單的 in-memory 帳號系統：
 * - 註冊時保存 password 的雜湊（不保存明碼）
 * - 登入成功後回傳 User，並由 API 實作方維護 currentUser
 */
public interface AuthApi {

    /**
     * 註冊帳號。
     *
     * @param username 帳號（需唯一）
     * @param password 密碼（會雜湊保存）
     * @param role     角色（CUSTOMER 時必須綁定 customerProfile）
     * @param customerProfileOrNull CUSTOMER 角色的客戶資料（其他角色可為 null）
     */
    User register(String username, String password, Role role, Customer customerProfileOrNull);

    /**
     * 登入。
     */
    User login(String username, String password);

    /**
     * 登出。
     */
    void logout();

    /**
     * 取得目前登入的使用者（可能為 null）。
     */
    User currentUser();
}
