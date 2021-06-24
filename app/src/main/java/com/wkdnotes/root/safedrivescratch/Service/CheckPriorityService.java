package com.wkdnotes.root.safedrivescratch.Service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.wkdnotes.root.safedrivescratch.Database.ContactsDbHelper;
import com.wkdnotes.root.safedrivescratch.Receiver.PhoneStateReceiver;


public class CheckPriorityService extends Service{
    Context context;
    public Runnable mRunnable = null;
    public Intent intent_call;
    static String TAG=CheckPriorityService.class.getSimpleName();

    public CheckPriorityService()
    {

    }


    //constructor to receive the context passed from the calling activity
    public CheckPriorityService(Context context) {
        this.context = context;
    }
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String number=intent.getStringExtra("Number");
        PhoneStateReceiver phoneStateReceiver=new PhoneStateReceiver();
        SharedPreferences sharedPreferen= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        intent_call=new Intent(getApplicationContext(), EnableRingtoneService.class);//media player service
        ContactsDbHelper contactsDbHelper=new ContactsDbHelper(context);
        boolean result=contactsDbHelper.check_(getApplicationContext(),number);

        if(result)//If the contact is in priority list then
        {
            Log.i(TAG,"Contains in priority");
            startService(intent_call);//Media Player

        }
        //not in priority list
        else
        {
            Log.i(TAG,"Not in priority");

            PhoneStateReceiver.killCall(context);

            //Send SMS to the non priority callers
            try {

                if(sharedPreferen.getBoolean("sms_feature",false)==true)
                {
                    SmsManager smsManager=SmsManager.getDefault();

                    String message=sharedPreferen.getString("custom_sms","Hello,your friend is driving.Please call him later!!");
                    String msgs=message;

                    smsManager.sendTextMessage(number,null,msgs,null,null);
                    Toast.makeText(getApplicationContext(),"Sms Sent",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(),"Enable Sms Feature from the settings",Toast.LENGTH_SHORT).show();
            }
            catch (SecurityException e)
            {
                Log.e(CheckPriorityService.class.getSimpleName(),e.getMessage());
            }
        }
        return START_STICKY;
    }

}
