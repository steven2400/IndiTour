package com.example.inditour;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText searchBar;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyD0NZoADtZhfi0YL1_fizo7PJIFo-NL8MY");
        }

        // Initialize Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize Search Bar (Clickable)
        searchBar = findViewById(R.id.search_bar);
        searchBar.setOnClickListener(v -> openAutocomplete());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Default Location - Mandalay, Myanmar
        LatLng mandalay = new LatLng(21.9162, 96.0866);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mandalay, 12)); // Adjust zoom level
    }

    private void openAutocomplete() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        try {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        } catch (Exception e) {
            Log.e("AutoCompleteError", "Error launching autocomplete: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();

                if (latLng != null) {
                    searchBar.setText(place.getName()); // Update search bar text
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

                    // Fetch nearby hotels when a place is selected
                    fetchNearbyHotels(latLng);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e("MapsActivity", "Autocomplete Error: " + status.getStatusMessage());
            }
        }
    }

    private void fetchNearbyHotels(LatLng location) {
        String apiKey = "AIzaSyD0NZoADtZhfi0YL1_fizo7PJIFo-NL8MY";
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + location.latitude + "," + location.longitude +
                "&radius=2000" +  // 2km radius
                "&type=lodging" +  // Hotels
                "&key=" + apiKey;

        // Make API Request
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

                            // Check rating
                            double rating = hotel.has("rating") ? hotel.getDouble("rating") : 0;
                            if (rating >= 3.0) {  // Filter hotels with 3+ stars
                                LatLng hotelLocation = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions().position(hotelLocation).title(name + " ★" + rating));
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
