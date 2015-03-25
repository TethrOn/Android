package com.amind.android.demo.tethrondemoapp;
import android.database.Cursor;
import android.database.CursorWrapper;

import com.amind.amdcom.infra.utils.constants.StrConsts;

/**
 * In order to use the android CursorAdapter, the data must have a column
 * named "_id". The TethrOn DB does not currently have a column with
 * this name. The UID column in the TethrOn DB is installed called
 * AMDCRowId. This cursor wrapper class passes the AMDCRowID value
 * to the adapter when the _id field is requested.
 */
public class AMindCursorWrapper extends CursorWrapper {

    public AMindCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    @Override
    public int getColumnIndex(String columnName) {
        /**
         * If _id index is requested, return the AMDCRowID index, otherwise,
         * return the index as is.
         */
        if (columnName.equals(consts.StrConsts.ID)) {
            return super.getColumnIndex(StrConsts.FieldsConsts.CLIENTID);
        }
        return super.getColumnIndex(columnName);
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        if (columnName.equals(consts.StrConsts.ID)) {
            return super.getColumnIndex(StrConsts.FieldsConsts.CLIENTID);
        }
        return super.getColumnIndex(columnName);
    }

}
