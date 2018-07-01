package ifixcar.crismon.com.ifixcar.WebServices;

import ifixcar.crismon.com.ifixcar.Model.DataSender;
import ifixcar.crismon.com.ifixcar.Model.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({"Content-Type:application/json",
            "Authorization:key=AAAASjqQw6o:APA91bHOBl2TK6e1UqXPpGrrQHwZAS1JJ3lo8XEZjrionAOY_WFCpyKBb0hPdjidUiJ9g5hKEXyFzl1TizmzOZsnVCl1tQso1OWSyTTyOVvL6PHUgBDFpK3-merKDlJtNueRboqOANiO"})
@POST("fcm/send")
    Call<Response> respondToCall(@Body DataSender sender);
        }
