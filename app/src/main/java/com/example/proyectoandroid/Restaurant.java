package com.example.proyectoandroid;

import java.util.UUID;

/**
 * Modelo de datos para un Restaurante.
 * Firestore lo usará para guardar y leer documentos automáticamente.
 */
public class Restaurant {
    private String id;
    private String name;
    private String info;
    private String address;
    private String horarios; // <--- NUEVO: Campo para el horario
    private String imageUrl; // <--- NUEVO: Campo para la foto (URL)
    private String creatorId;

    // Constructor vacío requerido por Firestore para la deserialización
    public Restaurant() {}

    public Restaurant(String name, String info, String address, String horarios, String imageUrl, String creatorId) {
        this.id = UUID.randomUUID().toString(); // Generamos un ID único si no existe
        this.name = name;
        this.info = info;
        this.address = address;
        this.horarios = horarios;
        this.imageUrl = imageUrl;
        this.creatorId = creatorId;
    }

    // Getters y Setters necesarios para Firestore
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getHorarios() { return horarios; }
    public void setHorarios(String horarios) { this.horarios = horarios; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
}
