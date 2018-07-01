package ifixcar.crismon.com.ifixcar.WebServices;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private  static Retrofit client=null;

    public  static  Retrofit getClient(String url)
    {
        if(client==null)
            client= new Retrofit.Builder().baseUrl(url)
                                 .addConverterFactory(GsonConverterFactory.create())
                                 .build();
        return  client;
    }
} 
