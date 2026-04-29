package progs.restaurante;

import java.time.LocalDateTime;

public class Venta {

    private int idVenta;
    private int idPedido;
    private int numeroMesa;
    private double total;
    private LocalDateTime fechaHora;

    public Venta(int idVenta, int idPedido, int numeroMesa, double total, LocalDateTime fechaHora) {
        this.idVenta = idVenta;
        this.idPedido = idPedido;
        this.numeroMesa = numeroMesa;
        this.total = total;
        this.fechaHora = fechaHora;
    }

    public int getIdVenta() { return idVenta; }
    public int getIdPedido() { return idPedido; }
    public int getNumeroMesa() { return numeroMesa; }
    public double getTotal() { return total; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}