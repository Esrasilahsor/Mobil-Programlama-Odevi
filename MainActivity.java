package com.example.mobilproje;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    TextView txtWeather;
    ArrayList<String> noteTitles = new ArrayList<>();
    ArrayList<Integer> noteIds = new ArrayList<>();
    SQLiteDatabase db;
    NotesDBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        txtWeather = findViewById(R.id.txtWeather);

        helper = new NotesDBHelper(this);
        db = helper.getWritableDatabase();

        loadNotes();
        loadWeather();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(MainActivity.this, DetailActivity.class);
            i.putExtra("id", noteIds.get(position));
            startActivity(i);
        });

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddNoteActivity.class))
        );
        // ListView için uzun basış ile silme
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            int noteId = noteIds.get(position);
            String title = noteTitles.get(position);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Notu Sil")
                    .setMessage("\"" + title + "\" notunu silmek istiyor musunuz?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        db.delete("notes", "id=?", new String[]{String.valueOf(noteId)});
                        loadNotes();  // Listeyi yenile
                        Toast.makeText(MainActivity.this, "Not silindi", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hayır", null)
                    .show();

            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes(); // Listeyi yenile
    }

    void loadNotes() {
        noteTitles.clear();
        noteIds.clear();

        Cursor cursor = db.rawQuery("SELECT * FROM notes ORDER BY id DESC", null);

        while (cursor.moveToNext()) {
            noteIds.add(cursor.getInt(0));
            noteTitles.add(cursor.getString(1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, noteTitles);

        listView.setAdapter(adapter);
        cursor.close();
    }

    void loadWeather() {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=40.35&longitude=27.97&current_weather=true");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String result = response.toString();

                runOnUiThread(() -> {
                    // Sadece sıcaklık bilgisini göster
                    try {
                        String temp = result.split("\"temperature\":")[1].split(",")[0];
                        txtWeather.setText("🌡️ Sıcaklık: " + temp + "°C");
                    } catch (Exception e) {
                        txtWeather.setText("Hava durumu alındı");
                    }
                });

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> txtWeather.setText("Hava durumu yüklenemedi: " + e.getMessage()));
            }
        }).start();
    }


    public static class NotesDBHelper extends SQLiteOpenHelper {

        public NotesDBHelper(Context context) {
            super(context, "notes.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE notes(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, date TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }
}