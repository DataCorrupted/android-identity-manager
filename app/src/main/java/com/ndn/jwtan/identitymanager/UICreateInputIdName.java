package com.ndn.jwtan.identitymanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UICreateInputIdName extends Fragment {
    public UICreateInputIdName(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_uicreate_input_id_name, container, false);
    }
}
