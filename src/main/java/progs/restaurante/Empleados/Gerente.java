
package progs.restaurante.Empleados;

import progs.restaurante.Empleado;

/**
 *
 * @author camil
 */
public class Gerente extends Empleado {

    public Gerente(String idEmpleado, String nombre, String puesto, String contrasena, boolean estaPresente) {
        super(idEmpleado, nombre, puesto, contrasena, estaPresente);
        this.rol = "Gerente";
    }

    public Gerente() {
    }


}
