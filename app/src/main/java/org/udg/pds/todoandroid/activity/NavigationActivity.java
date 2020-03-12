package org.udg.pds.todoandroid.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.fragment.FavoritesFragment;
import org.udg.pds.todoandroid.fragment.TaskList;
import org.udg.pds.todoandroid.rest.TodoApi;

// FragmentActivity is a base class for activities that want to use the support-based Fragment and Loader APIs.
// http://developer.android.com/reference/android/support/v4/app/FragmentActivity.html
public class NavigationActivity extends AppCompatActivity {

    private TaskList mTaskList;

    TodoApi mTodoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
            item -> {
                switchView(item.getItemId());
                return true;
            });

        switchView(bottomNavigationView.getSelectedItemId());
    }

    private void switchView(int itemId) {
        final FrameLayout content = findViewById(R.id.main_content);
        switch (itemId) {
            case R.id.action_favorites:
                content.removeAllViews();
                getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content, new FavoritesFragment())
                    .commit();
                break;
            case R.id.action_schedules:
                content.removeAllViews();
                getLayoutInflater().inflate(R.layout.content_schedules, content);
                break;
            case R.id.action_tasks:
                content.removeAllViews();
                getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content, new TaskList())
                    .commit();
                break;
        }
    }
}
