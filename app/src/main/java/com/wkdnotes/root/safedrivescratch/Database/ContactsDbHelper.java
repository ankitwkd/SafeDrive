package com.wkdnotes.root.safedrivescratch.Database;




import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class
ContactsDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG=ContactsDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME="safeDrive.db";
    private static final int DATABASE_VERSION=1;
    public ContactsDbHelper(Context context) {
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ ContactContract.ContactEntry.TABLE_NAME);
    }
    public static boolean check_(Context context,String number)
    {
        Log.i("Contacts Db heler",number);
        ContactsDbHelper contactsDbHelper=new ContactsDbHelper(context);
        SQLiteDatabase db=contactsDbHelper.getReadableDatabase();
        db.execSQL(ContactContract.ContactEntry.SQL_CREATE_PRIORITY_CONTACTS);
        String query="SELECT * FROM "+ ContactContract.ContactEntry.Table_Priority_Contacts+" where "+ ContactContract.ContactEntry.COLUMN_NUMBER+" = '"+ number+"';";
        Cursor cursor=db.rawQuery(query,null);
        Log.i("Contacts Db heler",query);
        Log.i("Contacts Db heler",String.valueOf(cursor.getCount()));
        if(cursor.getCount()>0)
            return true;
        else
            return false;

    }
}
