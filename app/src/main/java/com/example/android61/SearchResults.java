package com.example.android61;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SearchResults extends AppCompatActivity {

    private ArrayList<Photo> photos;
    private Button homeButton;
    private ListView photoView;
    private ArrayAdapter<String> photoAdapter;
    private List<String> photoUris = new ArrayList<>();

    static final int REQUEST_IMAGE_GET = 1;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);

        //currentAlbum = getIntent().getStringExtra("albumName");
        photos = (ArrayList<Photo>) getIntent().getSerializableExtra("searchResults");
        //System.out.println(albums.size());
        //TextView albumNameView = findViewById(R.id.textViewAlbumName);

        //load all the photos

        photoView = findViewById(R.id.searchList);



        for (int j = 0; j < photos.size(); j++) {
            Uri u = photos.get(j).getUri(this);
            if (u != null) {
                photoUris.add(u.toString());
            } else {
                Log.e("AlbumScreen", "Invalid or inaccessible URI: " + photos.get(j).getUriString());
            }

        }




        // Create a simple ArrayAdapter with custom item layout
        photoAdapter = new ArrayAdapter<String>(this, R.layout.photo_item, photoUris) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Inflate layout if not reused
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.photo_item, parent, false);
                }

                // Get the current photo URI (string)
                String currentPhotoUri = getItem(position);
                ImageView photoImageView = convertView.findViewById(R.id.photoImageView);

                try {
                    Uri photoUri = Uri.parse(currentPhotoUri);

                    // Attempt to open an InputStream for the URI using ContentResolver
                    InputStream imageStream = getContext().getContentResolver().openInputStream(photoUri);

                    if (imageStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        if (bitmap != null) {
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);  // Resize image if needed
                            photoImageView.setImageBitmap(scaledBitmap);
                        }
                        if (bitmap != null) {
                            photoImageView.setImageBitmap(bitmap);  // Set the image if decoding is successful
                        } else {
                            photoImageView.setImageDrawable(null);  // Set to null (empty) if the image can't be decoded
                        }
                        imageStream.close();  // Always close the input stream
                    } else {
                        // If the InputStream is null, set the ImageView to null (empty view)
                        photoImageView.setImageDrawable(null);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    photoImageView.setImageDrawable(null);  // Set to null (empty) in case of file not found
                } catch (IOException e) {
                    e.printStackTrace();
                    photoImageView.setImageDrawable(null);  // Set to null (empty) in case of any IO exception
                } catch (Exception e) {
                    e.printStackTrace();
                    photoImageView.setImageDrawable(null);  // Set to null (empty) for any other unexpected errors
                }

                return convertView;
            }
        };

        photoView.setAdapter(photoAdapter);

        // deal with back button
        homeButton = findViewById(R.id.HomePage);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homePage();
            }

        });

    }


    private void homePage() {
        // go back
        finish();
    }

}



