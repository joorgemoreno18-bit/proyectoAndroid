package com.example.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoandroid.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Pantalla de inicio de sesion.
 * Verifica las credenciales de usuario y redirecciona segun su rol asignado.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding; // Objeto para el enlace de vistas (View Binding).
    private FirebaseAuth mAuth; // Gestor de autenticacion de Firebase.
    private FirebaseFirestore db; // Gestor de la base de datos Firestore.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializacion del View Binding para acceder a los componentes de la interfaz.
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance(); // Inicializacion del servicio de autenticacion.
        db = FirebaseFirestore.getInstance(); // Inicializacion del servicio de base de datos distribuidora.

        // Listener para procesar el intento de inicio de sesion.
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();

            // Validacion basica de campos no vacios.
            if (!email.isEmpty() && !password.isEmpty()) {
                // Ejecucion del proceso de autenticacion en Firebase Auth.
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Redireccion al validador de roles tras el exito del login.
                                FirebaseUser user = mAuth.getCurrentUser();
                                checkUserRole(user.getUid());
                            } else {
                                // Notificacion de error en las credenciales proporcionadas.
                                Toast.makeText(LoginActivity.this, "Error de autenticacion: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Listener para abrir la actividad de registro de nuevos usuarios.
        binding.tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    /**
     * Consulta el rol del usuario en la base de datos para determinar su flujo de actividad.
     */
    private void checkUserRole(String uid) {
        // Peticion asincrona al documento de usuario correspondiente en la coleccion "users".
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                Log.d("LOGIN_ROLE", "Rol de usuario identificado: " + role);

                // Bifurcacion de flujo segun el rol del usuario registrado.
                if ("creator".equals(role)) {
                    // Acceso a la interfaz de gestion del creador.
                    startActivity(new Intent(LoginActivity.this, CreatorActivity.class));
                } else {
                    // Acceso a la interfaz de visualizacion del mapa para usuarios estandar.
                    startActivity(new Intent(LoginActivity.this, NormalUserActivity.class));
                }
                finish(); // Finaliza la actividad actual para evitar que el usuario vuelva atras.
            }
        });
    }
}
