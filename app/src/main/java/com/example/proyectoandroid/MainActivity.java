package com.example.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Actividad principal de arranque de la aplicacion.
 * Realiza el redireccionamiento inmediato hacia el flujo de inicio de sesion (LoginActivity).
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ejecuta el lanzamiento de la actividad de autenticacion al inicio del ciclo de vida.
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        
        // Libera la actividad de arranque de la pila de memoria tras la redireccion.
        finish(); 
    }
}