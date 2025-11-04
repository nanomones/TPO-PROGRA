import model.Mercado;
import io.CargadorDatosJson;
import validacion.ValidadorMercado;

public class App {
    public static void main(String[] args) {
        Mercado m = CargadorDatosJson.cargarMercado("data/mercado.json");
        // ✅ Validar mercado
        ValidadorMercado.validar(m);

        System.out.println("Activos: " + m.activos.size());
        System.out.println("Matriz ρ: " + m.rho.length + " x " + m.rho[0].length);
        for (int i = 0; i < Math.min(5, m.activos.size()); i++) {
            System.out.println(" - " + m.activos.get(i));
        }
        System.out.println("✔ Mercado válido");
    }
}
