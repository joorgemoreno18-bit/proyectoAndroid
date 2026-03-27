package com.example.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity: Esta clase ahora solo sirve para redirigir al LoginActivity
 * al iniciar la aplicación por primera vez.
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Redirigimos al LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        
        // Cerramos esta actividad para que no quede en el historial
        finish();
    }
}