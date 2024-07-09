package com.inspace.plugin;

public class DeteckManager {
    public static   Detect detect;

    public interface Detect{

        void onDetect(String msg,float score);
    }

    public void onDetect(String msg,float score){
        if(detect!=null){
            detect.onDetect(msg,score);
        }
    }

}
