package com.ndn.jwtan.identitymanager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import net.named_data.jndncert.challenge.ChallengeModule;

import java.util.ArrayList;

public class NdncertClientValidate extends Fragment {

    private static final String TAG
            = NdncertClientValidate.class.getSimpleName();

    public interface SendValidate{
        void sendValidate(ArrayList<String> validateParamList);
    }

    private SendValidate mCallback;
    private ArrayList<String> mRequirementList = new ArrayList<>();
    private ListView mListView;
    private View.OnClickListener sendValidate = view -> {
        ArrayList<String> validateParamList = new ArrayList<>();
        for (int k=0; k < mListView.getAdapter().getCount(); k++){
            View tempViewK = mListView.getChildAt(k);
            EditText tempEditText = tempViewK.findViewById(R.id.validate_edit);
            String tempStr = tempEditText.getText().toString();
            if (tempStr.isEmpty()){
                Toast.makeText(getContext(),
                        R.string.empty_input,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            validateParamList.add(tempStr);
        }
        Toast.makeText(getContext(),
                R.string.valid_input,
                Toast.LENGTH_SHORT).show();
        mCallback.sendValidate(validateParamList);
    };

    public NdncertClientValidate() {
        // Required empty public constructor
    }

    public static NdncertClientValidate newInstance() {

        Bundle args = new Bundle();

        NdncertClientValidate fragment = new NdncertClientValidate();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mCallback = (SendValidate) context;
        } catch (ClassCastException e){
            Log.e(TAG, context.toString() + "must implement interface SendNew");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ndncert_client_validate, container, false);
        mListView = view.findViewById(android.R.id.list);
        ArrayAdapter<String> challengeListAdapter =
                new ArrayAdapter<>(getActivity(),
                        R.layout.fragment_ndncert_client_validate_list_item,
                        R.id.validate_text,
                        mRequirementList);
        mListView.setAdapter(challengeListAdapter);

        Button btnContinue = view.findViewById(R.id.buttonContinue);
        btnContinue.setOnClickListener(sendValidate);

        // Inflate the layout for this fragment
        return view;
    }

    public void setRequirementList(ArrayList<String> requirementList){
        mRequirementList = requirementList;
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .detach(this).attach(this)
                .commit();
    }
}
