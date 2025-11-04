import model.Mercado;
import io.CargadorDatosJson;

public class App {
    public static void main(String[] args) {
        // OJO: tu archivo está en la carpeta "datos/"
        String ruta = "datos/mercado.json";

        Mercado mercado = CargadorDatosJson.cargarMercado(ruta);

        System.out.println("Activos cargados: " + mercado.activos.size());
        System.out.println("Matriz ρ: " + mercado.rho.length + " x " + mercado.rho[0].length);

        // Mostrar los primeros 3 activos como verificación
        int k = Math.min(3, mercado.activos.size());
        for (int i = 0; i < k; i++) {
            System.out.println(" - " + mercado.activos.get(i));
        }
    }
}
