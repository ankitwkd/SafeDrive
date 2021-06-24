package com.wkdnotes.root.safedrivescratch;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.PointerSpeedometer;

import com.google.gson.Gson;

import com.melnykov.fab.FloatingActionButton;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import com.wang.avi.AVLoadingIndicatorView;
import com.wkdnotes.root.safedrivescratch.ContactPreview.ShowPriorityContacts;
import com.wkdnotes.root.safedrivescratch.Receiver.PhoneStateReceiver;
import com.wkdnotes.root.safedrivescratch.Service.CheckPriorityService;
import com.wkdnotes.root.safedrivescratch.Service.GpsServices;
import com.wkdnotes.root.safedrivescratch.Service.EnableRingtoneService;
import com.wkdnotes.root.safedrivescratch.Service.TextToSpeechService;
import com.wkdnotes.root.safedrivescratch.Service.VoiceRecognition;
import com.wkdnotes.root.safedrivescratch.Settings.SettingsActivity;
import com.wkdnotes.root.safedrivescratch.Util.Data;


import java.util.Locale;




public class MainActivity extends AppCompatActivity implements LocationListener,GpsStatus.Listener {



    //Declaration of Views
    private Chronometer time;
    private PointerSpeedometer speedometer;
    private FloatingActionButton fab;
    private FloatingActionButton res;
    private TextView distance;
    private TextView status;
    private ImageView cellNetwork;
   // private FloatingActionButton settingButton;
    private ImageView iv_settings;
    private int initial_media_volume;
    //Flag variables
    private boolean firstfix;
    public static boolean first=false; //Used to pop gps on dialog only once at first run



    //Speed functionality variables
    private static Data data; //static Data object to access all members of Data.java
    private Data.onGpsServiceUpdate onGpsServiceUpdate;
    private SharedPreferences sharedPreferences;//Shared preferences required to fetch settings data
    private double maxSpeedTemp; //To extract max speed from Data object in update()
    private double distanceTemp; //To extract distance covered from Data object in update()
    private double averageTemp;  //To calculate average speed from getAverageSpeed() or getAverageSpeedMotion()
    private String speedUnits;//speed unit in speedometer gauge
    private String distanceUnits;//distance unit in speedometer gauge
    private TextView maxSpeed;
    private ImageView iv_map;//image view to open map activity
    private TextView averageSpeed;
    private LocationManager mLocationManager;
    public static String satStatus; //fetching satellite status and Toasting it on button click
    private String limit; //fetching warning speed limit from settings preferences
    private boolean warning_pref; //fetching warning speed limit preference from settings

    //DND feature variables
    private NotificationManager notificationManager;
    private AudioManager audioManager; //used to modify ringer mode (normal or dnd)
    private ComponentName componentName; //used to enable and disable phone state receiver
    private SoundPool soundPool;//used to trigger media play on crossing speed limit
    private boolean loaded; //used to notify if sound pool feature is ready
    private int soundID; //used to identify loaded sound pool

    //initial beep sound
    private SoundPool initialSound;
    private boolean initialLoaded;
    private int initialSoundID;


    //Text to speech
    private static final int TTS_CHECK_CODE =101; //used as request code for tts download if not found
    private AVLoadingIndicatorView avi;
    public static int ctr=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //disabling Phone state receiver if enabled by default
        disablePhoneStateReceiver();

