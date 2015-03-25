package com.amind.android.demo.tethrondemoapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import java.util.HashMap;
import consts.StrConsts;
import com.amind.amdcom.dao.AMDCObjFactory;
import com.amind.amdcom.dao.AMDCSqlDataObj;


/**
 * This fragment creates an edit view for an account record.
 * In the list fragment, we only queried for 3 fields, but
 * want to provide more fields on the edit screen.
 * To display extra fields, we query for that object by ID
 * in the local DB. To save the object, we set the new field values,
 * persist this object using the same DAO and relaunch the list
 * fragment, telling it to refresh it's list.
 */
public class AccountEditFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_edit, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        // Get the ID that was passed in by the list fragment.
        final Long id = getArguments().getLong(StrConsts.ACCOUNT_ID);
        /**
         *  When passing an id to the Object factory, the returned DAO will be initialized
         *  with the data from a single record corresponding to that id.
         */
        final AMDCSqlDataObj accountDao = (AMDCSqlDataObj) AMDCObjFactory.instance().create(StrConsts.OBJTYPE_ACCOUNT, id);

        final EditText name = (EditText) activity.findViewById(R.id.ae_et_name);
        name.setText((String) accountDao.getFieldValue(StrConsts.NAME));

        final EditText city = (EditText) activity.findViewById(R.id.ae_et_city);
        city.setText((String) accountDao.getFieldValue(StrConsts.BILLING_CITY));

        final EditText site = (EditText) activity.findViewById(R.id.ae_et_site);
        site.setText((String) accountDao.getFieldValue(StrConsts.SITE));

        final EditText state = (EditText) activity.findViewById(R.id.ae_et_state);
        state.setText((String) accountDao.getFieldValue(StrConsts.BILLING_STATE));

        final EditText phoneNumber = (EditText) activity.findViewById(R.id.ae_et_phoneNumber);
        phoneNumber.setText((String) accountDao.getFieldValue(StrConsts.PHONE_NUMBER));


        Button saveButton = (Button) activity.findViewById(R.id.ae_button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * For the simplicity of this example, we just set all the fields,
                 * whether they were changed or not.
                 */
                HashMap<String, Object> fieldsToSave = new HashMap<String, Object>();

                fieldsToSave.put(StrConsts.NAME, name.getText().toString());
                fieldsToSave.put(StrConsts.BILLING_CITY, city.getText().toString());
                fieldsToSave.put(StrConsts.SITE, site.getText().toString());
                fieldsToSave.put(StrConsts.BILLING_STATE, state.getText().toString());
                fieldsToSave.put(StrConsts.PHONE_NUMBER, phoneNumber.getText().toString());

                /**
                 * Set the new values on the DAO, and persist the update to the DB.
                 */
                accountDao.setValues(fieldsToSave);
                accountDao.update();

                /**
                 * Find the account list fragment instance, tell it to refresh the list
                 * from the database, and swap it back onto the screen.
                 */
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                AccountListFragment listFrag = (AccountListFragment) fragmentManager.findFragmentByTag("listFrag");
                listFrag.refreshList();

                fragmentManager.beginTransaction()
                        .replace(R.id.container, listFrag)
                        .commit();

            }
        });


        Button cancelButton = (Button) activity.findViewById(R.id.ae_button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                AccountListFragment listFrag = (AccountListFragment) fragmentManager.findFragmentByTag("listFrag");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, listFrag)
                        .commit();
            }
        });


    }

}
