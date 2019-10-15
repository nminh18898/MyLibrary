package com.nhatminh.example.datastorage.mylibrary.book;

public class ThumbnailPath {
    int thumbnailPathId;
    String originalPath;
    String databasePath;

    public ThumbnailPath(int thumbnailPathId, String originalPath, String databasePath) {
        this.thumbnailPathId = thumbnailPathId;
        this.originalPath = originalPath;
        this.databasePath = databasePath;
    }

    public ThumbnailPath() {
    }

    public ThumbnailPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public int getThumbnailPathId() {
        return thumbnailPathId;
    }

    public void setThumbnailPathId(int thumbnailPathId) {
        this.thumbnailPathId = thumbnailPathId;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }
}
