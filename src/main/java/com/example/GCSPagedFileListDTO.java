package com.example;

import com.google.cloud.storage.BlobInfo;

import java.util.LinkedList;
import java.util.List;

public class GCSPagedFileListDTO {

    List<BlobInfo> blobList = new LinkedList<>();
    String pageToken = null;

    public List<BlobInfo> getBlobList() {
        return blobList;
    }

    public void setBlobList(List<BlobInfo> blobList) {
        this.blobList = blobList;
    }

    public String getPageToken() {
        return pageToken;
    }

    public void setPageToken(String pageToken) {
        this.pageToken = pageToken;
    }
}
