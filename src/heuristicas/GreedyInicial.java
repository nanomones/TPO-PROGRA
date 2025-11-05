package heuristicas;

import model.*;
import java.util.*;

public final class GreedyInicial {
    private GreedyInicial(){}

    /**
     * Construye una cartera inicial greedy:
     * - Prioriza activos con mejor retorno/sigma
     * - Penaliza correlación con los ya elegidos
     * - Respeta 3..6 activos distintos
     */
    public static Asignacion construir(Mercado m, Perfil p){
        // Semilla inicial válida
        Asignacion base = SemillaFactible.construir(m, p);

        // Estado mutable
        LinkedHashMap<String,Double> asig = new LinkedHashMap<>(base.getMontos());
        Map<String,Double> usoTipo   = new HashMap<>();
        Map<String,Double> usoSector = new HashMap<>();
        int distintos = 0;
        for (Map.Entry<String,Double> e : asig.entrySet()) {
            if (e.getValue() > 0) {
                distintos++;
                int idx = m.indexOf(e.getKey());
                Activo a = m.activos.get(idx);
                usoTipo.put(a.tipo, usoTipo.getOrDefault(a.tipo,0.0) + e.getValue());
                usoSector.put(a.sector, usoSector.getOrDefault(a.sector,0.0) + e.getValue());
            }
        }

        double invertido = 0.0;
        for (double v : asig.values()) invertido += v;
        double presupuestoRest = p.presupuesto - invertido;

        double topePorActivoAbs = p.maxPorActivo * p.presupuesto;

        boolean progreso = true;
        while (progreso) {
            progreso = false;

            // Orden dinámico: score = retorno/sigma penalizado por correlación
            List<Activo> candidatos = new ArrayList<>(m.activos);
            candidatos.sort((a1, a2) -> {
                double s1 = scoreConPenalizacion(m, asig, a1);
                double s2 = scoreConPenalizacion(m, asig, a2);
                return Double.compare(s2, s1); // descendente
            });

            for (Activo a : candidatos) {
                boolean yaEsta = asig.containsKey(a.ticker) && asig.get(a.ticker) > 0.0;

                if (!yaEsta && distintos >= 6) continue;

                double delta = a.montoMin;
                if (delta > presupuestoRest + 1e-9) continue;

                double actual = asig.getOrDefault(a.ticker, 0.0);
                if (actual + delta - topePorActivoAbs > 1e-9) continue;

                double nuevoTipo   = usoTipo.getOrDefault(a.tipo,0.0) + delta;
                double nuevoSector = usoSector.getOrDefault(a.sector,0.0) + delta;
                double limTipo     = p.maxPorTipo.getOrDefault(a.tipo,1.0)*p.presupuesto;
                double limSector   = p.maxPorSector.getOrDefault(a.sector,1.0)*p.presupuesto;
                if (nuevoTipo - limTipo > 1e-9 || nuevoSector - limSector > 1e-9) continue;

                asig.put(a.ticker, actual + delta);
                usoTipo.put(a.tipo, nuevoTipo);
                usoSector.put(a.sector, nuevoSector);

                Asignacion parcial = new Asignacion(asig);
                double sigma = CalculadoraRiesgo.riesgoCartera(m, parcial, p.presupuesto);
                if (sigma - p.riesgoMax > 1e-9) {
                    // deshacer
                    asig.put(a.ticker, actual);
                    usoTipo.put(a.tipo, nuevoTipo - delta);
                    usoSector.put(a.sector, nuevoSector - delta);
                    continue;
                }

                presupuestoRest -= delta;
                if (!yaEsta) distintos++;
                progreso = true;

                if (presupuestoRest < 1e-6) break;
            }
        }

        // Intento final de relleno
        if (presupuestoRest > 1e-6) {
            for (Activo a : m.activos) {
                double delta = Math.min(presupuestoRest, a.montoMin);
                if (delta < a.montoMin - 1e-9) continue;

                double actual = asig.getOrDefault(a.ticker, 0.0);
                double nuevoTipo   = usoTipo.getOrDefault(a.tipo,0.0) + delta;
                double nuevoSector = usoSector.getOrDefault(a.sector,0.0) + delta;
                double limTipo     = p.maxPorTipo.getOrDefault(a.tipo,1.0)*p.presupuesto;
                double limSector   = p.maxPorSector.getOrDefault(a.sector,1.0)*p.presupuesto;

                if (nuevoTipo > limTipo + 1e-9 || nuevoSector > limSector + 1e-9) continue;

                asig.put(a.ticker, actual + delta);
                usoTipo.put(a.tipo, nuevoTipo);
                usoSector.put(a.sector, nuevoSector);

                Asignacion parcial = new Asignacion(asig);
                double sigma = CalculadoraRiesgo.riesgoCartera(m, parcial, p.presupuesto);
                if (sigma <= p.riesgoMax + 1e-9) {
                    presupuestoRest -= delta;
                    break;
                } else {
                    asig.put(a.ticker, actual);
                    usoTipo.put(a.tipo, nuevoTipo - delta);
                    usoSector.put(a.sector, nuevoSector - delta);
                }
            }
        }

        if (distintos < 3 || distintos > 6) {
            return base;
        }

        return new Asignacion(asig);
    }

    // --- Nuevo: score con penalización por correlación ---
    private static double scoreConPenalizacion(Mercado m, Map<String,Double> asig, Activo candidato) {
        double base = candidato.sigma > 1e-12 ? candidato.retorno / candidato.sigma : candidato.retorno;

        // calcular correlación promedio con los ya elegidos
        List<Integer> idx = new ArrayList<>();
        for (String t : asig.keySet()) {
            if (asig.get(t) > 0.0) idx.add(m.indexOf(t));
        }
        if (idx.isEmpty()) return base;

        int idxCand = m.indexOf(candidato.ticker);
        double sum = 0.0; int cnt = 0;
        for (int i : idx) {
            sum += m.rho[i][idxCand];
            cnt++;
        }
        double corrProm = cnt==0?0.0:sum/cnt;

        // penalización: cuanto mayor correlación, menor score
        double alpha = 0.5; // factor de penalización
        return base / (1.0 + alpha * corrProm);
    }
}

