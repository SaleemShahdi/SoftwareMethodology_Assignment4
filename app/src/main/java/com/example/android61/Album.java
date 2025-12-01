package com.example.android61;
import java.util.*;
import java.io.*;
import java.io.Serializable;

public class Album implements Serializable {
    String name;
    String names;
    ArrayList<Photo> photos;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<Photo>();
    }

    // get album name
    public String getAlbumName() {
        return name;
    }
    // get photos in album
    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    // change name of album
    public void setName(String name) {
        this.name = name;

    }

    // get name of album
    public String getName() {
        return name;
    }

    // add photo
    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    // remove photo
    public void removePhoto(Photo photo) {
        photos.remove(photo);
    }

    //set entire arraylist of photos
    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }



}
