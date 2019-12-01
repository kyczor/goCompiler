package com.example.gocompiler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FixErrorsActivity extends AppCompatActivity {

    Intent intent;
    int line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fix_errors);

        intent = getIntent();
        line = intent.getIntExtra("lineNumber", 0);
        String[] filePaths = intent.getStringArrayExtra("filePaths");
        String filePath="";
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

        EditText et = findViewById(R.id.editCode);
        et.setText(text.toString());
        Button retryBtn = findViewById(R.id.tryAgainBtn);
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryAgain();
            }
        });
    }

    private void tryAgain()
    {
        intent.putExtra("key", RESULT_OK);
        setResult(500, intent);
        finish();
    }
}