        //download tts if not available
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_CHECK_CODE);

        //initialising Audio Manager object and Sound Pool object
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        soundID = soundPool.load(this, R.raw.warning ,1);

        //initial beep sound
        initialSound=new SoundPool(1,AudioManager.STREAM_MUSIC,0);
        initialSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                initialLoaded=true;
            }
        });
        initialSoundID=initialSound.load(this,R.raw.htc_alarm_beep,1);



        //Initial media volume
        initial_media_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        dndTask();

        work();



    }

    private void disablePhoneStateReceiver() {

        PackageManager packageManager = MainActivity.this.getPackageManager();
        componentName = new ComponentName(MainActivity.this, PhoneStateReceiver.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void dndTask() {
        //Initialising notification manager object to check for notification policy access
        notificationManager=(NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //set ringer mode as silent for phones with android version lower than Marshmallow
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M)
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

        else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {

                //TODO Check with shivam what to do with if condition
                if (!notificationManager.isNotificationPolicyAccessGranted()) {
                    //Asking for DND access permission, if already granted, then make the phone silent
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("DND-ACCESS");
                    alertDialogBuilder.setMessage("Please grant do not disturb access permission.");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Allow dnd-access", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(intent);
                        }

                    });
                    alertDialogBuilder.show();

                }
        }

    }
    //Text to speech utility
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance


            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }

    }

    private void work() {


        //initialise settings image view
        iv_settings = (ImageView)findViewById(R.id.iv_settings);
        iv_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        //initialise map image view
        iv_map = (ImageView)findViewById(R.id.iv_map);
        iv_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MapsActivity.class));
            }
        });


        //initialising progress bar
        avi=(AVLoadingIndicatorView)findViewById(R.id.avi);
        avi.setVisibility(View.VISIBLE);
        status = (TextView) findViewById(R.id.status);
        status.setText(getResources().getString(R.string.waiting_for_fix));

        //initialising timer
        time = (Chronometer) findViewById(R.id.timeValue);
        time.setVisibility(View.GONE);


        //initialising values of speedometer
        speedometer = (PointerSpeedometer) findViewById(R.id.pointerSpeedometer);
        speedometer.setMaxSpeed(280);
        speedometer.setMinSpeed(0);
        speedometer.setWithTremble(false);
        speedometer.setTickNumber(10);
        speedometer.speedTo(0);
        speedometer.setVisibility(View.INVISIBLE); //invisible until gps is set.
        //speedometer.setTickPadding(0);

        data = new Data(onGpsServiceUpdate); //Created a new object of Data and setting onGpsServiceUpdate to it

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Gets a SharedPreferences instance that points to the default file that is used by the preference
        // framework in the given context.


        //Fab to play/pause
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE); //invisible till gps is set.

        //Fab to reset all data
        res = (FloatingActionButton) findViewById(R.id.reset);
        res.setVisibility(View.INVISIBLE);//invisible till gps is set.






        //Initialising text views
        maxSpeed = (TextView) findViewById(R.id.maxSpeedValue);
        averageSpeed = (TextView) findViewById(R.id.averageSpeedValue);
        

        distance = (TextView) findViewById(R.id.distanceValue);



        cellNetwork = (ImageView) findViewById(R.id.cellNetwork);

        //Creating object for interface Data.onGpsServiceUpdate and overriding update()
        onGpsServiceUpdate = new Data.onGpsServiceUpdate() {
            @Override
            public void update() {

                //Extracting max speed, distance and average speed
                maxSpeedTemp = data.getMaxSpeed();
                distanceTemp = data.getDistance();
                averageTemp = data.getAverageSpeedMotion(); //excluding time spent in waiting


                //Checking for units preference- km/hr or miles/hr
                if (sharedPreferences.getBoolean("miles_per_hour", false)) {
                    maxSpeedTemp *= 0.62137119;
                    distanceTemp = distanceTemp / 1000.0 * 0.62137119;
                    averageTemp *= 0.62137119;
                    speedUnits = "mi/h";
                    distanceUnits = "mi";

                } else {
                    speedUnits = "km/h";
                    speedometer.setUnit(speedUnits);
                    if (distanceTemp <= 1000.0) {
                        distanceUnits = " m";
                    } else {
                        distanceTemp /= 1000.0;
                        distanceUnits = "km";
                    }
                }
                speedometer.setUnit(speedUnits);

                //Setting text for max speed, average speed and distance
                SpannableString s = new SpannableString(String.format("%.0f", maxSpeedTemp) + speedUnits);
                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 4, s.length(), 0);
                maxSpeed.setText(s);

                s = new SpannableString(String.format("%.0f", averageTemp) + speedUnits);
                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 4, s.length(), 0);
                averageSpeed.setText(s);

                s = new SpannableString(String.format("%.3f", distanceTemp) + distanceUnits);
                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 2, s.length(), 0);
                distance.setText(s);


            }
        };

        //Obtain an instance of Location Manager class
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Setting time

        time.setText("00:00:00");
        Typeface font=Typeface.createFromAsset(getAssets(),"font/digital.ttf");
        time.setTypeface(font);
        time.setTextColor(getResources().getColor(R.color.cyan_darker));
        time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            boolean isPair = true;

            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time;
                if (data.isRunning()) {
                    time = SystemClock.elapsedRealtime() - chronometer.getBase(); //se
                    data.setTime(time);
                } else
                    time = data.getTime();

                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                chronometer.setText(hh + ":" + mm + ":" + ss);

                if (data.isRunning()) {
                    chronometer.setText(hh + ":" + mm + ":" + ss);
                } else {
                    if (isPair) {                   //isPair=True which means Timer has already started
                        isPair = false;
                        chronometer.setText(hh + ":" + mm + ":" + ss);
                    } else {
                        isPair = true;
                        chronometer.setText("");        //isPair=False which means Timer has not always started
                    }
                }
            }
        });

    }





    @TargetApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        warning_pref=sharedPreferences.getBoolean("warning_speed_limit",false);
        Intent intent = new Intent(getApplicationContext(), VoiceRecognition.class);
        if(!sharedPreferences.getBoolean("voice_feature",false))
        {
            stopService(intent);
        }
        else
        {
            if(data.isRunning())
                startService(intent);

        }



