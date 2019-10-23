package com.example.gocompiler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    final int ACT_2_REQUEST = 1;
    final int RESULT_OK = 14;

    TextView tv;
    String strFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button fab = findViewById(R.id.fab);
        Button act2b = findViewById(R.id.act2button);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view) {
                new AsyncTask<Integer, Void, Void>(){
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @SuppressLint("WrongThread")
                    @Override
                    protected Void doInBackground(Integer... params) {
                        try {
                            TextView tv = findViewById(R.id.infoTV);
                            tv.setText(sendPostRequest());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute(1);
            }
        });

        act2b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity2();
            }
        });
    }

    private void openActivity2()
    {
        Intent intent = new Intent(this, Activity1.class);
        startActivityForResult(intent, ACT_2_REQUEST);
    }

    private String sendPostRequest() throws IOException {
        //zipFiles();
        URL url = new URL("http://192.168.0.13:8014");
        HttpURLConnection client = (HttpURLConnection) url.openConnection();
        client.setRequestMethod("POST");
        client.setRequestProperty("Content-Type", "application/json; utf-8");
        client.setRequestProperty("Accept", "application/json");
        client.setDoOutput(true);
        String jsonInputString = "{\"title\": \"Miss\",\"name\": \"Karolina\", \"numbers\": {\"age\": 23, \"foot\": 42}}";
        try(OutputStream os = client.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
            //unzipFiles();
            return(response.toString());
        }
    }

    private void zipFiles()
    {

    }

    private void unzipFiles()
    {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
