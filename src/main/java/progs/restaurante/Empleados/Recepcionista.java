/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package progs.restaurante.Empleados;

import progs.restaurante.Empleado;

/**
 *
 * @author camil
 */
public class Recepcionista extends Empleado {

    public Recepcionista(String idEmpleado, String nombre, String puesto, String contrasena, boolean estaPresente) {
        super(idEmpleado, nombre, puesto, contrasena, estaPresente);
        this.rol = "Recepcionista";
    }

    public Recepcionista() {
    }


}
