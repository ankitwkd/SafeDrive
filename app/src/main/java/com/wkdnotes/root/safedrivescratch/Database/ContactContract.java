package com.wkdnotes.root.safedrivescratch.Database;



import android.provider.BaseColumns;



public final class ContactContract {

    private ContactContract(){};

    public static final class ContactEntry implements BaseColumns{
        public final static String TABLE_NAME = "priority";
        public final static String Table_Priority_Contacts="table_priority_contacts";

        /**
         * Unique ID number for the pet (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME ="name";
        public final static String COLUMN_NUMBER = "number";

        public  static final String SQL_CREATE_CONTACTS_TABLE =  "CREATE TABLE IF NOT EXISTS " + ContactEntry.TABLE_NAME + " ("
                + ContactContract.ContactEntry._ID + " INTEGER AUTO INCREMENT, "
                + ContactContract.ContactEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ContactContract.ContactEntry.COLUMN_NUMBER + " TEXT PRIMARY KEY);";

        public final static String SQL_CREATE_PRIORITY_CONTACTS=
                "CREATE TABLE IF NOT EXISTS "+Table_Priority_Contacts +" ("
                        +_ID + " INTEGER AUTO INCREMENT, "
                        +COLUMN_NAME + " TEXT NOT NULL,  "
                        +COLUMN_NUMBER + " TEXT PRIMARY KEY);";
    }
}

