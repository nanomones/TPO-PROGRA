package heuristicas;

import model.*;
import java.util.*;

public final class GreedyInicial {
    private GreedyInicial(){}

    /**
     * Mejora la semilla: prioriza mayor retorno/sigma,
     * respeta 3..6 activos distintos. Si ya hay 6, sólo aumenta montos
     * en los que ya están (múltiplos del montoMin).
     */
    public static Asignacion construir(Mercado m, Perfil p){
        // partimos de una semilla válida (3..6)
        Asignacion base = SemillaFactible.construir(m, p);

        // estado mutable
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

        // orden candidatos por score retorno/sigma
        List<Activo> candidatos = new ArrayList<>(m.activos);
        candidatos.sort((a1, a2) -> {
            double s1 = a1.sigma > 1e-12 ? a1.retorno / a1.sigma : a1.retorno;
            double s2 = a2.sigma > 1e-12 ? a2.retorno / a2.sigma : a2.retorno;
            return Double.compare(s2, s1); // orden descendente
        });

        double topePorActivoAbs = p.maxPorActivo * p.presupuesto;

        boolean progreso = true;
        while (progreso) {
            progreso = false;

            for (Activo a : candidatos) {
                boolean yaEsta = asig.containsKey(a.ticker) && asig.get(a.ticker) > 0.0;

                // Si NO está y ya tengo 6 distintos, no puedo agregar otro distinto
                if (!yaEsta && distintos >= 6) continue;

                // siguiente incremento es 1× montoMin
                double delta = a.montoMin;
                if (delta > presupuestoRest + 1e-9) continue;

                // no pasarse del tope por activo
                double actual = asig.getOrDefault(a.ticker, 0.0);
                if (actual + delta - topePorActivoAbs > 1e-9) continue;

                // topes por tipo/sector
                double nuevoTipo   = usoTipo.getOrDefault(a.tipo,0.0) + delta;
                double nuevoSector = usoSector.getOrDefault(a.sector,0.0) + delta;
                double limTipo     = p.maxPorTipo.getOrDefault(a.tipo,1.0)*p.presupuesto;
                double limSector   = p.maxPorSector.getOrDefault(a.sector,1.0)*p.presupuesto;
                if (nuevoTipo - limTipo > 1e-9 || nuevoSector - limSector > 1e-9) continue;

                // aplicar y verificar riesgo
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

                // quedó aplicado
                presupuestoRest -= delta;
                if (!yaEsta) distintos++;
                progreso = true;

                // si ya no hay presupuesto útil, corto
                if (presupuestoRest < 1e-6) break;
            }
        }

        // 🔧 Intento final: usar el presupuesto restante si quedó algo sin invertir
        if (presupuestoRest > 1e-6) {
            for (Activo a : candidatos) {
                double delta = Math.min(presupuestoRest, a.montoMin);
                if (delta < a.montoMin - 1e-9) continue; // no alcanza para un mínimo

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
                    break; // aplicamos un relleno y salimos
                } else {
                    // deshacer si rompe riesgo
                    asig.put(a.ticker, actual);
                    usoTipo.put(a.tipo, nuevoTipo - delta);
                    usoSector.put(a.sector, nuevoSector - delta);
                }
            }
        }

        // asegurar 3..6 distintos
        if (distintos < 3 || distintos > 6) {
            // En caso rarísimo, volvamos a la base
            return base;
        }

        return new Asignacion(asig);
    }
}

