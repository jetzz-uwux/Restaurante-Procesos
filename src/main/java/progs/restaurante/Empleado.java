
package progs.restaurante;

/**
 *
 * @author camil
 */
public abstract class Empleado {
    protected String idEmpleado;
    protected String usuario;
    protected String nombre;
    protected String puesto;
    protected String contrasena;
    protected boolean estaPresente;
    protected String rol;


    public Empleado(String idEmpleado, String nombre, String puesto, String contrasena, boolean estaPresente) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.puesto = puesto;
        this.contrasena = contrasena;
        this.estaPresente = false;
    }

    public Empleado() {
    }
    
    
    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
    
    public boolean login(String id, String pass){
        return this.idEmpleado.equals(id) && this.contrasena.equals(pass);
    }
    
    public void marcarAsistencia() {
        this.estaPresente = true;
        System.out.println(nombre + " (" + puesto + ") ha marcado entrada.");
    }
    
    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public boolean isEstaPresente() {
        return estaPresente;
    }

    public void setEstaPresente(boolean estaPresente) {
        this.estaPresente = estaPresente;
    }        

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    
}
