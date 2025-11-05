// imports
import io.Reporte;
import java.util.Map;
import java.util.Scanner;
import model.*;
import validacion.*;
import heuristicas.*;
import optimizacion.*;
import io.*;

public class App {
    public static void main(String[] args) {
        Mercado m = CargadorDatosJson.cargarMercado("data/mercado.json");
        ValidadorMercado.validar(m);
        // ... logs de mercado ...

        Perfil perfil = elegirPerfil();
        ValidadorPerfil.validar(perfil);
        // ... resto igual (Semilla, Greedy, BB, mutación) ...
    }

    private static Perfil elegirPerfil() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Elegí perfil:");
        System.out.println("1) Moderadamente agresivo (original)");
        System.out.println("2) Agresivo extremo (flexible)");
        System.out.print("> ");
        String opt = sc.nextLine().trim();

        if ("2".equals(opt)) {
            return new Perfil(
                100_000.0,  // presupuesto
                0.50,       // maxPorActivo (50%)
                Map.of("Accion", 1.00, "Bono", 1.00, "ETF", 1.00, "Obligacion Negociable", 1.00),
                Map.of("Tecnologia", 1.00, "Energia", 1.00, "Salud", 1.00, "Consumo", 1.00, "Finanzas", 1.00),
                "Agresivo extremo",
                0.18
            );
        } else {
            return new Perfil(
                100_000.0,
                0.15,
                Map.of("Accion", 0.70, "Bono", 0.60, "ETF", 0.50),
                Map.of("Tecnologia", 0.60, "Energia", 0.50, "Salud", 0.50),
                "Moderadamente agresivo",
                0.18
            );
        }
    }
}


