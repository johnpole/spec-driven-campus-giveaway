package edu.rpi.giveaway.media;

public record MediaUploadResponse(String photoUrl, String url) {
    public MediaUploadResponse(String photoUrl) {
        this(photoUrl, photoUrl);
    }
}
