package com.example.mobilproje;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    TextView txtTitle, txtContent;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        txtTitle = findViewById(R.id.txtTitle);
        txtContent = findViewById(R.id.txtContent);

        int id = getIntent().getIntExtra("id", -1);

        MainActivity.NotesDBHelper helper = new MainActivity.NotesDBHelper(this);
        db = helper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM notes WHERE id=" + id, null);

        if (c.moveToFirst()) {
            txtTitle.setText(c.getString(1));
            txtContent.setText(c.getString(2));
        }

        c.close();
    }
}