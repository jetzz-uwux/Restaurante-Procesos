/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package progs.restaurante;

/**
 *
 * @author camil
 */
public class Mesa {

    private int numero;
    private String estado; //Disponible, Ocupada, Reservada
    private Orden ordenActual;

    public Mesa(int numero) {
        this.numero = numero;
        this.estado = "Disponible"; // Por defecto al iniciar
        this.ordenActual = new Orden();
    }

    public int getNumero() {
        return numero;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
        // Si se marca como Ocupada, aseguramos tener una orden limpia
        if (estado.equalsIgnoreCase("Ocupada")) {
            this.ordenActual = new Orden();
        }
    }

    public Orden getOrdenActual() {
        return ordenActual;
    }
}
