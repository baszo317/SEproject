package logistics.api;

import logistics.enums.Role;
import logistics.model.Customer;
import logistics.model.User;

public interface AuthApi {
    User loginAsRole(Role role, Customer customerProfileOrNull);
    User currentUser();
}
