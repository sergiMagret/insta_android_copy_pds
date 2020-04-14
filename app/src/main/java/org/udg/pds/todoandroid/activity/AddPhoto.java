package org.udg.pds.todoandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Publication;
import org.udg.pds.todoandroid.entity.PublicationPost;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPhoto extends AppCompatActivity {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    TodoApi mTodoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        Button add = findViewById(R.id.add_button);

        add.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                EditText c = AddPhoto.this.findViewById(R.id.comentari);
                EditText i = AddPhoto.this.findViewById(R.id.imatge);
                AddPhoto.this.afegir(i.getText().toString(), c.getText().toString());
            }
        });
    }

    public void afegir (String imatge, String comentari){
        PublicationPost p= new PublicationPost();
        p.photo = imatge;
        p.description = comentari;
        p.date = new Date(2020, Calendar.APRIL, 28);
        Call<Publication> call = mTodoService.postPublication(p);
        call.enqueue(new Callback<Publication>() {
            @Override
            public void onResponse(Call<Publication> call, Response<Publication> response) {
                if(response.isSuccessful()){
                    AddPhoto.this.startActivity(new Intent(AddPhoto.this, NavigationActivity.class));
                    AddPhoto.this.finish();
                }
                else{
                    Toast toast = Toast.makeText(AddPhoto.this, "Error AddPhoto bad response", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<Publication> call, Throwable t) {
                Toast toast = Toast.makeText(AddPhoto.this, "Error addPhoto no response", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
}
