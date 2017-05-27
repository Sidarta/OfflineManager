package fernandes.sidarta.sampleapp;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import fernandes.sidarta.sampleapp.Model.QuoteResult;
import fernandes.sidarta.sampleapp.Retrofit.ChuckNorrisApi;
import fernandes.sidarta.sampleapp.Retrofit.InvalidApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    AppCompatTextView mQuoteTV;
    Button mGetQuoteBtn, mGetInvalidBtn;

    ChuckNorrisApi mChuckNorrisApi;
    InvalidApi mInvalidApi;

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

        mGetQuoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChuckNorrisApi.getQuote().enqueue(new Callback<QuoteResult>() {
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
            }
        });

        mGetInvalidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInvalidApi.getSomething().enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        //not gonna be successful
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Snackbar.make(mGetQuoteBtn, t.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });

    }
}