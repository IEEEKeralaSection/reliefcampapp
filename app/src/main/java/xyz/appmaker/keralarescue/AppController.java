package xyz.appmaker.keralarescue;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.appmaker.keralarescue.Tools.APIService;
import xyz.appmaker.keralarescue.Tools.Config;
import xyz.appmaker.keralarescue.Tools.PreferensHandler;

public class AppController extends Application {
    public static APIService RetrofitService;
    static Retrofit retrofit;
    public static PreferensHandler pref;
    public static OkHttpClient.Builder httpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        pref = new PreferensHandler(this);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient = new OkHttpClient.Builder();

        httpClient.connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS);

        httpClient.addInterceptor(logging);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);
                if (response.code() == 401) {
                    pref.setUserToken("");
//                    Toast.makeText(AppController.this,"Session expired",Toast.LENGTH_SHORT).show();
                    Intent actLogin = new Intent(AppController.this, MainActivity.class);
                    startActivity(actLogin);
                }
                return response;
            }
        });


    }

    public static APIService getRetrofitInstance() {
        if (retrofit == null) {


            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(APIService.class);
    }
}
