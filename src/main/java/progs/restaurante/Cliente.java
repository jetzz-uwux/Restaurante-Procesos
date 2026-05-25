package progs.restaurante;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Cliente {

    private int id;
    private String nombre;
    private int personas;
    private String horaLlegada;

    public Cliente(String nombre, int personas) {
        this.nombre = nombre;
        this.personas = personas;
        // Captura la hora en la que se crea el objeto
        this.horaLlegada = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPersonas() {
        return personas;
    }

    public String getHoraLlegada() {
        return horaLlegada;
    }
}
