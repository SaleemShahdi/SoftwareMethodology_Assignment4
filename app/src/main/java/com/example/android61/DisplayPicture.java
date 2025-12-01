package com.example.android61;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.net.Uri;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.util.*;


public class DisplayPicture extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private ListView listView;

    private Button addTag;
    private Button SlideShow;
    private Button HomePage;
    private Button MovePic;

    private Photo pho;
    private Button deleteTag;

    private Button deletePhoto;

    private Button slideshow;

    private Album currentAlbum;

    private ArrayList<String> stringTags;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_picture);

        ImageView imageView = findViewById(R.id.photoImageView);

        // Get photo URI from Intent
        String photoUri = getIntent().getStringExtra("photoUri");
        String albumName= getIntent().getStringExtra("albumName");

        for (Album album: Home.albums){
            if (album.getName().equals(albumName)){
                currentAlbum = album;
            }
        }

        if (photoUri != null) {
            imageView.setImageURI(Uri.parse(photoUri));
        }

        ArrayList<Tag> tags = new ArrayList<Tag>();
        stringTags = new ArrayList<String>();
        // get all the tags on the photo

        for (int i = 0; i < Home.albums.size(); i++){
            Album temp = Home.albums.get(i);
            ArrayList<Photo> p = temp.getPhotos();
            for (int j = 0; j < p.size(); j++){
                // check to see if the the photo's URI matches the current one
                if (p.get(j).getUriString().equals(photoUri)){
                    //get tags
                    pho = p.get(j);
                    tags = p.get(j).getTags();
                    break;
                }
            }

        }

        // convert tags to strings
        for (int i = 0; i < tags.size(); i++){
            stringTags.add(tags.get(i).getName() + "=" + tags.get(i).getValue());
        }

        // display stringTags
        listView = findViewById(R.id.tagsListView);
        adapter = new ArrayAdapter<>(this, R.layout.list_item, stringTags);
        listView.setAdapter(adapter);

        // deal with deleting tags
        deleteTag = findViewById(R.id.deleteTag);
        deleteTag.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                deleteATag();
            }

        });


        // deal with adding tags
        addTag = findViewById(R.id.addTag);
        addTag.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                addATag();
            }

        });

        // deal with moving the picture
        MovePic = findViewById(R.id.movePicture);
        MovePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                moveAPicture();
            }

        });

        // deal with homepage
        HomePage = findViewById(R.id.homePage);
        HomePage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                home();
            }

        });

        //deal with deleting photo
        deletePhoto = findViewById(R.id.deletePhoto);
        deletePhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                deleteAPhoto();
            }

        });

        //deal with slideshow
        slideshow = findViewById(R.id.slideshow);
        slideshow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                Intent intent = new Intent(DisplayPicture.this, Slideshow.class);
                intent.putExtra("photoList", currentAlbum.getPhotos()); // Assuming getPhotos() returns Serializable
                startActivity(intent);
            }

        });

    }


    public void deleteAPhoto(){
        View dialogView = getLayoutInflater().inflate(R.layout.delete_album, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setView(dialogView)
                .setPositiveButton("Delete", null)  // null listener for manual control
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button createButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            createButton.setOnClickListener(v -> {
                // delete the album from the album list
                String photoUri = getIntent().getStringExtra("photoUri");

                boolean found = false;
                for (Album album : Home.albums) {
                    for (Photo photo : album.getPhotos()) {
                        if (photo.getUriString().equals(photoUri)) {
                            album.removePhoto(photo);
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }

                home();

            });
        });
        dialog.show();
    }

    public void home(){
        // make sure we're writing to the file first
        try {
            Home.writeAlbums(this, Home.albums);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    public void addATag(){
        View dialogView = getLayoutInflater().inflate(R.layout.add_tag_dialog, null);

        Spinner tagNameSpinner = dialogView.findViewById(R.id.tagNameSpinner);
        EditText tagValueInput = dialogView.findViewById(R.id.tagValueInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();

        Button cancelBtn = dialogView.findViewById(R.id.cancelTagButton);
        Button addBtn = dialogView.findViewById(R.id.addTagButton);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());


        addBtn.setOnClickListener(v -> {
            String tagName = tagNameSpinner.getSelectedItem().toString().trim();
            String tagValue = tagValueInput.getText().toString().trim();

            if (tagValue.isEmpty()) {
                Toast.makeText(this, "Please enter a tag value", Toast.LENGTH_SHORT).show();
                return;
            }
            else{
                // make sure the tag doesn't exist already
                if (pho.hasTag(tagName, tagValue)){
                    Toast.makeText(this, "Tag already exists", Toast.LENGTH_SHORT).show();
                }
                else{
                    pho.addTag(tagName, tagValue);
                    // update the adapter
                    stringTags.add(tagName + "=" + tagValue);
                    adapter.notifyDataSetChanged();

                    // write to the stream
                    try{
                        Home.writeAlbums(this,Home.albums);
                        //System.out.println("success");
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }


            }

            dialog.dismiss();
        });
    }

    public void deleteATag(){
        View dialogView = getLayoutInflater().inflate(R.layout.delete_tag_dialog, null);

        Spinner tagNameSpinner1 = dialogView.findViewById(R.id.tagNameSpinner1);
        EditText tagValueInput1 = dialogView.findViewById(R.id.tagValueInput1);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();

        Button cancelBtn = dialogView.findViewById(R.id.cancelTagButton1);
        Button deleteBtn = dialogView.findViewById(R.id.deleteTagButton1);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());


        deleteBtn.setOnClickListener(v -> {
            String tagName = tagNameSpinner1.getSelectedItem().toString().trim();
            String tagValue = tagValueInput1.getText().toString().trim();

            if (tagValue.isEmpty()) {
                Toast.makeText(this, "Please enter a tag value", Toast.LENGTH_SHORT).show();
                return;
            }
            else{
                // make sure the tag exists
                if (!pho.hasTag(tagName, tagValue)){
                    Toast.makeText(this, "Tag does not exist", Toast.LENGTH_SHORT).show();
                }
                else{
                    pho.removeTag(tagName, tagValue);
                    // update the adapter
                    for (int i = 0; i < stringTags.size();i++){
                        if (stringTags.get(i).equals(tagName + "=" + tagValue)){
                            // delete from adapter
                            stringTags.remove(i);
                            break;
                        }

                    }

                    adapter.notifyDataSetChanged();

                    // write to the stream
                    try{
                        Home.writeAlbums(this,Home.albums);
                        //System.out.println("success");
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }


            }

            dialog.dismiss();
        });
    }

    public void moveAPicture() {
        // Inflate the dialog view
        View dialogView = getLayoutInflater().inflate(R.layout.move_picture_dialog, null);

        // Find views inside the dialog
        Spinner albumSpinner = dialogView.findViewById(R.id.albumSpinner);

        // Populate the Spinner with album names (assumes you have a list of album names)
        List<String> albumNames = new ArrayList<>();
        for (Album album : Home.albums) {
            albumNames.add(album.getName()); // Assuming Album has a getName() method
        }

        ArrayAdapter<String> albumAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, albumNames);
        albumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        albumSpinner.setAdapter(albumAdapter);

        // Build the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Show dialog before wiring up buttons
        dialog.show();

        // Now that it's shown, we can safely reference the dialog buttons
        Button cancelBtn = dialogView.findViewById(R.id.cancelMoveButton);
        Button moveBtn = dialogView.findViewById(R.id.moveButton);

        // Handle cancel
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        // Handle move action
        moveBtn.setOnClickListener(v -> {
            String selectedAlbum = albumSpinner.getSelectedItem().toString().trim();

            if (selectedAlbum.isEmpty()) {
                Toast.makeText(this, "Please select an album", Toast.LENGTH_SHORT).show();
                return;
            }

            // Find the selected album from the list
            Album selectedAlbumObject = null;
            for (Album album : Home.albums) {
                if (album.getName().equals(selectedAlbum)) {
                    selectedAlbumObject = album;
                    break;
                }
            }

            if (selectedAlbumObject != null) {
                // Move the picture to the selected album (this assumes you have the URI or other identifier for the picture)
                String photoUri = getIntent().getStringExtra("photoUri");
                Photo photoToMove = null;

                // Find the photo object by its URI
                boolean found = false;
                for (Album album : Home.albums) {
                    for (Photo photo : album.getPhotos()) {
                        if (photo.getUriString().equals(photoUri)) {
                            album.removePhoto(photo);
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }

                // Add the photo to the selected album
                if (pho != null && selectedAlbumObject != null) {
                    selectedAlbumObject.addPhoto(pho);
                    Toast.makeText(this, "Picture moved to " + selectedAlbum, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Couldn't move picture", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Album doesn't exist", Toast.LENGTH_SHORT).show();
            }
        });

    }


}
