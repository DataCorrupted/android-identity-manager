package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ShowIdentity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_identity);
        Intent intent = getIntent();
        String identityName = intent.getStringExtra("identityName");
        Log.i("ShowIdentity: ", "ID name: " + identityName);
        // Now you have your identityName,
        // go get more info and display it.

        TextView tv = findViewById(R.id.show_identity);
        tv.setText(identityName);
    }

    public void toMain(View v){
        Intent intent = new Intent(ShowIdentity.this, MainActivity.class);
        startActivity(intent);
    }
}
