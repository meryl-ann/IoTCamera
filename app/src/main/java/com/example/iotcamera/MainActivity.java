package com.example.iotcamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iotcamera.ml.MaskDetector;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class MainActivity extends AppCompatActivity {

    // Define the pic id
    private static final int pic_id = 123;
    // Define the button and imageview type variable
    Button camera_open_id;
    Button temp_check_id;
    Button proceed;
    ImageView click_image_id;
    TextView result;
    TextView prediction;
    TextView temp;


    int imageSize=224;
    int maxPos=0;
    boolean mask_check=false;
    boolean temp_check=false;
    String output_value="";



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // By ID we can get each component which id is assigned in XML file get Buttons and imageview.
        camera_open_id = findViewById(R.id.camera_button);
        temp_check_id= findViewById(R.id.temp_button);
        click_image_id = findViewById(R.id.click_image);
        result= findViewById(R.id.result);
        prediction=findViewById(R.id.prediction);
        temp=findViewById(R.id.temp_value);
        proceed=findViewById(R.id.proceed_button);

        // Camera_open button is for open the camera and add the setOnClickListener in this button
        camera_open_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camera_intent,3);
                }
                else{
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }
            }
        });
        temp_check_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Integer, Void, Void>(){
                    @Override
                    protected Void doInBackground(Integer... params) {
                        run("python Desktop/Face-Mask-Detection-master/temp/temp_dataset.py");
                        // Add code to fetch data via SSH
                        return null;
                    }
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    protected void onPostExecute(Void v) {
                        temp.setText(output_value + "°C");
                        temp_check=true;


                        // Add code to preform actions after doInBackground
                    }
                }.execute(1);


            }
        });

        proceed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                System.out.println(mask_check);
                System.out.println(temp_check);

                if(mask_check&temp_check){
                    int mask_id=maxPos;
                    String temperature=output_value;
                    Intent confirmation=new Intent(MainActivity.this, ConfirmationActivity.class);
                    confirmation.putExtra("mask", mask_id);
                    confirmation.putExtra("temperature", temperature);
                    startActivity(confirmation);
                }
                else{
                    Context context=getApplicationContext();
                    CharSequence text="Please scan mask and temperature first!";
                    int duration= Toast.LENGTH_SHORT;
                    Toast error_popup=Toast.makeText(context,text,duration);
                    error_popup.show();

                }

            }
        });


    }

    public void run (String command) {
        String hostname = "192.168.208.117"; //Tanmay's Phone Hotspot (change for any other network
        String username = "pi";
        String password = "raspberry";

        StringBuilder output = new StringBuilder();

        try
        {
            Connection conn = new Connection(hostname); //init connection
            conn.connect(); //start connection to the hostname
            boolean isAuthenticated = conn.authenticateWithPassword(username,
                    password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");
            Session sess = conn.openSession();
            sess.execCommand(command);
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
//reads text
            while (true){
                String line = br.readLine(); // read line
                if (line == null)
                    break;
                output.append(line); //stores output in StringBuilder variable
                System.out.println(line);
            }
            output_value=output.toString(); //convert to string, used in function
            System.out.println("Output" + output_value); //debug

            /* Show exit status, if available (otherwise "null") */
            System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close(); // Close this session
            conn.close();
        }
        catch (IOException e)
        { e.printStackTrace(System.err);
            System.exit(2); }
    }

    public void classifyImage(Bitmap image){
        try {
            MaskDetector model = MaskDetector.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer=ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            Log.d("shape", byteBuffer.toString());
            Log.d("shape", inputFeature0.getBuffer().toString());

            int[] intValues= new int[imageSize*imageSize];
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel=0;
            for(int i=0;i<imageSize;i++){
                for(int j=0;j<imageSize;j++){
                    int val=intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat((val >> 0xFF) * (1.f / 255));

                }
            }


            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            MaskDetector.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] predictions =outputFeature0.getFloatArray();


            float maxPrediction=0;
            for(int i=0;i<predictions.length;i++){
                if(predictions[i]>maxPrediction){
                    maxPrediction=predictions[i];
                    maxPos=i;
                }
            }
            String[] labels={"Mask","No Mask"};
            result.setText(labels[maxPos]+" detected");
            prediction.setText(String.format("%2f", maxPrediction*100f));
            mask_check=true;


            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
            System.out.println("Classify image failed:" +e.getMessage());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                click_image_id.setImageBitmap(image);
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
}
