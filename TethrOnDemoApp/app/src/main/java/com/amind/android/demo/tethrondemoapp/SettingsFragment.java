package com.amind.android.demo.tethrondemoapp;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amind.amdcom.AMDCom;
import com.amind.amdcom.infra.utils.AMDCSettings;
import com.amind.amdcom.infra.utils.constants.StrConsts.SettingsConst;

import com.amind.android.demo.tethrondemoapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    AMDCSettings settings;
    EditText et_server;
    EditText et_port;
    EditText et_user;
    EditText et_pass;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        settings = AMDCom.instance().getSettings();
        Activity activity = getActivity();

        et_server = (EditText) activity.findViewById(R.id.se_et_server);
        et_port = (EditText) activity.findViewById(R.id.se_et_port);
        et_user = (EditText) activity.findViewById(R.id.se_et_user);
        et_pass = (EditText) activity.findViewById(R.id.se_et_password);

        loadSettings();

        Button saveButton = (Button) activity.findViewById(R.id.se_button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setProperty(SettingsConst.Property.BASEURL, et_server.getText().toString());
                settings.setProperty(SettingsConst.Property.PORT, et_port.getText().toString());
                settings.setUserName(et_user.getText().toString());
                settings.setPassword(et_pass.getText().toString());

                settings.saveSettings();

                Toast.makeText(getActivity(), getString(R.string.settings_saved), Toast.LENGTH_LONG).show();
            }
        });


        Button resetButton = (Button) activity.findViewById(R.id.se_button_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               loadSettings();
               Toast.makeText(getActivity(), getString(R.string.settings_reset), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void loadSettings() {
        et_server.setText((String) settings.getProperty(SettingsConst.Property.BASEURL));
        et_port.setText((String) settings.getProperty(SettingsConst.Property.PORT));
        et_user.setText(settings.getUserName());
        et_pass.setText(settings.getPassword());

    }


    }
