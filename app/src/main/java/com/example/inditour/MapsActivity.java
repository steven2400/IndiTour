package com.example.inditour;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String apiKey = "AIzaSyD0NZoADtZhfi0YL1_fizo7PJIFo-NL8MY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Logout Icon
        ImageButton logoutIcon = findViewById(R.id.logoutIcon);
        logoutIcon.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get data from intent
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);
        String placeName = getIntent().getStringExtra("place_name");

        if (lat != 0 && lng != 0) {
            LatLng selectedPlace = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(selectedPlace).title(placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPlace, 14));

            // ✅ Fetch hotels near the selected location
            fetchNearbyHotels(selectedPlace);
        } else {
            // Default Location - Mandalay
            LatLng mandalay = new LatLng(21.9162, 96.0866);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mandalay, 12));
            fetchNearbyHotels(mandalay);
        }
    }

    private void fetchNearbyHotels(LatLng location) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + location.latitude + "," + location.longitude +
                "&radius=2000" +        // search radius in meters (2km)
                "&type=lodging" +       // hotels/lodging
                "&key=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject hotel = results.getJSONObject(i);
                            JSONObject locationObj = hotel.getJSONObject("geometry").getJSONObject("location");

                            double lat = locationObj.getDouble("lat");
                            double lng = locationObj.getDouble("lng");
                            String name = hotel.getString("name");

                            // check if rating exists
                            double rating = hotel.has("rating") ? hotel.getDouble("rating") : 0;

                            // ✅ only show hotels with rating >= 3
                            if (rating >= 3.0) {
                                LatLng hotelLocation = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions()
                                        .position(hotelLocation)
                                        .title(name + " ★" + rating));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("PlacesAPI", "Error: " + error.getMessage())
        );

        queue.add(request);
    }
}
