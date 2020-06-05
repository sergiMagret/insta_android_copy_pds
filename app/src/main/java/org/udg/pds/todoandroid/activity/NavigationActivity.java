package org.udg.pds.todoandroid.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.udg.pds.todoandroid.MyFirebaseMessagingService;
import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// FragmentActivity is a base class for activities that want to use the support-based Fragment and Loader APIs.
// http://developer.android.com/reference/android/support/v4/app/FragmentActivity.html
public class NavigationActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    TodoApi mTodoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setUpNavigation();
        mTodoService = ((TodoApp)getApplication()).getAPI();
        MyFirebaseMessagingService messagingService = new MyFirebaseMessagingService(mTodoService);
        messagingService.sendRegistrationToServer();
    }


    @Override
    protected void onResume() {
        /* When returning to the main activity (that means returning to any of the fragments that
         * are shown inside this activity) ask for the user's profile to keep the variable
         * TodoApp.loggedUserID updated, and to check if there's connection with the server.
         * Also the FCM token is sent to the server.*/
        super.onResume();

        Call<User> call = mTodoService.getUserProfile();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful() && response.body() != null){
                    TodoApp.loggedUserID = response.body().id;
                }else{
                    Toast.makeText(NavigationActivity.this, "Error reading profile information.", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(NavigationActivity.this, "Server not responding. Closing app...", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        // Send token to server
        MyFirebaseMessagingService messagingService = new MyFirebaseMessagingService(mTodoService);
        messagingService.sendRegistrationToServer();
    }

    public void setUpNavigation(){
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView,navHostFragment.getNavController());
    }
}
