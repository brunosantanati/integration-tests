package me.brunosantana.wiremock.model;

public class Verse {
    private String book_id;
    private String book_name;
    private int chapter;
    private int verse;
    private String text;

    public Verse(String book_id, String book_name, int chapter, int verse, String text) {
        this.book_id = book_id;
        this.book_name = book_name;
        this.chapter = chapter;
        this.verse = verse;
        this.text = text;
    }

    public Verse(){}

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getBook_name() {
        return book_name;
    }

    public void setBook_name(String book_name) {
        this.book_name = book_name;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public int getVerse() {
        return verse;
    }

    public void setVerse(int verse) {
        this.verse = verse;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
