package edu.rpi.giveaway.interest;

import jakarta.persistence.*;

import java.util.Date;

/** One message in the conversation between a poster and an interested student about a pickup. */
@Entity
@Table(name = "interest_replies", indexes = {
        @Index(name = "idx_interest_replies_interest_id", columnList = "interest_id")
})
public class InterestReplyEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "interest_id", length = 36, nullable = false)
    private String interestId;

    @Column(name = "author_user_id", length = 255, nullable = false)
    private String authorUserId;

    @Column(name = "author_name", length = 100, nullable = false)
    private String authorName;

    /** SELLER (the poster) or BUYER (the interested student). */
    @Column(name = "author_role", length = 10, nullable = false)
    private String authorRole;

    @Column(name = "message", length = 500, nullable = false)
    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public InterestReplyEntity() {
    }

    public InterestReplyEntity(String id, String interestId, String authorUserId, String authorName,
                               String authorRole, String message, Date createdAt) {
        this.id = id;
        this.interestId = interestId;
        this.authorUserId = authorUserId;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getInterestId() { return interestId; }
    public String getAuthorUserId() { return authorUserId; }
    public String getAuthorName() { return authorName; }
    public String getAuthorRole() { return authorRole; }
    public String getMessage() { return message; }
    public Date getCreatedAt() { return createdAt; }
}
