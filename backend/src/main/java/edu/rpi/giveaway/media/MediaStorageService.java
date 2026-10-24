package edu.rpi.giveaway.media;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Contract for photo persistence and binary streaming across local and cloud storage providers.
 */
public interface MediaStorageService {

    /**
     * Persist an uploaded photo file and return the public access URL or path.
     *
     * @param file the uploaded multipart file
     * @return the accessible URL or relative media path (e.g. /api/media/{filename})
     * @throws IOException if binary storage fails
     */
    String storePhoto(MultipartFile file) throws IOException;

    /**
     * Retrieve a photo by filename as a streamable Spring Resource.
     *
     * @param filename the image filename
     * @return streamable Resource, or null if not found
     * @throws IOException on I/O failure
     */
    Resource loadPhotoAsResource(String filename) throws IOException;
}
