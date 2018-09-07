package com.ndn.jwtan.identitymanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class NdncertClientIdPictureSelect extends Fragment {

    private static final String TAG
            = NdncertClientIdPictureSelect.class.getSimpleName();

    public NdncertClientIdPictureSelect() {
        // Required empty public constructor
    }

    public static NdncertClientIdPictureSelect newInstance() {
        NdncertClientIdPictureSelect fragment = new NdncertClientIdPictureSelect();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ndncert_client_id_picture_select, container, false);

        // TODO: Create image for select and bind with identity.

        Button btnContinue = view.findViewById(R.id.buttonContinue);
        btnContinue.setOnClickListener(
                v-> startActivity(new Intent(getActivity(), MainActivity.class)));
        return view;
    }
}
