package org.udg.pds.todoandroid.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Url;

public class UserProfile extends AppCompatActivity {
    TodoApi mTodoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        Bundle b = getIntent().getExtras();
        boolean private_profile = false;
        if(b != null) {
            private_profile = b.getBoolean("is_private");
        }

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        if(private_profile) { // If the user is trying to see its profile
            Call<User> call = mTodoService.getUserProfile();
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if(response.isSuccessful() && response.body() != null){
                        UserProfile.this.updateProfile(response);
                    }else{
                        UserProfile.this.launchErrorConnectingToServer();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    UserProfile.this.launchErrorConnectingToServer();
                }
            });

        }else{ // If the user is trying to see another user's profile
            long idToSearch = -1;
            try {
                idToSearch = b.getInt("user_to_search");
            }catch(NullPointerException e) { // If there's no user to search
                Toast.makeText(UserProfile.this, "Error loading user profile, there's no id user to search.", Toast.LENGTH_LONG).show();
                this.finish();
            }

            Call<User> call = mTodoService.getUserProfileByID(idToSearch);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserProfile.this.updateProfile(response);
                    } else {
                        UserProfile.this.launchErrorConnectingToServer();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    UserProfile.this.launchErrorConnectingToServer();
                }
            });
        }



    }

    public void launchErrorConnectingToServer(){
        Toast.makeText(UserProfile.this, "Error connecting to server.", Toast.LENGTH_LONG).show();
    }

    public void updateProfile(Response<User> response){
        User u = response.body();
        TextView userName = UserProfile.this.findViewById(R.id.user_name);
        userName.setText(u.username);

        TextView userDesc = UserProfile.this.findViewById(R.id.user_description);
        userDesc.setText(u.description);

        //TextView userFollowers = UserProfile.this.findViewById(R.id.user_number_followers);
        //userFollowers.setText(u.followers.size());

        //TextView userFollowing = UserProfile.this.findViewById(R.id.user_number_following);
        //userFollowing.setText(u.following.size());

        // Per el nombre de publicacions que tingui l'usuari.
        //TextView userPublications = UserProfile.this.findViewById(R.id.user_number_publications);
        //userFollowers.setText(u.publications.size());

        // Per posar la foto de perfil.
        ImageView profilePicture = UserProfile.this.findViewById(R.id.user_profile_picture);
        Picasso.get().load(u.profilePicture).into(profilePicture);
    }

}
