package org.udg.pds.todoandroid.activity;

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

import androidx.appcompat.app.AppCompatActivity;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.PublicationPost;
import org.udg.pds.todoandroid.rest.TodoApi;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.IOUtils;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPhoto extends AppCompatActivity {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private ImageView i;
    private Uri selectedImage = null;
    private String imatge;
    private static final int SELECT_FILE = 1;
    TodoApi mTodoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        Button add = findViewById(R.id.add_button);
        Button choose = findViewById(R.id.choose);

        choose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Choose a Photo"), SELECT_FILE);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText c = AddPhoto.this.findViewById(R.id.comentari);
                AddPhoto.this.afegir(c.getText().toString());
            }
        });
    }

    public void afegir(String comentari) {
        PublicationPost p = new PublicationPost();
        p.description = comentari;
        p.date = new Date();

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
                        p.photo = response.body();
                        Call<Long> call2 = mTodoService.postPublication(p);
                        call2.enqueue(new Callback<Long>() {
                            @Override
                            public void onResponse(Call<Long> call, Response<Long> response) {
                                if (response.isSuccessful()) {
                                    Intent intent = new Intent(AddPhoto.this, TagPeople.class);
                                    Bundle b = new Bundle();
                                    b.putLong("id", response.body());
                                    intent.putExtras(b);
                                    startActivity(intent);
                                } else {
                                    Toast toast = Toast.makeText(AddPhoto.this, "Error AddPhoto bad response", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Long> call, Throwable t) {
                                Toast toast = Toast.makeText(AddPhoto.this, "Error addPhoto no response", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    } else
                        Toast.makeText(AddPhoto.this, "Response error !", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(AddPhoto.this, "Failure !", Toast.LENGTH_SHORT).show();
                }
            });


        } catch (Exception e) {
            Toast toast = Toast.makeText(AddPhoto.this, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Uri selectedImageUri = null;

        String filePath = null;

        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = imageReturnedIntent.getData();
                    String selectedPath = selectedImage.getPath();
                    if (requestCode == SELECT_FILE) {
                        if (selectedPath != null) {
                            InputStream imageStream = null;
                            try {
                                imageStream = getContentResolver().openInputStream(selectedImage);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            i = (ImageView) findViewById(R.id.imatge);
                            selectedImage = imageReturnedIntent.getData();
                            i.setImageURI(selectedImage);
                        }

                    }
                }
                break;
        }
    }
}
