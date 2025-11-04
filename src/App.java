import model.*;
import io.CargadorDatosJson;

public class App {
    public static void main(String[] args) {
        Mercado mercado = CargadorDatosJson.cargarMercado("data/mercado.json");
        System.out.println("Activos cargados: " + mercado.activos.size());
        System.out.println("Matriz ρ: " + mercado.rho.length + " x " + mercado.rho[0].length);

        // Mostrar los primeros 3 activos como verificación
        for (int i = 0; i < Math.min(3, mercado.activos.size()); i++) {
            System.out.println(" - " + mercado.activos.get(i));
        }
    }
}
