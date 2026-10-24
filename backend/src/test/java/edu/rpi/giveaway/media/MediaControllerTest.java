package edu.rpi.giveaway.media;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MediaService mediaService;

    @Test
    void uploadWithoutSignInReturns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[16]);
        mockMvc.perform(multipart("/api/media/upload").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadValidJpegReturns201Created() throws Exception {
        byte[] content = new byte[1024]; // 1 KB
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", content);

        when(mediaService.storePhoto(any())).thenReturn("/api/media/photo.jpg");

        mockMvc.perform(multipart("/api/media/upload").file(file).header("Authorization", "Bearer test-token:u-student:student@rpi.edu:Student"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photoUrl").value("/api/media/photo.jpg"));
    }

    @Test
    void uploadOversizedFileReturns413PayloadTooLarge() throws Exception {
        // 5 MB + 1 byte
        byte[] largeContent = new byte[(int) MediaService.MAX_FILE_SIZE + 1];
        MockMultipartFile file = new MockMultipartFile("file", "huge.jpg", "image/jpeg", largeContent);

        mockMvc.perform(multipart("/api/media/upload").file(file).header("Authorization", "Bearer test-token:u-student:student@rpi.edu:Student"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void uploadUnsupportedTypeReturns415UnsupportedMediaType() throws Exception {
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", content);

        mockMvc.perform(multipart("/api/media/upload").file(file).header("Authorization", "Bearer test-token:u-student:student@rpi.edu:Student"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void uploadFailsWithStorageErrorReturns502BadGateway() throws Exception {
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", content);

        when(mediaService.storePhoto(any())).thenThrow(new StorageServiceException("GCS connection timeout", new RuntimeException()));

        mockMvc.perform(multipart("/api/media/upload").file(file).header("Authorization", "Bearer test-token:u-student:student@rpi.edu:Student"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").exists());
    }
}
