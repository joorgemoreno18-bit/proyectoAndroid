package com.example.proyectoandroid;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Actividad para el Usuario Normal.
 * Utiliza GLIDE para cargar las fotos desde la nube (Firebase Storage). ☁️📸✨🚀
 */
public class NormalUserActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private Geocoder geocoder;
    
    // UI detail card
    private MaterialCardView cardInfo;
    private TextView tvNameDetail, tvInfoDetail, tvHorariosDetail, tvAddressDetail;
    private ImageView ivPhoto;
    private Button btnClose;
    private FloatingActionButton fabRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        db = FirebaseFirestore.getInstance();
        geocoder = new Geocoder(this, Locale.getDefault());
        
        cardInfo = findViewById(R.id.cardRestaurantInfo);
        tvNameDetail = findViewById(R.id.tvNameDetail);
        tvInfoDetail = findViewById(R.id.tvInfoDetail);
        tvHorariosDetail = findViewById(R.id.tvHorariosDetail);
        tvAddressDetail = findViewById(R.id.tvAddressDetail);
        ivPhoto = findViewById(R.id.ivRestaurantPhoto);
        btnClose = findViewById(R.id.btnCloseDetail);
        fabRefresh = findViewById(R.id.fabRefresh);

        btnClose.setOnClickListener(v -> cardInfo.setVisibility(View.GONE));

        fabRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Actualizando con la nube... 🔄☁️", Toast.LENGTH_SHORT).show();
            loadAllRestaurantsMarkers();
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(marker -> {
            Restaurant r = (Restaurant) marker.getTag();
            if (r != null) {
                showRestaurantDetails(r);
            }
            return false;
        });

        loadAllRestaurantsMarkers();
    }

    private void showRestaurantDetails(Restaurant r) {
        tvNameDetail.setText(r.getName());
        tvInfoDetail.setText(r.getInfo());
        tvHorariosDetail.setText("🕒 " + (r.getHorarios() != null ? r.getHorarios() : "Sin horario"));
        tvAddressDetail.setText("📍 " + r.getAddress());
        
        // ¡LA MAGIA DE GLIDE! ✨📸🚀🏆
        if (r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(r.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery) // Mientras carga
                    .error(android.R.drawable.ic_menu_report_image) // Si falla
                    .centerCrop()
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        cardInfo.setVisibility(View.VISIBLE);
    }

    private void loadAllRestaurantsMarkers() {
        db.collection("restaurants").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (mMap != null) {
                    mMap.clear(); 
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Restaurant r = doc.toObject(Restaurant.class);
                        if (r.getAddress() != null && !r.getAddress().isEmpty()) {
                            addMarkerFromAddress(r);
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error conectando con la nube", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMarkerFromAddress(Restaurant r) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(r.getAddress(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(r.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                
                if (marker != null) {
                    marker.setTag(r); 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
