package fernandes.sidarta.sampleapp.Retrofit;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by Sidarta on 26/05/17.
 */

public interface InvalidApi {
    String BASE_URL = "http://1.1.1.1:8081/";

    Retrofit RETROFIT = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build();

    @GET("/something")
    Call<Object> getSomething();
}
