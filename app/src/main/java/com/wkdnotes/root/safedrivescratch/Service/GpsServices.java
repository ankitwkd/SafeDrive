package com.wkdnotes.root.safedrivescratch.Service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.Voice;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import com.wkdnotes.root.safedrivescratch.MainActivity;
import com.wkdnotes.root.safedrivescratch.R;
import com.wkdnotes.root.safedrivescratch.Util.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class GpsServices extends Service implements LocationListener, GpsStatus.Listener, SensorEventListener
{
    private LocationManager mLocationManager;

    Location lastlocation = new Location("last");
    Data data;

    double currentLon=0 ;
    double currentLat=0 ;
    double lastLon = 0;
    double lastLat = 0;

    PendingIntent contentIntent;

    private List<Location> loc;
    private List<Float> speed;
    private double AccProbability;
    private float curSpeed;
    private double brakingDistance;

    Intent intent;
    Timer timer;
    myTimerTask myTask;

    /////////////////////////Accelerometer///////////////////////////////////////////////////////////////////////////////
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mLastX, mLastY, mLastZ;


    public static boolean flag = false;
    public static int countTime = 0;
    public static int countJerk = 0;

    public static float x;
    public static float y;
    public static float z;
    private Handler handler;
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onTaskRemoved(Intent rootIntent) {

        super.onTaskRemoved(rootIntent);

        stopSelf();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0);

        updateNotification(false);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addGpsStatusListener( this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);


        //intiialising both lists
        speed = new ArrayList<Float>();
        loc = new ArrayList<Location>();

        //initialsing voice recognition class
        intent=new Intent(GpsServices.this, VoiceRecognition.class);

        ///////////////////////////////////////////////Accelerometer////////////////////////////////////////
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        ////////////////////////////////////////////////////////////////////////////////////////////////////


    }

    class myTimerTask extends TimerTask {

        @Override
        public void run() {
            if(Data.checkOK==false){
                sendMySMS();
            }
            else
                Data.checkOK=false;
            stopService(intent);
        }
    }

    public void sendMySMS()
    {
        SmsManager sms = SmsManager.getDefault();

        String lati= String.valueOf(lastlocation.getLatitude());
        String longi=String.valueOf(lastlocation.getLongitude());
        //Toast.makeText(getApplicationContext(),"SMS SENT",Toast.LENGTH_SHORT).show();
        String msgs="Your friend has asked for emergency help through SafeDrive app, location link: "+"http://maps.google.com/maps?q="+lati+","+longi;
        List<String> messages = sms.divideMessage(msgs);
        for (String msg : messages) {
            PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
            sms.sendTextMessage("+917064007719", null, msg, null, null);
            sms.sendTextMessage("+919140037768", null, msg, null, null);
        }
    }

    public void onLocationChanged(Location location) {
//TODO JERKTOAST        Toast.makeText(this,String.valueOf(countJerk),Toast.LENGTH_SHORT).show();
        data = MainActivity.getData();
        if (data.isRunning()) {
            currentLat = location.getLatitude();
            currentLon = location.getLongitude();

            if (data.isFirstTime()) {
                lastLat = currentLat;
                lastLon = currentLon;
                data.setFirstTime(false);
            }

            lastlocation.setLatitude(lastLat);
            lastlocation.setLongitude(lastLon);
            double distance = lastlocation.distanceTo(location);

            if (location.getAccuracy() < distance) {
                data.addDistance(distance);

                lastLat = currentLat;
                lastLon = currentLon;
            }

            if (location.hasSpeed()) {
                data.setCurSpeed(location.getSpeed() * 3.6);
                Data.speedForTraffic = location.getSpeed() * 3.6;

                if (location.getSpeed() == 0) {
                    new isStillStopped().execute();
                }

                //calculation of accProbability
                if (speed.size() == 50) {

                    speed.remove(0);
                    loc.remove(0);
                }
                curSpeed = location.getSpeed() * 3.6f;
                loc.add(location);
                speed.add(curSpeed);
                if (curSpeed == 0.0f) {

                    int i = 0;
                    for (Float s : speed) {
                        brakingDistance = Math.pow(curSpeed, 2.0) / (250 * 0.8);
                        float actualdist = location.distanceTo(loc.get(i));
                        if (actualdist < brakingDistance) {
                            AccProbability = (brakingDistance - actualdist) / brakingDistance;
                            Toast.makeText(getApplicationContext(), String.valueOf(AccProbability), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        i++;
                    }

                    loc.clear();
                    speed.clear();
                }


            }
            if (AccProbability > 0.5 && countJerk>=3) {
                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sos);

                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        startService(intent);
                        if (timer != null)
                            timer.cancel();

                        timer = new Timer();
                        myTask = new myTimerTask();
                        timer.schedule(myTask, 10000);
                    }
                });


            }

            data.update();
            updateNotification(true);
        }
    }


    public void updateNotification(boolean asData){
        Notification.Builder builder = new Notification.Builder(getBaseContext())
                .setContentTitle("Safe Drive")
                .setSmallIcon(R.mipmap.car)
                .setContentIntent(contentIntent);


        if(asData){
            String speedunit,distanceunit;
            if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("miles_per_hour",false))
            {
                speedunit="mi/h";
                distanceunit="mi";
            }
            else
            {
                speedunit="km/h";
                distanceunit="km";
            }
            builder.setContentText("Max speed: "+ String.format("%.0f", data.getMaxSpeed())+" "+speedunit+" "+ "Distance: "+String.format("%.0f", data.getDistance()/1000.0f)+distanceunit);
        }else{
            builder.setContentText(String.format(getString(R.string.notification), '-', '-'));
        }
        Notification notification = builder.build();
        startForeground(R.string.noti_id, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //////////////////////////////////Accelerometer///////////////////////////////////////////////////
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);








        //////////////////////////////////////////////////////////////////////////////////////////////////


        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    /* Remove the locationlistener updates when Services is stopped */
    @Override
    public void onDestroy() {
        ////////////////////////Accelerometer///////////////////////////////////////////////////////
        mSensorManager.unregisterListener(this);
        ////////////////////////////////////////////////////////////////////////////////////////////


        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
        stopForeground(true);
    }

    @Override
    public void onGpsStatusChanged(int event) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    class isStillStopped extends AsyncTask<Void, Integer, String> {
        int timer = 0;
        @Override
        protected String doInBackground(Void... unused) {
            try {
                while (data.getCurSpeed() == 0) {
                    Thread.sleep(1000);
                    timer++;
                }
            } catch (InterruptedException t) {
                return ("The sleep operation failed");
            }
            return ("return object when task is finished");
        }

        @Override
        protected void onPostExecute(String message) {
            data.setTimeStopped(timer);
        }
    }
    //////////////////////////////////////////Accelerometer//////////////////////////////////////////////
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        } else {
            mLastX = x;
            mLastY = y;
            mLastZ = z;

            if ((flag==false)&&((Math.abs(x) > 13 && Math.abs(y) > 13) || (Math.abs(y) > 13 && Math.abs(z) > 13) || (Math.abs(z) > 13 && Math.abs(x) > 13)))
            {
                flag = true;

                handler = new Handler();
                final Runnable r = new Runnable()
                {
                    public void run()
                    {
                        countTime++;
                        if(countTime<=5)
                        {
                            if ((Math.abs(x) > 13 && Math.abs(y) > 13) || (Math.abs(y) > 13 && Math.abs(z) > 13) || (Math.abs(z) > 13 && Math.abs(x) > 13))
                            {
                                countJerk++;
                            }

                            handler.postDelayed(this,2000);
                        }
                        else if(countTime>5)
                        {

                            countJerk=0;
                            flag=false;
                            countTime=0;
                            x=0;
                            y=0;
                            z=0;
                        }

                    }
                };
                handler.postDelayed(r, 0);

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }




    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

