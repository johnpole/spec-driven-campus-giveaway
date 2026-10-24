package edu.rpi.giveaway.media;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MediaServiceTest {

    private Storage mockStorage;
    private MediaService mediaServiceWithGcs;
    private MediaService mediaServiceLocal;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockStorage = mock(Storage.class);
        mediaServiceWithGcs = new MediaService(mockStorage, "test-bucket", tempDir.toString());
        mediaServiceLocal = new MediaService(null, "test-bucket", tempDir.toString());
    }

    @Test
    void storePhotoWithGcsUploadsToCloudStorageAndReturnsApiPath() throws IOException {
        byte[] content = "dummy-image-bytes".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "lamp.jpg", "image/jpeg", content);

        String result = mediaServiceWithGcs.storePhoto(file);

        assertThat(result).startsWith("/api/media/");
        assertThat(result).endsWith(".jpg");

        ArgumentCaptor<BlobInfo> blobCaptor = ArgumentCaptor.forClass(BlobInfo.class);
        verify(mockStorage).create(blobCaptor.capture(), eq(content));
        assertThat(blobCaptor.getValue().getBucket()).isEqualTo("test-bucket");
        assertThat(blobCaptor.getValue().getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void loadPhotoAsResourceWithGcsReturnsBlobBytes() throws IOException {
        String filename = "photo-uuid.jpg";
        byte[] expectedBytes = "photo-binary-payload".getBytes();

        Blob mockBlob = mock(Blob.class);
        when(mockBlob.exists()).thenReturn(true);
        when(mockBlob.getContent()).thenReturn(expectedBytes);
        when(mockStorage.get(BlobId.of("test-bucket", filename))).thenReturn(mockBlob);

        Resource resource = mediaServiceWithGcs.loadPhotoAsResource(filename);

        assertThat(resource).isNotNull();
        assertThat(resource.getInputStream().readAllBytes()).isEqualTo(expectedBytes);
        assertThat(resource.getFilename()).isEqualTo(filename);
    }

    @Test
    void storeAndLoadPhotoWithLocalFallbackWorksWithoutGcs() throws IOException {
        byte[] content = "local-image-bytes".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "desk.png", "image/png", content);

        String result = mediaServiceLocal.storePhoto(file);
        assertThat(result).startsWith("/api/media/");

        String savedFilename = result.substring("/api/media/".length());
        Resource resource = mediaServiceLocal.loadPhotoAsResource(savedFilename);

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.getInputStream().readAllBytes()).isEqualTo(content);
    }
}
