package ifixcar.crismon.com.ifixcar.WebServices;

import ifixcar.crismon.com.ifixcar.Model.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;

public interface IDirectionAPI {


    @GET
    public Call<ResponseBody> getClientInfos(@Url String url);
} 
