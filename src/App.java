import model.Mercado;
import io.CargadorDatosJson;
import validacion.ValidadorMercado;

public class App {
    public static void main(String[] args) {
        // 1️⃣ Cargar el JSON del mercado
        Mercado m = CargadorDatosJson.cargarMercado("data/mercado.json");

        // 2️⃣ Validar los datos cargados
        ValidadorMercado.validar(m);

        // 3️⃣ Mostrar resumen si todo está correcto
        System.out.println("Activos: " + m.activos.size());
        System.out.println("Matriz ρ: " + m.rho.length + " x " + m.rho[0].length);
        for (int i = 0; i < Math.min(5, m.activos.size()); i++) {
            System.out.println(" - " + m.activos.get(i));
        }
        System.out.println("✔ Mercado válido");
    }
}
