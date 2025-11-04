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
    import model.Mercado;
import io.CargadorDatosJson;
import validacion.ValidadorMercado;

import model.Perfil;
import model.Cliente;
import validacion.ValidadorPerfil;

import java.util.Map;

public class App {
    public static void main(String[] args) {
        // 1) Cargar y validar mercado
        Mercado m = CargadorDatosJson.cargarMercado("data/mercado.json");
        ValidadorMercado.validar(m);

        System.out.println("Activos: " + m.activos.size());
        System.out.println("Matriz rho: " + m.rho.length + " x " + m.rho[0].length);
        for (int i = 0; i < Math.min(5, m.activos.size()); i++) {
            System.out.println(" - " + m.activos.get(i));
        }
        System.out.println("✔ Mercado válido");

        // 2) Definir un perfil de ejemplo (ajustá estos números a tu caso del TPO)
        Perfil perfil = new Perfil(
                100_000.0,                 // presupuesto total
                0.25,                      // riesgoMax (desvío std máximo de la cartera)
                0.15,                      // maxPorActivo (máximo 15% del presupuesto por activo)
                Map.of(                    // límites por TIPO (opcionales)
                        "Accion", 0.70,
                        "Bono",   0.60,
                        "ETF",    0.50
                ),
                Map.of(                    // límites por SECTOR (opcionales)
                        "Tecnologia", 0.60,
                        "Energia",    0.50,
                        "Salud",      0.50
                )
        );

        // 3) Validar perfil
        ValidadorPerfil.validar(perfil);
        System.out.println("✔ Perfil válido para optimizar");

        // 4) Asociar a un cliente (opcional en esta fase)
        Cliente c = new Cliente("Cliente Demo", perfil);
        System.out.println("Cliente: " + c.nombre);
    }
}

}
