package me.brunosantana.wiremock.model;

public class Response {
    private Album album;
    private BibleResponse bible;

    public Response(Album album, BibleResponse bible) {
        this.album = album;
        this.bible = bible;
    }

    public Response(){}

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public BibleResponse getBible() {
        return bible;
    }

    public void setBible(BibleResponse bible) {
        this.bible = bible;
    }
}
