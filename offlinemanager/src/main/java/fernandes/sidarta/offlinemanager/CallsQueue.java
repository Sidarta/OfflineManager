package fernandes.sidarta.offlinemanager;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

/**
 * Created by Sids-Monitora on 3/7/2017.
 */


/**
 * CallsQueue is a static hash map that stores an job ID that identifies the job responsible for calling the call again when parameter are met,
 * and also stores an instance of CallsCallbacks - which represents the call and context for that call (callback success and fail, context and view)
 */
class CallsQueue extends Application{

    private Map<Integer, CallsCallbacks> hashMap;
    private static final String TAG = "CallsQueue";

    public CallsQueue(){
        hashMap = new HashMap<>();
    }

    public void putCallsCallbacks(int id,
                                  Call call,
                                  OfflineManager.CustomCallbackSuccess callbackSuccess,
                                  OfflineManager.CustomCallbackFail callbackFail,
                                  Context context,
                                  View view,
                                  OfflineManager.DeviceOfflineTreatment deviceOfflineTreatment,
                                  OfflineManager.ServerOfflineTreatment serverOfflineTreatment,
                                  boolean verbose,
                                  int retries){


        Log.d(TAG , "Put Queue");
        hashMap.put(id, new CallsCallbacks(call, callbackSuccess, callbackFail, context, view, deviceOfflineTreatment, serverOfflineTreatment, verbose, retries));
    }

    public void putCallsCallbacks(int jobid, CallsCallbacks cb) {
        hashMap.put(jobid, cb);
    }

    public CallsCallbacks getCallCallback(int id){
        Log.d(TAG , "Get Queue CB");
        return hashMap.get(id);
    }

    public void removeCallCallback(int id){
        hashMap.remove(id);
        Log.d(TAG , "Remove Queue CB");
    }

    public Map<Integer, CallsCallbacks> getHashMap() {
        return hashMap;
    }
}
