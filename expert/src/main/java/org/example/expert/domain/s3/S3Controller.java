package org.example.expert.domain.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    /**
     * 파일 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String key = file.getOriginalFilename();
        String url = s3Service.uploadFile(key, file);
        return ResponseEntity.ok(url);
    }

    /**
     * 파일 다운로드
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("key") String key) {
        byte[] fileData = s3Service.downloadFile(key);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", key);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam("key") String key) {
        s3Service.deleteFile(key);
        return ResponseEntity.noContent().build();
    }
}