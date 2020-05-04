package org.udg.pds.todoandroid.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.ModifiedData;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModifyProfile extends AppCompatActivity {

    TodoApi mTodoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_profile);

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        Button ch = findViewById(R.id.Change);

        ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText f = ModifyProfile.this.findViewById(R.id.Mod_Foto);
                EditText n = ModifyProfile.this.findViewById(R.id.Mod_Name);
                EditText d = ModifyProfile.this.findViewById(R.id.Mod_Desc);
                ModifyProfile.this.modifyItems(f.getText().toString(),n.getText().toString(),d.getText().toString());
            }
        });


    }

    private void modifyItems(String foto, String name, String desc) {
        ModifiedData data = new ModifiedData();
        data.pic = foto;
        data.name = name;
        data.desc = desc;
        Call<String> callPic = mTodoService.modifyProfile(data);
        callPic.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    ModifyProfile.this.finish();
                } else {
                    Toast toast = Toast.makeText(ModifyProfile.this, "Error modify bad response", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast toast = Toast.makeText(ModifyProfile.this, "Error modify no response", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
