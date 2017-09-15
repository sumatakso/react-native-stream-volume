package com.reactlibrary;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNVolumeModule extends ReactContextBaseJavaModule{
    private static final String TAG = "Volume";
    private AudioManager audio;
    public ReactContext mReactContext;
    public boolean willNotify = true; 
    private DeviceEventManagerModule.RCTDeviceEventEmitter mJSModule = null;
    private boolean isAttachOnMusic = false;

    public RNVolumeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        isAttachOnMusic = Build.VERSION.SDK_INT <= Build.VERSION_CODES.M;
        mReactContext = reactContext;
        audio = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
        getReactApplicationContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, new VolumeListener(null,getReactApplicationContext()));
    }

    @Override
    public String getName() {
        return "RNVolume";
    }

    @ReactMethod
    public void acivateListner() {
    }

    /**
     * Call this method first to adjust volume of the audio
     * <= Build.VERSION_CODES.M api devince
     */
    @ReactMethod
    public void adjustVolume(){
        if(isAttachOnMusic) {
            audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                audio.getStreamVolume(AudioManager.STREAM_MUSIC), 0);
        }
    }

    @ReactMethod
    public void getVolume(Callback callback) {
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        if(isAttachOnMusic) {
            currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        callback.invoke(((float) currentVolume / maxVolume));
        //callback.invoke(null,((String) "Hello"));
    }

    @ReactMethod
    public void setVolume(int value,boolean onVolumeChangeNotify) {
        this.willNotify = onVolumeChangeNotify;
        audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL,value,0);
        if(isAttachOnMusic)
            audio.setStreamVolume(AudioManager.STREAM_MUSIC,value,0);
    }

    public class VolumeListener extends ContentObserver {
        private Context context;
        private Handler handler;

        //private DeviceEventManagerModule.RCTDeviceEventEmitter mJSModule = null;
        private DeviceEventManagerModule.RCTDeviceEventEmitter mJSModule = null;

        public VolumeListener(Handler handler,Context context) {
            super(handler);
            this.context= context;

        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (
                uri.compareTo(Uri.parse("content://settings/system/volume_music_speaker")) == 0 ||
                uri.compareTo(Uri.parse("content://settings/system/volume_music_hdmi")) == 0) {
                    AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    float currentVolume = audio.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                    float maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
                    if(isAttachOnMusic){
                        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                        maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    }
                    //Log.d("TEST now " , String.valueOf(currentVolume/maxVolume));
                    if (mJSModule == null) {
                        mJSModule = mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
                    }

                    if(willNotify == true){
                        mJSModule.emit("onVolumeChange",currentVolume/maxVolume);
                    }
                    willNotify = true;
            }

        }
    }

}