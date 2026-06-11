package com.example.mobilproje;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class AddNoteActivity extends AppCompatActivity {

    EditText edtTitle, edtContent;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        btnSave = findViewById(R.id.btnSave);

        MainActivity.NotesDBHelper helper = new MainActivity.NotesDBHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        btnSave.setOnClickListener(v -> {

            ContentValues cv = new ContentValues();
            cv.put("title", edtTitle.getText().toString());
            cv.put("content", edtContent.getText().toString());
            cv.put("date", System.currentTimeMillis() + "");

            db.insert("notes", null, cv);

            Toast.makeText(this, "Not kaydedildi", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}