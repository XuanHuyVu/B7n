package com.tlu.tsms.config;

import com.tlu.tsms.entity.UserEntity;
import com.tlu.tsms.common.EAccountStatus;
import com.tlu.tsms.common.EUserRole;
import com.tlu.tsms.service.dao.UserDAO;
import com.tlu.tsms.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Timestamp;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.init-admin.enabled:false}")
    private boolean initAdminEnabled;

    @Value("${app.init-admin.email:}")
    private String adminEmail;

    @Value("${app.init-admin.password:}")
    private String adminPassword;


    @Bean
    public CommandLineRunner initAdmin(UserDAO userDAO) {
        return args -> {
            if (!initAdminEnabled) return;
            if (userDAO.findByEmail(adminEmail).isEmpty()) {
                UserEntity admin = UserEntity.builder()
                        .email(adminEmail)
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .userCode("ADMIN001")
                        .fullName("System Administrator")
                        .role(EUserRole.SYSTEM_ADMINISTRATOR)
                        .accountStatus(EAccountStatus.ACTIVE)
                        .isLocked(false)
                        .failedLoginAttempts(0)
                        .createdDate(DateUtils.now())
                        .build();
                userDAO.save(admin);
                System.out.println("⚡ Default admin account created: admin@tlu.edu.vn / 12345678");
            }
        };
    }
}