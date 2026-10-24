package edu.rpi.giveaway.interest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataInterestRepository extends JpaRepository<InterestEntity, String> {

    @Query("SELECT i FROM InterestEntity i WHERE i.postId = :postId ORDER BY i.createdAt DESC")
    List<InterestEntity> findByPostIdOrderByCreatedAtDesc(@Param("postId") String postId);

    @Query("SELECT i FROM InterestEntity i WHERE i.inquirerUserId = :inquirerUserId ORDER BY i.createdAt DESC")
    List<InterestEntity> findByInquirerUserIdOrderByCreatedAtDesc(@Param("inquirerUserId") String inquirerUserId);
}
