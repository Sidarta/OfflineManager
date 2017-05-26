package fernandes.sidarta.offlinemanager;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Sidarta on 5/12/17.
 */

public class OfflineManagerService extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        final CallsQueue queue = (CallsQueue)getApplicationContext();
        final int jobId = jobParameters.getJobId();

        final CallsCallbacks cb = queue.getCallCallback(jobId);

        final OfflineManager manager = new OfflineManager.Builder(cb.getContext(), cb.getView()).build();

        cb.getCall().enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                //if verbose - show feedback message and pass control to callback
                if(cb.isVerbose())
                    Snackbar.make(cb.getView(), R.string.service_onresponse, Snackbar.LENGTH_LONG).show();
                cb.getCallbackSuccess().responseCallback(call, response);
                queue.removeCallCallback(jobId);
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                final int retries = cb.getRetries();

                cb.getCallbackFail().failCallback(call, t); //retrying or not, we need to toss callback for fine control on the application

                if(retries == 0) {
                    if(cb.isVerbose())
                        Snackbar.make(cb.getView(), R.string.last_retry_finished, Snackbar.LENGTH_LONG).show();
                    queue.removeCallCallback(jobId);

                } else {
                    switch (cb.getServerOfflineTreatment()){
                        case NoAction:
                            //will only reach here if there is retries, but mode is NO ACTION
                            //in this case only a message will show - BUT this case should not happen
                            //-> makes no sense to try again if mode is no action
                            if(cb.isVerbose())
                                Snackbar.make(cb.getView(), R.string.no_action, Snackbar.LENGTH_LONG).show();
                            queue.removeCallCallback(jobId);
                            break;
                        case Flexible:
                            AlertDialog dialog = new AlertDialog.Builder(cb.getContext())
                                    .setTitle(R.string.flexible_dialog_server_retry_title)
                                    .setMessage(R.string.flexible_dialog_server_retry_message)
                                    .setPositiveButton(R.string.pop_up_yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Call newCallCloned = cb.getCall().clone();
                                            cb.setCall(newCallCloned);
                                            cb.setRetries(retries - 1);
                                            manager.handleServerOffline(jobId, cb);
                                            if(cb.isVerbose())
                                                Snackbar.make(cb.getView(), R.string.flexible_yes_selected_server_offline, Snackbar.LENGTH_LONG).show();
                                        }
                                    })
                                    .setNegativeButton(R.string.pop_up_no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(cb.isVerbose())
                                                Snackbar.make(cb.getView(), R.string.flexible_no_selected_server_offline, Snackbar.LENGTH_LONG).show();
                                            queue.removeCallCallback(jobId);
                                        }
                                    })
                                    .show();
                            break;
                        case Transparent:
                            Call newCallCloned = cb.getCall().clone();
                            cb.setCall(newCallCloned);
                            cb.setRetries(retries - 1);
                            manager.handleServerOffline(jobId, cb);
                            break;
                    }

                }
            }
        });

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
