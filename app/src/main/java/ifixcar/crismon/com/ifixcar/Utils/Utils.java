package ifixcar.crismon.com.ifixcar.Utils;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import ifixcar.crismon.com.ifixcar.Model.User;
import ifixcar.crismon.com.ifixcar.WebServices.FCMRetrofitClient;
import ifixcar.crismon.com.ifixcar.WebServices.IDirectionAPI;
import ifixcar.crismon.com.ifixcar.WebServices.IFCMService;
import ifixcar.crismon.com.ifixcar.WebServices.RetrofitClient;

public class Utils {


    public  static  final  String BASE_URL="https://maps.googleapis.com";
    public  static  final  String FCM_URL="https://fcm.googleapis.com/";

    public  final  static  String DRIVERS="Drivers";
    public  final  static  String PICK_UP_REQUEST="PickUpRequest";
    public  final  static  String CLIENTS="Clients";
    public  final  static  String DRIVERS_LOCATION="locationOfDrivers";
    public  final  static  String TOKENS="Tokens";
    public static  final  String HISTORY="History";
    public static User currentUser=null;
    public  static Location lastLocation=null;

    public static String EMAIL="email";
    public static String PASSWORD="password";



    public static boolean isConnected(Context context)
    {

        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info= connectivityManager.getActiveNetworkInfo();
        return  info!=null&& info.isAvailable()&&info.isConnectedOrConnecting();
    }

    public  static IDirectionAPI getDirectionAPI()
    {
        return RetrofitClient.getClient(BASE_URL).create(IDirectionAPI.class);
    }
    public  static IFCMService getFCMCLient()
    {
        return FCMRetrofitClient.getClient(FCM_URL).create(IFCMService.class);
    }
} 
