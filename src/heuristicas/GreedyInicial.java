package heuristicas;

import model.*;
import validacion.ValidadorAsignacion;
import java.util.*;

/**
 * Construye una asignación factible priorizando activos con mayor retorno por riesgo (retorno/sigma).
 * Invierte el monto mínimo de cada activo que entra sin violar:
 * - presupuesto
 * - tope por activo
 * - límites por tipo/sector
 * - riesgo máximo de cartera
 */
public final class GreedyInicial {
    private GreedyInicial(){}

    public static Asignacion construir(Mercado m, Perfil p){
        // 1) calcular score por activo
        record Sc(int idx, double score){}
        List<Sc> lista = new ArrayList<>();
        for (int i = 0; i < m.activos.size(); i++) {
            var a = m.activos.get(i);
            double s = (a.sigma > 0 ? a.retorno / a.sigma : a.retorno);
            lista.add(new Sc(i, s));
        }
        // 2) ordenar desc por score
        lista.sort((u,v) -> Double.compare(v.score, u.score));

        // 3) greedy: intentar sumar cada activo con su monto mínimo
        var asig = new LinkedHashMap<String, Double>();
        double restante = p.presupuesto;

        // llevamos acumulados por tipo/sector para podar más rápido
        Map<String, Double> usoTipo   = new HashMap<>();
        Map<String, Double> usoSector = new HashMap<>();

        for (var sc : lista) {
            var act = m.activos.get(sc.idx);

            // presupuesto y tope por activo
            double topeActivo = p.maxPorActivo * p.presupuesto;
            double monto = act.montoMin;
            if (monto > restante + 1e-9) continue;
            if (monto > topeActivo + 1e-9) continue;

            // límites por tipo/sector (chequeo tentativo)
            double nuevoTipo   = usoTipo.getOrDefault(act.tipo, 0.0) + monto;
            double nuevoSector = usoSector.getOrDefault(act.sector, 0.0) + monto;
            double limTipo   = p.maxPorTipo.getOrDefault(act.tipo, 1.0) * p.presupuesto;
            double limSector = p.maxPorSector.getOrDefault(act.sector, 1.0) * p.presupuesto;
            if (nuevoTipo > limTipo + 1e-9)   continue;
            if (nuevoSector > limSector + 1e-9) continue;

            // probar agregar y chequear riesgo
            asig.put(act.ticker, asig.getOrDefault(act.ticker, 0.0) + monto);
            Asignacion tentativa = new Asignacion(asig);
            double sigma = CalculadoraRiesgo.riesgoCartera(m, tentativa, p.presupuesto);

            if (sigma <= p.riesgoMax + 1e-9) {
                // queda, actualizar acumulados y presupuesto
                usoTipo.put(act.tipo, nuevoTipo);
                usoSector.put(act.sector, nuevoSector);
                restante -= monto;
                // validación completa (extra seguridad / mensajes claros)
                try {
                    ValidadorAsignacion.validar(m, p, tentativa);
                } catch (IllegalArgumentException ex){
                    // si por alguna razón cae acá, deshacer y seguir
                    asig.put(act.ticker, asig.get(act.ticker) - monto);
                    if (Math.abs(asig.get(act.ticker)) < 1e-9) asig.remove(act.ticker);
                    restante += monto;
                    usoTipo.put(act.tipo, nuevoTipo - monto);
                    usoSector.put(act.sector, nuevoSector - monto);
                }
            } else {
                // riesgo excedido: deshacer
                asig.put(act.ticker, asig.get(act.ticker) - monto);
                if (Math.abs(asig.get(act.ticker)) < 1e-9) asig.remove(act.ticker);
            }

            if (restante < 1e-6) break; // sin presupuesto útil
        }

        // garantizar factibilidad final o lanzar si quedó vacía y no cabe nada
        Asignacion a = new Asignacion(asig);
        ValidadorAsignacion.validar(m, p, a);
        return a;
    }
}
