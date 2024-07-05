package com.inspace.plugin;

public class DeteckManager {
   public static   Detect detect;

    public interface Detect{

        void onDetect(String msg);
    }

    public void onDetect(String msg){
        if(detect!=null){
            detect.onDetect(msg);
        }
    }

}
