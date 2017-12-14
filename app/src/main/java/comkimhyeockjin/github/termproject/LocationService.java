package comkimhyeockjin.github.termproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationService extends Service {
    private PlaceDetectionClient mPlaceDetectionClient;
    private int timer = 60000;
    private double placeTime = 0;
    private String lastPlace = "";
    private double lat;
    private double lng;

    private LocationDB locationDB;
    private PlaceDB placeDB;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_STICKY;
        } else {
            locationDB = new LocationDB(this);
            placeDB = new PlaceDB(this);
            checkLocation();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void checkLocation() {
        while (true) {
            checkCurrentPlace();
            try {
                Thread.sleep(timer);
                placeTime += timer;
                if (timer < 900000) {
                    timer *= 2;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkCurrentPlace() {
        @SuppressWarnings("MissingPermission") final
        Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);

        placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    PlaceLikelihoodBufferResponse places = task.getResult();
                    Place currentPlace = places.get(0).getPlace();
                    double curLat = currentPlace.getLatLng().latitude;
                    double curLng = currentPlace.getLatLng().longitude;
                    if (lat == curLat && lng == curLng) {

                        if (timer > 60000) {
                            timer /= 4;
                        }
                        // DB 저장 필요.



                        placeTime = 0;
                        return ;
                    }
                    lat = curLat;
                    lng = curLng;
                    lastPlace =  currentPlace.getName().toString();

                    Log.d("MainActivity", "name : " + currentPlace.getName() + "long : " + currentPlace.getLatLng().longitude
                    + "lat : " + currentPlace.getLatLng().latitude);

                } else {
                    Log.d("LocationService", task.getException().toString());
                }
            }
        });

    }


    @Override
    public void onCreate() {
        super.onCreate();
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
    }
}