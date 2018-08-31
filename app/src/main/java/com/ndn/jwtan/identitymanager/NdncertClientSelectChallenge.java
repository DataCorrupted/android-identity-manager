package com.ndn.jwtan.identitymanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.named_data.jndncert.challenge.ChallengeFactory;
import net.named_data.jndncert.challenge.ChallengeModule;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class NdncertClientSelectChallenge extends ListFragment {
    public interface SendSelect{
        void sendSelect(
                String choice, ArrayList<String> requirementList);
    }
    private final String TAG = "NdncertClientSelect";
    private ArrayList<String> mChallengeList;
    private SendSelect mCallback;

    public NdncertClientSelectChallenge() {
        // Required empty public constructor
    }
    public static NdncertClientSelectChallenge newInstance() {

        Bundle args = new Bundle();

        NdncertClientSelectChallenge fragment = new NdncertClientSelectChallenge();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mCallback = (SendSelect) context;
        } catch (ClassCastException e){
            Log.e(TAG, context.toString() + "must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            mChallengeList =
                    getArguments().getStringArrayList("challengeList");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ndncert_client_identity_input, container, false);
        ListView lv = view.findViewById(R.id.select_challenge);
        ArrayAdapter<String> challengeListAdapter =
                new ArrayAdapter<>(getActivity(), R.layout.fragment_ndncert_select_challenge , mChallengeList);
        lv.setAdapter(challengeListAdapter);
        return view;
    }

    private EditText editTexts[];
    private AlertDialog dialog;
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final String choice = mChallengeList.get((int) id);
        ChallengeModule challenge =
                ChallengeFactory.createChallengeModule(choice);
        ArrayList<String> requirementList =
                challenge.getRequirementForSelection();

        // No need to create this dialog if there is nothing to input.
        if (requirementList.size() == 0) {
            mCallback.sendSelect(choice, requirementList);
        }

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        // We will set them up when the dialog is shown.
        mBuilder.setPositiveButton(R.string.button_enter, null)
                .setNegativeButton(R.string.button_cancel, null)
                .setCancelable(false);

        int requestCnt = requirementList.size();
        editTexts = new EditText[requestCnt];
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        for (int i=0; i<requestCnt; i++){
            TextView tempTv = new TextView(getContext());
            tempTv.setText(requirementList.get(i));
            tempTv.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
            layout.addView(tempTv);

            EditText tempEt = new EditText(getContext());
            tempEt.setMaxLines(1);
            tempEt.setInputType(InputType.TYPE_CLASS_TEXT);
            layout.addView(tempEt);
            editTexts[i] = tempEt;
        }
        mBuilder.setView(layout);
        dialog = mBuilder.create();

        // Setup click listener.
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnEnter = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnEnter.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        // Extract all the inputs given by the user and call cb function
                        // Don't worry about editTexts length, they will be set
                        // by the time user clicks.
                        ArrayList<String> inputs = new ArrayList<>();
                        for (int k=0; k<editTexts.length; k++){
                            String tmp = editTexts[k].getText().toString();
                            if (tmp.isEmpty()){
                                Toast.makeText(getContext(),
                                        R.string.empty_input,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                inputs.add(tmp);
                            }
                        }
                        Toast.makeText(getContext(),
                                R.string.valid_input,
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        mCallback.sendSelect(choice, inputs);
                    }
                });
                Button btnCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getContext(), MainActivity.class));
                    }
                });
            }
        });

        // Woola, you have your dialog.
        dialog.show();
    }

    public void setChallengeList(ArrayList<String> challengeList){
        mChallengeList = challengeList;
    }
}
