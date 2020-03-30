package org.udg.pds.todoandroid.activity;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.entity.Publication;

public class AddPhoto extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        Button b = findViewById(R.id.add_button);

        // això passarà quan el usuari pulsi el botó ADD PHOTO
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText c = AddPhoto.this.findViewById(R.id.comentari);
                ImageView i = AddPhoto.this.findViewById(R.id.imageView);
                Login.this.add_photo(c.getText().toString(), i.toString());
            }
        });
    }

    public void add_photo (String comentari, String i){
        Publication p = new Publication ();
        p.description = comentari;
        p.photo = i;
    }
}
