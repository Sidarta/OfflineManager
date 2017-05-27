package fernandes.sidarta.offlinemanager;

import android.content.Context;
import android.view.View;

import retrofit2.Call;

/**
 * Created by Sidarta on 3/31/17.
 */

/**
 * Class responsible to store call and respective callbacks, context and view.
 * This is based on the fact that a call will be retried and needs its context for when this happens.
 * Callbacks are used so user can control behaviour on success and on fail.
 */

class CallsCallbacks {

    private Call call;
    private OfflineManager.CustomCallbackSuccess callbackSuccess;
    private OfflineManager.CustomCallbackFail callbackFail;
    private Context context;
    private View view;
    private OfflineManager.DeviceOfflineTreatment deviceOfflineTreatment;
    private OfflineManager.ServerOfflineTreatment serverOfflineTreatment;
    private boolean verbose;
    private int retries;

    CallsCallbacks(Call call, OfflineManager.CustomCallbackSuccess callbackSuccess, OfflineManager.CustomCallbackFail callbackFail, Context context, View view, OfflineManager.DeviceOfflineTreatment deviceOfflineTreatment, OfflineManager.ServerOfflineTreatment serverOfflineTreatment, boolean verbose, int retries) {
        this.call = call;
        this.callbackSuccess = callbackSuccess;
        this.callbackFail = callbackFail;
        this.context = context;
        this.view = view;
        this.deviceOfflineTreatment = deviceOfflineTreatment;
        this.serverOfflineTreatment = serverOfflineTreatment;
        this.verbose = verbose;
        this.retries = retries;
    }

    Call getCall() {
        return call;
    }

    void setCall(Call call) {
        this.call = call;
    }

    OfflineManager.CustomCallbackSuccess getCallbackSuccess() {
        return callbackSuccess;
    }

    void setCallbackSuccess(OfflineManager.CustomCallbackSuccess callbackSuccess) {
        this.callbackSuccess = callbackSuccess;
    }

    OfflineManager.CustomCallbackFail getCallbackFail() {
        return callbackFail;
    }

    public void setCallbackFail(OfflineManager.CustomCallbackFail callbackFail) {
        this.callbackFail = callbackFail;
    }

    Context getContext() {
        return context;
    }

    void setContext(Context context) {
        this.context = context;
    }

    View getView() {
        return view;
    }

    void setView(View view) {
        this.view = view;
    }

    OfflineManager.DeviceOfflineTreatment getDeviceOfflineTreatment() {
        return deviceOfflineTreatment;
    }

    void setDeviceOfflineTreatment(OfflineManager.DeviceOfflineTreatment deviceOfflineTreatment) {
        this.deviceOfflineTreatment = deviceOfflineTreatment;
    }

    OfflineManager.ServerOfflineTreatment getServerOfflineTreatment() {
        return serverOfflineTreatment;
    }

    void setServerOfflineTreatment(OfflineManager.ServerOfflineTreatment serverOfflineTreatment) {
        this.serverOfflineTreatment = serverOfflineTreatment;
    }

    boolean isVerbose() {
        return verbose;
    }

    void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    int getRetries() {
        return retries;
    }

    void setRetries(int retries) {
        this.retries = retries;
    }
}
