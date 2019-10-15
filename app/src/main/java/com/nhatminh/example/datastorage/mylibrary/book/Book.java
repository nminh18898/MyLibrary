package com.nhatminh.example.datastorage.mylibrary.book;


public class Book {

    int bookId;
    String name;
    String author;
    String content;
    int thumbnailPathId;
    int categoryId;


    public Book(int bookId, String name, String author, String content, int thumbnailPathId, int categoryId) {
        this.bookId = bookId;
        this.name = name;
        this.author = author;
        this.content = content;
        this.thumbnailPathId = thumbnailPathId;
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

    public int getThumbnailPathId() {
        return thumbnailPathId;
    }

    public void setThumbnailPathId(int thumbnailPathId) {
        this.thumbnailPathId = thumbnailPathId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}
