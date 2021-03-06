package com.example.remoteCompiler;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class DisplayErrorsActivity extends AppCompatActivity {
    Intent prevIntent;
    String dirPath;
    String flags;
    String mainFile;
    ArrayList<String> filePaths;
    AsyncTask<Integer, Void, Void> executePostReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_errors);

        prevIntent = getIntent();
        dirPath = prevIntent.getStringExtra(String.valueOf(R.string.path_intent));
        flags = prevIntent.getStringExtra(String.valueOf(R.string.flags_intent));
        mainFile = prevIntent.getStringExtra(String.valueOf(R.string.main));
        filePaths = prevIntent.getStringArrayListExtra(String.valueOf(R.string.file_paths_intent));

        Button backB = findViewById(R.id.goBackFromErrors);
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
            successTV.setTextSize(40);
            successTV.setTextColor(getResources().getColor(R.color.gradient1));
            successTV.setTypeface(null, Typeface.BOLD);
            successTV.setVisibility(View.VISIBLE);
            TextPaint paint = successTV.getPaint();
            float width = paint.measureText(String.valueOf(R.string.comp_success));

            Shader textShader = new LinearGradient(0,0,width*1.10f,100,
                    new int[]{getResources().getColor(R.color.gradient1),
                            getResources().getColor(R.color.gradient2),
                            getResources().getColor(R.color.gradient3)},
                    null, Shader.TileMode.CLAMP);
            successTV.getPaint().setShader(textShader);
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
            params.setMargins(5,10,5,10);
            row.setLayoutParams(params);
            Button lineBtn = (Button)getLayoutInflater().inflate(R.layout.buttontemplate, null);
            StringBuilder sb = new StringBuilder();
            String[] filenameParts = errParts[0].split("/");
            sb.append(filenameParts[filenameParts.length-1]);
            sb.append("\n");
            sb.append("Line: ");
            sb.append(errParts[1]);
            lineBtn.setText(sb.toString());
            lineBtn.setTextSize(10);
            row.addView(lineBtn);
            final TextView itemText = new TextView(this);
            itemText.setText(errParts[4]);
            itemText.setPadding(10,0,10,0);
            row.addView(itemText);

            lineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button)v;
                    String textToIntent = (String) itemText.getText();
                    String[] whereToGo = b.getText().toString().split("\n");
                    String[] lineNoInfo = whereToGo[1].split(" ");
                    int lineNumToIntent = Integer.parseInt(lineNoInfo[1]);
                    String filename = whereToGo[0];
                    createFixIntent(textToIntent, lineNumToIntent, filename);
                }
            });
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
            b64encoding = "\"" + Base64.encodeToString(bytes, 0, length, Base64.NO_WRAP) + "\"";

        } catch (IOException e) {
            e.printStackTrace();
        }

        return b64encoding;
    }

    private void sendPostRequest() throws IOException {
        String base64files = encodeAllFiles();
        String fileNames = fileNamesToString();
        //String base64file = encodeB64File(filePaths[0]);
        //String postJson = "{\"encode\": \"" + base64file + "\"}";
        String postJson =
                "{\"encode\": [" + base64files + "], " +
                "\"filenames\": [" + fileNames + "], " +
                "\"mainfile\": \"" + mainFile + "\", " +
                "\"flags\": \"" + flags + "\"}";

        System.out.println("POST JSON: " + postJson);
        URL url = new URL("http://54.80.215.77:8014/b64");
        // URL url = new URL("http://192.168.0.14:8014/b64");

        HttpURLConnection client = (HttpURLConnection) url.openConnection();
        client.setRequestMethod("POST");
        client.setRequestProperty("Content-Type", "application/json; utf-8");
        client.setRequestProperty("Accept", "application/json");
        client.setDoOutput(true);
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
    }

    private String encodeAllFiles()
    {
        StringBuilder sb = new StringBuilder();
        for(int fileId = 0; fileId<filePaths.size(); fileId++)
        {
            sb.append(encodeB64File(filePaths.get(fileId)));
            sb.append(", ");
        }
        return sb.substring(0, sb.length()-2);
    }

    private String fileNamesToString()
    {
        StringBuilder sb = new StringBuilder();
        for(int fileId = 0; fileId<filePaths.size(); fileId++)
        {
            String[] parts = filePaths.get(fileId).split("/");
            sb.append("\"");
            sb.append(parts[parts.length-1]);   //wez ostatnia czastke sciezki, czyli nazwe pliku
            sb.append("\", ");
        }
        return sb.substring(0, sb.length()-2);
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
            if(Pattern.compile(".+[a-zA-Z]+(.c:)\\d+(.)\\d+(: error: ).*").matcher(err).matches())
            {
                parsed.add(err);
            }
        }
        errorsAndWarnings.add(parsed.toArray(new String[0]));

        parsed = new ArrayList<>();
        for(String err : temp)
        {
            //sprawdz czy to ma forme warninga
            if(Pattern.compile(".+[a-zA-Z]+(.c:)\\d+(.)\\d+(: warning: ).*").matcher(err).matches())
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
        return gson.fromJson(jsonOutputString, ErrorsData.class);
    }

    private void createFixIntent(String textToIntent, int lineNumToIntent, String fileName){
        //start new activity and pass line number
        Intent intent = new Intent(this, FixErrorsActivity.class);
        intent.putExtra("filePaths", filePaths);
        intent.putExtra("displayMsg", textToIntent);
        intent.putExtra("lineNumber", lineNumToIntent);
        intent.putExtra("fileName", fileName);
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
