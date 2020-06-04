package org.udg.pds.todoandroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.udg.pds.todoandroid.activity.Login;
import org.udg.pds.todoandroid.activity.NavigationActivity;
import org.udg.pds.todoandroid.entity.Token;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private String CHANNEL_ID="channel" ;
    private TodoApi mTodoService;

    public MyFirebaseMessagingService() {
        //createNotificationChannel();
    }

    public MyFirebaseMessagingService(TodoApi api){
        mTodoService = api;
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage != null) {
            Log.d("msg", "onMessageReceived: " + remoteMessage.getData().get("message"));
            Intent intent = new Intent(this, NavigationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);


            NotificationCompat.Builder builder = new  NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                .setContentText(remoteMessage.getNotification().getBody()).setAutoCancel(true).setContentIntent(pendingIntent);;
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Instagram", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("PDS");
                manager.createNotificationChannel(channel);
            }
            manager.notify(0, builder.build());
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        TodoApp.tokenFCM = token;
        Log.d(TAG, "TodoApp.tokenFCM updated: " + TodoApp.tokenFCM);
        sendRegistrationToServer();
    }




    public void sendRegistrationToServer() {
        updateToken(); // Get/update the token the user has
        String token = TodoApp.tokenFCM; // Update the local saved token

        Log.d("FIREBASE", "Token sent: " + token);
        Call<String> call = mTodoService.sendToken(new Token(token)); // Send the token to the server to keep it updated
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    Log.d("FIREBASE", "token updated: " + response.body());
                }else{
                    Log.e("FIREBASE", "token update NOT SUCCESSFUL");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("FIREBASE", "token update FAILED");
            }
        });
    }

    public void updateToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if(task.isSuccessful() && task.getResult() != null){
                        TodoApp.tokenFCM = task.getResult().getToken();
                        Log.d("FIREBASE","Token updated correctly");
                    }else{
                        Log.e("FIREBASE", "Error updating the FCM token" + task.getException());
                    }
                }
            });
    }

}
