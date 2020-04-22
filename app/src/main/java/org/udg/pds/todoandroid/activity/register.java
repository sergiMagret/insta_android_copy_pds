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
import org.udg.pds.todoandroid.entity.UserToReg;
import org.udg.pds.todoandroid.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class register extends AppCompatActivity {

    TodoApi mTodoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        Button reg = findViewById(R.id.RegButton);

        reg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText u = register.this.findViewById(R.id.Reg_usuari);
                EditText e = register.this.findViewById(R.id.Reg_email);
                EditText p = register.this.findViewById(R.id.Reg_password);
                EditText c = register.this.findViewById(R.id.Reg_Check);
                register.this.checkCredentials(u.getText().toString(),e.getText().toString(), p.getText().toString(),c.getText().toString());
            }
        });
    }

    public void checkCredentials(String username,String email, String password, String check) {

        if(check.equals(password)){
            UserToReg user = new UserToReg();
            user.username = username;
            user.email = email;
            user.password = password;
            Call<User> call = mTodoService.register(user);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        register.this.startActivity(new Intent(register.this, NavigationActivity.class));
                        register.this.startActivity(new Intent(register.this, ModifyProfile.class));
                        register.this.finish();
                    } else {
                        Toast toast = Toast.makeText(register.this, "Error register bad response", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast toast = Toast.makeText(register.this, "Error register no response", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
        else{
            Toast toast = Toast.makeText(register.this, "Passwords don't match", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

}
