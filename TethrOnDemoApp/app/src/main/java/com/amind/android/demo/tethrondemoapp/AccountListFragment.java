package com.amind.android.demo.tethrondemoapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import consts.StrConsts;
import com.amind.amdcom.dao.AMDCObjFactory;
import com.amind.amdcom.dao.AMDCSqlDataObj;

/**
 * This fragment is an example of how to display a list view
 * using the local database of synchronized data and a subclass
 * of androids CursorAdapter.
 *
 *
 */
public class AccountListFragment extends Fragment {

    private AccountAdapter accountAdapter;
    private ListView list;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list = (ListView) getActivity().findViewById(R.id.account_list);

        /**
         * TethrOn provides various Data Access Objects for accessing data.
         * The AMDCSqlDataObj is used for accessing objects stored in the local
         * SQLite database. Each DAO is created with a CDM type using the Object
         * factory. This initializes the object with the correct attributes provided
         * in the data model.
         *
         */
        AMDCSqlDataObj accountDao = (AMDCSqlDataObj) AMDCObjFactory.instance().create("Account");

        /**
         * When retrieving data, we can specify specific fields to retrieve to avoid
         * wasting memory retrieving all the columns when only some are needed. In this
         * case, since our table only has 3 columns, we specify them here.
         */
        List<Object> fieldNamesToGet = new ArrayList<Object>();
        fieldNamesToGet.add(StrConsts.NAME);
        fieldNamesToGet.add(StrConsts.ACCOUNT_NUMBER);
        fieldNamesToGet.add(StrConsts.SITE);


        /**
         * A query on our DAO object will retrieve all the matching records from
         * the corresponding table. In this case, the searchExpression parameter
         * is null, meaning it will return all the data in the Account table.
         * The last parameter tells the DAO object to keep the cursor, so we can
         * pass it to our adapter.
         *
         * Query will return -1 for an error, or 0 on success.
         * In the interest of keeping code short, we won't check it in
         * this tutorial.
         */
        long result = accountDao.query(fieldNamesToGet, (String) null, true);

        /**
         * Here, we create our custom adapter, pass it our cursor, and set it on the list.
         * The data will display in the list and the cursor adapter will take care
         * of buffering data for the scrolling list.
         *
         * Explanations of AccountAdapter and AMindCursorWrapper are given in their respective
         * classes.
         *
         */
        accountAdapter = new AccountAdapter(getActivity().getApplicationContext(), new AMindCursorWrapper(accountDao.getCursor()), false);
        list.setAdapter(accountAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                /**
                 * When a row is clicked, a new AccountEditFragment is created,
                 * and the id of the object in the clicked row is passed to it.
                 * It will then query the DB for the data it needs based on that
                 * ID.
                 *
                 */
                Bundle args = new Bundle();
                Long itemId = accountAdapter.getIdFromRow(position);
                args.putLong(StrConsts.ACCOUNT_ID, itemId);
                AccountEditFragment editFragment = new AccountEditFragment();
                editFragment.setArguments(args);
                // update the main content by replacing fragments
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .addToBackStack(StrConsts.LIST_FRAG)
                        .replace(R.id.container, editFragment, StrConsts.EDIT_FRAG)
                        .commit();
            }
        });




    }



    /**
     * When an object is modified from the Edit View, the list must be refreshed to
     * show the modified data. This will be called when the save button is clicked on
     * the edit view.
     */
    public void refreshList() {
        AMDCSqlDataObj accountDao = (AMDCSqlDataObj) AMDCObjFactory.instance().create(StrConsts.OBJTYPE_ACCOUNT);

        List<Object> fieldNamesToGet = new ArrayList<Object>();
        fieldNamesToGet.add(StrConsts.NAME);
        fieldNamesToGet.add(StrConsts.ACCOUNT_NUMBER);
        fieldNamesToGet.add(StrConsts.SITE);

        accountDao.query(fieldNamesToGet, (String) null, true);
        accountAdapter = new AccountAdapter(getActivity().getApplicationContext(), new AMindCursorWrapper(accountDao.getCursor()), false);
        list.setAdapter(accountAdapter);

    }
}



