package com.example.android61;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Slideshow extends AppCompatActivity {

    private ArrayList<Photo> photoList;
    private int currentIndex = 0;

    private ImageView slideshowImageView;
    private TextView photoIndexOverlay;
    private ImageButton nextButton;
    private ImageButton prevButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slideshow);

        // Initialize views
        slideshowImageView = findViewById(R.id.slideshowImageView);
        photoIndexOverlay = findViewById(R.id.photoIndexOverlay);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);

        // Retrieve photo list passed from previous activity
        photoList = (ArrayList<Photo>) getIntent().getSerializableExtra("photoList");

        if (photoList == null || photoList.size() == 0) {
            finish(); // Exit if no photos to show
            return;
        }

        // Show the first image
        updatePhoto();

        // Next button logic
        nextButton.setOnClickListener(v -> {
            if (currentIndex < photoList.size() - 1) {
                currentIndex++;
                updatePhoto();
            }
        });

        // Previous button logic
        prevButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updatePhoto();
            }
        });
    }

    private void updatePhoto() {
        Photo currentPhoto = photoList.get(currentIndex);
        Uri photoUri = Uri.parse(currentPhoto.getUriString());
        slideshowImageView.setImageURI(photoUri);
        photoIndexOverlay.setText((currentIndex + 1) + " / " + photoList.size());
    }
}