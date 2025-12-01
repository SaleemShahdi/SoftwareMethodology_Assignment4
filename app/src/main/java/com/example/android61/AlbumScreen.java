package com.example.android61;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.content.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.net.Uri;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.*;
import java.util.*;




public class AlbumScreen extends AppCompatActivity {

    private Button renameButton;
    private String currentAlbum;
    private ArrayList<Album> albums;

    private Button deleteButton;
    private Button backButton;

    private FloatingActionButton addPhoto;

    private ListView photoView;
    private ArrayAdapter<String> photoAdapter;
    private List<String> photoUris = new ArrayList<>();

    static final int REQUEST_IMAGE_GET = 1;


    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album);

        currentAlbum = getIntent().getStringExtra("albumName");
        albums = (ArrayList<Album>) getIntent().getSerializableExtra("albums");
        //System.out.println(albums.size());
        TextView albumNameView = findViewById(R.id.textViewAlbumName);
        if (!currentAlbum.isEmpty()) {
            albumNameView.setText(currentAlbum);
        }


        //load all the photos

        photoView = findViewById(R.id.recyclerViewAlbum);

        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getAlbumName().equals(currentAlbum)) {
                //get URI of each photo in album
                ArrayList<Photo> p = albums.get(i).getPhotos();
                for (int j = 0; j < p.size(); j++) {
                    Uri u = p.get(j).getUri(this);
                    if (u != null) {
                        photoUris.add(u.toString());
                    } else {
                        Log.e("AlbumScreen", "Invalid or inaccessible URI: " + p.get(j).getUriString());
                    }

                }
                break;
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

                photoImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getContext(), DisplayPicture.class);
                        intent.putExtra("photoUri", currentPhotoUri);
                        intent.putExtra("albumName", currentAlbum);
                        getContext().startActivity(intent);
                    }
                });


                return convertView;
            }
        };

        photoView.setAdapter(photoAdapter);



        // find current album
        Album currAlbum = albums.get(0);
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getAlbumName().equals(currentAlbum)) {
                currAlbum = albums.get(i);
            }

        }


        // deal with adding a photo
        addPhoto = findViewById(R.id.fabAddPhoto);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }

        });


        //deal with renaming album
        renameButton = findViewById(R.id.buttonRenameAlbum);

        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renameAlbumDialog();
            }

        });


        //deal with deleting album
        deleteButton = findViewById(R.id.buttonDeleteAlbum);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAlbumDialog();
            }

        });

        // deal with back button
        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }

        });


    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_GET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK && data != null) {
            Uri fullPhotoUri = data.getData();

            if (fullPhotoUri != null) {

                final int takeFlags = data.getFlags() &
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(fullPhotoUri, takeFlags);


                // create new photo object
                Photo newPhoto = new Photo(fullPhotoUri);

                for (int i = 0; i < albums.size(); i++) {
                    if (albums.get(i).getAlbumName().equals(currentAlbum)) {
                        //add to album
                        Home.albums.get(i).addPhoto(newPhoto);
                        albums = Home.albums;
                    }
                }

                photoUris.add(newPhoto.getUri(this).toString());
                photoAdapter.notifyDataSetChanged();

                // Save updated albums
                try {
                    Home.writeAlbums(this, albums);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(this, "Photo added to album", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void back() {
        // go back

        // make sure we're writing to the file first
        try {
            Home.writeAlbums(this, albums);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    private void deleteAlbumDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.delete_album, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setView(dialogView)
                .setPositiveButton("Delete", null)  // null listener for manual control
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button createButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            createButton.setOnClickListener(v -> {
                // delete the album from the album list
                for (int i = 0; i < albums.size(); i++) {
                    if (albums.get(i).getAlbumName().equals(currentAlbum)) {
                        Home.albums.remove(i);
                        albums = Home.albums;
                        break;
                    }
                }

                // write to stream and exit
                try {
                    Home.writeAlbums(this, albums);
                    //System.out.println("success");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                back();

            });
        });
        dialog.show();
    }

    private void renameAlbumDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.rename_album, null);

        EditText newAlbumName = dialogView.findViewById(R.id.albumRename);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(dialogView)
                .setPositiveButton("Rename", null)  // null listener for manual control
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button createButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            createButton.setOnClickListener(v -> {
                String albumName = newAlbumName.getText().toString().trim();

                if (albumName.isEmpty()) {
                    newAlbumName.setError("Please enter album name");
                    newAlbumName.requestFocus();
                } else {
                    boolean found = false;
                    for (Album a : albums) {
                        if (a.getAlbumName().equalsIgnoreCase(albumName)) {
                            found = true;
                            break;
                        }
                    }

                    if (found) {
                        newAlbumName.setError("Album already exists");
                        newAlbumName.requestFocus();
                    } else {
                        // find the album
                        for (int i = 0; i < albums.size(); i++) {
                            if (albums.get(i).getAlbumName().equals(currentAlbum)) {
                                Album curr = albums.get(i);
                                curr.setName(albumName);
                            }
                        }

                        Toast.makeText(this, "Album \"" + albumName + "\" created", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();  // Only dismiss when successful
                        currentAlbum = albumName;
                        TextView albumNameView = findViewById(R.id.textViewAlbumName);
                        if (!currentAlbum.isEmpty()) {
                            albumNameView.setText(currentAlbum);
                        }

                        try {
                            Home.writeAlbums(this, (ArrayList<Album>) albums);  // Persist the changes
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }
            });
        });

        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            Home.writeAlbums(this, albums);
            //System.out.println("success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onStop() {
        super.onStop();
        try {
            Home.writeAlbums(this, albums);
            //System.out.println("success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}