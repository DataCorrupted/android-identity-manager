package com.ndn.jwtan.identitymanager;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import android.support.design.widget.TabLayout;

import net.named_data.jndn.Face;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.pib.AndroidSqlite3Pib;
import net.named_data.jndn.security.pib.PibImpl;
import net.named_data.jndn.security.policy.NoVerifyPolicyManager;
import net.named_data.jndn.security.tpm.TpmBackEndFile;
import net.named_data.jndncert.client.ClientCaItem;
import net.named_data.jndncert.client.ClientModule;

public class GenerateToken extends AppCompatActivity {

    private final String TAG = "GenerateTokes";
    private ClientModule client;

    private final static String mURL = MainActivity.HOST + "/tokens/request/";
    private String caption = "";
    private String picture = "";

    private UICustomViewPager viewPager;
    private int selectedImageViewId = -1;
    /*
    private static int RESULT_LOAD_IMAGE = 1;
    public static final int KITKAT_VALUE = 1002;
    */
    private TabLayout.Tab tab0;
    private TabLayout.Tab tab1;
    private TabLayout.Tab tab2;
    private TabLayout.Tab tab3;
    private TabLayout.Tab tab4;

    ////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_token_and_identity);

        String HOME = this.getFilesDir().getAbsolutePath();
        try{
            client = new ClientModule(
                    new Face(),
                    new KeyChain(
                            new AndroidSqlite3Pib(HOME + "/.ndn"),
                            new TpmBackEndFile(HOME + "/.ndn"),
                            new NoVerifyPolicyManager()));
        } catch (PibImpl.Error e){
            Log.e(TAG, e.getMessage());
        }
        client.getClientConf().load(ClientConf.getJsonConf());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tab0 = tabLayout.newTab().setIcon(R.drawable.icon_filled);
        tab1 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab2 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab3 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab4 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tabLayout.addTab(tab0);
        tabLayout.addTab(tab1);
        tabLayout.addTab(tab2);
        tabLayout.addTab(tab3);
        tabLayout.addTab(tab4);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (UICustomViewPager) findViewById(R.id.pager);
        final UICreateIDPageAdapter adapter = new UICreateIDPageAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), getResources().getString(R.string.token_success), false);

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

    }

    public void submitEmail(View view) {
        Button button = (Button) findViewById(R.id.submitEmail);
        button.setEnabled(false);

        // Do something in response to button
        EditText editText = (EditText) findViewById(R.id.emailText);
        String email = editText.getText().toString();

        sendHttpRequest(email);
    }

    public void imageViewClick(View view) {
        CustomImageViewer v = (CustomImageViewer) view;
        // test: getDrawable() gives null with "background" instead of "src"
        //overlay is black with transparency of 0x77 (119)
        if (!v.selected) {
            v.getDrawable().setColorFilter(0x33000000, PorterDuff.Mode.MULTIPLY);
            v.selected = true;
            if (this.selectedImageViewId != -1) {
                CustomImageViewer oriV = (CustomImageViewer) findViewById(this.selectedImageViewId);
                imageViewClick(oriV);
            }
            this.picture = (String)v.getTag();
            this.selectedImageViewId = v.getId();
        } else {
            v.getDrawable().setColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
            v.selected = false;
            this.picture = "";
            this.selectedImageViewId = -1;
        }
        v.invalidate();
    }

    public void returnClick(View view) {
        Intent i = new Intent(GenerateToken.this, MainActivity.class);
        startActivity(i);
    }

    public void tab1Click(View view) {
        viewPager.setCurrentItem(1);
        tab1.setIcon(R.drawable.icon_filled);
    }

    public void declineClick(View view) {
        Intent i = new Intent(GenerateToken.this, MainActivity.class);
        startActivity(i);
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public void tab2Click(View view) {
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        EditText editText = (EditText) findViewById(R.id.emailText);
        String email = editText.getText().toString();

        EditText editID = (EditText) findViewById(R.id.idNameText);
        String idName = editID.getText().toString();

        if (isValidEmailAddress(email)) {
            if (! idName.equals("")) {
                this.caption = idName;
                ClientCaItem caItem = client.getClientConf().m_caItems.get(0);
                Name identityName = caItem.m_caName.getPrefix(-1);
                identityName.append(idName);
                client.sendNew(
                        caItem, identityName,
                        (state -> {
                            viewPager.setCurrentItem(2);
                            tab2.setIcon(R.drawable.icon_filled);
                            Log.e(TAG, "Data received.");
                        }),
                        (errInfo -> Log.e(TAG, "Got NACK")));
                Log.e(TAG, "Data sent.");
            } else {
                String toastString = "Please give an identity name";
                Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
            }
        } else {
            String toastString = "Please put valid email address";
            Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
        }
    }

    public void tab0Click(View view) {
        viewPager.setCurrentItem(0);
    }

    ////////////////////////////////////////////////////////////
    private void sendHttpRequest(final String email) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // for passing into the new Response.Listener
        final String caption = this.caption;
        final String picture = this.picture;

        final CustomImageViewer oriV;
        // resets the selected image
        if (this.selectedImageViewId != -1) {
            oriV = (CustomImageViewer) findViewById(this.selectedImageViewId);
        } else {
            oriV = null;
        }

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsResponse = new JSONObject(response);
                            if (jsResponse.getInt("status") == 200) {
                                DataBaseHelper dbHelper = new DataBaseHelper(getApplicationContext());
                                SQLiteDatabase db = dbHelper.getWritableDatabase();

                                // Insert the new row, returning the primary key value of the new row
                                ContentValues values = new ContentValues();
                                values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY, jsResponse.getString("assigned_namespace"));
                                values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_APPROVED, false);
                                values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_CAPTION, caption);
                                values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_PICTURE, picture);

                                db.insert(
                                        DataBaseSchema.IdentityEntry.TABLE_NAME,
                                        null,
                                        values);
                            }
                            else {
                                DialogFragment newFragment = new MessageDialogFragment(R.string.token_fail);
                                newFragment.show(getFragmentManager(), "message");
                            }

                            TextView emailView = (TextView) findViewById(R.id.step4Email);
                            emailView.setText(email);
                            emailView.setVisibility(View.VISIBLE);

                            TextView idNameView = (TextView) findViewById(R.id.step4IdName);
                            idNameView.setText(caption);
                            idNameView.setVisibility(View.VISIBLE);

                            ImageView profileImageView = (ImageView) findViewById(R.id.step4ImageView);
                            // Could need a better approach to clear
                            profileImageView.setImageResource(0);
                            profileImageView.setImageResource(getResources().getIdentifier(picture, "drawable", getPackageName()));
                            profileImageView.setVisibility(View.VISIBLE);

                            Button returnBtn = (Button) findViewById(R.id.returnBtn);
                            returnBtn.setEnabled(true);

                            viewPager.setCurrentItem(3);
                            tab3.setIcon(R.drawable.icon_filled);
                            if (oriV != null) {
                                imageViewClick(oriV);
                            }
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
                            String toastString = "Error code: " + error.networkResponse.statusCode;
                            Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();

                            finish();
                        }
                    }
                }) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("email", email);

                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
