package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    protected static final String DB_NAME = "certDb.db";
    protected static final String CERT_DIR = "certDir";
    // TODO: Stuck at submit email address without timeout if HOST misconfigured.
    // On memoria, my ICN chat cert runs on 5000, while the openmhealth cert runs on 5001
    protected static final String HOST = "http://memoria.ndn.ucla.edu:5001";
    private static final String slotTaken = "used";

    private String usage = "main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();

        String appID = intent.getStringExtra("app_id");
        if (appID != null) {
            usage = "authorize";
        }

        Log.i("Peter", NdncertClient.init());
        Log.e("zhehao", usage);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.identity_list);

        // Get your id list.
        final String[] idNames = getIdentities();
        ListView listView = findViewById(R.id.identity_list);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.identity_entry, idNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, ShowIdentity.class);
                // Present identity name to next activity.
                intent.putExtra("identityName", idNames[(int) l]);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.e("zhehao", "on resume");
        // getIdentities();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void newIdentity(View view){
        Intent intent = new Intent(MainActivity.this, NdncertClient.class);
        startActivity(intent);
    }

    // Always exit the application when back is pressed for Main activity,
    // instead of potentially returning to "action finished", or "create identity"
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public String[] getIdentities(){
        // TODO: Get id list from HOME/.ndn/
        String[] identities = {"PeterRong"};
        return identities;
        //return new String[0];
    }
}
