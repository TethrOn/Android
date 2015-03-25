package com.amind.android.demo.tethrondemoapp;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import consts.StrConsts;

/**
 * This class is an adapter for the Account object based
 * on the android CursorAdapter class. It gets created with the
 * result of the Account DAO query and handles displaying the data
 * in a list, and scrolling the data.
 */
public class AccountAdapter extends CursorAdapter {

    public AccountAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.account_table_row,
                parent, false);
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) retView.findViewById(R.id.row_tv_name);
        holder.accountNumber = (TextView) retView.findViewById(R.id.row_tv_id);
        holder.site = (TextView) retView.findViewById(R.id.row_tv_site);

        retView.setTag(holder);
        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String name = getData(cursor, StrConsts.NAME);
        holder.name.setText(name == null ? "" : name);

        String accountNumber = getData(cursor, StrConsts.ACCOUNT_NUMBER);
        holder.accountNumber.setText(accountNumber == null ? "" : accountNumber);

        String site = getData(cursor, StrConsts.SITE);
        holder.site.setText(site == null ? "" : site);
    }

    public Long getIdFromRow(int row) {
        return getCursor().getLong(getCursor().getColumnIndex(StrConsts.ID));
    }

    private String getData(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }

    private class ViewHolder {
        TextView name;
        TextView accountNumber;
        TextView site;
    }

}
