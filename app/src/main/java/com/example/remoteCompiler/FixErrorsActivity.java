package com.example.remoteCompiler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FixErrorsActivity extends AppCompatActivity {

    Intent intent;
    int cursorLine;
    EditText et;
    String filePath;
    String fileName;

    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fix_errors);

        intent = getIntent();
        cursorLine = intent.getIntExtra("lineNumber", 0);
        ArrayList<String> filePaths = intent.getStringArrayListExtra("filePaths");
        fileName = intent.getStringExtra("fileName");
        if (filePaths != null) {
            filePath = findFilePath(filePaths);
        }

        String msgText = intent.getStringExtra("displayMsg");
        TextView msgTV = findViewById(R.id.displayMsg);
        msgTV.setText(msgText);

        //read whole text from file
        File file = new File(filePath);
        StringBuilder text = new StringBuilder();
        int cursorIndex=0;
        try
        {
            int currLine = 1;
            int index = 0;
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while(line != null)
            {
                if(currLine == cursorLine)
                {
                    cursorIndex = index + currLine;
                }
                else
                {
                    currLine++;
                    index += line.length();
                }
                text.append(line);
                text.append("\n");
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        et = findViewById(R.id.editCode);
        et.setText(text.toString());
        et.requestFocus();
        et.setSelection(cursorIndex);

        Button cancelBtn = findViewById(R.id.cancelFix);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryAgain();
            }
        });

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

        myCheckPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE);
    }

    private String findFilePath(ArrayList<String> filePaths)
    {
        for(String path : filePaths)
        {
            String[] check = path.split("/");
            //porownaj ostatni czlon sciezki - nazwe pliku
            if(fileName.equals(check[check.length-1]))
            {
                return path;
            }
        }
        return "nope!";
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

    // Function to check and request permission.
    public void myCheckPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(FixErrorsActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(FixErrorsActivity.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            Toast.makeText(FixErrorsActivity.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void tryAgain()
    {
        intent.putExtra("key", RESULT_OK);
        setResult(500, intent);
        finish();
    }
}
