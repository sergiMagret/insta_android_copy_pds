package org.udg.pds.todoandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.fragment.SearchFragment;
import org.udg.pds.todoandroid.fragment.TaskList;
import org.udg.pds.todoandroid.fragment.TimelineFragment;
import org.udg.pds.todoandroid.fragment.UserProfileFragment;
import org.udg.pds.todoandroid.rest.TodoApi;

// FragmentActivity is a base class for activities that want to use the support-based Fragment and Loader APIs.
// http://developer.android.com/reference/android/support/v4/app/FragmentActivity.html
public class NavigationActivity extends AppCompatActivity {

    private TaskList mTaskList;

    private BottomNavigationView bottomNavigationView;

    TodoApi mTodoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setUpNavigation();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
            item -> {
                switchView(item.getItemId());
                return true;
            });

        switchView(bottomNavigationView.getSelectedItemId());
    }

    private void switchView(int itemId) { //Definim que fa quan s'apreten els botons home, add, search i profile del menu
        final FrameLayout content = findViewById(R.id.main_content);
        switch (itemId) {
           /* case R.id.action_home: //On anem quan s'apreta home
                content.removeAllViews();
                getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content, new TimelineFragment())
                    .commit();
                break;*/
            case R.id.action_add://On anem quan s'apreta add
                content.removeAllViews();
                NavigationActivity.this.startActivity(new Intent(NavigationActivity.this, AddPhoto.class));
                break;
        }
    }

    public void setUpNavigation(){
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView,navHostFragment.getNavController());
    }
}
