package com.wkdnotes.root.safedrivescratch.Receiver;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


import com.wkdnotes.root.safedrivescratch.Service.CheckPriorityService;
import com.wkdnotes.root.safedrivescratch.Service.EnableRingtoneService;
import com.wkdnotes.root.safedrivescratch.Service.TextToSpeechService;

import java.lang.reflect.Method;



public class PhoneStateReceiver extends BroadcastReceiver {
    private final String TAG = PhoneStateReceiver.class.getSimpleName();
    public static String number;
    private static boolean ofHook = false;
    private Intent texttospeech_service, ringtone_service, check;

    @Override
    public void onReceive(final Context context, Intent intent) {


        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);//getting the state of the phone RINGING/IDLE/HOOKUP

        //Initialization of intents
        ringtone_service = new Intent(context, EnableRingtoneService.class);
        texttospeech_service = new Intent(context, TextToSpeechService.class);
        check = new Intent(context, CheckPriorityService.class);

        //if phone is ringing then cut the call
        if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {

            //check if priority table contains this number
            number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            check.putExtra("Number", number);
            context.startService(check);
            Log.i(TAG,"RINGING STATE");

            //phone received state that means the number is not in priority
        } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {

            if (EnableRingtoneService.flag == true) {//if true that means the number is not in priority and rings
                EnableRingtoneService.flag = false;
                ofHook = true;                       //stores the state of the phone i.e HOOKOFF
                EnableRingtoneService.mediaPlayer.stop();   //if ringing then stop the ringing

                Log.i(TAG, "OFF HOOK " + String.valueOf(EnableRingtoneService.flag));
            }
            Log.i(TAG,"OFF HOOK STATE");
        }
        // if the number is not in priority then controls falls this statement
        //phone cut state called idle state
        else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {

            Log.i(TAG, "IDLE " + String.valueOf(ofHook));

            if (EnableRingtoneService.flag == true) //if the user cut the priority phone calls
            {
                EnableRingtoneService.flag = false;
                EnableRingtoneService.mediaPlayer.stop();
                Log.i(TAG, "IDLE " + String.valueOf(EnableRingtoneService.flag));

            }
            else {
                if (ofHook == false) {


                    texttospeech_service.putExtra("number", number);
                    context.startService(texttospeech_service);//Notification and Text-to-Speech Service
                } else {
                    ofHook = false;
                }
            }

        }

    }

    //function for killing the call
    public static void killCall(Context context) {
        try {

            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";
            Class<?> telephonyClass;
            Class<?> telephonyStubClass;
            Class<?> serviceManagerClass;
            Class<?> serviceManagerNativeClass;
            Method telephonyEndCall;
            Object telephonyObject;
            Object serviceManagerObject;
            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.invoke(telephonyObject);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}