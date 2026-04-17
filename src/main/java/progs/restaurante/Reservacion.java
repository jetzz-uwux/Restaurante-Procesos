package progs.restaurante;


public class Reservacion {
    private String numreservacion;
    private String nombrecliente;
    private Mesa mesaasignada;
    private String estado; // Activa, Cancelada

    public Reservacion() {
    }

    public Reservacion(String numreservacion, String nombrecliente, Mesa mesaasignada, String estado) {
        this.numreservacion = numreservacion;
        this.nombrecliente = nombrecliente;
        this.mesaasignada = mesaasignada;
        this.estado = estado;
    }

    
    
    public Mesa getMesaasignada() {
        return mesaasignada;
    }

    public void setMesaasignada(Mesa mesaasignada) {
        this.mesaasignada = mesaasignada;
    }

    public String getNumreservacion() {
        return numreservacion;
    }

    public void setNumreservacion(String numreservacion) {
        this.numreservacion = numreservacion;
    }

    public String getNombrecliente() {
        return nombrecliente;
    }

    public void setNombrecliente(String nombrecliente) {
        this.nombrecliente = nombrecliente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public int getNumeroMesa() {
    return this.mesaasignada.getNumero();
}
    
}
