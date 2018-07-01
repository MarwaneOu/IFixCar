package ifixcar.crismon.com.ifixcar;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import ifixcar.crismon.com.ifixcar.Model.DataSender;
import ifixcar.crismon.com.ifixcar.Model.Notification;
import ifixcar.crismon.com.ifixcar.Model.Token;
import ifixcar.crismon.com.ifixcar.Utils.DirectionsJSONParser;
import ifixcar.crismon.com.ifixcar.Utils.Utils;
import ifixcar.crismon.com.ifixcar.WebServices.IDirectionAPI;
import ifixcar.crismon.com.ifixcar.WebServices.IFCMService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ClientTrackerActivity extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private  static  final  int PLAY_SERVICE_CODE=800;

    private static  final  int UPDATE=5000;
    private static  final  int FASTEST=3000;
    private static  final  int DISPLACMENT=10;
    private Location lastLocation;

    private double longtitude;
    private  double latitude;
    private String clientId;

    private Marker driverMarker;
    private Circle clientMarker;
    private Polyline directionPolyline;
    private List<LatLng> listOfDirections;

    private IDirectionAPI directionAPI;
    private IFCMService iFCMService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_tracker);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // init Web services
        directionAPI= Utils.getDirectionAPI();
        iFCMService=Utils.getFCMCLient();


        if(getIntent()!=null)
        {
            latitude=getIntent().getDoubleExtra("LATITUDE",-1);
            longtitude=getIntent().getDoubleExtra("LONGTITUDE",-1);
            clientId=getIntent().getStringExtra("CLIENT_ID");
        }

        setUpLocation();
    }

    private void setUpLocation()
    {
        if(isGooglePlayServicesAvaiable(this))
        {
             buildApiClient();
             requestLocation();
             displayLocation();

        }
    }
    private void updateLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {

        mMap=googleMap;
        boolean isSuccess=mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_style_map));
        Log.d("isStyled!", String.valueOf(isSuccess));
        clientMarker=mMap.addCircle( new CircleOptions().center(new LatLng(latitude,longtitude))
                                                          .strokeWidth(5.0f)
                                                          .strokeColor(Color.BLUE)
                                                          .fillColor(0x220000EE)
                                                          .radius(50));

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude,longtitude))
                .title("Client")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.client_marker)));

        // track of arrival of the target driver
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference(Utils.DRIVERS_LOCATION);
        GeoFire geoFire= new GeoFire(ref);
        geoFire.queryAtLocation(new GeoLocation(latitude,longtitude),0.05f).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

            }

            @Override
            public void onKeyExited(String key) {
                sendArrivalNotification(clientId);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendArrivalNotification(String clientId) {
        Token token = new Token(clientId);
        Map<String ,String> data= new HashMap<>();
        data.put("title","Cancel");
        data.put("message",String.format("The Driver %s arrived to your place !", FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
        DataSender sender = new DataSender(token.getToken(),data);
        iFCMService.respondToCall(sender).enqueue(new Callback<ifixcar.crismon.com.ifixcar.Model.Response>() {
            @Override
            public void onResponse(Call<ifixcar.crismon.com.ifixcar.Model.Response> call, Response<ifixcar.crismon.com.ifixcar.Model.Response> response)
            {
                if(!response.isSuccessful())
                    Toast.makeText(getBaseContext(),"Failed!",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ifixcar.crismon.com.ifixcar.Model.Response> call, Throwable t) {
                Log.d("Exception!!!!!!", "onFailure: "+t.getMessage());
            }
        });

    }
    private void  buildApiClient()
    {
        googleApiClient= new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();

    }
    private void requestLocation()
    {
        locationRequest= new LocationRequest();
        locationRequest.setFastestInterval(FASTEST);
        locationRequest.setInterval(UPDATE);
        locationRequest.setSmallestDisplacement(DISPLACMENT);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private boolean isGooglePlayServicesAvaiable(Activity activity)
    {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, PLAY_SERVICE_CODE).show();
            }
            return false;
        }
        return true;
    }



    private void displayLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
       lastLocation=LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation!=null)
        {
            double latitude=lastLocation.getLatitude();
            double longtitude=lastLocation.getLongitude();

            if(driverMarker!=null)
                driverMarker.remove();
            driverMarker=mMap.addMarker( new MarkerOptions()
                              .position( new LatLng(latitude,longtitude))
                               .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                               .title("Me"));
            // move camera to current place;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longtitude),15.0f));

            if(directionPolyline!=null)
                directionPolyline.remove();
            getDirections();
        }


    }

    private void getDirections()
    {
        String apiUrl;

        //String destination="Hay Al Haouz, Marrakech 40130, Morocco";
        //destination=destination.replace(" ","+");
        LatLng nowLocation= new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        try {
            apiUrl ="https://maps.googleapis.com/maps/api/directions/json?"
                    + "mode=driving&" + "transit_routing_preference=less_driving&" +
                    "origin=" + nowLocation.latitude + "," + nowLocation.longitude +
                    "&" + "destination="+latitude+","+longtitude+"&"
                    +"key=" + getResources().getString(R.string.google_direction_api);

            Log.d("APIURL", apiUrl);

            directionAPI.getClientInfos(apiUrl).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                {
                    Log.d("Code",  String.valueOf(response.code()));
                    try
                    {
                        PolylineOptions polylineOptions = new PolylineOptions();
                        DirectionsJSONParser parser= new DirectionsJSONParser();
                        JSONObject jsonObject= new JSONObject(response.body().string().toString());
                        JSONArray array=jsonObject.getJSONArray("routes");
                        for(int i=0;i<array.length();i++)
                        {
                            JSONObject route= array.getJSONObject(i);
                            JSONObject poly=route.getJSONObject("overview_polyline");
                            String polyline=poly.getString("points");
                            listOfDirections = parser.decodePoly(polyline);
                        }


                        polylineOptions.addAll(listOfDirections);
                        polylineOptions.width(8);
                        polylineOptions.color(R.color.pink);
                        polylineOptions.geodesic(true);
                        directionPolyline=mMap.addPolyline(polylineOptions);

                    } catch (IOException e) {

                        e.printStackTrace();
                    } catch (JSONException e) {

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t)
                {
                    Log.d("ApiUrl", "onFailure: "+t.getMessage());
                }
            });
        }
        catch (Exception e)
        {
           e.printStackTrace();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
     displayLocation();
     updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
             googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
     lastLocation=location;
        displayLocation();
    }

    private class JSONParserAsync  extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
    {
        SpotsDialog spotsDialog = new SpotsDialog(ClientTrackerActivity.this,R.style.CustomAlert);


        @Override
        protected void onPreExecute() {
            spotsDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings)
        {
            JSONObject jsonObject=null;
            List<List<HashMap<String,String>>> routes=null;
            try
            {
                jsonObject = new JSONObject(strings[0]);
                DirectionsJSONParser parser= new DirectionsJSONParser();
                routes=parser.parse(jsonObject);
            }
            catch (JSONException e)
            {
                Log.d("Exception", e.getMessage());
            }
            return routes;
        }


        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            spotsDialog.dismiss();
            List points=null;
            PolylineOptions polylineOptions=null;

            for(int i=0;i<lists.size();i++)
            {
                points= new ArrayList();
                polylineOptions= new PolylineOptions();
                List<HashMap<String,String>> path=lists.get(i);

                for( int j=0;j<path.size();j++)
                {
                    HashMap<String, String> point= path.get(i);
                    double latitude=Double.parseDouble(point.get("lat"));
                    double longtitude=Double.parseDouble(point.get("lng"));
                    LatLng position= new LatLng(latitude,longtitude);
                    points.add(position);

                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.GREEN);
                polylineOptions.geodesic(true);
            }

            directionPolyline=mMap.addPolyline(polylineOptions);
        }
    }



}

