package edu.rpi.giveaway.user;

import java.util.Optional;

public interface UserAccountRepository {
    UserAccountDocument save(UserAccountDocument user);
    Optional<UserAccountDocument> findById(String id);
    Optional<UserAccountDocument> findByEmail(String email);
    Optional<UserAccountDocument> findByPhoneNumber(String phoneNumber);
}
