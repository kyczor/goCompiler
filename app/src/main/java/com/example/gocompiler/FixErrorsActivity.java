package com.example.gocompiler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FixErrorsActivity extends AppCompatActivity {

    Intent intent;
    int line;
    EditText et;
    String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fix_errors);

        intent = getIntent();
        line = intent.getIntExtra("lineNumber", 0);
        String[] filePaths = intent.getStringArrayExtra("filePaths");
        if (filePaths != null) {
            filePath = filePaths[0];
        }

        //read whole text from file
        File file = new File(filePath);
        StringBuilder text = new StringBuilder();
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while(line != null)
            {
                text.append(line);
                text.append("\n");
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        et = findViewById(R.id.editCode);
        et.setText(text.toString());
        Button retryBtn = findViewById(R.id.tryAgainBtn);
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryAgain();
                try {
                    updateFile(et.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Funkcja przyjmujaca nowa tresc pliku
     * i nadpisujaca wczesniej skompilowany plik
     *
     * @param newText nowa tresc pliku
     * @throws IOException
     */
    private void updateFile(String newText) throws IOException {
        FileOutputStream out = new FileOutputStream(filePath);
        byte[] contents = newText.getBytes();
        out.write(contents);
        out.flush();
        out.close();
    }

    private void tryAgain()
    {
        intent.putExtra("key", RESULT_OK);
        setResult(500, intent);
        finish();
    }
}
