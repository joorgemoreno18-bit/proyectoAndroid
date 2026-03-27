package com.example.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoandroid.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Actividad Principal: Esta es la primera pantalla que verá el usuario.
 * Se encarga de iniciar sesión y redirigirlo a su rol correspondiente.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding; // ViewBinding para acceder fácilmente a los componentes
    private FirebaseAuth mAuth; // Firebase Authentication: Maneja el login
    private FirebaseFirestore db; // Firebase Firestore: Almacena los roles de los usuarios

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializamos Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Si el usuario ya está logueado, vamos directamente hacia su menú
        if (mAuth.getCurrentUser() != null) {
            fetchUserRoleAndStartActivity(mAuth.getUid());
            // No inflamos el layout para evitar el parpadeo de la pantalla de login
            return;
        }

        // Si no está logueado, mostramos el formulario de login
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Al pulsar en "Regístrate aquí", saltamos a la actividad de registro
        binding.tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // Evento al pulsar el botón principal "Entrar"
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String pwd = binding.etPassword.getText().toString().trim();

            if (!email.isEmpty() && !pwd.isEmpty()) {
                // Intentamos loguear al usuario con email y contraseña
                mAuth.signInWithEmailAndPassword(email, pwd)
                        .addOnSuccessListener(authResult -> {
                            // Si el login es exitoso, comprobamos su rol en Firestore
                            fetchUserRoleAndStartActivity(authResult.getUser().getUid());
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error de acceso: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(this, "Falta completar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Consulta en Firestore cuál es el rol del usuario actual (Creador o Normal)
     * para enviarlo a la pantalla que le corresponde.
     */
    private void fetchUserRoleAndStartActivity(String uid) {
        // Buscamos el documento con el UID del usuario dentro de la colección "users"
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtenemos el campo "role" que guardamos al registrar al usuario
                        String role = documentSnapshot.getString("role");
                        Log.d("LOGIN_ROLE", "Usuario identificado como: " + role);

                        // Redireccionamos a la actividad correcta
                        Intent intent;
                        if ("creator".equalsIgnoreCase(role)) {
                            intent = new Intent(this, CreatorActivity.class);
                        } else {
                            intent = new Intent(this, NormalUserActivity.class);
                        }
                        // Impedimos que el usuario vuelva al Login con el botón de atrás
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // Cerramos definitivamente el login
                    } else {
                        Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error verificando perfil: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