//        if (notificationManager.isNotificationPolicyAccessGranted())
//        {
//            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),1);
//        }

        firstfix=true;

        //If service has not started, convert String json into object of Data type
        if(!data.isRunning()) {
            Gson gson = new Gson();
            String json = sharedPreferences.getString("data", "");
            data = gson.fromJson(json, Data.class);
        }
        if(data==null)
            data=new Data(onGpsServiceUpdate);          //initialise data object
        else
            data.setOnGpsServiceUpdate(onGpsServiceUpdate); //for already initialised data object, set onGpsServiceUpdate
         if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        } else {
            Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
        }

        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !first)
        {
            showGpsDisabledDialog();
            first=true;
        }


        mLocationManager.addGpsStatusListener(this);

        if (sharedPreferences.getBoolean("miles_per_hour", false))
            speedUnits="mi/h";
        else
            speedUnits="km/h";
        speedometer.setUnit(speedUnits);
        if(speedUnits=="mi/h")
            speedometer.setMaxSpeed(140);
        else
            speedometer.setMaxSpeed(280);
        speedometer.setTickNumber(10);
    }


    @Override
    public void onPause(){
        super.onPause();
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        prefsEditor.putString("data", json);

        prefsEditor.commit();
    }

    @Override
    public void onDestroy()
    {
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,initial_media_volume,0);
        super.onDestroy();
        Log.i("onDestroy","GOT CALLED");
        stopService(new Intent(getBaseContext(), VoiceRecognition.class));
        stopService(new Intent(getBaseContext(), TextToSpeechService.class));
        stopService(new Intent(getBaseContext(), EnableRingtoneService.class));
        stopService(new Intent(getBaseContext(), CheckPriorityService.class));
        disablePhoneStateReceiver();
        res.performClick();


    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.hasAccuracy())  //check if location has horizontal accuracy
        {
            //Get the estimated horizontal accuracy of this location, radial, in meters.(68%)
            SpannableString s = new SpannableString(String.format("%.0f", location.getAccuracy()) + "m");
            s.setSpan(new RelativeSizeSpan(0.75f), s.length() - 1, s.length(), 0);
//            accuracy.setText(s);

            if (firstfix) {              //firstfix is true, data is initialised and it has registered for location updates
                status.setText("");
                fab.setVisibility(View.VISIBLE);
                if (!data.isRunning() && !maxSpeed.getText().equals("")) //If service is not started but max speed is set.
                {
                    res.setVisibility(View.VISIBLE);
                }
                firstfix = false;
            }
        } else {
            firstfix = true;
        }

        if (location.hasSpeed()) {

            avi.setVisibility(View.GONE);
            status.setText("");
            speedometer.setVisibility(View.VISIBLE);
            time.setVisibility(View.VISIBLE);
            time.bringToFront();

            String speed = String.format(Locale.ENGLISH, "%.0f", location.getSpeed() * 3.6) + "km/h";

            if (sharedPreferences.getBoolean("miles_per_hour", false)) { // Convert to MPH
                speed = String.format(Locale.ENGLISH, "%.0f", location.getSpeed() * 3.6 * 0.62137119) + "mi/h";
            }
            SpannableString s = new SpannableString(speed);
            s.setSpan(new RelativeSizeSpan(0.25f), s.length() - 4, s.length(), 0);
            //TODO currentSpeed.setText(s);


    if(ctr<3)
        speedometer.speedTo(speedometer.getMaxSpeed());
    else if(ctr < 4)
    {
        speedometer.speedTo(0);
        if(initialLoaded)
            initialSound.play(initialSoundID,1.0f,1.0f, 1, 0, 0.9f);
    }
    else if(ctr>6)
    {
        initialSound.stop(initialSoundID);
        speedometer.speedTo(location.getSpeed()*3.6f);
    }
    ctr++;

            //warning speed limit

            limit=sharedPreferences.getString("custom_speed","40");

            if(warning_pref && data.isRunning() && !Data.isWarningAlarmOn && speedometer.getCurrentSpeed()>=Float.parseFloat(limit) )
            {
                if(loaded)//soundpool load on oncreate
                {
                    Data.isWarningAlarmOn=true;
                    soundPool.play(soundID,1.0f,1.0f, 1, 1, 0.9f);
                    Toast.makeText(this, "Plays loop", Toast.LENGTH_SHORT).show();
                }
                // Data.isWarningAlarmOn=false;
            }
            else
            {
                if(speedometer.getCurrentSpeed()<Float.parseFloat(limit) && Data.isWarningAlarmOn)
                {
                    soundPool.stop(soundID);
                    Data.isWarningAlarmOn=false;
                }
            }
            Float custom_dnd_speed;
            custom_dnd_speed=Float.parseFloat(sharedPreferences.getString("custom_dnd_speed","40"));





            if (speedometer.getCurrentSpeed() >= custom_dnd_speed && data.isRunning() && !data.isDndOn() && Data.turnDndOn) {
                Toast.makeText(getApplicationContext(), String.valueOf(speedometer.getCurrentSpeed()), Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (notificationManager.isNotificationPolicyAccessGranted()) {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                        data.setDndOn(true);

                        PackageManager packageManager = MainActivity.this.getPackageManager();
                        componentName = new ComponentName(MainActivity.this, PhoneStateReceiver.class);
                        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);

                        Toast.makeText(getApplicationContext(), "RUNNING", Toast.LENGTH_LONG).show();
                    }


                }


            } else if ((speedometer.getCurrentSpeed() < custom_dnd_speed && data.isRunning() && data.isDndOn()) || (!data.isRunning() && data.isDndOn()) || (Data.turnDndOff && data.isDndOn())) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
              disablePhoneStateReceiver();
                if (Data.turnDndOff && data.isDndOn()) {
                    Data.turnDndOff = false;
                    Data.turnDndOn = false;
                }
                data.setDndOn(false);

            }


        }
    }
    public void onGpsStatusChanged (int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                @SuppressLint("MissingPermission") GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                int satsInView = 0;
                int satsUsed = 0;
                Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
                for (GpsSatellite sat : sats) {
                    satsInView++;
                    if (sat.usedInFix()) {
                        satsUsed++;
                    }
                }
//                satellite.setText(String.valueOf(satsUsed) + "/" + String.valueOf(satsInView));


                //setting Cell Netork Strength


                satStatus=String.valueOf(satsUsed) + "/" + String.valueOf(satsInView);
                 if((satsUsed/(satsInView*1.0))*100.0>=75.0)
                     cellNetwork.setImageDrawable(getResources().getDrawable(R.drawable.cell_tower_4));
                 else if((satsUsed/(satsInView*1.0))*100.0>=50.0)
                    cellNetwork.setImageDrawable(getResources().getDrawable(R.drawable.cell_tower_3));
                 else if((satsUsed/(satsInView*1.0))*100>=25.0)
                     cellNetwork.setImageDrawable(getResources().getDrawable(R.drawable.cell_tower_2));
                 else
                     cellNetwork.setImageDrawable(getResources().getDrawable(R.drawable.cell_tower_1));

                if (satsUsed == 0) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
                    data.setRunning(false);
                    status.setText("");
                    stopService(new Intent(getBaseContext(), GpsServices.class));
                    fab.setVisibility(View.INVISIBLE);
                    res.setVisibility(View.INVISIBLE);
               //     accuracy.setText("");
                    status.setText(getResources().getString(R.string.waiting_for_fix));
                    firstfix = true;
                }
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    showGpsDisabledDialog();
                }

                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
        }
    }

    private void showGpsDisabledDialog() {
        //Dialog dialog = new Dialog(this, getResources().getString(R.string.gps_disabled), getResources().getString(R.string.please_enable_gps));

//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        alertDialogBuilder.setTitle(getResources().getString(R.string.gps_disabled));
//        alertDialogBuilder.setMessage(getResources().getString(R.string.please_enable_gps));
//        alertDialogBuilder.setCancelable(false);
//        alertDialogBuilder.setPositiveButton("Turn on gps", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
//
//            }
//
//        });
//        alertDialogBuilder.show();
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("GPS")
                .setContentText("Your gps is turned off!")
                .setConfirmText("Turn on gps!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));

                        sDialog.dismissWithAnimation();
//                        sDialog
//                                .setTitleText("Gps is on!")
//                                .setContentText("Continue working!")
//                                .setConfirmText("OK")
//                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                    @Override
//                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                        sweetAlertDialog.dismissWithAnimation();
//                                    }
//                                })
//                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    }
                })
                .show();
