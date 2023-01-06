package com.example.iotcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ConfirmationActivity extends AppCompatActivity {

    //Declare button variable
    TextView confirmationText;
    TextView reasonText;
    Button proceed;
    Button scanAgain;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation_page);

        confirmationText = findViewById(R.id.confirmation_text);
        reasonText=findViewById(R.id.reason_text);
        proceed=findViewById(R.id.continue_button);
        scanAgain=findViewById(R.id.scan_again_button);
        //name button id

    }
}
