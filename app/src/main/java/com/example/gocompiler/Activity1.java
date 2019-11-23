package com.example.gocompiler;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Activity1 extends AppCompatActivity {
    Intent prevIntent;
    String dirPath;
    String[] filePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1);

        prevIntent = getIntent();
        dirPath = prevIntent.getStringExtra("path");
        filePaths = prevIntent.getStringArrayExtra("filePaths");
        TextView tv2 = findViewById(R.id.textView2);
        tv2.setText(Arrays.toString(filePaths));

        Button backB = findViewById(R.id.cancelFilePick);
        backB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button fab = findViewById(R.id.fab);
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
                            sendPostRequest(tv);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute(1);
            }
        });
    }

    private String encodeB64File(String filePath) {
        File file = new File(filePath);
        String b64encoding = "";

        try {
            byte[] bytes = new byte[(int)file.length() + 100];
            int length = new FileInputStream(file).read(bytes);
            b64encoding = Base64.encodeToString(bytes, 0, length, Base64.NO_WRAP);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return b64encoding;
    }

    private void sendPostRequest(TextView tv) throws IOException {
        String base64file = encodeB64File(filePaths[0]);
        String postJson = "{\"encode\": \"" + base64file + "\"}";
        String postJson2 = "{\"encode\": \"" + "yaaaay" + "\"}";

        System.out.println("POST JSON: " + postJson);
        URL url = new URL("http://192.168.0.11:8014/b64");
        HttpURLConnection client = (HttpURLConnection) url.openConnection();
        client.setRequestMethod("POST");
        client.setRequestProperty("Content-Type", "application/json; utf-8");
        client.setRequestProperty("Accept", "application/json");
        client.setDoOutput(true);
        String jsonInputString = postJson;
        //String jsonInputString = "{\"title\": \"Miss\",\"name\": \"Karolina\", \"numbers\": {\"age\": 23, \"foot\": 42}}";
        try(OutputStream os = client.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            //odbierz odpowiedz z serwera
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            //wypisz w konsoli - ok lub errory
            //System.out.println(response.toString());
            tv.setText(response.toString());
        }
        return;
    }
}
