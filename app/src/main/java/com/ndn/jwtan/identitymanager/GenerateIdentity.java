package com.ndn.jwtan.identitymanager;

import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import net.named_data.jndn.Name;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.policy.NoVerifyPolicyManager;
import net.named_data.jndn.security.v2.CertificateV2;
import net.named_data.jndn.security.pib.AndroidSqlite3Pib;
import net.named_data.jndn.security.tpm.TpmBackEndFile;

import org.json.JSONObject;

public class GenerateIdentity extends AppCompatActivity {

    private final static String mURL = MainActivity.HOST + "/cert-requests/submit/";
    private UICustomViewPager viewPager;

    ////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_token_and_identity);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_filled));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_filled));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_filled));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_filled));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (UICustomViewPager) findViewById(R.id.pager);
        final UICreateIDPageAdapter adapter = new UICreateIDPageAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), getResources().getString(R.string.please_wait), true);

        // Disabling clicking on tabs to switch
        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        viewPager.setCurrentItem(3);

        // Get the message from the intent
        Intent intent = getIntent();
        mEmail = intent.getStringExtra(UriHandler.EXTRA_MESSAGE_EMAIL);
        mToken = intent.getStringExtra(UriHandler.EXTRA_MESSAGE_TOKEN);
        assignedNamespace = intent.getStringExtra(UriHandler.EXTRA_MESSAGE_NAMESPACE);
        try {
            Log.e("zhehao", mEmail);
            Log.e("zhehao", mToken);
            Log.e("zhehao", assignedNamespace);
        } catch (Exception e) {
            Log.e("zhehao", e.getMessage());
        }

        //sendHttpGetRequest();
        String idName = getIdentityName(assignedNamespace);
        submitRequest(idName);
    }

    public String getIdentityName(String assignedNamespace) {
        // Establish Database connection
        DataBaseHelper dbHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY + " DESC";

        String[] projection = {
                DataBaseSchema.IdentityEntry._ID,
                DataBaseSchema.IdentityEntry.COLUMN_NAME_CAPTION
        };

        String[] whereClause = {
                assignedNamespace
        };

        Cursor c = db.query(
                DataBaseSchema.IdentityEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY + "=?",                                     // The columns for the WHERE clause
                whereClause,                              // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        if (c.moveToNext()) {
            String idName = c.getString(1);
            c.close();
            db.close();
            return idName;
        } else {
            Log.e("zhehao", "Matching record doesn't exist");
            return "default_name";
        }
    }

    public void submitRequest(String idName) {
        try {
            String certification = generateKey();
            sendHttpPostRequest(idName, certification);
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), e.getMessage());
            finish();
        }
    }

    // TODO: check what if the returns from GenerateId, GenerateToken, and InstallCert do different things?
    public void returnClick(View view) {
        Intent i = new Intent(GenerateIdentity.this, MainActivity.class);
        startActivity(i);
        return;
    }

    ////////////////////////////////////////////////////////////
    private String generateKey() throws net.named_data.jndn.security.SecurityException {
        String identity = assignedNamespace;

        String path = getApplicationContext().getFilesDir().getAbsolutePath();
        try {
            AndroidSqlite3Pib storage = new AndroidSqlite3Pib(path, MainActivity.DB_NAME);
            TpmBackEndFile tpm = new TpmBackEndFile(path + "/" + MainActivity.CERT_DIR);
            final KeyChain keyChain = new KeyChain(storage, tpm, new
                    NoVerifyPolicyManager());
            keyChain.createIdentityV2(new Name(identity));
            CertificateV2 certificate = keyChain.getPib().getIdentity(new Name(identity)).getDefaultKey().getDefaultCertificate();

            String encodedString = Base64.encodeToString(certificate.wireEncode().getImmutableArray(), Base64.DEFAULT);
            return encodedString;
        } catch (Exception e) {
            e.printStackTrace();
            return ""; // fix this
        }
    }

    private void sendHttpPostRequest(final String name, final String certification) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsResponse = new JSONObject(response);

                            DialogFragment newFragment;
                            if (jsResponse.getInt("status") == 200) {
                                /*
                                newFragment = new MessageDialogFragment(R.string.submit_success);
                                newFragment.show(getFragmentManager(), "message");
                                */
                                // By this time the view should always be available
                                TextView hintText = (TextView) findViewById(R.id.step4Hint);
                                hintText.setText(R.string.submit_success);
                            }
                            else if (jsResponse.getInt("status") == 2) {
                                TextView hintText = (TextView) findViewById(R.id.step4Hint);
                                hintText.setText(R.string.submit_fail);
                            }
                            else {

                            }

                            Button returnBtn = (Button) findViewById(R.id.returnBtn);
                            returnBtn.setEnabled(true);
                        }
                        catch (Exception e) {
                            Log.e(getResources().getString(R.string.app_name), e.getMessage());
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            String toastString = "Error code: " + error.networkResponse.statusCode + "; msg: " + new String(error.networkResponse.data);
                            Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();

                            finish();
                        }
                    }
                }) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("full_name", name);
                params.put("email", mEmail);
                params.put("token", mToken);
                params.put("cert_request", certification);

                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    ////////////////////////////////////////////////////////////
    private String mEmail;
    private String mToken;
    private String assignedNamespace;
}
