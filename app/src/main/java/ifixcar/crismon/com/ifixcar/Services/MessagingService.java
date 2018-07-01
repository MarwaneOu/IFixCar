package ifixcar.crismon.com.ifixcar.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

import ifixcar.crismon.com.ifixcar.ClientCallActivity;
import ifixcar.crismon.com.ifixcar.R;
import ifixcar.crismon.com.ifixcar.Utils.NotificationHelper;
import ifixcar.crismon.com.ifixcar.Utils.Utils;

public class MessagingService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {

        Map<String,String> data=remoteMessage.getData();
        String clientId=data.get("token");
        String latitude=data.get("latitude");
        String longtitude=data.get("longtitude");




        Intent intent = new Intent(getBaseContext(),ClientCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("LATITUDE",latitude);
        intent.putExtra("LONGTITUDE",longtitude);
        intent.putExtra("CLIENT_ID",clientId);
        startActivity(intent);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            notifyMeForOreo("New Call","You have a new client's Call");
        else
            notifyMe( new Intent());

    }

     private  void notifyMe(Intent intent)
     {
         PendingIntent pendingIntent=PendingIntent.getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_ONE_SHOT);
         Notification.Builder builder= new Notification.Builder(getBaseContext());
         builder.setAutoCancel(true)
                 .setWhen(System.currentTimeMillis())
                 .setContentTitle("New Call")
                 .setContentText("You have a new client's Call")
                 .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                 .setSmallIcon(R.drawable.car_notify)
                 .setContentIntent(pendingIntent);



         NotificationManager manager= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
         manager.notify(0,builder.build());
     }

     @RequiresApi(api = Build.VERSION_CODES.O)
     private  void notifyMeForOreo(String title, String content)
     {
         PendingIntent pendingIntent=PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
         NotificationHelper helper= new NotificationHelper(getBaseContext());
         Notification.Builder builder=helper.builder(title,content,pendingIntent);
         helper.getManager().notify(1,builder.build());

     }
}
