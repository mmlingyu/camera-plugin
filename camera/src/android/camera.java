package com.inspace.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import android.app.Activity;
import org.apache.cordova.PluginResult;
/**
 * This class echoes a string called from JavaScript.
 */
public class camera extends CordovaPlugin {
    // Service that keeps the app awake

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Toast.makeText(cordova.getActivity(), "------来了 老弟~", Toast.LENGTH_SHORT).show();
        if (action.equals("init")) {
           /* Activity context = cordova.getActivity();
            Intent intent = new Intent(context, CameraActivity.class);
            Log.e("camera",action);
            context.startActivity(intent);*/
            // CameraService.startToStartRecording(context, 1,null)
            /* Intent intent = new Intent(context, CameraService.class);
            try {
                context.bindService(intent, connection, BIND_AUTO_CREATE);
                fireEvent(Event.ACTIVATE, null);
                context.startService(intent);
            } catch (Exception e) {

            }*/
        }
        else if (action.equals("coolMethod")) {
            Activity context = cordova.getActivity();
            Intent intent = new Intent(context, CameraActivity.class);
            Log.e("------------------------",action);
            context.startActivity(intent);
           String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        return false;
    }

    private CallbackContext callbackContext;
    private void coolMethod(String message, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        DeteckManager.detect = new DeteckManager.Detect() {
            @Override
            public void onDetect(String msg) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,msg);
                pluginResult.setKeepCallback(true);
                Log.e("onDetect------------------------",msg);
                callbackContext.sendPluginResult(pluginResult);
            }
        };
     /*   if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }*/





    }
}
