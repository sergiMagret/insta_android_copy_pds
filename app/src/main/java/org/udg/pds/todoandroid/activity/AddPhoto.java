package org.udg.pds.todoandroid.activity;
import android.app.Activity;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ser.Serializers;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Publication;
import org.udg.pds.todoandroid.entity.PublicationPost;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    private Uri selectedImage = null ;
    private String imatge;
    private static final int SELECT_FILE  = 1;
    TodoApi mTodoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        Button add = findViewById(R.id.add_button);
        Button choose = findViewById(R.id.choose);

        choose.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Choose a Photo"), SELECT_FILE);
            }
        });

        add.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                EditText c = AddPhoto.this.findViewById(R.id.comentari);
                AddPhoto.this.afegir(c.getText().toString());
            }
        });
    }

    public void afegir (String comentari){
        PublicationPost p= new PublicationPost();
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

            p.photo = null;

            Call<String> call1 = mTodoService.uploadImage(body);
            call1.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Call<String> call2 = mTodoService.postPublication(p);
                    call2.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful()) {
                                AddPhoto.this.startActivity(new Intent(AddPhoto.this, NavigationActivity.class));
                                AddPhoto.this.finish();
                            } else {
                                Toast toast = Toast.makeText(AddPhoto.this, "Error AddPhoto bad response", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast toast = Toast.makeText(AddPhoto.this, "Error addPhoto no response", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
        catch (Exception e){
            Toast toast = Toast.makeText(AddPhoto.this, e.getMessage(), Toast.LENGTH_SHORT);
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
                            i = (ImageView) findViewById(R.id.imatge);
                            selectedImage = imageReturnedIntent.getData();
                            i.setImageURI(selectedImage);
                        }
                    }
                }
        }
    }
}
