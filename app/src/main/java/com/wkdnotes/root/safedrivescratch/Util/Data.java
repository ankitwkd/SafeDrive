package com.wkdnotes.root.safedrivescratch.Util;



public class Data {
    public static boolean checkOK;
    private boolean isRunning; //To check if the service is running
    private long time;
    private long timeStopped;// To calulate the duration of halting
    private boolean isFirstTime;
    private boolean isDndOn;
    public static boolean turnDndOff;
    public static boolean turnDndOn;
    public static boolean isMediaOn;
    private double distanceM;
    private double curSpeed;
    private double maxSpeed;
    public static boolean isWarningAlarmOn;
    public static double speedForTraffic=0.0;


    //interface to be overridden
    public interface onGpsServiceUpdate{
        public void update();
    }

    private onGpsServiceUpdate onGpsServiceUpdate; //object of above interface


    //Link onGpsServiceUpdate of Main Activity to Data's.
    public void setOnGpsServiceUpdate(onGpsServiceUpdate onGpsServiceUpdate){
        this.onGpsServiceUpdate = onGpsServiceUpdate;
    }

    //To be overridden wherever used.
    public void update(){
        onGpsServiceUpdate.update();
    }

    //Initialising the data. (Default Constructor
    public Data() {
        isRunning = false;
        distanceM = 0;
        curSpeed = 0;
        maxSpeed = 0;
        timeStopped = 0;
        isWarningAlarmOn=false;
        isDndOn=false;
        turnDndOff=false;
        turnDndOn=true;
        isMediaOn=false;
    }
    //Parameterised Constructor
    public Data(onGpsServiceUpdate onGpsServiceUpdate){
        this();
        setOnGpsServiceUpdate(onGpsServiceUpdate);
    }


    public void addDistance(double distance){
        distanceM = distanceM + distance;
    }

    public double getDistance(){
        return distanceM;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public boolean isDndOn() {
        return isDndOn;
    }

    public void setDndOn(boolean dndOn) {
        isDndOn = dndOn;
    }

    //Average speed excluding halting time.
    public double getAverageSpeedMotion(){
        double motionTime = time - timeStopped;
        double average;
        String units;
        if (motionTime <= 0){               //If timestopped is same or more than time elapsed.
            average = 0.0;
        } else {
            average = (distanceM / (motionTime / 1000)) * 3.6;
        }
        return average;
    }

    public void setCurSpeed(double curSpeed) {
        this.curSpeed = curSpeed;
        if (curSpeed > maxSpeed){
            maxSpeed = curSpeed;
        }
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public void setFirstTime(boolean isFirstTime) {
        this.isFirstTime = isFirstTime;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void setTimeStopped(long timeStopped) {
        this.timeStopped += timeStopped;
    }

    public double getCurSpeed() {
        return curSpeed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
