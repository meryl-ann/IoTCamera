package com.example.iotcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;

public class IntroPageActivity extends AppCompatActivity {

    //Declare button variable
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_page);

        //name button id
        btnContinue = findViewById(R.id.btnContinue);

        //Add clicklistener to button
        btnContinue.setOnClickListener(v -> {
            //Once the button is pressed, move to the next screen
            Intent nextPage = new Intent(IntroPageActivity.this, MainActivity.class);

            // Start the result Intent
            startActivity(nextPage);
        });
    }
}