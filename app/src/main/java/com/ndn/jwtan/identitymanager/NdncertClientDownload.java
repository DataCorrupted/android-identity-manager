package com.ndn.jwtan.identitymanager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class NdncertClientDownload extends Fragment {
    private static final String TAG = "NdncertClientDownload";
    public interface SendDownload{
        void sendDownload();
    }
    SendDownload mCallback;

    public NdncertClientDownload() {
        // Required empty public constructor
    }
    public static NdncertClientDownload newInstance() {

        Bundle args = new Bundle();

        NdncertClientDownload fragment = new NdncertClientDownload();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mCallback = (SendDownload) context;
        } catch (ClassCastException e){
            Log.e(TAG, context.toString() + "must implement interface SendDownload");
        }
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
        View view = inflater.inflate(R.layout.fragment_ndncert_client_download, container, false);
        Button btnContinue = view.findViewById(R.id.buttonContinue);
        btnContinue.setOnClickListener(sendDownload);
        return view;
    }

    private View.OnClickListener sendDownload = view -> mCallback.sendDownload();
}
