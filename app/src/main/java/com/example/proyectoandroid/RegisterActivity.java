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
 * Pantalla para el registro de nuevos usuarios.
 * Almacena la informacion de usuario en Firebase Auth y define su rol en Firestore Database.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding; // Gestor de vistas vinculadas para la UI de registro.
    private FirebaseAuth mAuth; // Gestor de autenticacion de Firebase.
    private FirebaseFirestore db; // Gestor de almacenamiento en base de datos NoSQL distribuida.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializacion del View Binding para las vistas de la interfaz de registro.
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance(); // Inicializacion del servicio de autenticacion de red.
        db = FirebaseFirestore.getInstance(); // Inicializacion del servicio de base de datos distribuidora.

        // Listener para procesar el registro tras el clic en el boton btnRegister.
        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            
            // Asignacion del rol basandose en el estado del componente switchRole.
            String role = binding.switchRole.isChecked() ? "creator" : "normal"; 

            // Validacion basica para asegurar campos obligatorios no nulos.
            if (!email.isEmpty() && !password.isEmpty()) {
                // Etapa 1: Registro en el servicio de autenticacion central de Firebase.
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Etapa 2: Si el registro en Auth es exitoso, persistimos el perfil en la base de datos.
                                String uid = mAuth.getCurrentUser().getUid();
                                saveUserToFirestore(uid, email, role);
                            } else {
                                // Notificacion de error producida durante el registro de credenciales.
                                Toast.makeText(RegisterActivity.this, "Error en el registro del usuario: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Listener para regresar de forma manual a la actividad de inicio de sesion.
        binding.tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Persiste la informacion basica del perfil de usuario y su rol en la coleccion "users".
     */
    private void saveUserToFirestore(String uid, String email, String role) {
        // Estructuracion de la informacion de usuario mediante un objeto Map de clave-valor.
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", role);

        // Escritura asincrona del objeto usuario en el nodo documental correspondiente de Firestore.
        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(unused -> {
                    // Notificacion de operacion de guardado finalizada correctamente.
                    Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente en el servidor", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                });
    }
}
