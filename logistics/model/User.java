package logistics.model;


import logistics.enums.Role;

public class User {
    private String username;
    private Role role;
    private Customer customerProfile; // 若為客戶角色，綁定其客戶帳號

    public User(String username, Role role, Customer customerProfile) {
        this.username = username;
        this.role = role;
        this.customerProfile = customerProfile;
    }

    // getter
    public Role getRole() {
        return role;
    }

    public Customer getCustomerProfile() {
        return customerProfile;
    }

    public String getUsername() {
        return username;
    }
}
