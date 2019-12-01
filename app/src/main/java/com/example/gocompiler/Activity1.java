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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Activity1 extends AppCompatActivity implements View.OnClickListener {
    Intent prevIntent;
    String dirPath;
    String[] filePaths;
    AsyncTask<Integer, Void, Void> executePostReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1);

        prevIntent = getIntent();
        dirPath = prevIntent.getStringExtra("path");
        filePaths = prevIntent.getStringArrayExtra("filePaths");

        Button backB = findViewById(R.id.cancelFilePick);
        backB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        startPostReqProcess();
    }

    private void displayErrorButtons(String[] errorsParsed, String[] warningsParsed)
    {
        if(errorsParsed.length == 0 && warningsParsed.length == 0)
        {
            TextView successTV = findViewById(R.id.successTV);
            successTV.setText(R.string.comp_success);
            successTV.setVisibility(View.VISIBLE);
            return;
        }

        LinearLayout wrapperLayout = findViewById(R.id.errorDisplayLayout);
        wrapperLayout.setVisibility(View.VISIBLE);

        if(errorsParsed.length != 0)
        {
            LinearLayout eLayout = findViewById(R.id.errorButtonsLayout);
            addItemsToDisplay(eLayout, errorsParsed);
        }

        if(warningsParsed.length != 0)
        {
            LinearLayout wLayout = findViewById(R.id.warningButtonsLayout);
            addItemsToDisplay(wLayout, warningsParsed);
        }

    }

    private void addItemsToDisplay(LinearLayout layout, String[] items)
    {
        for(String item : items)
        {
            //nazwa_pliku, linia, znak, "error" / "warning", tresc bledu
            String[] errParts = item.split(":");

            //create new horizontal linear layout
            // and add line number as a Button
            // and error message as a TextView
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(params);
            Button lineBtn = new Button(this);
            lineBtn.setOnClickListener(this);
            lineBtn.setText(errParts[1]);
            row.addView(lineBtn);
            TextView itemText = new TextView(this);
            itemText.setText(errParts[4]);
            itemText.setPadding(10,0,10,0);
            row.addView(itemText);

            //add the item to our layout
            layout.addView(row);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void startPostReqProcess()
    {
        executePostReq = new AsyncTask<Integer, Void, Void>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    sendPostRequest();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1);
        try {
            executePostReq.get(1000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
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

    private void sendPostRequest() throws IOException {
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
        //String jsonInputString = "{\"title\": \"Miss\",\"name\": \"Karolina\", \"numbers\": {\"age\": 23, \"foot\": 42}}";
        try(OutputStream os = client.getOutputStream()) {
            byte[] input = postJson.getBytes(StandardCharsets.UTF_8);
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
            ErrorsData errData = decodeRes(response.toString());
            ArrayList<String[]> responseParsed = parseErrors(errData.getErrorsList());
            String[] errorsParsed = responseParsed.get(0);
            String[] warningsParsed = responseParsed.get(1);
            displayErrorButtons(errorsParsed, warningsParsed);
        }
        return;
    }

    /**
     * Funkcja skanująca kazda linie z outputu z gcc i zwracająca tylko liste errorow
     *
     * @param errorsList caly output z gcc
     * @return same errory
     */
    private ArrayList<String[]> parseErrors(String errorsList)
    {
        ArrayList<String[]> errorsAndWarnings = new ArrayList<>();

        String[] temp = errorsList.split("\n");
        List<String> parsed = new ArrayList<>();
        for(String err : temp)
        {
            //sprawdz czy to ma forme erroru
            if(Pattern.compile("[a-zA-Z]+(.c:)\\d+(.)\\d+(: error: ).*").matcher(err).matches())
            {
                parsed.add(err);
            }
        }
        errorsAndWarnings.add(parsed.toArray(new String[0]));

        parsed = new ArrayList<>();
        for(String err : temp)
        {
            //sprawdz czy to ma forme erroru
            if(Pattern.compile("[a-zA-Z]+(.c:)\\d+(.)\\d+(: warning: ).*").matcher(err).matches())
            {
                parsed.add(err);
            }
        }
        errorsAndWarnings.add(parsed.toArray(new String[0]));

        return errorsAndWarnings;
    }

    /**
     * Funkcja odkodowująca Json zwrocony przez serwer
     * i zwracajaca pelna liste bledow wraz z ich opisami
     *
     * @param response server response
     * @return tresci bledow
     * @throws UnsupportedEncodingException
     */
    private ErrorsData decodeRes(String response) throws UnsupportedEncodingException {
        byte[] jsonOutput = Base64.decode(response, Base64.NO_WRAP);
        String jsonOutputString = new String(jsonOutput, "UTF-8");
        Gson gson = new Gson();
        ErrorsData errData = gson.fromJson(jsonOutputString, ErrorsData.class);
        return errData;
    }

    @Override
    public void onClick(View v) {
        Button b = (Button)v;
        int lineNum = Integer.parseInt(b.getText().toString());

        //start new activity and pass line number
        Intent intent = new Intent(this, FixErrorsActivity.class);
        intent.putExtra("filePaths", filePaths);
        intent.putExtra("lineNumber", lineNum);
        startActivityForResult(intent, 500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        prevIntent.putExtra("key", RESULT_OK);
        setResult(500, prevIntent);
        finish();
}
}
