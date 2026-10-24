package edu.rpi.giveaway.auth;

import edu.rpi.giveaway.user.UserAccountDocument;
import edu.rpi.giveaway.user.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppTokenService appTokenService;

    public AuthService(
            UserAccountRepository userRepository,
            PasswordEncoder passwordEncoder,
            AppTokenService appTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appTokenService = appTokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        String cleanEmail = request.email().trim().toLowerCase();
        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            throw new DuplicateEmailException("An account with email " + cleanEmail + " already exists");
        }

        String userId = UUID.randomUUID().toString();
        String firstName = request.firstName().trim();
        String lastName = request.lastName() != null ? request.lastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        String phoneNumber = request.phoneNumber() != null && !request.phoneNumber().isBlank()
                ? request.phoneNumber().trim()
                : null;

        // Self-registration never grants ADMIN: emails are not verified. Admins come from AdminAccountSeeder.
        boolean isAdmin = false;
        String role = "USER";
        String passwordHash = passwordEncoder.encode(request.password());

        UserAccountDocument doc = new UserAccountDocument(
                userId,
                cleanEmail,
                phoneNumber,
                firstName,
                lastName,
                fullName,
                passwordHash,
                role,
                Instant.now()
        );

        userRepository.save(doc);
        log.info("Registered new user {} (admin: {})", cleanEmail, isAdmin);

        String token = generateToken(userId, cleanEmail, fullName, isAdmin);
        AuthUserDto userDto = new AuthUserDto(userId, cleanEmail, fullName, firstName, lastName, phoneNumber, isAdmin);
        return new AuthResponse(token, userDto);
    }

    public AuthResponse login(LoginRequest request) {
        String username = request.username().trim();
        Optional<UserAccountDocument> userOpt = userRepository.findByEmail(username.toLowerCase());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPhoneNumber(username);
        }

        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        UserAccountDocument user = userOpt.get();
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        boolean isAdmin = user.isAdmin();
        String token = generateToken(user.getId(), user.getEmail(), user.getFullName(), isAdmin);
        AuthUserDto userDto = new AuthUserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                isAdmin
        );
        return new AuthResponse(token, userDto);
    }

    private String generateToken(String userId, String email, String fullName, boolean isAdmin) {
        return appTokenService.issue(userId, email, fullName, isAdmin);
    }
}
