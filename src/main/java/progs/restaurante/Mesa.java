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
        this.ordenActual = new Orden(0, this);
    }

    public int getNumero() {
        return numero;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
        
        // Si la mesa pasa a "Ocupada", se crea una orden limpia
        if (estado.equalsIgnoreCase("Ocupada")) {
            /*Usa el numero de mesa como ID temporal del pedido y pasa
            'this' para cumplir con el constructor de Orden*/
            this.ordenActual = new Orden(this.numero, this);
        }
    }

    public Orden getOrdenActual() {
        return ordenActual;
    }
}
