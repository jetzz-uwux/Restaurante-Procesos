package progs.restaurante.lib;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  EstilosApp.java — Gestor centralizado de CSS                ║
 * ║                                                              ║
 * ║  Estructura de tu proyecto:                                  ║
 * ║    resources/                                                ║
 * ║      progs/                                                  ║
 * ║        css/          ← tus archivos .css van aquí           ║
 * ║        imagenes/                                             ║
 * ║        fuentes/                                              ║
 * ║                                                              ║
 * ║  Uso en cualquier controlador:                               ║
 * ║    EstilosApp.aplicar(scene, CSS.DIALOGO);                   ║
 * ║    EstilosApp.aplicar(scene, CSS.LOGIN, CSS.DIALOGO);        ║
 * ║    EstilosApp.aplicarTodos(scene);                           ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class EstilosApp {

    // ══════════════════════════════════════════════════════
    //  RUTA BASE hacia tu carpeta CSS dentro de resources
    //  La / al inicio significa "desde la raíz del classpath"
    //  que en tiempo de ejecución apunta a tu carpeta resources/
    // ══════════════════════════════════════════════════════

    private static final String RUTA_CSS = "/progs/css/";

    // ══════════════════════════════════════════════════════
    //  REGISTRO DE ARCHIVOS CSS
    //
    //  ¿Cómo agregar uno nuevo?
    //    1. Crea tu archivo en resources/progs/css/
    //    2. Agrega una línea aquí abajo siguiendo el mismo patrón
    //    3. Ya puedes usarlo en cualquier controlador
    // ══════════════════════════════════════════════════════

    public enum CSS {

        DIALOGO ("dialogo.css"),
        PANELES ("paneles.css"),
        FUENTES ("fuentes.css"),
        TABLA_1 ("tablas.css"),
        TABLA_2 ("tablas_2.css"),
        BOTONES ("botones.css"),
        JUEGO ("css_overcooked.css"),
        IMAGEN ("imagenes.css"),
        TEXTFIELD("textfields.css"),
        TEXTO ("texto.css");
        
        private final String archivo;

        CSS(String archivo) {
            this.archivo = archivo;
        }

        public String getArchivo() {
            return archivo;
        }
    }

    // Constructor privado — clase de utilidad, no se instancia
    private EstilosApp() {}

    // ══════════════════════════════════════════════════════
    //  MÉTODO 1: Aplicar CSS específicos a una Scene
    //
    //  Ejemplo:
    //    EstilosApp.aplicar(scene, CSS.DIALOGO);
    //    EstilosApp.aplicar(scene, CSS.LOGIN, CSS.GLOBAL);
    // ══════════════════════════════════════════════════════

    public static void aplicar(Scene scene, CSS... estilos) {
        for (CSS css : estilos) {
            cargarEnScene(scene, css.getArchivo());
        }
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODO 2: Aplicar CSS específicos a un Stage
    //
    //  Ejemplo:
    //    EstilosApp.aplicar(stage, CSS.DIALOGO);
    // ══════════════════════════════════════════════════════

    public static void aplicar(Stage stage, CSS... estilos) {
        if (stage.getScene() == null) {
            System.err.println("⚠️  EstilosApp: el Stage no tiene Scene todavía.");
            return;
        }
        aplicar(stage.getScene(), estilos);
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODO 3: Aplicar TODOS los CSS del enum
    //
    //  Ejemplo:
    //    EstilosApp.aplicarTodos(scene);
    //
    //  OJO: solo úsalo si realmente quieres TODOS,
    //  si no, usa aplicar() con los que necesites.
    // ══════════════════════════════════════════════════════

    public static void aplicarTodos(Scene scene) {
        for (CSS css : CSS.values()) {
            cargarEnScene(scene, css.getArchivo());
        }
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODO 4: Limpiar CSS actuales y aplicar nuevos
    //  Útil al cambiar de pantalla
    //
    //  Ejemplo:
    //    EstilosApp.limpiarYAplicar(scene, CSS.DASHBOARD, CSS.GLOBAL);
    // ══════════════════════════════════════════════════════

    public static void limpiarYAplicar(Scene scene, CSS... estilos) {
        scene.getStylesheets().clear();
        aplicar(scene, estilos);
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODO INTERNO: carga un archivo CSS en la Scene
    // ══════════════════════════════════════════════════════

    private static void cargarEnScene(Scene scene, String nombreArchivo) {
        try {
            // Construye la ruta completa: /progs/css/dialogo.css
            String rutaCompleta = RUTA_CSS + nombreArchivo;

            // Busca el archivo en el classpath (dentro de resources/)
            // La / inicial es clave: busca desde la raíz del classpath,
            // NO desde el paquete de la clase Java
            URL url = EstilosApp.class.getResource(rutaCompleta);

            if (url == null) {
                // El archivo no existe en esa ruta — avisa pero no rompe la app
                System.err.println("⚠️  EstilosApp: CSS no encontrado en → " + rutaCompleta);
                return;
            }

            String ruta = url.toExternalForm();

            // Evitar duplicados: si ya está cargado, no lo agrega de nuevo
            if (!scene.getStylesheets().contains(ruta)) {
                scene.getStylesheets().add(ruta);
                System.out.println("✅ CSS cargado: " + rutaCompleta);
            }

        } catch (Exception e) {
            System.err.println("❌ EstilosApp: error cargando "
                + nombreArchivo + " → " + e.getMessage());
        }
    }
}