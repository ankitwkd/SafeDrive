package com.wkdnotes.root.safedrivescratch.Background;



import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.wkdnotes.root.safedrivescratch.Database.ContactContract;
import com.wkdnotes.root.safedrivescratch.Database.ContactsDbHelper;
import com.wkdnotes.root.safedrivescratch.Util.Flag;


public class BackgroundTask extends AsyncTask<Void,Void,Void> {
    Context ctx;
    private static final String LOG_TAG=BackgroundTask.class.getSimpleName();
    ContactsDbHelper contactsDbHelper;
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;
    String method;
    int flag=1;
    public BackgroundTask(Context context,String method)
    {
        Flag.setFlag(1);
        flag=0;
        this.ctx=context;
        this.method=method;
        contactsDbHelper = new ContactsDbHelper(ctx);
    }
    @Override
    protected Void doInBackground(Void... voids) {
        String phoneNumber = null;
        String email = null;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;

        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = ctx.getContentResolver();
        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);
        if (cursor.getCount() > 0)
        {
            Log.e(LOG_TAG,String.valueOf(cursor.getCount()));
            while (cursor.moveToNext())
            {
                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                    }
                    phoneCursor.close();
                }
                if(method=="load")
                    addContacts(name,phoneNumber);
                else if(method=="refresh")
                    refresh_table(name,phoneNumber);
            }
        }

        return null;
    }
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Flag.setFlag(0);
    }

    public void addContacts(String name, String number) {

        String res_number = number.replaceAll("[-:,()]","").trim();
        res_number=res_number.replaceAll(" ","");
        sqLiteDatabase = contactsDbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL(ContactContract.ContactEntry.SQL_CREATE_CONTACTS_TABLE);

        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactContract.ContactEntry.COLUMN_NAME, name);
        contentValues.put(ContactContract.ContactEntry.COLUMN_NUMBER, res_number);

        sqLiteDatabase.insert(ContactContract.ContactEntry.TABLE_NAME,null,contentValues);
        sqLiteDatabase.close();
    }
    public void refresh_table(String name,String number)
    {
        String res_number="";
        if(number!=null){
            res_number = number.replaceAll("[-:,()]","").trim();
            res_number=res_number.replaceAll(" ","");}
        sqLiteDatabase=contactsDbHelper.getWritableDatabase();
        if(flag==0)
        {
            sqLiteDatabase.execSQL("DELETE FROM "+ ContactContract.ContactEntry.TABLE_NAME) ;
            flag=1;
        }
        sqLiteDatabase.execSQL(ContactContract.ContactEntry.SQL_CREATE_CONTACTS_TABLE);

        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactContract.ContactEntry.COLUMN_NAME, name);
        contentValues.put(ContactContract.ContactEntry.COLUMN_NUMBER, res_number);

        sqLiteDatabase.insert(ContactContract.ContactEntry.TABLE_NAME,null,contentValues);
        sqLiteDatabase.close();
    }
}

