package com.ndn.jwtan.identitymanager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NdncertClientIdentityInput extends Fragment {

    private static final String TAG
            = NdncertClientIdentityInput.class.getSimpleName();

    public interface SendNew{
        void sendNew(String identityStr);
    }

    SendNew mCallback;
    private View.OnClickListener sendNewClick = view -> {
        EditText editIdentity =
                getActivity().findViewById(R.id.editIdentity);
        String identityStr = editIdentity.getText().toString();
        if (existsID(identityStr) || identityStr.isEmpty()){
            Toast.makeText(getContext(),
                    R.string.invalid_identity,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(),
                R.string.valid_input,
                Toast.LENGTH_SHORT).show();
        mCallback.sendNew(identityStr);
    };

    public NdncertClientIdentityInput() {
        // Required empty public constructor
    }

    public static NdncertClientIdentityInput newInstance() {

        Bundle args = new Bundle();

        NdncertClientIdentityInput fragment = new NdncertClientIdentityInput();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context){
        try{
            mCallback = (SendNew) context;
        } catch (ClassCastException e){
            Log.e(TAG, context.toString() + "must implement interface SendNew");
        }
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ndncert_client_identity_input, container, false);
        Button btnContinue = view.findViewById(R.id.buttonContinue);
        btnContinue.setOnClickListener(sendNewClick);
        return view;
    }

    // TODO: Check if the id name exists before.
    Boolean existsID(String name){
        return false;
    }
}
