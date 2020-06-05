package org.udg.pds.todoandroid.activity;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import org.apache.commons.io.IOUtils;
import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.ModifiedData;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModifyProfile extends AppCompatActivity {

    TodoApi mTodoService;
    private ImageView i;
    private Uri selectedImage = null ;
    private String imatge;
    private static final int SELECT_FILE  = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_profile);

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        Button ch = findViewById(R.id.Change);
        Button choose = findViewById(R.id.choose);

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Choose a Photo"), SELECT_FILE);
            }
        });

        ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = ModifyProfile.this.findViewById(R.id.Mod_Foto);
                EditText n = ModifyProfile.this.findViewById(R.id.Mod_Name);
                EditText d = ModifyProfile.this.findViewById(R.id.Mod_Desc);
                ModifyProfile.this.modifyItems(n.getText().toString(),d.getText().toString());
            }
        });


    }

    private void modifyItems(String name, String desc) {
        ModifiedData data = new ModifiedData();
        data.name = name;
        data.desc = desc;
        try {
            InputStream is = getContentResolver().openInputStream(selectedImage);
            String extension = "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(selectedImage));
            File tempFile = File.createTempFile("upload", extension, getCacheDir());
            FileOutputStream outs = new FileOutputStream(tempFile);
            IOUtils.copy(is, outs);
            RequestBody requestFile =
                RequestBody.create(
                    MediaType.parse(getContentResolver().getType(selectedImage)),
                    tempFile
                );
            MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

            Call<String> call = mTodoService.uploadImage(body);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        data.pic = response.body();
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
                    else
                        Toast.makeText(ModifyProfile.this, "Response error !", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(ModifyProfile.this, "Failure !", Toast.LENGTH_SHORT).show();
                }
            });


        }
        catch (Exception e){
            Toast toast = Toast.makeText(ModifyProfile.this, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode,resultCode,imageReturnedIntent);
        Uri selectedImageUri = null;

        String filePath = null;
        switch(requestCode){
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK){
                    selectedImage = imageReturnedIntent.getData();
                    String selectedPath = selectedImage.getPath();
                    if(requestCode == SELECT_FILE){
                        if (selectedPath != null){
                            InputStream imageStream = null;
                            try{
                                imageStream = getContentResolver().openInputStream(selectedImage);
                            } catch(FileNotFoundException e){
                                e.printStackTrace();
                            }
                            i = (ImageView) findViewById(R.id.Mod_Foto);
                            selectedImage = imageReturnedIntent.getData();
                            i.setImageURI(selectedImage);
                        }
                    }
                }
        }
    }
}
