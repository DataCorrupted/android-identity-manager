package com.ndn.jwtan.identitymanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class NdncertClient extends AppCompatActivity {
    static{
        System.loadLibrary("ndncert-client");
    }

    private HashMap<String, String> params = new HashMap<>();

    static public native String init();
    public native void startNdncertClient(Map<String, String> params);
    public native void cppSendNew(String[] s);
    public native void cppSelectChallenge(String[] s);
    public native void cppSendSelect(String[] s);
    public native void cppSendValidate(String[] s);

    private interface Callback{
        void call(String[] s);
    }
    Callback sendNew = new Callback() {
        @Override
        public void call(String[] s) {
            cppSendNew(s);
        }
    };
    Callback selectChallenge = new Callback() {
        @Override
        public void call(String[] s) {
            cppSelectChallenge(s);
        }
    };
    Callback sendSelect = new Callback() {
        @Override
        public void call(String[] s) {
            cppSendSelect(s);
        }
    };
    Callback sendValidate = new Callback() {
        @Override
        public void call(String[] s) {
            cppSendValidate(s);
        }
    };
    // Just a place holder, we will never call this functions.
    Callback download = null;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);

        params.put("HOME", this.getFilesDir().getAbsolutePath());
        startNdncertClient(params);

        setContentView(R.layout.fragment_uicreate_show_legal_info);
        Button btnAccept = findViewById(R.id.buttonAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] texts = {
                        "Please type in your identity name:"};
                String[] hints = {"PeterRong"};
                promptInputDialog(texts, hints, sendNew);
            }
        });
        Button btnDecline = findViewById(R.id.buttonDecline);
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NdncertClient.this, MainActivity.class));
            }
        });
    }

    // Make these two global so that inner onClick functions can use it.
    private EditText[] editTexts;
    private AlertDialog dialog;
    private void promptInputDialog(String[] texts, String[] hints, final Callback cb){
        // No need to create this dialog if there is nothing to input.
        if (texts.length == 0) {
            cb.call(new String[0]);
            return;
        }
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(NdncertClient.this);
        // We will set them up when the dialog is shown.
        mBuilder.setPositiveButton(R.string.button_enter, null)
                .setNegativeButton(R.string.button_cancel, null);

        // Setup dialog view.
        // Including all the input boxes and hints.
        assert texts.length == hints.length;
        int requestCnt = texts.length;
        editTexts = new EditText[requestCnt];
        LinearLayout layout = new LinearLayout(NdncertClient.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        for (int i=0; i<requestCnt; i++){
            TextView tempTv = new TextView(NdncertClient.this);
            tempTv.setText(texts[i]);
            tempTv.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
            layout.addView(tempTv);

            EditText tempEt = new EditText(NdncertClient.this);
            tempEt.setHint(hints[i]);
            tempEt.setMaxLines(1);
            tempEt.setInputType(InputType.TYPE_CLASS_TEXT);
            layout.addView(tempEt);
            editTexts[i] = tempEt;
        }
        mBuilder.setView(layout);
        dialog = mBuilder.create();

        // Setup click listener.
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnEnter = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnEnter.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        // Extract all the inputs given by the user and call cb function
                        // Don't worry about editTexts length, they will be set
                        // by the time user clicks.
                        String[] inputs = new String[editTexts.length];
                        for (int k=0; k<editTexts.length; k++){
                            inputs[k] = editTexts[k].getText().toString();
                            if (inputs[k].isEmpty()){
                                Toast.makeText(NdncertClient.this,
                                        R.string.empty_input,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Toast.makeText(NdncertClient.this,
                                R.string.valid_input,
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        cb.call(inputs);
                    }
                });
                Button btnCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(NdncertClient.this, MainActivity.class));
                    }
                });
            }
        });

        // Woola, you have your dialog.
        dialog.show();
    }
    private void promptSelectDialog(String[] texts, final String[] hints, final Callback cb){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(NdncertClient.this);
        mBuilder.setTitle(texts[0])
                .setItems(hints, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String[] choice = new String[1];
                        choice[0] = hints[i];
                        cb.call(choice);
                    }
                }).create().show();
    }
    private void promptTextDialog(String title, String text){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(NdncertClient.this);
        mBuilder.setTitle(title)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(NdncertClient.this, MainActivity.class));
                    }
                });
        LinearLayout layout = new LinearLayout(NdncertClient.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(NdncertClient.this);
        tv.setText(text);
        tv.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        layout.addView(tv);
        mBuilder.setView(layout);
        dialog = mBuilder.create();
        dialog.show();
    }}