//        dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
//            }
//        });
//        dialog.show();
    }

    public static Data getData() {
        return data;
    }

    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }




    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void resetCall(View view) {
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
        res.setVisibility(View.INVISIBLE);
        time.stop();
        maxSpeed.setText("");
        averageSpeed.setText("");
        distance.setText("");
        time.setText("00:00:00");
        data = new Data(onGpsServiceUpdate);

        stopService(new Intent(getBaseContext(), GpsServices.class));

    }

    //Set chronometer's base time and start service/stop service
    public void fabClick(View view) {
        if (!data.isRunning()) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
            data.setRunning(true);
            time.setBase(SystemClock.elapsedRealtime() - data.getTime());
            time.start();
            data.setFirstTime(true);

            startService(new Intent(getBaseContext(), GpsServices.class));
            res.setVisibility(View.INVISIBLE);

            if(sharedPreferences.getBoolean("voice_feature",false))
            {
                Intent intent = new Intent(getApplicationContext(), VoiceRecognition.class);
                startService(intent);
            }

        }
        else{
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
            data.setRunning(false);
            status.setText("");
            //status.setText("Tracking Paused");
            stopService(new Intent(getBaseContext(), GpsServices.class));
            stopService(new Intent(getBaseContext(),VoiceRecognition.class));
            res.setVisibility(View.VISIBLE);


        }
    }


    public void satelliteStatus(View view) {

        int x=(int)cellNetwork.getX();
        int y=(int)cellNetwork.getY();

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_view,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        TextView text = (TextView) layout.findViewById(R.id.sat);
        text.setText(satStatus);
        Toast toast=Toast.makeText(getApplicationContext(),satStatus,Toast.LENGTH_SHORT);

        toast.setView(layout);
        //toast.setGravity(0,x,y);
        toast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 0, 0);
        toast.show();
    }


    public void priority(View view) {
        Toast.makeText(getApplicationContext(),"CLICKED",Toast.LENGTH_LONG).show();
        Intent intent=new Intent(MainActivity.this, ShowPriorityContacts.class);
        startActivity(intent);
    }

}

