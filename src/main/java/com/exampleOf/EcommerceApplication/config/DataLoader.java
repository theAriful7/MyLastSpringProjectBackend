package com.exampleOf.EcommerceApplication.config;

import com.exampleOf.EcommerceApplication.dto.UserDto;
import com.exampleOf.EcommerceApplication.enums.UserRole;
import com.exampleOf.EcommerceApplication.service.UserService;
import com.exampleOf.EcommerceApplication.service.VendorService;
import com.exampleOf.EcommerceApplication.dto.VendorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserService userService;
    private final VendorService vendorService;
    private final PasswordEncoder passwordEncoder; // Added this line

    @Override
    public void run(String... args) throws Exception {
        createDemoUsers();
    }

    private void createDemoUsers() {
        createDemoAdmin();
        createDemoVendor();
        createDemoCustomer();
        createDemoDeliveryAgent();
    }

    private void createDemoAdmin() {
        if (!userExists("admin@demo.com")) {
            UserDto adminDto = new UserDto();
            adminDto.setEmail("admin@demo.com");
            adminDto.setPassword("admin123");
            adminDto.setFirstName("Demo");
            adminDto.setLastName("Admin");
            adminDto.setPhone("123-456-7890");
            adminDto.setRole(UserRole.ADMIN);
            userService.registerUser(adminDto);
            System.out.println("Demo admin user created: admin@demo.com / admin123");
        } else {
            System.out.println("Demo admin user already exists: admin@demo.com");
        }
    }

    private void createDemoVendor() {
        if (!userExists("vendor@demo.com")) {
            UserDto vendorUserDto = new UserDto();
            vendorUserDto.setEmail("vendor@demo.com");
            vendorUserDto.setPassword("vendor123");
            vendorUserDto.setFirstName("Demo");
            vendorUserDto.setLastName("Vendor");
            vendorUserDto.setPhone("123-456-7891");
            vendorUserDto.setRole(UserRole.VENDOR);
            userService.registerUser(vendorUserDto);

            // Create vendor profile
            VendorDto vendorDto = new VendorDto();
            vendorDto.setShopName("Demo Vendor Store");
            vendorDto.setBusinessDescription("A demo vendor for testing purposes");
            vendorDto.setTaxNumber("DEMO123456");
            vendorDto.setAddress("123 Vendor Street, City, Country");
            vendorDto.setUserEmail("vendor@demo.com");
            vendorService.registerVendor(vendorDto);

            System.out.println("Demo vendor user created: vendor@demo.com / vendor123");
        } else {
            System.out.println("Demo vendor user already exists: vendor@demo.com");
        }
    }

    private void createDemoCustomer() {
        if (!userExists("customer@demo.com")) {
            UserDto customerDto = new UserDto();
            customerDto.setEmail("customer@demo.com");
            customerDto.setPassword("customer123");
            customerDto.setFirstName("Demo");
            customerDto.setLastName("Customer");
            customerDto.setPhone("123-456-7892");
            customerDto.setRole(UserRole.CUSTOMER);
            userService.registerUser(customerDto);
            System.out.println("Demo customer user created: customer@demo.com / customer123");
        } else {
            System.out.println("Demo customer user already exists: customer@demo.com");
        }
    }

    private void createDemoDeliveryAgent() {
        if (!userExists("delivery@demo.com")) {
            UserDto deliveryDto = new UserDto();
            deliveryDto.setEmail("delivery@demo.com");
            deliveryDto.setPassword("delivery123");
            deliveryDto.setFirstName("Demo");
            deliveryDto.setLastName("Delivery");
            deliveryDto.setPhone("123-456-7893");
            deliveryDto.setRole(UserRole.DELIVERY_AGENT);
            userService.registerUser(deliveryDto);
            System.out.println("Demo delivery agent created: delivery@demo.com / delivery123");
        } else {
            System.out.println("Demo delivery agent already exists: delivery@demo.com");
        }
    }

    private boolean userExists(String email) {
        try {
            userService.getUserByEmail(email);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
