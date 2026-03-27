package com.example.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoandroid.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Actividad donde los nuevos usuarios se registran.
 * Permite elegir si eres un creador o un usuario normal mediante un checkbox.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding; // Usamos ViewBinding para acceder a los elementos del layout fácilmente
    private FirebaseAuth mAuth; // Instancia de Firebase para manejar la autenticación
    private FirebaseFirestore db; // Instancia de Firestore para guardar los datos del usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Si el usuario ya existe, lo mandamos al login
        binding.tvGoToLogin.setOnClickListener(v -> {
            finish(); // Cierra esta actividad y vuelve a la anterior (que es el Login)
        });

        // Evento al pulsar el botón de registro
        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etRegisterEmail.getText().toString().trim();
            String pwd = binding.etRegisterPassword.getText().toString().trim();
            String name = binding.etName.getText().toString().trim();
            boolean isCreator = binding.cbIsCreator.isChecked();

            if (!email.isEmpty() && !pwd.isEmpty() && !name.isEmpty()) {
                // Paso 1: Intentamos registrar el usuario en Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, pwd)
                        .addOnSuccessListener(authResult -> {
                            // Paso 2: Si tiene éxito, guardamos su rol en Firestore
                            String uid = authResult.getUser().getUid();
                            saveUserToFirestore(uid, name, isCreator);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Guarda la información básica del usuario (con su rol) en Firestore.
     */
    private void saveUserToFirestore(String uid, String name, boolean isCreator) {
        // Obtenemos el nombre del rol según el checkbox
        String role = isCreator ? "creator" : "normal";

        // Creamos un mapa con los datos del perfil
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("role", role);
        userProfile.put("uid", uid);

        // Guardamos en la colección "users" usando el UID como nombre del documento
        db.collection("users").document(uid).set(userProfile)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Registro completado con éxito", Toast.LENGTH_SHORT).show();
                    // Redirigir según el rol
                    startCorrectActivity(role);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error guardando datos: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Lanza la actividad que corresponde según el rol del usuario recién creado.
     */
    private void startCorrectActivity(String role) {
        Intent intent;
        if (role.equals("creator")) {
            intent = new Intent(this, CreatorActivity.class);
        } else {
            intent = new Intent(this, NormalUserActivity.class);
        }
        // Limpiamos el stack de actividades para que no se pueda volver atrás al registro
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
