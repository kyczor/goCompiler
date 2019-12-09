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

public class MainActivity extends AppCompatActivity {

    final int ACT_2_REQUEST = 1;
    String[] filePaths;
    ArrayList<Integer> selectedItems;
    TextView dirTV;
    Button errorListBtn;
    String flags = " -Wall";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedItems = new ArrayList<>();

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
        for(int selectedIndex : selectedItems)
        {
            boolSelItems[selectedIndex] = true;
        }

        selectedItems = new ArrayList<>();
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
                        if(isChecked)
                        {
                            selectedItems.add(which);
                        }
                        else if(selectedItems.contains(which))
                        {
                            selectedItems.remove(which);
                        }
                    }
                });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            // get all the checked flags
            StringBuilder addToFlags = new StringBuilder();
            for(int flagId : selectedItems)
            {
                addToFlags.append(" ");
                TypedArray flagsAvailable = getResources().obtainTypedArray(R.array.flags);
                String chosenFlag = flagsAvailable.getString(flagId);
                addToFlags.append(chosenFlag);
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
        intent.putExtra("filePaths", filePaths);
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
