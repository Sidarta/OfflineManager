package fernandes.sidarta.sampleapp;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import fernandes.sidarta.offlinemanager.OfflineManager;
import fernandes.sidarta.sampleapp.Model.QuoteResult;
import fernandes.sidarta.sampleapp.Retrofit.ChuckNorrisApi;
import fernandes.sidarta.sampleapp.Retrofit.InvalidApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SampleApp.MainActivity";

    AppCompatTextView mQuoteTV;
    Button mGetQuoteBtn, mGetInvalidBtn;

    //Retrofit APIs
    ChuckNorrisApi mChuckNorrisApi;
    InvalidApi mInvalidApi;

    //OfflineManager
    OfflineManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mQuoteTV = (AppCompatTextView) findViewById(R.id.textview_main_quotetext);
        mGetQuoteBtn = (Button) findViewById(R.id.button_main_getquote);
        mGetInvalidBtn = (Button) findViewById(R.id.button_main_getinvalid);

        //retrofit initialization
        mChuckNorrisApi = ChuckNorrisApi.RETROFIT.create(ChuckNorrisApi.class);
        mInvalidApi = InvalidApi.RETROFIT.create(InvalidApi.class);

        //initializing offlineManager
        mManager = new OfflineManager.Builder(this, mGetQuoteBtn)
        .maxTimeoutSeconds(3) //on builder you can specify custom timeout times for retries
        .build();

        mGetQuoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //separating creation of a retrofit call
                Call getQuoteCall = mChuckNorrisApi.getQuote();

                /**
                //using retrofit enqueue directly
                retrofitCall.enqueue(new Callback<QuoteResult>() {
                    @Override
                    public void onResponse(Call<QuoteResult> call, Response<QuoteResult> response) {
                        if(response.isSuccessful()){
                            QuoteResult result = response.body();
                            mQuoteTV.setText(result.getValue().getJoke());
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                Snackbar.make(mGetQuoteBtn, errorBody, Snackbar.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<QuoteResult> call, Throwable t) {
                        Snackbar.make(mGetQuoteBtn, t.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
                */

                /*
                 * Using OfflineManager API
                 * We pass the ways we want the call to be treated both relating to when device has no internet connection
                 * and to when server seems to be offline (onFailure callback received).
                 * We also pass number of retries we want
                 *
                 * mManager.treatedCall(call, deviceMode, serverMode, retries, isVerbose, callbackSuccess, callbackFailure)
                 */
                mManager.treatedCall(
                        getQuoteCall, //retrofit call, already initialized with the method/parameters to be used
                        OfflineManager.DeviceOfflineTreatment.Flexible, //Flexible mode - user interaction via pop up
                        OfflineManager.ServerOfflineTreatment.Flexible, //Flexible mode - user interaction via pop up
                        5,          //number of retries in case of server offline
                        true,       //verbose - true: all interactions will give a feedback message to the user
                        new OfflineManager.CustomCallbackSuccess() {        //implement response callback here
                            @Override
                            public void responseCallback(Call call, Response response) {
                                //we check if its success and set quote text
                                if(response.isSuccessful()){
                                    QuoteResult result = (QuoteResult) response.body();
                                    mQuoteTV.setText(result.getValue().getJoke());
                                } else {
                                    try {
                                        String errorBody = response.errorBody().string();
                                        Snackbar.make(mGetQuoteBtn, errorBody, Snackbar.LENGTH_LONG).show();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        },
                        new OfflineManager.CustomCallbackFail() {           //implement failure callback here
                            @Override
                            public void failCallback(Call call, Throwable t) {
                                Snackbar.make(mGetQuoteBtn, t.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                );
            }
        });

        mGetInvalidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //separate call initialization
                Call getInvalidCall = mInvalidApi.getSomething();

                /**
                //Using Retrofit directly
                getInvalidCall.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        //not gonna be successful because this endpoint does not exists
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Snackbar.make(mGetQuoteBtn, t.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
                */

                /*
                 * Using OfflineManager API
                 * We pass the ways we want the call to be treated both relating to when device has no internet connection
                 * and to when server seems to be offline (onFailure callback received).
                 * We also pass number of retries we want
                 *
                 * mManager.treatedCall(call, deviceMode, serverMode, retries, isVerbose, callbackSuccess, callbackFailure)
                 */
                mManager.treatedCall(
                        getInvalidCall,
                        OfflineManager.DeviceOfflineTreatment.Enforce,
                        OfflineManager.ServerOfflineTreatment.Flexible,
                        3,
                        true,
                        new OfflineManager.CustomCallbackSuccess() {
                            @Override
                            public void responseCallback(Call call, Response response) {
                                //not gonna be successful because this endpoint does not exists
                            }
                        },
                        new OfflineManager.CustomCallbackFail() {
                            @Override
                            public void failCallback(Call call, Throwable t) {
                                //Logging failiure callbacks here,
                                //User messages are automatically handled by OfflineManager
                                Log.d(TAG, "fail callback");
                            }
                        }
                );
            }
        });

    }
}