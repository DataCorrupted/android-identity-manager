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

public class NdncertClient extends AppCompatActivity {
    static{
        System.loadLibrary("ndncert-client");
    }

    static public native String init();
    static public native void cppSendNew(String[] s);

    private interface Callback{
        void call(String[] s);
    }

    Callback sendNew = new Callback() {
        @Override
        public void call(String[] s) {
            cppSendNew(s);
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
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
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(NdncertClient.this);
        // We will set them up when the dialog is shown.
        mBuilder.setPositiveButton(R.string.button_enter, null)
                .setNegativeButton(R.string.button_cancel, null);

        // Setup dialog view.
        // Including all the input boxes and hints.
        assert texts.length == hints.length;
        int requestCnt = texts.length;
        editTexts = new EditText[requestCnt];
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        for (int i=0; i<requestCnt; i++){
            TextView tempTv = new TextView(this);
            tempTv.setText(texts[i]);
            tempTv.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
            layout.addView(tempTv);

            EditText tempEt = new EditText(this);
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
}
