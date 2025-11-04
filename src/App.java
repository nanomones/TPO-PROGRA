import model.Mercado;
import io.CargadorDatosJson;

public class App {
    public static void main(String[] args) {
        // El conversor va a generar data/mercado.json
        Mercado m = CargadorDatosJson.cargarMercado("data/mercado.json");
        System.out.println("Activos: " + m.activos.size());
        System.out.println("Matriz ρ: " + m.rho.length + " x " + m.rho[0].length);
        for (int i = 0; i < Math.min(5, m.activos.size()); i++) {
            System.out.println(" - " + m.activos.get(i));
        }
    }
}
