package com.example.proyectoandroid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
 * Actividad para la gestion de locales por parte de los creadores.
 * Permite añadir restaurantes mediante una URL de imagen externa.
 */
public class CreatorActivity extends AppCompatActivity {

    private ActivityCreatorBinding binding; // Herramienta para conectar el codigo con el diseño XML.
    private FirebaseFirestore db; // Instancia de la base de datos Firestore.
    private String currentUid; // ID unico del usuario identificado.
    private RestaurantAdapter adapter; // Adaptador para mostrar la lista de restaurantes.
    private List<Restaurant> restaurantList; // Lista interna de objetos de tipo Restaurante.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializamos el view binding para acceder a los elementos del diseño.
        binding = ActivityCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar); // Configuramos la barra de herramientas superior.
        db = FirebaseFirestore.getInstance(); // Inicializamos la conexion con Firestore.
        currentUid = FirebaseAuth.getInstance().getUid(); // Obtenemos el ID del usuario actual.

        // Configuracion del RecyclerView para mostrar la lista de locales.
        binding.rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        restaurantList = new ArrayList<>();
        adapter = new RestaurantAdapter(restaurantList);
        binding.rvRestaurants.setAdapter(adapter);

        loadMyRestaurants(); // Carga inicial de datos desde el servidor.

        // Listener para abrir el dialogo de creacion al pulsar el boton flotante.
        binding.fabAddRestaurant.setOnClickListener(v -> showAddRestaurantDialog());
    }

    /**
     * Consulta Firestore para obtener solo los restaurantes asociados a este creador.
     */
    private void loadMyRestaurants() {
        db.collection("restaurants")
                .whereEqualTo("creatorId", currentUid) // Filtro por el ID del creador actual.
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        restaurantList.clear(); // Limpieza previa de la lista para evitar duplicados.
                        for (QueryDocumentSnapshot doc : value) {
                            Restaurant r = doc.toObject(Restaurant.class);
                            restaurantList.add(r);
                        }
                        adapter.notifyDataSetChanged(); // Actualizacion visual de la lista.
                    }
                });
    }

    /**
     * Muestra un cuadro de dialogo para introducir los datos del nuevo restaurante.
     */
    private void showAddRestaurantDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo Restaurante");

        // Contenedor vertical para los campos de texto.
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(60, 40, 60, 40);

        final EditText etName = new EditText(this); etName.setHint("Nombre del local");
        final EditText etInfo = new EditText(this); etInfo.setHint("Descripcion");
        final EditText etAddress = new EditText(this); etAddress.setHint("Direccion");
        final EditText etHorarios = new EditText(this); etHorarios.setHint("Horarios (Ej: 10:00 - 22:00)");
        final EditText etImgUrl = new EditText(this); etImgUrl.setHint("URL de la imagen");

        container.addView(etName); 
        container.addView(etInfo); 
        container.addView(etAddress);
        container.addView(etHorarios);
        container.addView(etImgUrl);
        builder.setView(container);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String name = etName.getText().toString();
            String info = etInfo.getText().toString();
            String address = etAddress.getText().toString();
            String horarios = etHorarios.getText().toString();
            String imgUrl = etImgUrl.getText().toString();

            if (!name.isEmpty() && !address.isEmpty()) {
                // Se asigna una imagen generica si el usuario no proporciona ninguna.
                if (imgUrl.isEmpty()) {
                    imgUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=500";
                }
                
                Restaurant newR = new Restaurant(name, info, address, horarios, imgUrl, currentUid);
                db.collection("restaurants").document(newR.getId()).set(newR)
                        .addOnSuccessListener(unused -> Toast.makeText(this, "Restaurante guardado correctamente", Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    /**
     * Adaptador para gestionar la visualizacion de cada fila en la lista de restaurantes.
     */
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
