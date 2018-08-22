package com.ndn.jwtan.identitymanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class NdncertClient extends AppCompatActivity{
    static{
        System.loadLibrary("ndncert-client");
    }

    static public native String init();

    private UICustomViewPager viewPager;
    private TabLayout.Tab tab0;
    private TabLayout.Tab tab1;
    private TabLayout.Tab tab2;
    private TabLayout.Tab tab3;
    private int currTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_token_and_identity);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tab0 = tabLayout.newTab().setIcon(R.drawable.icon_filled);
        tab1 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab2 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab3 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tabLayout.addTab(tab0);
        tabLayout.addTab(tab1);
        tabLayout.addTab(tab2);
        tabLayout.addTab(tab3);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = findViewById(R.id.pager);
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
        currTab = 0;
    }

    public void goToMainPage(View view) {
        Intent i = new Intent(NdncertClient.this, MainActivity.class);
        startActivity(i);
    }
    public void goToNextTab(View view){
        currTab ++;
        viewPager.setCurrentItem(currTab);

    }
    public void goToPrevTab(View view){
        currTab --;
        viewPager.setCurrentItem(currTab);
    }

}
