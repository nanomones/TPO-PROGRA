import model.Mercado;
import io.CargadorDatosJson;
import validacion.ValidadorMercado;

import model.Perfil;
import model.Cliente;
import validacion.ValidadorPerfil;

import model.Asignacion;
import heuristicas.SemillaFactible;
import heuristicas.GreedyInicial;

import io.Reporte;

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

        // 2) Perfil de ejemplo
        Perfil perfil = new Perfil(
                100_000.0,                 // presupuesto
                0.25,                      // riesgo max
                0.15,                      // max % por activo
                Map.of("Accion", 0.70, "Bono", 0.60, "ETF", 0.50),
                Map.of("Tecnologia", 0.60, "Energia", 0.50, "Salud", 0.50)
        );
        ValidadorPerfil.validar(perfil);
        System.out.println("OK: Perfil valido");

        Cliente c = new Cliente("Cliente Demo", perfil);
        System.out.println("Cliente: " + c.nombre);

        // 3) SEMILLA (siempre valida)
        Asignacion a0 = SemillaFactible.construir(m, perfil);
        System.out.println("\n--- SEMILLA ---");
        Reporte.imprimirResumen(m, perfil, a0);

        // 4) GREEDY (mejora retorno manteniendo restricciones)
        Asignacion aGreedy = GreedyInicial.construir(m, perfil);
        System.out.println("\n--- GREEDY ---");
        Reporte.imprimirResumen(m, perfil, aGreedy);
    }
}
