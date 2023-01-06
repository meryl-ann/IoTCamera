package com.example.iotcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ConfirmationActivity extends AppCompatActivity {

    //Declare button variable
    TextView confirmationText;
    TextView reasonText;
    Button proceed;
    Button scanAgain;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation_page);

        confirmationText = findViewById(R.id.confirmation_text);
        reasonText=findViewById(R.id.reason_text);

        scanAgain=findViewById(R.id.scan_again_button);

        MediaPlayer approve=MediaPlayer.create(this, R.raw.good);
        MediaPlayer deny= MediaPlayer.create(this, R.raw.error);

        Intent confirmation=getIntent();
        int mask_id=confirmation.getIntExtra("mask",1);
        String temp_string=confirmation.getStringExtra("temperature");

        int temp_int=Integer.parseInt(temp_string);

        //Evaluation

        if(mask_id==0 & temp_int<38) {
            approve.start();
            confirmationText.setText("Approved");
            confirmationText.setTextColor(Color.parseColor("#01ff00"));
            reasonText.setText("Looks good! Please proceed!");
        }
        else if(mask_id==1 & temp_int<38){
            deny.start();
            confirmationText.setText("Denied");
            confirmationText.setTextColor(Color.parseColor("#ff0000"));
            reasonText.setText("Please wear a mask!");
        }
        else if(mask_id==0 & temp_int>38){
            deny.start();
            confirmationText.setText("Denied");
            confirmationText.setTextColor(Color.parseColor("#ff0000"));
            reasonText.setText("You're burning up! Please seek medical attention!");
        }
        else if(mask_id==1 & temp_int>38){
            deny.start();
            confirmationText.setText("Denied");
            confirmationText.setTextColor(Color.parseColor("#ff0000"));
            reasonText.setText("Please wear a mask! You have a fever!");
        }

        //end Evaluation

        scanAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent retry=new Intent(ConfirmationActivity.this, MainActivity.class);
                startActivity(retry);
            }
        });


    }
}
