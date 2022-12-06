package com.example.merosinges3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Notifications extends AppCompatActivity {

    Button notify_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        notify_btn = findViewById(R.id.btn_notify);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("MyNotification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        notify_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(Notifications.this, "MyNotification");
                builder.setContentTitle("No olvides reaplicarte!");
                builder.setContentText("Bien ahí por usar bloqueador! Te recordaremos...");
                builder.setSmallIcon(R.drawable.meros_inges);
                builder.setAutoCancel(true);
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Bien ahí por usar bloqueador! Te recordaremos que lo reapliques dentro de dos horas :)"));

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(Notifications.this);
                managerCompat.notify(69, builder.build());
            }
        });
    }
}