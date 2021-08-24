package com.example;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class GcsService {

    private static final Logger LOG = LoggerFactory.getLogger(GcsService.class);

    @Autowired
    private Storage storage;

    @Value("${gcs-resource-test-bucket}")
    private String bucketName;

    public BlobInfo uploadFileToGCS(MultipartFile file) throws IOException {
        BlobId blobId = BlobId.of(this.bucketName, file.getOriginalFilename());
        BlobInfo blobInfo = BlobInfo
                .newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        this.storage.createFrom(blobInfo, file.getInputStream());
        return blobInfo;
    }

    public List<BlobInfo> uploadMultiFileToGCS(List<MultipartFile> files) throws IOException {
        List<BlobInfo> blobInfoList = new LinkedList<>();
        for (ListIterator<MultipartFile> it = files.listIterator(); it.hasNext(); ) {
            MultipartFile file = it.next();
            blobInfoList.add(this.uploadFileToGCS(file));
        }
        return blobInfoList;
    }

    public boolean deleteFileFromGCS(String fileName) throws IOException {

        LOG.info("{} will be deleted.", fileName);

        BlobId blobId = BlobId.of(this.bucketName, fileName);
        Blob blob = this.storage.get(blobId);

        if (blob == null) {
            throw new FileNotFoundException(String.format("%s is not existed in GCS.", fileName));
        }

        return this.storage.delete(blobId);
    }

    public BlobInfo getBlobInfoFromGCS(String fileName) throws IOException {
        BlobId blobId = BlobId.of(this.bucketName, fileName);
        Blob blob = this.storage.get(blobId);
        if (blob == null) {
            throw new FileNotFoundException(String.format("%s is not existed in GCS.", fileName));
        }
        BlobInfo blobInfo = BlobInfo
                .newBuilder(blob.getBlobId())
                .setContentType(blob.getContentType())
                .setCustomTime(blob.getCustomTime())
                .setMd5(blob.getMd5())
                .build();

        LOG.info("{}", blobInfo);

        return blobInfo;
    }

    public List<BlobInfo> getWholeBlobInfoFromGCS() throws IOException {
        Page<Blob> blobs = this.storage.list(this.bucketName);
        List<BlobInfo> blobList = new LinkedList<>();
        for (Blob blob : blobs.iterateAll()) {
            blobList.add(
                    BlobInfo.newBuilder(blob.getBlobId())
                            .setContentType(blob.getContentType())
                            .setMd5(blob.getMd5())
                            .setCustomTime(blob.getCustomTime())
                            .build());
        }
        LOG.info("{}", String.format("%d files is existed in %s bucket.", blobList.size(), this.bucketName));

        return blobList;
    }

    public GCSPagedFileListDTO getPagedFileListFromGCS(String pageToken, int rowCount) {
        List<BlobInfo> blobList = new LinkedList<>();
        Page<Blob> blobs = null;
        if (pageToken != null && !pageToken.isEmpty()) {
            blobs = this.storage.list(
                    this.bucketName,
                    Storage.BlobListOption.pageSize(rowCount),
                    Storage.BlobListOption.currentDirectory(),
                    Storage.BlobListOption.pageToken(pageToken)
            );
        } else {
            blobs = this.storage.list(
                    this.bucketName,
                    Storage.BlobListOption.pageSize(rowCount),
                    Storage.BlobListOption.currentDirectory()
            );
        }

        for (Iterator<Blob> ir = blobs.getValues().iterator(); ir.hasNext();) {
            Blob blob = ir.next();
            blobList.add(
                    BlobInfo.newBuilder(blob.getBlobId())
                            .setContentType(blob.getContentType())
                            .setMd5(blob.getMd5())
                            .setCustomTime(blob.getCustomTime())
                            .build());
        }

        GCSPagedFileListDTO paged = new GCSPagedFileListDTO();
        paged.setPageToken(blobs.getNextPageToken());
        paged.setBlobList(blobList);

        return paged;
    }

    public URL getSignedURL(String fileName) throws IOException {
        BlobId blobId = BlobId.of(this.bucketName, fileName);

        return this.storage.signUrl(
                BlobInfo.newBuilder(blobId).build(),
                5*60,
                TimeUnit.SECONDS,
                Storage.SignUrlOption.withV4Signature()
                );
    }

    public File downloadFile(String fileName) {
        BlobId blobId = BlobId.of(this.bucketName, fileName);
        Blob blob = this.storage.get(blobId);

        File f = new File(String.format("./%s", fileName));
        blob.downloadTo(Paths.get(f.toURI()));

        return f;
    }
}
