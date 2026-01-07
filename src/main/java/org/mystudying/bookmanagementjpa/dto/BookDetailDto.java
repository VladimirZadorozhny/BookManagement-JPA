package org.mystudying.bookmanagementjpa.dto;

public class BookDetailDto {
    private long id;
    private String title;
    private int year;
    private int available;
    private String authorName;
    private long authorId;


    public BookDetailDto() {
    }

    public BookDetailDto(long id, String title, int year, int available, String authorName, long authorId) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.available = available;
        this.authorName = authorName;
        this.authorId = authorId;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }
}

