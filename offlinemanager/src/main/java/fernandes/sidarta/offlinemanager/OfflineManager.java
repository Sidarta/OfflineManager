package fernandes.sidarta.offlinemanager;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sidarta buda on 2/27/2017.
 */

public class OfflineManager {

    private final View mView;
    private final Context mContext;

    public enum DeviceOfflineTreatment {
        Enforce,
        Flexible,
        Transparent
    }

    public enum ServerOfflineTreatment {
        NoAction,
        Flexible,
        Transparent
    }

    public interface CustomCallbackSuccess {
        void responseCallback(Call call, Response response);
    }

    public interface CustomCallbackFail {
        void failCallback(Call call, Throwable t);
    }

    /**
     *
     * @param objCall call to be executed
     * @param deviceOfflineTreatment type of device treatment
     * @param interSuccess sucess callback
     * @param interFail fail callback
     * @param <T> ??
     */
    public <T> void treatedCall(final Call<T> objCall,
                                       final DeviceOfflineTreatment deviceOfflineTreatment,
                                       final ServerOfflineTreatment serverOffTreatment,
                                       final int tries,
                                       final boolean verbose,
                                       final CustomCallbackSuccess interSuccess,
                                       final CustomCallbackFail interFail
                                       ) {



        if(tries < 0) return;

        if(!this.isOnline(mContext)) {
            //does not have connection
            switch (deviceOfflineTreatment){
                case Enforce:
                    //enforce we only give a feedback and stop the call, because connection is mandatory
                    Snackbar.make(mView, R.string.enforce_no_connection_feedback, Snackbar.LENGTH_LONG).show();
                    break;

                case Flexible:
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setTitle(R.string.flexible_dialog_connection_title)
                            .setMessage(R.string.flexible_dialog_connection_message)
                            .setPositiveButton(R.string.pop_up_yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    handleCallOffline(objCall, deviceOfflineTreatment, serverOffTreatment, verbose, tries,
                                            interSuccess, interFail, mContext, mView);

                                    if(verbose)
                                        Snackbar.make(mView, R.string.flexible_yes_selected_device_offline, Snackbar.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton(R.string.pop_up_no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                    // on flexible, is user clicks cancel maybe we can show show a snack bar saying the action was not made?

                                    if(verbose)
                                        //verbose mode
                                        Snackbar.make(mView, R.string.flexible_no_selected_device_offline, Snackbar.LENGTH_LONG).show();

                                }
                            })
                            .show();
                    break;

                case Transparent:
                    handleCallOffline(objCall, deviceOfflineTreatment, serverOffTreatment, verbose, tries,
                            interSuccess, interFail, mContext, mView);
                    //transparent mode by default have no feedback messages
                    break;
            }

        } else {
            //have connection, proceed and treat server offline cases
            objCall.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    try {
                        //success on call does not need to have feedback message, even on verbose mode
                        interSuccess.responseCallback(call, response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //treat server offline cases here, as number os tries
                @Override
                public void onFailure(final Call<T> call, Throwable t) {
                    interFail.failCallback(call, t);

                    switch (serverOffTreatment){
                        case NoAction:
                            if(verbose)
                                Snackbar.make(mView, R.string.no_action, Snackbar.LENGTH_LONG).show();
                            break;

                        //-1 on tries because first job schedule already counts as a try
                        case Flexible:
                            AlertDialog dialog = new AlertDialog.Builder(mContext)
                                    .setTitle(R.string.flexible_dialog_server_retry_title)
                                    .setMessage(R.string.flexible_dialog_server_retry_message)
                                    .setPositiveButton(R.string.pop_up_yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            handleServerOffline(0, call.clone(), deviceOfflineTreatment, serverOffTreatment, verbose, tries-1, interSuccess, interFail, mContext, mView);
                                            if(verbose)
                                                Snackbar.make(mView, R.string.flexible_yes_selected_server_offline, Snackbar.LENGTH_LONG).show();
                                        }
                                    })
                                    .setNegativeButton(R.string.pop_up_no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(verbose)
                                                Snackbar.make(mView, R.string.flexible_no_selected_server_offline, Snackbar.LENGTH_LONG).show();
                                        }
                                    })
                                    .show();
                            break;
                        case Transparent:
                            handleServerOffline(0, call.clone(), deviceOfflineTreatment, serverOffTreatment, verbose, tries-1, interSuccess, interFail, mContext, mView);
                            break;
                    }
                }
            });
        }
    }

    private void handleCallOffline(final Call call,
                                         final DeviceOfflineTreatment deviceOffTreatment,
                                         final ServerOfflineTreatment serverOffTreatment,
                                         final boolean verbose,
                                         final int retries,
                                         final CustomCallbackSuccess interSuccess,
                                         final CustomCallbackFail interFail,
                                         final Context context,
                                         final View view){
        //gerando o job id
        final Random rand = new Random(System.currentTimeMillis());
        int jobId = rand.nextInt(Integer.MAX_VALUE-1) + 1;

        //armazeno a call
        CallsQueue queue = (CallsQueue)context.getApplicationContext();
        queue.putCallsCallbacks(jobId, call, interSuccess, interFail, context, view, deviceOffTreatment, serverOffTreatment, verbose, retries);


        //agendo o job pra quando tiver connection
        scheduleJobConnection(jobId, context);
    }

    void handleServerOffline(int jobId,
                                final Call call,
                                final DeviceOfflineTreatment deviceOffTreatment,
                                final ServerOfflineTreatment serverOffTreatment,
                                final boolean verbose,
                                final int retries,
                                final CustomCallbackSuccess interSuccess,
                                final CustomCallbackFail interFail,
                                final Context context,
                                final View view){
        if(jobId == 0) {
            //gerando o job id
            final Random rand = new Random(System.currentTimeMillis());
            jobId = rand.nextInt(Integer.MAX_VALUE);
        }

        CallsQueue queue = (CallsQueue)context.getApplicationContext();
        queue.putCallsCallbacks(jobId, call, interSuccess, interFail, context, view, deviceOffTreatment, serverOffTreatment, verbose, retries);

        //agendo o job pra quando tiver connection + timeout
        scheduleJobConnectionAndTimeout(jobId, context);
    }

    //another signature, pra facilitar chamada recursiva no service
    void handleServerOffline(int jobId, CallsCallbacks cb) {
        if(jobId == 0) {
            //gerando o job id
            final Random rand = new Random(System.currentTimeMillis());
            jobId = rand.nextInt(Integer.MAX_VALUE);
        }

        CallsQueue queue = (CallsQueue)cb.getContext().getApplicationContext();
        queue.putCallsCallbacks(jobId, cb);

        //agendo o job pra quando tiver connection
        scheduleJobConnectionAndTimeout(jobId, cb.getContext());
    }

    private void scheduleJobConnection(int jobId, Context context){
        //verifica versao
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP ) {
            OneoffTask myTask = new OneoffTask.Builder()
                    .setService(OfflineManagerServiceLollipop.class)
                    .setRequiredNetwork(OneoffTask.NETWORK_STATE_CONNECTED)
                    .setExecutionWindow(0, 1)
                    .setTag(Integer.toString(jobId))
                    .build();
            GcmNetworkManager.getInstance(context).schedule(myTask);
        } else {
            JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo job = new JobInfo.Builder(
                    jobId,
                    new ComponentName(context, OfflineManagerService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build();
            js.schedule(job);
        }

    }

    private void scheduleJobConnectionAndTimeout(int jobId, Context context){
        //verifica versao
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP ) {
            OneoffTask myTask = new OneoffTask.Builder()
                    .setService(OfflineManagerServiceLollipop.class)
                    .setExecutionWindow(
                            3 , 5)
                    .setTag(Integer.toString(jobId))
                    .setRequiredNetwork(OneoffTask.NETWORK_STATE_CONNECTED)
                    .build();
            GcmNetworkManager.getInstance(context).schedule(myTask);
        } else {
            JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo job = new JobInfo.Builder(
                    jobId,
                    new ComponentName(context, OfflineManagerService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setMinimumLatency(3000)
                    .build();
            js.schedule(job);
        }
    }

    //check internet connection
    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //return netInfo != null && netInfo.isConnectedOrConnecting();
        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Builder class for offline manager
     */
    public static class Builder {
        private final Context context;
        private final View view;

        public Builder(Context context, View view){
            this.context = context;
            this.view = view;
        }

        public OfflineManager build(){
            return new OfflineManager(this);
        }
    }

    private OfflineManager(Builder builder){
        this.mContext = builder.context;
        this.mView = builder.view;
    }
}
