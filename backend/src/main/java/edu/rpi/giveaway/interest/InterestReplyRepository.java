package edu.rpi.giveaway.interest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestReplyRepository extends JpaRepository<InterestReplyEntity, String> {

    List<InterestReplyEntity> findByInterestIdOrderByCreatedAtAsc(String interestId);
}
