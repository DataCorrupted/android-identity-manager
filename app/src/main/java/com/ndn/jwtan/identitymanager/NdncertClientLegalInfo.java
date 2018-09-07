package com.ndn.jwtan.identitymanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NdncertClientLegalInfo extends Fragment {

    private static final String TAG
            = NdncertClientLegalInfo.class.getSimpleName();

    public NdncertClientLegalInfo() {
        // Required empty public constructor
    }
    public static NdncertClientLegalInfo newInstance() {

        Bundle args = new Bundle();

        NdncertClientLegalInfo fragment = new NdncertClientLegalInfo();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ndncert_client_legal_info, container, false);
    }
}
