package progs.restaurante;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  Pedido.java — Modelo para la TableView del Chef             ║
 * ║                                                              ║
 * ║  Usa JavaFX Properties para que la tabla se actualice        ║
 * ║  automáticamente sin necesidad de refrescar manualmente.     ║
 * ║                                                              ║
 * ║  Mapeo con BD (tabla: pedidos):                              ║
 * ║    id_pedido → idPedido                                      ║
 * ║    numero_mesa → numeroMesa                                  ║
 * ║    total → total                                             ║
 * ║    estado → estado                                           ║
 * ║    fecha → fecha (se muestra como HH:mm:ss)                  ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class Pedido {

    // ── JavaFX Properties ─────────────────────────────────────────
    // Son "observables" — la TableView los escucha y se redibuja
    // sola cuando cambian, sin llamar a refresh() manualmente
    private final SimpleIntegerProperty idPedido;
    private final SimpleIntegerProperty numeroMesa;
    private final SimpleDoubleProperty  total;
    private final SimpleStringProperty  estado;
    private final SimpleStringProperty  fecha;
    private final SimpleStringProperty  tiempoEspera; // calculado desde fecha

    // Formato para mostrar la hora en la tabla
    private static final DateTimeFormatter FORMATO =
        DateTimeFormatter.ofPattern("HH:mm:ss");

    // ── Constructor ───────────────────────────────────────────────
    public Pedido(int idPedido, int numeroMesa, double total,
                  String estado, LocalDateTime fecha) {

        this.idPedido   = new SimpleIntegerProperty(idPedido);
        this.numeroMesa = new SimpleIntegerProperty(numeroMesa);
        this.total      = new SimpleDoubleProperty(total);
        this.estado     = new SimpleStringProperty(estado);

        // Mostrar solo la hora (más útil para el chef que la fecha completa)
        this.fecha = new SimpleStringProperty(
            fecha != null ? fecha.format(FORMATO) : "--:--:--"
        );

        // Calcular cuánto tiempo lleva esperando este pedido
        this.tiempoEspera = new SimpleStringProperty(
            calcularEspera(fecha)
        );
    }

    // ── Calcula minutos desde que se creó el pedido ───────────────
    private String calcularEspera(LocalDateTime fechaPedido) {
        if (fechaPedido == null) return "N/A";

        long minutos = java.time.Duration
            .between(fechaPedido, LocalDateTime.now())
            .toMinutes();

        if (minutos < 1)  return "< 1 min";
        if (minutos < 60) return minutos + " min";
        return (minutos / 60) + "h " + (minutos % 60) + "min";
    }

    // ── Actualiza el tiempo de espera (llamado cada 5 segundos) ───
    public void actualizarEspera(LocalDateTime fechaOriginal) {
        tiempoEspera.set(calcularEspera(fechaOriginal));
    }

    // ══════════════════════════════════════════════════════
    //  GETTERS DE PROPERTY — requeridos por TableView
    //  PropertyValueFactory los llama por convención:
    //  "idPedido" → busca idPedidoProperty()
    // ══════════════════════════════════════════════════════
    public IntegerProperty idPedidoProperty()     { return idPedido; }
    public IntegerProperty numeroMesaProperty()   { return numeroMesa; }
    public DoubleProperty  totalProperty()        { return total; }
    public StringProperty  estadoProperty()       { return estado; }
    public StringProperty  fechaProperty()        { return fecha; }
    public StringProperty  tiempoEsperaProperty() { return tiempoEspera; }

    // ── Getters normales ──────────────────────────────────────────
    public int    getIdPedido()     { return idPedido.get(); }
    public int    getNumeroMesa()   { return numeroMesa.get(); }
    public double getTotal()        { return total.get(); }
    public String getEstado()       { return estado.get(); }
    public String getFecha()        { return fecha.get(); }
    public String getTiempoEspera() { return tiempoEspera.get(); }

    // ── Setter de estado (para actualizar sin recargar toda la fila)
    public void setEstado(String estado) { this.estado.set(estado); }
}
