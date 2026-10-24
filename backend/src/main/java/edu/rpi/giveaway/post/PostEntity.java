package edu.rpi.giveaway.post;

import edu.rpi.giveaway.interest.InterestEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_status_deadline", columnList = "status, available_until"),
        @Index(name = "idx_posts_created_at", columnList = "created_at")
})
public class PostEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "condition_state", length = 50, nullable = false)
    private String condition;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "is_free", nullable = false)
    private Boolean isFree;

    @Column(name = "pickup_location", length = 120, nullable = false)
    private String pickupLocation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "available_until", nullable = false)
    private Date availableUntil;

    @Column(name = "photo_url", length = 500, nullable = false)
    private String photoUrl;

    @Column(name = "legal_confirmed", nullable = false)
    private Boolean legalConfirmed;

    @Column(name = "status", length = 20, nullable = false)
    private String status; // AVAILABLE, CLAIMED

    @Column(name = "poster_name", length = 100, nullable = false)
    private String posterName;

    @Column(name = "poster_contact", length = 100, nullable = false)
    private String posterContact; // Strictly private

    @Column(name = "edit_token_hash", length = 64, nullable = false)
    private String editTokenHash; // Strictly private

    @Column(name = "author_user_id", length = 255)
    private String authorUserId;

    @Column(name = "author_email", length = 255)
    private String authorEmail;

    @Column(name = "interest_count", nullable = false)
    private Integer interestCount = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "claimed_at")
    private Date claimedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_keywords", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<InterestEntity> interests = new ArrayList<>();

    public PostEntity() {
    }

    public static PostEntity fromDocument(PostDocument doc) {
        PostEntity entity = new PostEntity();
        entity.setId(doc.getId());
        entity.setTitle(doc.getTitle());
        entity.setDescription(doc.getDescription());
        entity.setCategory(doc.getCategory());
        entity.setCondition(doc.getCondition());
        entity.setPrice(doc.getPrice());
        entity.setIsFree(doc.getIsFree());
        entity.setPickupLocation(doc.getPickupLocation());
        entity.setAvailableUntil(doc.getAvailableUntil());
        entity.setPhotoUrl(doc.getPhotoUrl());
        entity.setLegalConfirmed(doc.getLegalConfirmed());
        entity.setStatus(doc.getStatus());
        entity.setPosterName(doc.getPosterName());
        entity.setPosterContact(doc.getPosterContact());
        entity.setEditTokenHash(doc.getEditTokenHash());
        entity.setAuthorUserId(doc.getAuthorUserId());
        entity.setAuthorEmail(doc.getAuthorEmail());
        entity.setInterestCount(doc.getInterestCount());
        entity.setCreatedAt(doc.getCreatedAt());
        entity.setKeywords(doc.getKeywords() != null ? new ArrayList<>(doc.getKeywords()) : new ArrayList<>());
        return entity;
    }

    public PostDocument toDocument() {
        PostDocument doc = new PostDocument();
        doc.setId(this.id);
        doc.setTitle(this.title);
        doc.setDescription(this.description);
        doc.setCategory(this.category);
        doc.setCondition(this.condition);
        doc.setPrice(this.price);
        doc.setIsFree(this.isFree);
        doc.setPickupLocation(this.pickupLocation);
        doc.setAvailableUntil(this.availableUntil);
        doc.setPhotoUrl(this.photoUrl);
        doc.setLegalConfirmed(this.legalConfirmed);
        doc.setStatus(this.status);
        doc.setPosterName(this.posterName);
        doc.setPosterContact(this.posterContact);
        doc.setEditTokenHash(this.editTokenHash);
        doc.setAuthorUserId(this.authorUserId);
        doc.setAuthorEmail(this.authorEmail);
        doc.setInterestCount(this.interestCount);
        doc.setCreatedAt(this.createdAt);
        doc.setKeywords(this.keywords != null ? new ArrayList<>(this.keywords) : new ArrayList<>());
        return doc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public Date getAvailableUntil() {
        return availableUntil;
    }

    public void setAvailableUntil(Date availableUntil) {
        this.availableUntil = availableUntil;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Boolean getLegalConfirmed() {
        return legalConfirmed;
    }

    public void setLegalConfirmed(Boolean legalConfirmed) {
        this.legalConfirmed = legalConfirmed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getPosterContact() {
        return posterContact;
    }

    public void setPosterContact(String posterContact) {
        this.posterContact = posterContact;
    }

    public String getEditTokenHash() {
        return editTokenHash;
    }

    public void setEditTokenHash(String editTokenHash) {
        this.editTokenHash = editTokenHash;
    }

    public Integer getInterestCount() {
        return interestCount != null ? interestCount : 0;
    }

    public void setInterestCount(Integer interestCount) {
        this.interestCount = interestCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(Date claimedAt) {
        this.claimedAt = claimedAt;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(String authorUserId) {
        this.authorUserId = authorUserId;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public List<InterestEntity> getInterests() {
        return interests;
    }

    public void setInterests(List<InterestEntity> interests) {
        this.interests = interests;
    }
}
