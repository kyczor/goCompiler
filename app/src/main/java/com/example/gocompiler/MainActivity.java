package com.example.gocompiler;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    final int ACT_2_REQUEST = 1;
    String[] filePaths;
    HashMap<String, Integer> selectedItems;
    TextView dirTV;
    Button errorListBtn;
    String flags = " -Wall";
    List<String> choicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedItems = new HashMap<>();
        choicesList = Arrays.asList(getResources().getStringArray(R.array.flags));

        dirTV = findViewById(R.id.dirTV);
        dirTV.setText(R.string.path);

        Button dirBtn = findViewById(R.id.chooseDirBtn);
        dirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFilePicker();
            }
        });

        Button flagsBtn = findViewById(R.id.flagsBtn);
        flagsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayFlagOptions();
            }
        });

        errorListBtn = findViewById(R.id.sendSrvBtn);
        errorListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityDispErr();
            }
        });
        errorListBtn.setClickable(false);
    }

    private void displayFlagOptions()
    {
        int selectedItemsLength = getResources().getStringArray(R.array.flags).length;
        boolean[] boolSelItems = new boolean[selectedItemsLength];
        Iterator it = selectedItems.entrySet().iterator();
        //for each of the previously selected items tick the flag "on"
        while(it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            boolSelItems[(int) pair.getValue()] = true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_flags);
        builder.setMultiChoiceItems(R.array.flags, boolSelItems,
                new DialogInterface.OnMultiChoiceClickListener() {

                    /**
                     * Every time we tick or un-tick any flag,
                     * the list of chosen flags is being updated
                     *
                     * @param dialog Dialog window that displays flag options
                     * @param which chosen flag's id
                     * @param isChecked state of the flag
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        String whichKey = choicesList.get(which);

                        if(isChecked)
                        {
                            selectedItems.put(whichKey,which);
                        }
                        else if(selectedItems.containsKey(whichKey))
                        {
                            selectedItems.remove(whichKey);
                        }
                    }
                });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            // get all the checked flags
            StringBuilder addToFlags = new StringBuilder();
            Iterator it = selectedItems.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                addToFlags.append(" ");
                addToFlags.append(pair.getKey());
            }

            //update the flags' string
            flags = "-Wall" + addToFlags.toString();
        }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
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
        fpd.setTitle("Select a file:");
        fpd.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files = array of paths of files selected
                filePaths = files;
                errorListBtn.setClickable(true);
                dirTV.setText(Arrays.toString(filePaths));
            }
        });

        fpd.show();
    }

    private void openActivityDispErr()
    {
        Intent intent = new Intent(this, DisplayErrorsActivity.class);
        intent.putExtra(String.valueOf(R.string.file_paths_intent), filePaths);
        intent.putExtra(String.valueOf(R.string.flags_intent), flags);
        startActivityForResult(intent, ACT_2_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode) {
            case RESULT_OK:
                Log.i("Test", "Result URI " + data.getData());
                break;
        }
    }
}
