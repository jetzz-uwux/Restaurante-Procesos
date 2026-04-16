/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package progs.restaurante.Empleados;

import progs.restaurante.Empleado;
import progs.restaurante.Mesa;
import progs.restaurante.Productos.Platillo;
import progs.restaurante.Orden;
import java.util.ArrayList;
import progs.restaurante.Producto;

/**
 *
 * @author camil
 */
public class Mesero extends Empleado {

    public Mesero(String idEmpleado, String nombre, String puesto, String contrasena, boolean estaPresente) {
        super(idEmpleado, nombre, puesto, contrasena, estaPresente);
    }

    public void registrarPedido(Mesa mesa, ArrayList<Producto> seleccion) {
        //Mesa debe estar ocupada
        if (!mesa.getEstado().equalsIgnoreCase("Ocupada")) {
            return;
        }
        //Validar platillos seleccionados
        for (Producto p : seleccion) {
            if (p.getStock() <= 0) {
                continue;
            }
            //Agregar a la cuenta de la mesa y reducir el stock
            mesa.getOrdenActual().agregarItem(p);
            p.reducirStock(1);
        }
        mesa.getOrdenActual().setEstado("En preparación");
        System.out.println("Pedido enviado con éxito a la pantalla de cocina.");
    }

    public void notificarMesaDesocupada(Mesa mesa) {
        mesa.setEstado("Disponible");
    }
}
