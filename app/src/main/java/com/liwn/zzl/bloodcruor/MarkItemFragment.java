package com.liwn.zzl.bloodcruor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MarkItemFragment extends Fragment {
    private final static String TAG = MarkItemFragment.class.getSimpleName();

    public static final int MENU1 = 1;
    public static final int MENU2 = 2;

    private static final String ARG_COLUMN_COUNT = "column-count";

    private static final int SUB_PAGE = 2;
    private int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;
    public Context mContext;
    public static final String OLD_POS_ID = "OLD_POS_ID";
    public static final String NEW_POS_ID = "NEW MARK ID";
    public static final int REQUEST_CHOOSE_NEW_MARK_A = 6;
    public static final int REQUEST_CHOOSE_NEW_MARK_B = 7;


    private Switch channelSwitch;
    private Button switch_A;
    private Button switch_B;
    private EditText number;
    private EditText name;
    private Spinner gender;
    private EditText age;
    private EditText detectionType;
    private Button upload;

    private ArrayList<HashMap<Integer, String>> maps = new ArrayList<>(SUB_PAGE);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MarkItemFragment() {
    }

    @SuppressWarnings("unused")
    public static MarkItemFragment newInstance(int columnCount) {
        MarkItemFragment fragment = new MarkItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_markitem_list, container, false);

//        channelSwitch = (Switch) view.findViewById(R.id.channelSwitch);
        switch_A = (Button) view.findViewById(R.id.switcher_A);
        switch_B = (Button) view.findViewById(R.id.switcher_B);

        number = (EditText) view.findViewById(R.id.number);
        name = (EditText) view.findViewById(R.id.name);
        gender = (Spinner) view.findViewById(R.id.gender);
        ArrayAdapter<String> a = new ArrayAdapter<String>(this.getContext(), R.layout.spinner_battery_dropdown_item, getResources().getStringArray(R.array.gender));
        a.setDropDownViewResource(R.layout.spinner_battery_dropdown_item);
        gender.setAdapter(a);
        age = (EditText) view.findViewById(R.id.age);
        detectionType = (EditText) view.findViewById(R.id.detectionType);

        upload = (Button) view.findViewById(R.id.upload);

        for (int i = 0; i < SUB_PAGE; ++i) {
            maps.add(new HashMap<Integer, String>());
            maps.get(i).put(R.id.number, "");
            maps.get(i).put(R.id.name, "");
            maps.get(i).put(R.id.gender, "0");
            maps.get(i).put(R.id.age, "");
            maps.get(i).put(R.id.detectionType, "");
        }

//        channelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                switchChannel(isChecked);
//            }
//        });

        switch_A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchChannel(true);
            }
        });

        switch_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchChannel(false);
            }
        });

        return view;
    }

    private void switchChannel(boolean isChecked) {
        int index = isChecked ? 0 : 1;
        int tmp = isChecked ? 1 : 0;
        maps.get(tmp).put(R.id.number, number.getText().toString());
        maps.get(tmp).put(R.id.name, name.getText().toString());
        maps.get(tmp).put(R.id.gender, String.valueOf(gender.getSelectedItemId()));
        maps.get(tmp).put(R.id.age, age.getText().toString());
        maps.get(tmp).put(R.id.detectionType, detectionType.getText().toString());

        number.setText(maps.get(index).get(R.id.number));
        name.setText(maps.get(index).get(R.id.name));
        gender.setSelection(Integer.valueOf(maps.get(index).get(R.id.gender)));
        age.setText(maps.get(index).get(R.id.age));
        detectionType.setText(maps.get(index).get(R.id.detectionType));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.index_setting);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
    }
}
