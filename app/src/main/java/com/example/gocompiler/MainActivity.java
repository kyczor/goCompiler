package com.example.gocompiler;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    final int ACT_2_REQUEST = 1;
    String[] filePaths;
    String dirPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dirPath = "test";

        Button act2b = findViewById(R.id.act2button);
        act2b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity2();
            }
        });

        Button dirBtn = findViewById(R.id.chooseDirBtn);
        dirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    startFilePicker();
                }
        });
    }

    private void startFilePicker()
    {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        FilePickerDialog fpd = new FilePickerDialog(this, properties);
        fpd.setTitle("Select a directory");
        fpd.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files = array of paths of files selected
                filePaths = files;
            }
        });

        fpd.show();
    }

    private void openActivity2()
    {
        Intent intent = new Intent(this, Activity1.class);
        intent.putExtra("path", dirPath);
        intent.putExtra("filePaths", filePaths);
        startActivityForResult(intent, ACT_2_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 9999:
                Log.i("Test", "Result URI " + data.getData());
                dirPath = String.valueOf(data.getData());
                break;
        }
    }
}
