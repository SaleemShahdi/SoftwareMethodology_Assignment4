package com.example.android61;
import android.content.Context;
import android.os.Parcelable;

import java.util.*;
import java.io.*;
//import java.text.SimpleDateFormat;
import android.net.Uri;




public class Photo implements Serializable {


    ArrayList<Tag> tags;
    String uri;
    String decoded;


    public Photo(Uri uri) {
        this.uri = uri.toString();
        this.decoded = Uri.decode(uri.toString());

        // figure out how to open the filepath and get the date
        this.tags = new ArrayList<Tag>();
    }

    public Uri getUri(Context context) {
        try {
            // Try the decoded version first
            Uri decoded = Uri.parse(this.decoded);
            if (canReadUri(context, decoded)) {
                return decoded;
            }

            // Fall back to original URI
            Uri original = Uri.parse(uri);
            if (canReadUri(context, original)) {
                return original;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean canReadUri(Context context, Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            return is != null;
        } catch (Exception e) {
            return false;
        }
    }

    //get dateTime

    //get filePath
    public String getUriString() {
        return uri;
    }

    // get caption

    // get tags
    public ArrayList<Tag> getTags() {
        return tags;
    }

    // create a new tag for the photo if it doesn't already exist
    public void addTag(String name, String value) {
        for (Tag tag : tags) {
            if (tag.name.equals(name) && tag.value.equals(value)) {
                return;
            }
        }
        tags.add(new Tag(name, value));
    }

    // remove a tag from the photo if it exists
    public void removeTag(String name, String value) {
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).name.equals(name) && tags.get(i).value.equals(value)) {
                tags.remove(i);
                return;
            }
        }
    }

    // caption or recaption a photo

    public boolean hasTag(String tagName, String tagValue){
        for (Tag tag : tags) {
            if (tag.name.equals(tagName) && tag.value.equals(tagValue)) {
                return true;
            }
        }
        return false;
    }



}
