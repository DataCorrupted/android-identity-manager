package com.ndn.jwtan.identitymanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import net.named_data.jndn.Face;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.pib.AndroidSqlite3Pib;
import net.named_data.jndn.security.pib.PibImpl;
import net.named_data.jndn.security.policy.NoVerifyPolicyManager;
import net.named_data.jndn.security.tpm.TpmBackEndFile;
import net.named_data.jndncert.challenge.ChallengeFactory;
import net.named_data.jndncert.challenge.ChallengeModule;
import net.named_data.jndncert.client.ClientCaItem;
import net.named_data.jndncert.client.ClientModule;
import net.named_data.jndncert.client.RequestState;

import org.json.JSONObject;

import java.util.ArrayList;

public class NdncertClient extends AppCompatActivity
        implements NdncertClientSelectChallenge.SendSelect{
    private final String TAG = "NdncertClient";

    private ClientModule client;

    private int tabsCnt = 6;
    private UICustomViewPager viewPager;
    private TabLayout.Tab[] tabs = new TabLayout.Tab[tabsCnt];
    private NdncertClientPageAdapter adapter;

    private RequestState mState = new RequestState();
    private ClientModule.ErrorCallback errorCb = (errInfo -> Log.e(TAG, errInfo));
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ndncert_client);

        // Setup client
        try{
            String home = this.getFilesDir().getAbsolutePath();
            client = new ClientModule(
                    new Face(),
                    new KeyChain(
                            new AndroidSqlite3Pib(home + "/.ndn"),
                            new TpmBackEndFile(home + "/.ndn"),
                            new NoVerifyPolicyManager()));
            Log.i(TAG, "Client init successfully.");
        } catch (PibImpl.Error e){
            Log.e(TAG, e.getMessage());
        }
        client.getClientConf().load(ClientConf.getJsonConf());
        Log.i(TAG, "Client config loaded.");

        // Setup tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabs[0] = tabLayout.newTab().setIcon(R.drawable.icon_filled);
        tabLayout.addTab(tabs[0]);
        for (int i=1; i<tabsCnt; i++){
            tabs[i] = tabLayout.newTab().setIcon(R.drawable.icon_empty);
            tabLayout.addTab(tabs[i]);
        }
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        Log.i(TAG, "Tabs set.");

        // Setup view pager
        viewPager = (UICustomViewPager) findViewById(R.id.pager);
        adapter = new NdncertClientPageAdapter(
                getSupportFragmentManager(), tabsCnt);

        // Disabling clicking on tabs to switch
        LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnTouchListener(
                    (v,  event) -> true);
        }

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    void acceptClick(View view){
        Log.i(TAG, "User agreed to our condition. Collecting identity name.");
        viewPager.setCurrentItem(1);
        tabs[1].setIcon(R.drawable.icon_filled);
    }

    void declineClick(View view){
        Intent intent = new Intent(NdncertClient.this, MainActivity.class);
        startActivity(intent);
    }

    void sendNewClick(View view){
        // Define Cb function
        ClientModule.RequestCallback newCb = state -> {
            Log.i(TAG, "_NEW data received.");
            mState = state;
            Log.i(TAG, "State info recorded.");
            viewPager.setCurrentItem(2);
            tabs[2].setIcon(R.drawable.icon_filled);
            NdncertClientSelectChallenge selectFragment =
                    (NdncertClientSelectChallenge)
                            adapter.getCurrentFragment();
            selectFragment.setChallengeList(mState.m_challengeList);
            Log.i(TAG, "New fragment switched.");
        };

        EditText editIdentity =
                (EditText) findViewById(R.id.editIdentity);
        String identityStr = editIdentity.getText().toString();
        if (existsID(identityStr)){
            return;
        }
        ClientCaItem caItem = client.getClientConf().m_caItems.get(0);
        Name identityName = caItem.m_caName.getPrefix(-1);
        identityName.append(identityStr);
        // Send New Interest
        // client.sendNew(caItem, identityName, newCb, errorCb);
        // TODO: Fake server below.
        mState.m_challengeList = new ArrayList<>();
        mState.m_challengeList.add("Email");
        mState.m_challengeList.add("Pin");

        viewPager.setCurrentItem(2);
        tabs[2].setIcon(R.drawable.icon_filled);

        NdncertClientSelectChallenge selectFragment =
                (NdncertClientSelectChallenge) adapter.getCurrentFragment();
        selectFragment.setChallengeList(mState.m_challengeList);
        // Fake server above.

        Log.i(TAG, "_NEW interest expressed.");
    }
    @Override
    public void sendSelect(
            String choice,
            ChallengeModule challenge,
            ArrayList<String> paramList) {
        ClientModule.RequestCallback selectCb = state -> {
            Log.i(TAG, "_SELECT data received.");
            mState = state;
            Log.i(TAG, "State info recorded.");
            viewPager.setCurrentItem(3);
            tabs[3].setIcon(R.drawable.icon_filled);
        };
        JSONObject paramJson = challenge.genSelectParamsJson(
                mState.m_status, paramList);
        client.sendSelect(mState, choice, paramJson, selectCb, errorCb);
        Log.i(TAG, "_SELECT interest expressed.");
    }


    // TODO: Check if the id name exists before.
    Boolean existsID(String name){
        return false;
    }


}