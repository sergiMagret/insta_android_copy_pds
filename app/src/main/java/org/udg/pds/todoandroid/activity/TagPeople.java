package org.udg.pds.todoandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Publication;
import org.udg.pds.todoandroid.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TagPeople extends AppCompatActivity {
    TodoApi mTodoService;
    Long publicationId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taguser_layout);
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        Bundle b = getIntent().getExtras();
        publicationId = b.getLong("id");
        ImageView tagImage = findViewById(R.id.tagImage);
        tagImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                TextView username = TagPeople.this.findViewById(R.id.enterUsernameTag);
                String name = username.getText().toString();
                username.setText("");
                TagPeople.this.tagUser(name,publicationId);
            }
        });
        ImageView finish = findViewById(R.id.checkFinish);
        finish.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                TagPeople.this.startActivity(new Intent(TagPeople.this, NavigationActivity.class));
                TagPeople.this.finish();
            }
        });
    }

    void tagUser (String username, Long publicationId){
        if(username.length()>0){
            Call<Integer> call = mTodoService.tagUser(publicationId, username);
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful()) {
                        if (response.body() == 0) {
                            Toast toast = Toast.makeText(TagPeople.this, "User " + username + " tagged successfully", Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (response.body() == 1){
                            Toast toast = Toast.makeText(TagPeople.this, "You have tagged the maximum amount of users (20)", Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (response.body() == 2) {
                            Toast toast = Toast.makeText(TagPeople.this, "This user is already tagged", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } else {
                        Toast toast = Toast.makeText(TagPeople.this, "Error TagPeople, maybe you entedered an unexisting username", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Toast toast = Toast.makeText(TagPeople.this, "Error TagPeople no response", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
        else {
            Toast toast = Toast.makeText(TagPeople.this, "Write a username", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
