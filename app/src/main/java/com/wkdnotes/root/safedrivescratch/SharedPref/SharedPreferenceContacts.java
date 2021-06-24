package com.wkdnotes.root.safedrivescratch.SharedPref;


        import android.content.Context;
        import android.content.SharedPreferences;



public class SharedPreferenceContacts {

    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;
    private static final String PREF_NAME = "Contacts";
    // Shared pref mode
    int PRIVATE_MODE = 0;

    private static final String Contacts_Loaded="Loaded";
    private static final String CheckPriority="Check";
    public SharedPreferenceContacts(Context context)
    {
        this._context=context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    public void onSuccessLoad(int value)
    {
        if(value==1)
            editor.putBoolean(Contacts_Loaded,true);
        else
            editor.putBoolean(Contacts_Loaded,false);
        editor.commit();
    }
    public boolean checkLoad()
    {
        if(!this.isLoggedIn())//loggined failed {
            return false;
        return true;
    }
    public boolean isLoggedIn()
    {

        return pref.getBoolean(Contacts_Loaded,false);
    }
    //check variable in database
    public void setCheck()
    {
        editor.putBoolean(CheckPriority,true);
        editor.commit();
    }
    public void resetCheck()
    {
        editor.putBoolean(CheckPriority,false);
        editor.commit();
    }
    public boolean getCheck()
    {
        if(pref.getBoolean(CheckPriority,false))
            return true;
        else
            return false;

    }
}
