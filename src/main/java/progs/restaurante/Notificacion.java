package progs.restaurante.models;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  Notificacion.java — Modelo para la tabla del mesero         ║
 * ║                                                              ║
 * ║  Mapeo con BD (tabla: notificaciones):                       ║
 * ║    id_notificacion → idNotificacion                          ║
 * ║    id_pedido       → idPedido                                ║
 * ║    numero_mesa     → numeroMesa                              ║
 * ║    hora            → hora (se muestra como HH:mm:ss)         ║
 * ║    leida           → leida                                   ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class Notificacion {

    private static final DateTimeFormatter FORMATO =
        DateTimeFormatter.ofPattern("HH:mm:ss");

    private final SimpleIntegerProperty idNotificacion;
    private final SimpleIntegerProperty idPedido;
    private final SimpleIntegerProperty numeroMesa;
    private final SimpleStringProperty  hora;
    private final SimpleBooleanProperty leida;

    public Notificacion(int idNotificacion, int idPedido,
                        int numeroMesa, LocalDateTime hora, boolean leida) {
        this.idNotificacion = new SimpleIntegerProperty(idNotificacion);
        this.idPedido       = new SimpleIntegerProperty(idPedido);
        this.numeroMesa     = new SimpleIntegerProperty(numeroMesa);
        this.hora           = new SimpleStringProperty(
            hora != null ? hora.format(FORMATO) : "--:--:--"
        );
        this.leida = new SimpleBooleanProperty(leida);
    }

    // ── Properties para TableView ─────────────────────────────
    public IntegerProperty idNotificacionProperty() { return idNotificacion; }
    public IntegerProperty idPedidoProperty()       { return idPedido; }
    public IntegerProperty numeroMesaProperty()     { return numeroMesa; }
    public StringProperty  horaProperty()           { return hora; }
    public BooleanProperty leidaProperty()          { return leida; }

    // ── Getters normales ──────────────────────────────────────
    public int     getIdNotificacion() { return idNotificacion.get(); }
    public int     getIdPedido()       { return idPedido.get(); }
    public int     getNumeroMesa()     { return numeroMesa.get(); }
    public String  getHora()           { return hora.get(); }
    public boolean isLeida()           { return leida.get(); }

    public void setLeida(boolean leida) { this.leida.set(leida); }
}
