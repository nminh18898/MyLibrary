package com.nhatminh.example.datastorage.mylibrary.book;


public class Book {

    int bookId;
    String name;
    String author;
    String content;
    String thumbnailPath;
    int categoryId;


    public Book(int bookId, String name, String author, String content, String thumbnailPath, int categoryId) {
        this.bookId = bookId;
        this.name = name;
        this.author = author;
        this.content = content;
        this.thumbnailPath = thumbnailPath;
        this.categoryId = categoryId;
    }

    public Book() {
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}
