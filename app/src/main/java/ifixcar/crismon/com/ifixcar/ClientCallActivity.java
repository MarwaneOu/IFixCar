package ifixcar.crismon.com.ifixcar;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ifixcar.crismon.com.ifixcar.Model.DataSender;
import ifixcar.crismon.com.ifixcar.Model.HistoryInfo;
import ifixcar.crismon.com.ifixcar.Model.Notification;
import ifixcar.crismon.com.ifixcar.Model.Token;
import ifixcar.crismon.com.ifixcar.Utils.Utils;
import ifixcar.crismon.com.ifixcar.WebServices.IDirectionAPI;
import ifixcar.crismon.com.ifixcar.WebServices.IFCMService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClientCallActivity extends AppCompatActivity {


    private TextView timeView;
    private TextView distanceView;
    private TextView addressView;

    private Button acceptCall;
    private Button refuseCall;
    private MediaPlayer mediaPlayer;
    private IDirectionAPI directionAPI;
    private IFCMService iFCMService;

    private String clientId;
    private double latitude;
    private double longtitude;

    private HistoryInfo historyInfo;
    private DatabaseReference history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_call);

        // init UI
        timeView=(TextView)findViewById(R.id.text_time);
        distanceView=(TextView)findViewById(R.id.text_distance);
        addressView=(TextView)findViewById(R.id.text_address);

        //init webservices
        directionAPI= Utils.getDirectionAPI();
        iFCMService=Utils.getFCMCLient();
        // rigntone

        // init db
        history= FirebaseDatabase.getInstance().getReference(Utils.HISTORY);

        mediaPlayer=MediaPlayer.create(this,R.raw.ringtone);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();


        acceptCall=(Button)findViewById(R.id.accept_btn);
        acceptCall.setOnClickListener(view ->
        {
            historyInfo.setIsAccepted("Accepted");
            history.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().setValue(historyInfo);

            if(!clientId.isEmpty())
            {
                Token token= new Token(clientId);
                Map<String ,String> data= new HashMap<>();
                data.put("title","Accept");
                data.put("message","The Driver has accepted your call !");
                DataSender sender = new DataSender(token.getToken(),data);
                iFCMService.respondToCall(sender).enqueue(new Callback<ifixcar.crismon.com.ifixcar.Model.Response>() {
                    @Override
                    public void onResponse(Call<ifixcar.crismon.com.ifixcar.Model.Response> call, Response<ifixcar.crismon.com.ifixcar.Model.Response> response)
                    {

                        if(response.isSuccessful())
                        {
                            showSnackBar("You accepted the Call !");
                            finish();
                        }
                        else
                            showSnackBar("Something went wrong !");
                    }

                    @Override
                    public void onFailure(Call<ifixcar.crismon.com.ifixcar.Model.Response> call, Throwable t)
                    {
                        Log.d("Message", t.getMessage());
                    }
                });
            }
            // start tracking Activity !!
            Intent intent = new Intent(ClientCallActivity.this,ClientTrackerActivity.class);
            intent.putExtra("LATITUDE",latitude);
            intent.putExtra("LONGTITUDE",longtitude);
            intent.putExtra("CLIENT_ID",clientId);
            startActivity(intent);
            finish();
        });
        refuseCall=(Button)findViewById(R.id.refuse_btn);
        refuseCall.setOnClickListener(view ->
        {
            historyInfo.setIsAccepted("Declined");
            history.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().setValue(historyInfo);
              if(!clientId.isEmpty())
              {

                  Token token= new Token(clientId);
                  Map<String ,String> data= new HashMap<>();
                  data.put("title","Cancel");
                  data.put("message","The Driver refused your Call!");
                  DataSender sender = new DataSender(token.getToken(),data);
                  iFCMService.respondToCall(sender).enqueue(new Callback<ifixcar.crismon.com.ifixcar.Model.Response>() {
                      @Override
                      public void onResponse(Call<ifixcar.crismon.com.ifixcar.Model.Response> call, Response<ifixcar.crismon.com.ifixcar.Model.Response> response)
                      {

                              if(response.isSuccessful())
                              {
                                  showSnackBar("You canceled the Call !");
                                  finish();
                              }
                              else
                                  showSnackBar("Something went wrong !");
                      }

                      @Override
                      public void onFailure(Call<ifixcar.crismon.com.ifixcar.Model.Response> call, Throwable t)
                      {
                          Log.d("Message", t.getMessage());
                      }
                  });
              }
        });


        if( getIntent()!=null)
        {
             latitude= Double.valueOf(getIntent().getStringExtra("LATITUDE"));
             longtitude= Double.valueOf(getIntent().getStringExtra("LONGTITUDE"));
             clientId=getIntent().getStringExtra("CLIENT_ID");
             getDirection(latitude,longtitude);

        }


    }

    private void showSnackBar(String msg)
    {
        Snackbar.make(acceptCall,msg,Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.blueSky)).setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        }).show();
    }

    private void getDirection(double latitude, double longtitude)
    {
        String apiUrl;
        try {
            apiUrl ="https://maps.googleapis.com/maps/api/directions/json?"
                    +"mode=driving&"+"transit_routing_preference=less_driving&"+
                    "origin="+Utils.lastLocation.getLatitude()+","+Utils.lastLocation.getLongitude()+
                     "&"+"destination="+latitude+","+longtitude+"&"
                    +"key="+getResources().getString(R.string.google_direction_api);

              Log.d("ApUrl", apiUrl);
               directionAPI.getClientInfos(apiUrl).enqueue(new Callback<ResponseBody>() {
                   @Override
                   public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                       try
                       {
                           historyInfo= new HistoryInfo();
                           JSONObject jsonObject= new JSONObject(response.body().string().toString());
                           JSONArray routes= jsonObject.getJSONArray("routes");
                           JSONObject firstObject=routes.getJSONObject(0);
                           JSONArray legs=firstObject.getJSONArray("legs");
                           JSONObject firstLeg= legs.getJSONObject(0);

                           JSONObject distance=firstLeg.getJSONObject("distance");
                           JSONObject duration=firstLeg.getJSONObject("duration");
                            String endAddress=firstLeg.getString("end_address");

                           Log.d("distance", distance.getString("text"));
                           Log.d("duration", duration.getString("text"));

                           // set history
                           historyInfo.setAddress(endAddress);
                           historyInfo.setDistance(distance.getString("text"));
                           historyInfo.setDuration(duration.getString("text"));
                           // populate UI
                           timeView.setText(duration.getString("text"));
                           distanceView.setText(distance.getString("text"));
                           addressView.setText(endAddress);

                       }
                       catch (JSONException e)
                       {
                           Log.d("error here:",e.getMessage());
                       } catch (IOException e)
                       {
                           Log.d("IOException", e.getMessage());
                       }
                   }

                   @Override
                   public void onFailure(Call<ResponseBody> call, Throwable t) {

                       Log.d("Onfailure", t.getMessage());
                   }
               });



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release();
    }




}
