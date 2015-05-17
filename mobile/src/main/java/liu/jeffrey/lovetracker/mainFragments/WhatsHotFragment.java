package liu.jeffrey.lovetracker.mainFragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import liu.jeffrey.lovetracker.Constants;
import liu.jeffrey.lovetracker.R;

public class WhatsHotFragment extends Fragment implements OnMapReadyCallback {

    public WhatsHotFragment() {
    }

    MapView mapView;
    GoogleMap map;

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//
//
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_whats_hot, container, false);
//        MapFragment mapFragment = (MapFragment) getChildFragmentManager()
//                .findFragmentById(R.id.hotmap);
//
//        if (mapFragment != null) {
//            Log.d(Constants.TAG, "mapFragment not null, good");
//            mapFragment.getMapAsync(this);
//        }
        new MapTask().execute();


        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(Constants.TAG, "onMapReady");

        map.setMyLocationEnabled(true);

        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
    }

    private class MapTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void ... p) {
            MapFragment mMapFragment = MapFragment.newInstance();
            FragmentTransaction fragmentTransaction =
                    getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.my_container, mMapFragment);
            fragmentTransaction.commit();

            return true;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {

        }
    }


}
