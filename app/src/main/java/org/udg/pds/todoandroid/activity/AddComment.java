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
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.entity.UserLogin;
import org.udg.pds.todoandroid.fragment.TimelineFragment;
import org.udg.pds.todoandroid.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddComment extends AppCompatActivity {

    TodoApi mTodoService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_comment_layout);
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        Bundle b = getIntent().getExtras();
        Long publicationId = b.getLong("id");
        //Toast.makeText(getApplicationContext(), "id: " + publicationId, Toast.LENGTH_LONG).show();
    }
}
