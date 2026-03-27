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
 * Gestiona la visualizacion de marcadores en Google Maps y los detalles mediante Glide.
 */
public class NormalUserActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Objeto para controlar el componente de Google Maps.
    private FirebaseFirestore db; // Instancia de conexion con Firestore Database.
    private Geocoder geocoder; // Herramienta para conversion de direcciones textuales a coordenadas.
    
    // UI detail card components.
    private MaterialCardView cardInfo;
    private TextView tvNameDetail, tvInfoDetail, tvHorariosDetail, tvAddressDetail;
    private ImageView ivPhoto;
    private Button btnClose;
    private FloatingActionButton fabRefresh; // Boton flotante para forzar el refresco de marcadores.

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

        // Listener para cerrar la card de detalles.
        btnClose.setOnClickListener(v -> cardInfo.setVisibility(View.GONE));

        // Listener para refrescar manualmente los marcadores del mapa.
        fabRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Refrescando mapa...", Toast.LENGTH_SHORT).show();
            loadAllRestaurantsMarkers();
        });

        // Inicializacion del mapa de forma asincrona.
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

    /**
     * Muestra la informacion detallada del restaurante seleccionado en la tarjeta inferior.
     */
    private void showRestaurantDetails(Restaurant r) {
        tvNameDetail.setText(r.getName());
        tvInfoDetail.setText(r.getInfo());
        tvHorariosDetail.setText("Horario: " + (r.getHorarios() != null ? r.getHorarios() : "No especificado"));
        tvAddressDetail.setText("Direccion: " + r.getAddress());
        
        // Uso de Glide para la carga de imagenes desde red con placeholders.
        if (r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(r.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        cardInfo.setVisibility(View.VISIBLE);
    }

    /**
     * Recupera todos los restaurantes de la coleccion y coloca marcadores en el mapa.
     */
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
                Toast.makeText(this, "Error conectando con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Utiliza el servicio Geocoder para posicionar el marcador en base a la direccion textual.
     */
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
