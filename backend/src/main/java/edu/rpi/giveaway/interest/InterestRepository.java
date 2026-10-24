package edu.rpi.giveaway.interest;

import java.util.List;

public interface InterestRepository {
    InterestDocument save(InterestDocument interest);
    List<InterestDocument> findByPostId(String postId);
    List<InterestDocument> findByInquirerUserId(String inquirerUserId);
    java.util.Optional<InterestDocument> findById(String id);
}
