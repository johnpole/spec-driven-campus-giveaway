package edu.rpi.giveaway.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SpringDataPostRepository extends JpaRepository<PostEntity, String> {

    @Query("SELECT p FROM PostEntity p WHERE p.status = 'AVAILABLE' AND p.availableUntil > :now ORDER BY p.createdAt DESC")
    List<PostEntity> findAllActive(@Param("now") Date now);

    List<PostEntity> findAllByOrderByCreatedAtDesc();

    List<PostEntity> findByAuthorUserIdOrderByCreatedAtDesc(String authorUserId);
}
