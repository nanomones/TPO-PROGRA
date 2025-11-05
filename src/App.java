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
        Scanner sc = new Scanner(System.in);

        // 1) Cargar y validar mercado
        Mercado m = CargadorDatosJson.cargarMercado("data/mercado.json");
        ValidadorMercado.validar(m);

        System.out.println("Activos: " + m.activos.size());
        System.out.println("Matriz rho: " + m.rho.length + " x " + m.rho[0].length);
        for (int i = 0; i < Math.min(5, m.activos.size()); i++) {
            System.out.println(" - " + m.activos.get(i));
        }
        System.out.println("OK: Mercado valido");

        // 2) Elegir perfil base
        Perfil perfilBase = elegirPerfil(sc);

        // 3) Datos adicionales del cliente
        System.out.print("Ingrese monto máximo a invertir: ");
        double monto = Double.parseDouble(sc.nextLine().trim());

        System.out.print("Ingrese plazo de inversión (en años): ");
        int plazo = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Ingrese retorno mínimo deseado (ej. 0.18 para 18%): ");
        double retornoDeseado = Double.parseDouble(sc.nextLine().trim());

        // Preferencias de diversificación
        System.out.println("Ingrese preferencias de diversificación por sector (en % que sumen 100):");
        System.out.print("Tecnologia: ");
        double pctTec = Double.parseDouble(sc.nextLine().trim()) / 100.0;
        System.out.print("Energia: ");
        double pctEner = Double.parseDouble(sc.nextLine().trim()) / 100.0;
        System.out.print("Salud: ");
        double pctSalud = Double.parseDouble(sc.nextLine().trim()) / 100.0;
        double pctOtrosSector = 1.0 - (pctTec + pctEner + pctSalud);

        System.out.println("Ingrese preferencias de diversificación por tipo de activo (en % que sumen 100):");
        System.out.print("Bonos: ");
        double pctBonos = Double.parseDouble(sc.nextLine().trim()) / 100.0;
        System.out.print("Acciones: ");
        double pctAcciones = Double.parseDouble(sc.nextLine().trim()) / 100.0;
        System.out.print("ETFs: ");
        double pctEtfs = Double.parseDouble(sc.nextLine().trim()) / 100.0;
        double pctOtrosTipo = 1.0 - (pctBonos + pctAcciones + pctEtfs);

        // 4) Construir perfil final
        Perfil perfil = new Perfil(
            monto,
            perfilBase.maxPorActivo,
            Map.of("Accion", pctAcciones, "Bono", pctBonos, "ETF", pctEtfs, "Otros", pctOtrosTipo),
            Map.of("Tecnologia", pctTec, "Energia", pctEner, "Salud", pctSalud, "Otros", pctOtrosSector),
            perfilBase.nombre,
            Math.max(perfilBase.retornoMin, retornoDeseado)
        );

        ValidadorPerfil.validar(perfil);
        System.out.println("OK: Perfil valido");

        Cliente c = new Cliente("Cliente Demo", perfil);
        System.out.println("Cliente: " + c.nombre + " | Plazo: " + plazo + " año(s)");

        // 5) SEMILLA
        Asignacion a0 = SemillaFactible.construir(m, perfil);
        System.out.println("\n--- SEMILLA ---");
        Reporte.imprimirResumen(m, perfil, a0);

        // 6) GREEDY
        Asignacion aGreedy = GreedyInicial.construir(m, perfil);
        System.out.println("\n--- GREEDY ---");
        Reporte.imprimirResumen(m, perfil, aGreedy);

        // 7) BRANCH & BOUND
        BBPortafolio.Resultado res = BBPortafolio.maximizarRetorno(m, perfil);
        System.out.println("\n--- BRANCH & BOUND ---");
        Reporte.imprimirResumen(m, perfil, res.mejor);

        // Alternativa 1: Greedy
        System.out.println("\n===== Alternativa 1: Portafolio Greedy =====");
        Reporte.imprimirResumen(m, perfil, aGreedy);

        // Alternativa 2: Mutación
        System.out.println("\n===== Alternativa 2: Portafolio Mutado =====");
        Asignacion alternativa2 = mutarAsignacion(res.mejor, m, perfil);
        Reporte.imprimirResumen(m, perfil, alternativa2);

        System.out.println("Nodos visitados: " + res.nodosVisitados);
    }

    // --- Menú de perfiles base según TPO ---
    private static Perfil elegirPerfil(Scanner sc) {
        System.out.println("Elegí perfil:");
        System.out.println("1) Conservador (riesgo ≤ 0.20, retorno ≥ 0.10)");
        System.out.println("2) Moderadamente conservador (riesgo ≤ 0.30, retorno ≥ 0.12)");
        System.out.println("3) Moderado (riesgo ≤ 0.40, retorno ≥ 0.14)");
        System.out.println("4) Moderadamente agresivo (riesgo ≤ 0.50, retorno ≥ 0.16)");
        System.out.println("5) Agresivo (riesgo ≤ 0.60, retorno ≥ 0.18)");
        System.out.print("> ");
        String opt = sc.nextLine().trim();

        switch (opt) {
            case "1":
                return new Perfil(100_000.0, 0.20, Map.of(), Map.of(), "Conservador", 0.10);
            case "2":
                return new Perfil(100_000.0, 0.30, Map.of(), Map.of(), "Moderadamente conservador", 0.12);
            case "3":
                return new Perfil(100_000.0, 0.40, Map.of(), Map.of(), "Moderado", 0.14);
            case "4":
                return new Perfil(100_000.0, 0.50, Map.of(), Map.of(), "Moderadamente agresivo", 0.16);
            case "5":
            default:
                return new Perfil(100_000.0, 0.60, Map.of(), Map.of(), "Agresivo", 0.18);
        }
    }

    // --- Mutación simple ---
    private static Asignacion mutarAsignacion(Asignacion original, Mercado mercado, Perfil perfil) {
        Map<String, Double> nuevaAsignacion = new java.util.LinkedHashMap<>(original.getMontos());

        String tickerMenor = null;
        double retornoMenor = Double.MAX_VALUE;

        for (String t : nuevaAsignacion.keySet()) {
            Activo a = mercado.buscarPorTicker(t);
            if (a != null && a.retorno < retornoMenor) {
                retornoMenor = a.retorno;
                tickerMenor = t;
            }
        }

        if (tickerMenor != null) {
            nuevaAsignacion.remove(tickerMenor);

            Activo mejorReemplazo = null;
            for (Activo a : mercado.activos) {
                if (!nuevaAsignacion.containsKey(a.ticker)
                        && a.sigma <= perfil.riesgoMax
                        && a.retorno > retornoMenor) {
                    if (mejorReemplazo == null || a.retorno > mejorReemplazo.retorno) {
                        mejorReemplazo = a;
                    }
                }
            }

            if (mejorReemplazo != null) {
                double montoAnterior = original.getMonto(tickerMenor);
                nuevaAsignacion.put(mejorReemplazo.ticker, montoAnterior);
            } else {
                nuevaAsignacion.put(tickerMenor, original.getMonto(tickerMenor));
            }
        }

        return new Asignacion(nuevaAsignacion);
    }
}

