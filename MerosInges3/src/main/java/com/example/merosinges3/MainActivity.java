package com.example.merosinges3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.merosinges3.MESSAGE";
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user taps the Send button */
    public void seeRadiation(View view) {
        this.view = view;
        // Do something in response to button
        /* Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);*/
        Intent intent = new Intent(this, ShowUvIndex.class);
        startActivity(intent);
    }

    public void seePhototype(View view){
        Intent intent = new Intent(this, ShowPhototype.class);
        startActivity(intent);
    }

    public void seeNotifications(View view){
        Intent intent = new Intent(this, Notifications.class);
        startActivity(intent);
    }

    public void seeProduct(View view){
        Intent intent = new Intent(this, ProductInfo.class);
        startActivity(intent);
    }
}