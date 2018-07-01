package ifixcar.crismon.com.ifixcar;

import android.*;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.github.glomadrian.materialanimatedswitch.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import ifixcar.crismon.com.ifixcar.Model.Token;
import ifixcar.crismon.com.ifixcar.WebServices.IDirectionAPI;
import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        OnMapReadyCallback{


    private GoogleMap mMap;
    private Button findClient;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private MaterialAnimatedSwitch locationSwitcher;
    private Marker currentLocation;
    private SupportMapFragment mapFragment;
    private DatabaseReference locations;
    private GeoFire geoFire;

    private  static  final  int PERMISSION_CODE=800;
    private  static  final  int PLAY_SERVICE_CODE=801;

    private static  final  int UPDATE=5000;
    private static  final  int FASTEST=3000;
    private static  final  int DISPLACMENT=10;

    private PlaceAutocompleteFragment autocompleteFragment;
    private List<LatLng> listOfDirections;
    private LatLng nowLocation,startLocation,endLocation;
    private String destination;
    private Handler handler;
    private  TextView locationStatus;
    private Marker carMarker;
    private Marker placeMarker;
    private PolylineOptions blackPolylinesOptions,greyPolylinesOptions;
    private Polyline blackPolyline ,greyPolyline;
    private int  index, next;
    private float value;
    private double latitude,longtitude;
    private IDirectionAPI directionAPI;

    private DatabaseReference amIOnline,currentDriverRef;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // custom font goes here !!
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/raleWay.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        // init local DB
        Paper.init(this);

        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);

         locationStatus=(TextView)findViewById(R.id.location_status) ;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View view =navigationView.getHeaderView(0);
        TextView emailText=(TextView)view.findViewById(R.id.driver_email);
        emailText.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // init Firebase;
        locations= FirebaseDatabase.getInstance().getReference(ifixcar.crismon.com.ifixcar.Utils.Utils.DRIVERS_LOCATION);
        geoFire= new GeoFire(locations);

        // presence system goes here !
        amIOnline=FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentDriverRef=FirebaseDatabase.getInstance().getReference(ifixcar.crismon.com.ifixcar.Utils.Utils.DRIVERS_LOCATION)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        amIOnline.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentDriverRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        directionAPI= ifixcar.crismon.com.ifixcar.Utils.Utils.getDirectionAPI();
        listOfDirections= new ArrayList<>();


        autocompleteFragment=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment) ;
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination=place.getAddress().toString().replace(" ","+");
                // getDirections();
                if(placeMarker!=null)
                    placeMarker.remove();
                placeMarker=mMap.addMarker(new MarkerOptions()
                        .title(destination)
                        .position(place.getLatLng())
                        .flat(true)
                        .icon(BitmapDescriptorFactory.defaultMarker()));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));

            }

            @Override
            public void onError(Status status) {
                Snackbar.make(findClient,"Please turn on your Location !",Snackbar.LENGTH_LONG).show();
            }
        });

         // location switcher
        locationSwitcher=(MaterialAnimatedSwitch)findViewById(R.id.location_switch);
        locationSwitcher.setOnCheckedChangeListener(isOnline ->
        {
            if(isOnline)
            {
                locationStatus.setText("Online");
                locationStatus.setTextColor(getResources().getColor(R.color.green));
                FirebaseDatabase.getInstance().goOnline();
                updateLocation();
                displayLocation();
                Snackbar.make(mapFragment.getView(),"You are online!",Snackbar.LENGTH_LONG).show();

            }
            else
            {

                locationStatus.setText("Offline");
                locationStatus.setTextColor(getResources().getColor(R.color.pink));
                FirebaseDatabase.getInstance().goOffline();
                stopUpdateLocation();
                if(currentLocation!=null)
                    currentLocation.remove();
                mMap.clear();
                Snackbar.make(mapFragment.getView(),"You are offline!",Snackbar.LENGTH_LONG).show();
            }
        });

        setUpLocation();

        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.nav_home:
                startActivity(new Intent(DashboardActivity.this,DashboardActivity.class));
                finish();
                break;
            case R.id.nav_help:
                break;

            case  R.id.nav_sign_out:
                Paper.book().destroy();
                SpotsDialog spotsDialog= new SpotsDialog(this,R.style.CustomAlert);
                spotsDialog.show();

                FirebaseAuth.getInstance().signOut();
                FirebaseDatabase.getInstance().goOffline();
                spotsDialog.dismiss();
                startActivity(new Intent(DashboardActivity.this,MainActivity.class));
                finish();
                break;

            case R.id.history:
                startActivity(new Intent(DashboardActivity.this,CallsHistoryActivity.class));
                break;

                default:
                    startActivity(new Intent(DashboardActivity.this,DashboardActivity.class));
                    finish();
                    break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    public void onLocationChanged(Location location)
    {
         ifixcar.crismon.com.ifixcar.Utils.Utils.lastLocation=location;
         displayLocation();
    }
    private float getBearing(LatLng startLocation, LatLng newPosition)
    {

        double latitide=Math.abs(startLocation.latitude-endLocation.latitude);
        double longtitude=Math.abs(startLocation.longitude-startLocation.longitude);
        if(startLocation.latitude<endLocation.latitude&&startLocation.longitude<endLocation.longitude)
            return(float)(Math.toDegrees(Math.atan(latitide/longtitude)));

        if(startLocation.latitude>=endLocation.latitude&&startLocation.longitude<endLocation.longitude)
            return(float)((90-Math.toDegrees(Math.atan(latitide/longtitude)))+90);

        if(startLocation.latitude>=endLocation.latitude&&startLocation.longitude>=endLocation.longitude)
            return(float)(Math.toDegrees(Math.atan(latitide/longtitude))+180);

        if(startLocation.latitude<endLocation.latitude&&startLocation.longitude>=endLocation.longitude)
            return(float)((90-Math.toDegrees(Math.atan(latitide/longtitude)))+270);

        return -1;
    }


    private List<LatLng> decodePolyline(String polyline) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = polyline.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = polyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = polyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void setUpLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            },PERMISSION_CODE);
        }
        else
        {
            if(isGooglePlayServicesAvaiable(DashboardActivity.this))
            {
                buildApiClient();
                requestLocation();
                if (locationSwitcher.isChecked()) {
                    displayLocation();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case PERMISSION_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if(isGooglePlayServicesAvaiable(DashboardActivity.this))
                    {
                        buildApiClient();
                        requestLocation();
                        if(locationSwitcher.isChecked())
                            displayLocation();
                    }
                }
        }
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

    private void stopUpdateLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
    }

    private void displayLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        ifixcar.crismon.com.ifixcar.Utils.Utils.lastLocation=LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if( ifixcar.crismon.com.ifixcar.Utils.Utils.lastLocation!=null)
        {
            if(locationSwitcher.isChecked())
            {
                double latitude= ifixcar.crismon.com.ifixcar.Utils.Utils.lastLocation.getLatitude();
                double longtitude= ifixcar.crismon.com.ifixcar.Utils.Utils.lastLocation.getLongitude();
                String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
                geoFire.setLocation(userID, new GeoLocation(latitude,longtitude),(key, error)->
                {

                    if(currentLocation!=null)
                        currentLocation.remove();
                    currentLocation=mMap.addMarker( new MarkerOptions()
                            .position( new LatLng(latitude,longtitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                            .title("Me"));
                    // move camera to current place;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longtitude),15.0f));


                });

            }
        }
    }

    private void rotateMarker(Marker currentLocation, final float toRotation, GoogleMap mMap)
    {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = currentLocation.getRotation();
        final long duration = 1500;

        LinearInterpolator interpolator= new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;

                float bearing = -rot > 180 ? rot / 2 : rot;

                currentLocation.setRotation(bearing);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);

                }
            }
        });

    }


    private void updateLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
    }
    private void updateToken(String token) {
        DatabaseReference tokens= FirebaseDatabase.getInstance().getReference(ifixcar.crismon.com.ifixcar.Utils.Utils.TOKENS);
        Token stringToken= new Token(token);
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(stringToken);
    }

    private void getDirections ()
    {
        String apiUrl;
        nowLocation= new LatLng( ifixcar.crismon.com.ifixcar.Utils.Utils.lastLocation.getLatitude()
                , ifixcar.crismon.com.ifixcar.Utils.Utils.lastLocation.getLongitude());
        try
        {
            apiUrl="https://maps.googleapis.com/maps/api/directions/json?"
                    +"mode=driving&"+"transit_routing_preference=less_driving&"+
                    "origin="+nowLocation.latitude+","+nowLocation.longitude+"&"+"destination="+destination+"&"
                    +"key="+getResources().getString(R.string.google_direction_api);

            Log.d("APIURL", apiUrl);


            directionAPI.getClientInfos(apiUrl).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("Code", "onResponse: "+String.valueOf(response.code()));
                    try {
                        JSONObject jsonObject= new JSONObject(response.body().string().toString());
                        JSONArray array=jsonObject.getJSONArray("routes");
                        for(int i=0;i<array.length();i++)
                        {
                            JSONObject route= array.getJSONObject(i);
                            JSONObject poly=route.getJSONObject("overview_polyline");
                            String polyline=poly.getString("points");
                            listOfDirections=decodePolyline(polyline);
                        }

                        LatLngBounds.Builder builder= new LatLngBounds.Builder();
                        for(LatLng latLng:listOfDirections)
                        {
                            builder.include(latLng);
                        }
                        LatLngBounds bounds=builder.build();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,2));

                        greyPolylinesOptions= new PolylineOptions();
                        greyPolylinesOptions.addAll(listOfDirections);
                        greyPolylinesOptions.jointType(JointType.ROUND);
                        greyPolylinesOptions.color(Color.GRAY);
                        greyPolylinesOptions.endCap(new SquareCap());
                        greyPolylinesOptions.startCap( new SquareCap());
                        greyPolyline=mMap.addPolyline(greyPolylinesOptions);

                        blackPolylinesOptions= new PolylineOptions();
                        blackPolylinesOptions.addAll(listOfDirections);
                        blackPolylinesOptions.jointType(JointType.ROUND);
                        blackPolylinesOptions.color(Color.BLACK);
                        blackPolylinesOptions.endCap(new SquareCap());
                        blackPolylinesOptions.startCap( new SquareCap());
                        blackPolyline=mMap.addPolyline(greyPolylinesOptions);

                        mMap.addMarker( new MarkerOptions()
                                .title("Pick up")
                                .position(listOfDirections.get(listOfDirections.size()-1)));

                        ValueAnimator valueanimator=ValueAnimator.ofInt(0,100);
                        valueanimator.setDuration(2000);
                        valueanimator.setInterpolator(new LinearInterpolator());
                        valueanimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                List<LatLng> points=greyPolyline.getPoints();
                                int percentValue=(int)valueAnimator.getAnimatedValue();
                                int size=points.size();
                                int newPoints=(int)(size*(percentValue/100.0f));
                                List<LatLng> p=points.subList(0,newPoints);
                                blackPolyline.setPoints(p);
                            }
                        });

                        valueanimator.start();

                        carMarker=mMap.addMarker(new MarkerOptions()
                                .position(nowLocation)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                .flat(true));
                        handler= new Handler();
                        index=-1;
                        next=1;
                        handler.postDelayed(drawPathrunnable,3000);

                    } catch (JSONException e) {

                        Log.d("Exception here !!!!",e.getMessage());
                    } catch (IOException e) {
                        Log.d("Exception here !!!!",e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public Runnable drawPathrunnable= new Runnable() {
        @Override
        public void run() {
            if(index<listOfDirections.size()-1)
            {
                index++;
                next=index+1;
            }
            if(index<listOfDirections.size()-1)
            {
                startLocation= listOfDirections.get(index);
                endLocation=listOfDirections.get(next);
            }

            ValueAnimator valueAnimator=ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    value=valueAnimator.getAnimatedFraction();
                    latitude=value*endLocation.latitude+(1-value)*startLocation.latitude;
                    longtitude=value*endLocation.longitude+(1-value)*startLocation.longitude;
                    LatLng newPosition= new LatLng(latitude,longtitude);
                    carMarker.setPosition(newPosition);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startLocation,newPosition));
                    mMap.moveCamera(CameraUpdateFactory.
                            newCameraPosition(new CameraPosition.Builder()
                                    .target(newPosition)
                                    .zoom(15.0f)
                                    .build()));
                }


            });
            valueAnimator.start();
            handler.postDelayed(this,3000);

        }

    };

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        boolean isSuccess=mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_style_map));
        Log.d("isStyled!", String.valueOf(isSuccess));


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setTrafficEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private  void  testGit()
    {

    }
}
