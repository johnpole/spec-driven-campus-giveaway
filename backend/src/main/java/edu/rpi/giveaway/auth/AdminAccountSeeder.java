package edu.rpi.giveaway.auth;

import edu.rpi.giveaway.user.UserAccountDocument;
import edu.rpi.giveaway.user.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Creates (or updates) the admin account from ADMIN_EMAIL / ADMIN_PASSWORD at startup.
 * Credentials live only in the environment (.env locally, Secret Manager in the cloud), never in code.
 */
@Component
public class AdminAccountSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountSeeder.class);

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public AdminAccountSeeder(UserAccountRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${giveaway.admin.email:}") String adminEmail,
                              @Value("${giveaway.admin.password:}") String adminPassword,
                              @Value("${giveaway.admin.name:Admin}") String adminName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
        this.adminName = adminName == null || adminName.isBlank() ? "Admin" : adminName.trim();
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD not set; no admin account seeded.");
            return;
        }
        UserAccountDocument admin = userRepository.findByEmail(adminEmail).orElseGet(this::newAdmin);
        admin.setRole("ADMIN");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        userRepository.save(admin);
        log.info("Admin account ready for {}", adminEmail);
    }

    private UserAccountDocument newAdmin() {
        String[] names = adminName.split(" ", 2);
        String lastName = names.length > 1 ? names[1] : "";
        return new UserAccountDocument(UUID.randomUUID().toString(), adminEmail, null,
                names[0], lastName, adminName, null, "ADMIN", Instant.now());
    }
}
