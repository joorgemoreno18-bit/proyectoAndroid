package com.example.proyectoandroid;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Actividad para el Usuario Normal.
 * Ahora muestra la FOTO REAL seleccionada por el creador en la tarjeta de detalles.
 */
public class NormalUserActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private Geocoder geocoder;
    
    private MaterialCardView cardInfo;
    private TextView tvNameDetail, tvInfoDetail, tvHorariosDetail, tvAddressDetail;
    private ImageView ivPhoto;
    private Button btnClose;

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

        btnClose.setOnClickListener(v -> cardInfo.setVisibility(View.GONE));

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
        
        // PINTAMOS LA FOTO ELEGIDA POR EL DUEÑO
        if (r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
            if (r.getImageUrl().startsWith("content://")) {
                // Si es una foto de la galería del mismo dispositivo
                ivPhoto.setImageURI(Uri.parse(r.getImageUrl()));
            } else {
                // Si es una URL de internet (por defecto o futura)
                // ivPhoto.setImageResource(R.drawable.logo_restaurante); // Podrías poner un logo por defecto
                ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        cardInfo.setVisibility(View.VISIBLE);
    }

    private void loadAllRestaurantsMarkers() {
        db.collection("restaurants").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null && mMap != null) {
                mMap.clear(); 
                for (QueryDocumentSnapshot doc : value) {
                    Restaurant r = doc.toObject(Restaurant.class);
                    if (r.getAddress() != null && !r.getAddress().isEmpty()) {
                        addMarkerFromAddress(r);
                    }
                }
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
