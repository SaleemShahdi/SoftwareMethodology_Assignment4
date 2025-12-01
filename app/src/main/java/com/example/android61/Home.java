package com.example.android61;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import android.content.*;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Home extends AppCompatActivity {
    // list of albums
    public static ArrayList<Album> albums = new ArrayList<Album>();
    private FloatingActionButton fabAddAlbum;
    private Button fabSearchPhoto;
    private ArrayList photos = new ArrayList<Photo>();
    private ListView listView;
    private ArrayAdapter<String> adapter;

    public static final String storeFile = "users.dat";

    public static void writeAlbums(Context context, ArrayList<Album> albums) throws IOException{
        FileOutputStream fos = context.openFileOutput(storeFile, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(albums);
        oos.close();
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Album> readAlbums(Context context) throws IOException, ClassNotFoundException{
        FileInputStream fis = context.openFileInput(storeFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ArrayList<Album> albums = (ArrayList<Album>) ois.readObject();
        ois.close();
        return albums;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        // deal with serialization

        try {
            albums = readAlbums(this);
        } catch (IOException | ClassNotFoundException e) {
            //System.out.println("No users found, creating new user list.");
            albums = new ArrayList<>();
        }

        // show all albums
        listView = findViewById(R.id.listView);
        List<String> albumNames = new ArrayList();
        for (int i = 0; i < albums.size(); i++){
            albumNames.add(albums.get(i).getAlbumName());
        }
        adapter = new ArrayAdapter<>(this, R.layout.list_item, albumNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Album selectedAlbum = albums.get(position);
            onAlbumClick(selectedAlbum);
        });


        //deal with creating new album
        fabAddAlbum = findViewById(R.id.fab_add);
        fabSearchPhoto = findViewById(R.id.fab_search);
        fabAddAlbum.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                showCreateAlbumDialog();
            }

        });

        fabSearchPhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                showSearchDialog();
            }
        });
    }

    private void showSearchDialog(){
        // show popup for search

        View dialogView = getLayoutInflater().inflate(R.layout.search_photo, null);
        CheckBox enableTag2 = dialogView.findViewById(R.id.enable_second_tag);
        Spinner tag1TypeSpinner = dialogView.findViewById(R.id.tag1_type_spinner);
        EditText tag1ValueInput = dialogView.findViewById(R.id.tag1_value_input);
        LinearLayout tag2Section = dialogView.findViewById(R.id.tag2_section);
        Spinner tag2TypeSpinner = dialogView.findViewById(R.id.tag2_type_spinner);
        EditText tag2ValueInput = dialogView.findViewById(R.id.tag2_value_input);
        Spinner matchConditionSpinner = dialogView.findViewById(R.id.match_condition_spinner);

        enableTag2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tag2Section.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Search Photos by Tag")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        Button searchButton = dialogView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            // Get tag1
            String tag1Type = tag1TypeSpinner.getSelectedItem().toString();
            String tag1Value = tag1ValueInput.getText().toString().trim();

            if (tag1Value.isEmpty()) {
                tag1ValueInput.setError("Enter a value");
                tag1ValueInput.requestFocus();
                return;
            }

            ArrayList<Photo> results = new ArrayList<>();

            if (enableTag2.isChecked()) {
                // Get tag2
                String tag2Type = tag2TypeSpinner.getSelectedItem().toString();
                String tag2Value = tag2ValueInput.getText().toString().trim();

                if (tag2Value.isEmpty()) {
                    tag2ValueInput.setError("Enter a value");
                    tag2ValueInput.requestFocus();
                    return;
                }

                String condition = matchConditionSpinner.getSelectedItem().toString();

                for (Album album : albums) {
                    for (Photo photo : album.getPhotos()) {
                        boolean hasTag1 = photo.hasTag(tag1Type, tag1Value);
                        boolean hasTag2 = photo.hasTag(tag2Type, tag2Value);

                        if (condition.equals("AND") && hasTag1 && hasTag2) {
                            results.add(photo);
                        } else if (condition.equals("OR") && (hasTag1 || hasTag2)) {
                            results.add(photo);
                        }
                    }
                }
            } else {
                for (Album album : albums) {
                    for (Photo photo : album.getPhotos()) {
                        if (photo.hasTag(tag1Type, tag1Value)) {
                            results.add(photo);
                        }
                    }
                }
            }

            dialog.dismiss();
            // Now pass `results` to your result display logic
            //goToSearchResults(results);

            Intent intent = new Intent(this, SearchResults.class);
            //intent.putExtra("albumName", album.getAlbumName());
            //intent.putExtra("albumName", album.getAlbumName());
            intent.putExtra("searchResults", (Serializable) results);
            startActivity(intent);
        });


    }

    private void showCreateAlbumDialog(){
        View dialogView = getLayoutInflater().inflate(R.layout.create_album, null);

        EditText newAlbumName = dialogView.findViewById(R.id.album_name_input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Create New Album")
                .setView(dialogView)
                .setPositiveButton("Create", null)  // null listener for manual control
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
                        Album newAlbum = new Album(albumName);
                        albums.add(newAlbum);
                        Toast.makeText(this, "Album \"" + albumName + "\" created", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();  // Only dismiss when successful

                        // show the updated album list
                        adapter.clear();
                        List<String> albumNames = new ArrayList();
                        for (int i = 0; i < albums.size(); i++){
                            albumNames.add(albums.get(i).getAlbumName());
                        }
                        //System.out.println(albumNames.length);
                        adapter.addAll(albumNames);
                        adapter.notifyDataSetChanged();

                        // write to the stream
                        try{
                            writeAlbums(this,albums);
                            //System.out.println("success");
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }


                    }
                }
            });
        });

        dialog.show();

    }

    private void onAlbumClick(Album album){
        // open album when clicked
        Intent intent = new Intent(this, AlbumScreen.class);
        //intent.putExtra("albumName", album.getAlbumName());
        intent.putExtra("albumName", album.getAlbumName());
        intent.putExtra("albums", (Serializable) albums);
        startActivity(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            writeAlbums(this,albums);
            //System.out.println("success");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    protected void onStop() {
        super.onStop();
        try{
            writeAlbums(this,albums);
            //System.out.println("success");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
