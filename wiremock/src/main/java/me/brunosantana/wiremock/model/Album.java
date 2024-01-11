package me.brunosantana.wiremock.model;

import java.util.List;

public class Album {
    private Long albumId;
    private List<Photo> photos;

    public Album(Long albumId, List<Photo> photos) {
        this.albumId = albumId;
        this.photos = photos;
    }

    public Album(){}

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
}
