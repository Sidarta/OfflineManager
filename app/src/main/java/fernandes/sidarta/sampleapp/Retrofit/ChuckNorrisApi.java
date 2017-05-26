package fernandes.sidarta.sampleapp.Retrofit;

import fernandes.sidarta.sampleapp.Model.QuoteResult;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by Sidarta on 26/05/17.
 */

public interface ChuckNorrisApi {

    String BASE_URL = "http://api.icndb.com/";

    Retrofit RETROFIT = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build();

    @GET("/jokes/random")
    Call<QuoteResult> getQuote();
}
