package ifixcar.crismon.com.ifixcar.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

import ifixcar.crismon.com.ifixcar.Model.Notification;
import ifixcar.crismon.com.ifixcar.R;

/**
 * Created by ouardi15 on 28/02/2018.
 */
public class NotificationHelper extends ContextWrapper
{

    private NotificationManager manager;
    public  static  final String CHANNEL_Id="CHan01";
    public static  final String CHANNEL_NAME="CHan_Crismon";
    public NotificationHelper(Context base)
    {
        super(base);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            createChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel()
    {
        NotificationChannel channel= new NotificationChannel(CHANNEL_Id,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableVibration(true);
        channel.enableLights(true);
        channel.setLightColor(Color.GRAY);
        channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager()
    {
        if(manager==null)
            manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        return  manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public android.app.Notification.Builder builder(String title, String content, PendingIntent pendingIntent)
    {
        return  new android.app.Notification.Builder(getBaseContext(),CHANNEL_Id).setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.car_notify)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
    }
} 
