package progs.restaurante.models;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  DetallePedido.java — Un producto dentro de un pedido        ║
 * ║                                                              ║
 * ║  Mapeo con BD (tabla: detalles_pedido):                      ║
 * ║    id_detalle → idDetalle                                    ║
 * ║    id_pedido  → idPedido                                     ║
 * ║    nombre_producto → nombreProducto                          ║
 * ║    precio → precio                                           ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class DetallePedido {

    private final int    idDetalle;
    private final int    idPedido;
    private final String nombreProducto;
    private final double precio;

    public DetallePedido(int idDetalle, int idPedido,
                         String nombreProducto, double precio) {
        this.idDetalle      = idDetalle;
        this.idPedido       = idPedido;
        this.nombreProducto = nombreProducto;
        this.precio         = precio;
    }

    // ── Getters ───────────────────────────────────────────────────
    public int    getIdDetalle()      { return idDetalle; }
    public int    getIdPedido()       { return idPedido; }
    public String getNombreProducto() { return nombreProducto; }
    public double getPrecio()         { return precio; }
}
