package com.example;

import com.google.cloud.storage.BlobInfo;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@RestController
public class GcsController {

    private static final Logger LOG = LoggerFactory.getLogger(GcsController.class);

    @Autowired
    private GcsService gcsService;

    /**
     * Single File Upload
     *
     * @apiNote  curl --location --request POST 'http://localhost:8080/upload' \
     * --form 'files=@"/C:/Users/In Hwan Chun/Pictures/clipboardImage_21_0702_113845_579.jpeg"'
     *
     * @param file 업로드 하려는 파일 (단일)
     * @return file 생성 과정에서 만든 blobInfo
     * @throws Exception 현재 발생 가능한 Exception 은 IOException
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadSingleFileToGCS(@RequestParam("files") MultipartFile file) throws Exception {

        BlobInfo blobInfo = gcsService.uploadFileToGCS(file);
        LOG.info("blobInfo : " + blobInfo);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(blobInfo);
    }

    /**
     * Multi File Upload
     *
     * @apiNote curl --location --request POST 'http://localhost:8080/uploads' \
     * --form 'files=@"/C:/Users/In Hwan Chun/Pictures/clipboardImage_21_0702_113845_579.jpeg"' \
     * --form 'files=@"/C:/Users/In Hwan Chun/Pictures/clipboardImage_21_0702_114103_467.jpeg"'
     *
     * @param files 업로드 하려는 파일 (복수)
     * @return file 생성 과정에서 만든 blobInfo 정보 목록
     * @throws Exception 현재 발생 가능한 Exception 은 IOException
     */
    @PostMapping("/uploads")
    public ResponseEntity<?> uploadMultiFileToGCS(@RequestParam("files") List<MultipartFile> files) throws Exception {

        List<BlobInfo> blobInfoList = gcsService.uploadMultiFileToGCS(files);
        LOG.info("{}", blobInfoList);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(blobInfoList);
    }

    /**
     * Single File Delete by fileName with PathVariable
     *
     * @apiNote curl --location --request DELETE 'http://localhost:8080/clipboardImage_21_0702_113845_579.jpeg'
     *
     * @param fileName 삭제하려는 파일 이름
     * @return 삭제 완료 메시지 (String)
     * @throws Exception
     */
    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteFileFromGCS0(@PathVariable String fileName) throws Exception {

        boolean isDeleted = gcsService.deleteFileFromGCS(fileName);

        if (isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(String.format("%s is deleted successfully.", fileName));
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("%s is deleted in GCS.", fileName));
        }

    }

    /**
     * Single File Delete by fileName with RequestParam
     *
     * @apiNote curl --location --request DELETE 'http://localhost:8080/delete?fileName=clipboardImage_21_0702_113845_579.jpeg'
     *
     * @param fileName 삭제하려는 파일 이름
     * @return 삭제 완료 메시지 (String)
     * @throws Exception
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFileFromGCS1(@RequestParam String fileName) throws Exception {

        boolean isDeleted = gcsService.deleteFileFromGCS(fileName);

        if (isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(String.format("%s is deleted successfully.", fileName));
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("%s is deleted in GCS.", fileName));
        }

    }

    /**
     * Single File Delete by fileName with RequestBody
     *
     * @apiNote curl --location --request DELETE 'http://localhost:8080/delete2' \
     * --header 'Content-Type: application/json' \
     * --data-raw '{
     *     "fileName": "clipboardImage_21_0702_113845_579.jpeg"
     * }'
     *
     * @param gcsFileDTO 삭제하려는 파일 (JSON 형태의 raw data)
     * @return 삭제 완료 메시지 (String)
     * @throws Exception
     */
    @DeleteMapping("/delete2")
    public ResponseEntity<?> deleteFileFromGCS2(@RequestBody GcsFileDTO gcsFileDTO) throws Exception {

        boolean isDeleted = gcsService.deleteFileFromGCS(gcsFileDTO.getFileName());

        if (isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(String.format("%s is deleted successfully.", gcsFileDTO.getFileName()));
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("%s is deleted in GCS.", gcsFileDTO.getFileName()));
        }

    }

    /**
     * Single File Delete by fileName with ModelAttribute
     *
     * @apiNote
     * @param gcsFileDTO 삭제하려는 파일 (form-data)
     * @return 삭제 완료 메시지 (String)
     * @throws Exception
     */
    @DeleteMapping("/delete3")
    public ResponseEntity<?> deleteFileFromGCS3(@ModelAttribute GcsFileDTO gcsFileDTO) throws Exception {

        boolean isDeleted = gcsService.deleteFileFromGCS(gcsFileDTO.getFileName());

        if (isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(String.format("%s is deleted successfully.", gcsFileDTO.getFileName()));
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("%s is deleted in GCS.", gcsFileDTO.getFileName()));
        }

    }

    /**
     * Single File Retrieve by fileName
     *
     * @param fileName 조회하려는 파일 이름
     * @return 해당 파일의 blobInfo
     * @throws Exception
     * @apiNote curl --location --request GET 'http://localhost:8080/clipboardImage_21_0702_113845_579.jpeg'
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<?> getFileInfoFromGCS(@PathVariable String fileName) throws Exception {
        BlobInfo blobInfo = gcsService.getBlobInfoFromGCS(fileName);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(blobInfo);
    }

    /**
     * Whole File Retrieve (파일이 많으면 엄청 느리다)
     * 
     * @return 해당 파일의 blobInfo 목록
     * @throws Exception
     */
    @GetMapping("/list")
    public ResponseEntity<?> getWholeFileListFromGCS() throws Exception {
        List<BlobInfo> blobInfoList = gcsService.getWholeBlobInfoFromGCS();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(blobInfoList);
    }

    /**
     * Paged File Retrieve (페이징 개수와 페이징 토큰 이용)
     *
     * @param pageToken 다음 페이지의 조회를 위한 pageToken (매번 조회 시 마다 생성)
     * @param rowCount 한번에 page 조회할 개수
     * @return paged 조회된 blobInfo 목록과 다음 pageToken
     * @throws Exception
     * @apiNote curl --location --request GET 'http://localhost:8080/page' \
     * --form 'pageToken="ChJteS1maWxlLTAwMDI5OC50eHQ="' \
     * --form 'rowCount="100"'
     */
    @GetMapping("/page")
    public ResponseEntity<?> getPagedFileListFromGCS(@RequestParam String pageToken, @RequestParam int rowCount) throws Exception {
        GCSPagedFileListDTO paged = gcsService.getPagedFileListFromGCS(pageToken, rowCount);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(paged);
    }

    @GetMapping("/url")
    public ResponseEntity<?> getSignedURL(@RequestParam String fileName) throws IOException {

        URL signedURL = gcsService.getSignedURL(fileName);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(signedURL);
    }

    @GetMapping("/{fileName}/download")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName) throws IOException {

        File file = gcsService.downloadFile(fileName);

        try {
            byte[] res = Files.toByteArray(file);
            ByteArrayResource resource = new ByteArrayResource(res);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentLength(res.length)
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } finally {
            if (file.exists()) {
                file.delete();
            }
        }

    }
}
