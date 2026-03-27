package com.example.proyectoandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoandroid.databinding.ActivityCreatorBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Actividad para el Creador.
 * Se ha mejorado el permiso de la imagen para que sea persistente (se vea siempre).
 */
public class CreatorActivity extends AppCompatActivity {

    private ActivityCreatorBinding binding;
    private FirebaseFirestore db;
    private String currentUid;
    private RestaurantAdapter adapter;
    private List<Restaurant> restaurantList;
    
    private Uri selectedImageUri; 
    private ImageView dvImageView;

    // Pedimos permiso persistente al elegir la foto
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    
                    // ESTO ES LO CLAVE: Pedimos permiso a Android para leer esta foto siempre
                    if (selectedImageUri != null) {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        try {
                            getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (dvImageView != null) {
                        dvImageView.setImageURI(selectedImageUri);
                        dvImageView.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();

        binding.rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        restaurantList = new ArrayList<>();
        adapter = new RestaurantAdapter(restaurantList);
        binding.rvRestaurants.setAdapter(adapter);

        loadMyRestaurants();

        binding.fabAddRestaurant.setOnClickListener(v -> showAddRestaurantDialog());
    }

    private void loadMyRestaurants() {
        db.collection("restaurants")
                .whereEqualTo("creatorId", currentUid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        restaurantList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Restaurant r = doc.toObject(Restaurant.class);
                            restaurantList.add(r);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showAddRestaurantDialog() {
        selectedImageUri = null; 
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo Restaurante 🍕");

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(60, 40, 60, 40);

        final EditText etName = new EditText(this); etName.setHint("Nombre del local");
        final EditText etInfo = new EditText(this); etInfo.setHint("Descripción corta");
        final EditText etAddress = new EditText(this); etAddress.setHint("📍 Dirección");
        final EditText etHorarios = new EditText(this); etHorarios.setHint("🕒 Horarios");

        Button btnPickImg = new Button(this);
        btnPickImg.setText("Seleccionar Foto 📷");
        btnPickImg.setOnClickListener(v -> {
            // Usamos ACTION_OPEN_DOCUMENT para que el permiso persistente funcione mejor
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        dvImageView = new ImageView(this);
        dvImageView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(500, 300));
        dvImageView.setVisibility(View.GONE);

        container.addView(etName); 
        container.addView(etInfo); 
        container.addView(etAddress);
        container.addView(etHorarios);
        container.addView(btnPickImg);
        container.addView(dvImageView);
        builder.setView(container);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String name = etName.getText().toString();
            String info = etInfo.getText().toString();
            String address = etAddress.getText().toString();
            String horarios = etHorarios.getText().toString();

            if (!name.isEmpty() && !address.isEmpty()) {
                String imgPath = (selectedImageUri != null) ? selectedImageUri.toString() : "";
                Restaurant newR = new Restaurant(name, info, address, horarios, imgPath, currentUid);
                db.collection("restaurants").document(newR.getId()).set(newR)
                        .addOnSuccessListener(unused -> Toast.makeText(this, "¡Restaurante guardado!", Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
        private List<Restaurant> items;
        public RestaurantAdapter(List<Restaurant> items) { this.items = items; }
        @NonNull
        @Override
        public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
            return new RestaurantViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
            Restaurant r = items.get(position);
            holder.tvName.setText(r.getName());
            holder.tvInfo.setText(r.getInfo());
            holder.tvLocation.setText(r.getAddress());
        }
        @Override
        public int getItemCount() { return items.size(); }
        class RestaurantViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvInfo, tvLocation;
            public RestaurantViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvRestaurantName);
                tvInfo = itemView.findViewById(R.id.tvRestaurantInfo);
                tvLocation = itemView.findViewById(R.id.tvRestaurantLocation);
            }
        }
    }
}
