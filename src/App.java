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
        System.out.println("OK: Mercado valido");

        // 2) Perfil de ejemplo (ajustá valores a tu caso)
        Perfil perfil = new Perfil(
                100_000.0,                 // presupuesto
                0.25,                      // riesgo máximo cartera
                0.15,                      // max % por activo
                Map.of(                    // límites por tipo (opcionales)
                        "Accion", 0.70,
                        "Bono",   0.60,
                        "ETF",    0.50
                ),
                Map.of(                    // límites por sector (opcionales)
                        "Tecnologia", 0.60,
                        "Energia",    0.50,
                        "Salud",      0.50
                )
        );

        ValidadorPerfil.validar(perfil);
        System.out.println("OK: Perfil valido");

        Cliente c = new Cliente("Cliente Demo", perfil);
        System.out.println("Cliente: " + c.nombre);
        // Semilla factible (siempre valida)
var a0 = heuristicas.SemillaFactible.construir(m, perfil);
io.Reporte.imprimirResumen(m, perfil, a0);
System.out.println("✔ Parte 1: semilla factible generada y reportada");
  // 5) Demo de asignación y validación
// (ajustá montos a tu gusto, esto es solo para probar la validación)
var asignacion = new java.util.LinkedHashMap<String, Double>();
// invertimos 10% del presupuesto en los 3 primeros activos, por ejemplo
double P = perfil.presupuesto;
for (int i = 0; i < Math.min(3, m.activos.size()); i++) {
    var t = m.activos.get(i).ticker;
    asignacion.put(t, 0.10 * P);
}
// el resto en 0 (implícito)
var a = new model.Asignacion(asignacion);

// validar contra perfil/mercado
validacion.ValidadorAsignacion.validar(m, perfil, a);
System.out.println("OK: Asignacion valida. Riesgo dentro del limite.");
        // --- GREEDY ---
var aGreedy = heuristicas.GreedyInicial.construir(m, perfil);
System.out.println("\n--- GREEDY ---");
io.Reporte.imprimirResumen(m, perfil, aGreedy);
     }
  }

