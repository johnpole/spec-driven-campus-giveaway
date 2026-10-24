package edu.rpi.giveaway.interest;

import edu.rpi.giveaway.post.PostEntity;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "interests", indexes = {
        @Index(name = "idx_interests_post_id", columnList = "post_id"),
        @Index(name = "idx_interests_created_at", columnList = "created_at")
})
public class InterestEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "post_id", length = 36, nullable = false)
    private String postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private PostEntity post;

    @Column(name = "visitor_name", length = 100, nullable = false)
    private String visitorName;

    @Column(name = "visitor_contact", length = 100, nullable = false)
    private String visitorContact;

    @Column(name = "inquirer_user_id", length = 255)
    private String inquirerUserId;

    @Column(name = "inquirer_email", length = 255)
    private String inquirerEmail;

    @Column(name = "message", length = 500)
    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "pickup_at")
    private Date pickupAt;

    @Column(name = "pickup_place", length = 150)
    private String pickupPlace;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public InterestEntity() {
    }

    public static InterestEntity fromDocument(InterestDocument doc) {
        InterestEntity entity = new InterestEntity();
        entity.setId(doc.getId());
        entity.setPostId(doc.getPostId());
        entity.setVisitorName(doc.getVisitorName());
        entity.setVisitorContact(doc.getVisitorContact());
        entity.setInquirerUserId(doc.getInquirerUserId());
        entity.setInquirerEmail(doc.getInquirerEmail());
        entity.setMessage(doc.getMessage());
        entity.pickupAt = doc.getPickupAt();
        entity.pickupPlace = doc.getPickupPlace();
        entity.setCreatedAt(doc.getCreatedAt() != null ? doc.getCreatedAt() : new Date());
        return entity;
    }

    public InterestDocument toDocument() {
        InterestDocument doc = new InterestDocument();
        doc.setId(this.id);
        doc.setPostId(this.postId);
        doc.setVisitorName(this.visitorName);
        doc.setVisitorContact(this.visitorContact);
        doc.setInquirerUserId(this.inquirerUserId);
        doc.setInquirerEmail(this.inquirerEmail);
        doc.setMessage(this.message);
        doc.setPickupAt(this.pickupAt);
        doc.setPickupPlace(this.pickupPlace);
        doc.setCreatedAt(this.createdAt);
        return doc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public PostEntity getPost() {
        return post;
    }

    public void setPost(PostEntity post) {
        this.post = post;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getVisitorContact() {
        return visitorContact;
    }

    public void setVisitorContact(String visitorContact) {
        this.visitorContact = visitorContact;
    }

    public String getInquirerUserId() {
        return inquirerUserId;
    }

    public void setInquirerUserId(String inquirerUserId) {
        this.inquirerUserId = inquirerUserId;
    }

    public String getInquirerEmail() {
        return inquirerEmail;
    }

    public void setInquirerEmail(String inquirerEmail) {
        this.inquirerEmail = inquirerEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
