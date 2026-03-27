package com.example.proyectoandroid;

import java.util.UUID;

/**
 * Modelo de datos para la entidad Restaurante.
 * Representa la estructura de informacion almacenada en Firestore.
 */
public class Restaurant {
    private String id; // Identificador unico del restaurante.
    private String name; // Nombre comercial del establecimiento.
    private String info; // Descripcion informativa del local.
    private String address; // Direccion postal completa.
    private String horarios; // Horario de atencion al publico.
    private String imageUrl; // URL de la imagen representativa del local.
    private String creatorId; // Referencia al ID del usuario creador.

    // Constructor vacio requerido para la serializacion de Firebase Firestore.
    public Restaurant() {
    }

    // Constructor para la creacion de nuevas instancias de restaurante.
    public Restaurant(String name, String info, String address, String horarios, String imageUrl, String creatorId) {
        this.id = UUID.randomUUID().toString(); // Generacion de ID aleatorio.
        this.name = name;
        this.info = info;
        this.address = address;
        this.horarios = horarios;
        this.imageUrl = imageUrl;
        this.creatorId = creatorId;
    }

    /**
     * Getters y Setters para el acceso a las propiedades de la clase.
     */

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
