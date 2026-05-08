package progs.restaurante.lib;

import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  EstilosApp.java — Gestor centralizado de recursos           ║
 * ║                                                              ║
 * ║  Estructura de tu proyecto:                                  ║
 * ║    src/main/resources/                                       ║
 * ║      progs/                                                  ║
 * ║        css/       ← RUTA_CSS                                ║
 * ║        img/       ← RUTA_IMAGENES                           ║
 * ║        fuentes/   ← RUTA_FUENTES                            ║
 * ║        fxml/      ← tus FXML                                ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class EstilosApp {

    // ══════════════════════════════════════════════════════
    //  RUTAS BASE — si cambias una carpeta, solo edita aquí
    // ══════════════════════════════════════════════════════

    private static final String RUTA_CSS      = "/progs/css/";

    // Controla que las fuentes solo se carguen una vez
    private static boolean fuentesCargadas = false;

    private EstilosApp() {}

    // ══════════════════════════════════════════════════════
    //  ENUM CSS
    //  Solo el nombre del archivo, sin ruta
    // ══════════════════════════════════════════════════════

    public enum CSS {
        DIALOGO         ("dialogo.css"),
        PANELES         ("paneles.css"),
        FUENTES         ("fuentes.css"),
        TABLA_1         ("tablas.css"),
        TABLA_2         ("tablas_2.css"),
        BOTONES         ("botones.css"),
        JUEGO           ("css_overcooked.css"),
        IMAGEN          ("imagenes.css"),
        TEXTFIELD       ("textfields.css"),
        TEXTO           ("texto.css");

        final String archivo;
        CSS(String archivo) { this.archivo = archivo; }
    }

    // ══════════════════════════════════════════════════════
    //  ENUM FUENTE
    //  Agrega aquí cada .ttf o .otf que tengas en /progs/fuentes/
    // ══════════════════════════════════════════════════════

    public enum FUENTE {
        // Ponle el nombre exacto de tu archivo con extensión
        FREDOKA_ONE     ("FredokaOne-Regular.ttf"),
        NUNITO_BOLD     ("Nunito-Bold.ttf"),
        NUNITO_REGULAR  ("Nunito-Regular.ttf"),
        BOUNCY_THIN ("Bouncy-Thin.otf"),
        KG_PERFECT ("KGPerfectPenmanship.otf"),
        LEMONMILK ("LEMONMILK-Regular.otf"),
        LUCKYEST ("LuckiestGuy-Regular.ttf"),
        TOMMY ("MADE Tommy Soft Medium.otf"),
        SIMPLY("Simply Olive.ttf"),
        SOMELIST("Somelist.otf"),
        SUPER_CHIPS("Super Chips.ttf"),
        NEW_ROMANCE("Times New Romance.otf");
        

        // Agrega las tuyas aquí con el mismo patrón

        final String archivo;
        FUENTE(String archivo) { this.archivo = archivo; }
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODO PRINCIPAL: cargarFuentes()
    //
    //  Llama esto UNA VEZ en tu Main antes de mostrar
    //  cualquier pantalla. Registra todas las fuentes del
    //  enum en JavaFX para que el CSS pueda usarlas con
    //  -fx-font-family sin importar la máquina.
    //
    //  Uso en Main.java:
    //    EstilosApp.cargarFuentes();  // antes de stage.show()
    // ══════════════════════════════════════════════════════

    public static void cargarFuentes() {
        if (fuentesCargadas) return; // evitar cargar dos veces

        for (FUENTE fuente : FUENTE.values()) {
            String rutaCompleta = RUTA_CSS + fuente.archivo;
            URL url = EstilosApp.class.getResource(rutaCompleta);

            if (url == null) {
                System.err.println("⚠️  Fuente no encontrada → " + rutaCompleta);
                continue;
            }

            // Font.loadFont() registra la fuente en JavaFX globalmente
            // El segundo parámetro es el tamaño por defecto (no importa mucho,
            // el CSS lo sobreescribe con -fx-font-size)
            Font cargada = Font.loadFont(url.toExternalForm(), 14);

            if (cargada != null) {
                System.out.println("✅ Fuente cargada: " + cargada.getName());
            } else {
                System.err.println("❌ No se pudo cargar: " + rutaCompleta);
            }
        }

        fuentesCargadas = true;
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODOS CSS
    // ══════════════════════════════════════════════════════

    /** Aplica uno o más CSS a una Scene */
    public static void aplicar(Scene scene, CSS... estilos) {
        for (CSS css : estilos) {
            cargarCssEnScene(scene, css.archivo);
        }
    }

    /** Aplica uno o más CSS a un Stage */
    public static void aplicar(Stage stage, CSS... estilos) {
        if (stage.getScene() == null) {
            System.err.println("⚠️  EstilosApp: el Stage no tiene Scene todavía.");
            return;
        }
        aplicar(stage.getScene(), estilos);
    }

    /** Aplica TODOS los CSS del enum a una Scene */
    public static void aplicarTodos(Scene scene) {
        for (CSS css : CSS.values()) {
            cargarCssEnScene(scene, css.archivo);
        }
    }

    /** Limpia los CSS actuales y aplica los nuevos */
    public static void limpiarYAplicar(Scene scene, CSS... estilos) {
        scene.getStylesheets().clear();
        aplicar(scene, estilos);
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODOS IMAGEN
    // ══════════════════════════════════════════════════════

    /** Retorna URL absoluta de imagen (funciona en CSS dinámico desde Java) */
    public static String getUrlImagen(String nombreArchivo) {
        String rutaCompleta = RUTA_CSS + nombreArchivo;
        URL url = EstilosApp.class.getResource(rutaCompleta);
        if (url == null) {
            System.err.println("⚠️  Imagen no encontrada → " + rutaCompleta);
            return "";
        }
        return url.toExternalForm();
    }

    /** Retorna un objeto Image listo para ImageView o Stage.getIcons() */
    public static javafx.scene.image.Image getImagen(String nombreArchivo) {
        String url = getUrlImagen(nombreArchivo);
        if (url.isEmpty()) return null;
        return new javafx.scene.image.Image(url);
    }

    /** Aplica imagen de fondo a cualquier Region (VBox, StackPane, etc.) */
    public static void setFondo(javafx.scene.layout.Region nodo,
                                String nombreArchivo,
                                String tamanio) {
        String url = getUrlImagen(nombreArchivo);
        if (url.isEmpty()) return;
        nodo.setStyle(
            "-fx-background-image: url('" + url + "');" +
            "-fx-background-size: " + tamanio + ";" +
            "-fx-background-position: center;" +
            "-fx-background-repeat: no-repeat;"
        );
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODO INTERNO
    // ══════════════════════════════════════════════════════

    private static void cargarCssEnScene(Scene scene, String nombreArchivo) {
        try {
            String rutaCompleta = RUTA_CSS + nombreArchivo;
            URL url = EstilosApp.class.getResource(rutaCompleta);

            if (url == null) {
                System.err.println("⚠️  CSS no encontrado → " + rutaCompleta);
                return;
            }

            String ruta = url.toExternalForm();
            if (!scene.getStylesheets().contains(ruta)) {
                scene.getStylesheets().add(ruta);
                System.out.println("✅ CSS cargado: " + rutaCompleta);
            }

        } catch (Exception e) {
            System.err.println("❌ Error cargando CSS → " + e.getMessage());
        }
    }
}