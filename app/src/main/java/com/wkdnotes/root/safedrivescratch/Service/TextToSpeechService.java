package com.wkdnotes.root.safedrivescratch.Service;



import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;


import com.wkdnotes.root.safedrivescratch.R;

import java.util.Locale;

public class TextToSpeechService extends Service implements TextToSpeech.OnInitListener
{
    private final String TAG = "TextToSpeechService";
    private TextToSpeech tts;
    private String number;
    private Handler handler;
    private boolean isInit;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate()
    {
        tts=new TextToSpeech(this,this, TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        handler=new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onCreate();

        number = intent.getStringExtra("number");
        handler.removeCallbacksAndMessages(null);

        if (isInit) {
            speak();
        }
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                stopSelf();
            }
        }, 15*3000);

        return TextToSpeechService.START_NOT_STICKY;
    }
    //Text to speech onInit called when the initialization of text-to-speech engine completes
    @Override
    public void onInit(int status) {
        Toast.makeText(getApplicationContext(),"onInit CALLED",Toast.LENGTH_SHORT).show();

        if (status == TextToSpeech.SUCCESS) {
            Toast.makeText(getApplicationContext(),"status is success",Toast.LENGTH_SHORT).show();
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
            {
                setSpeed();
                Toast.makeText(getApplicationContext(),"LANG SUPPORTED",Toast.LENGTH_SHORT).show();
                speak();
                isInit = true;

            } else
                Toast.makeText(getApplicationContext(), "Language not supported", Toast.LENGTH_SHORT).show();

        }
        else if(status==TextToSpeech.ERROR)
            Toast.makeText(getApplicationContext(),"FAILURE",Toast.LENGTH_SHORT).show();
    }
    private void setSpeed()
    {
        Toast.makeText(getApplicationContext(),"SPEED OF SPEECH SET",Toast.LENGTH_SHORT).show();
        tts.setSpeechRate(0.85f);
    }

    //text to speech speak function
    private void speak()
    {

        Toast.makeText(getApplicationContext(),"IN SPEAK FUNCTION",Toast.LENGTH_SHORT).show();
        if (tts != null) {

            String name_from_number=getContactName(number,getApplicationContext());
            Toast.makeText(getApplicationContext(),"reached"+name_from_number,Toast.LENGTH_SHORT).show();
            if(name_from_number.isEmpty()) {
                String new_number = convertNumber(number);
                tts.speak("Incoming Call from " + new_number, TextToSpeech.QUEUE_FLUSH, null);
            }
            else if(name_from_number.length()>0)
                Toast.makeText(getApplicationContext(),String.valueOf(name_from_number.length()),Toast.LENGTH_SHORT).show();
            tts.speak("Incoming Call from "+name_from_number, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    //function for converting number 1234 to 1 2 3 4
    private String convertNumber(String number)//1234 to 1 2 3 4
    {
        String n="";
        for(int i=0;i<number.length();i++)
            n=n + Character.toString(number.charAt(i))+' ';
        Log.i(TAG,n);
        return n;
    }

    //Get contact name from number if exist
    public String getContactName(final String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }
    @Override
    public void onDestroy()
    {
        if(tts!=null){
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();

    }

}