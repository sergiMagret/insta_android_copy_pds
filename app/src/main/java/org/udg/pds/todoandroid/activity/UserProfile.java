package org.udg.pds.todoandroid.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfile extends AppCompatActivity {
    TodoApi mTodoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        Call<User> call = mTodoService.getUserProfile();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User u = response.body();
                    TextView userName = UserProfile.this.findViewById(R.id.user_name);
                    userName.setText(u.name);

                    TextView userDesc = UserProfile.this.findViewById(R.id.user_description);
                    userDesc.setText(u.description);

                    TextView userFollowers = UserProfile.this.findViewById(R.id.user_number_followers);
                    userFollowers.setText(u.followers.size());

                    TextView userFollowing = UserProfile.this.findViewById(R.id.user_number_following);
                    userFollowing.setText(u.following.size());

                    // Per el nombre de publicacions que tingui l'usuari.
                    //TextView userPublications = UserProfile.this.findViewById(R.id.user_number_publications);
                    //userFollowers.setText(u.publications.size());

                    // Per posar la foto de perfil.
                    //ImageView profilePicture = UserProfile.this.findViewById(R.id.user_profile_picture);
                    //profilePicture.setImageURI(); HOW TO ???
                    // Quina funcio del set s'ha de fer servir?

                    // Per posar les publicacions
                    TableLayout tl = UserProfile.this.findViewById(R.id.user_publications);
                } else {
                    Toast.makeText(UserProfile.this, "Error loading user profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(UserProfile.this, "Error loading user profile", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
