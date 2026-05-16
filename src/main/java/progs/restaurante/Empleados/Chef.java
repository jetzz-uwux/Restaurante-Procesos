
package progs.restaurante.Empleados;

import progs.restaurante.Empleado;

/**
 *
 * @author camil
 */
public class Chef extends Empleado{

    public Chef(String idEmpleado, String nombre, String puesto, String contrasena, boolean estaPresente) {
        super(idEmpleado, nombre, puesto, contrasena, estaPresente);
        this.rol = "Chef";
    }

    public Chef() {
    }


}
