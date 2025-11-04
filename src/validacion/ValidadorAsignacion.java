package validacion;

import model.*;
import java.util.*;

public final class ValidadorAsignacion {
    private ValidadorAsignacion(){}

    public static void validar(Mercado m, Perfil p, Asignacion a) {
        if (m == null || p == null || a == null) throw new IllegalArgumentException("Argumento nulo");

        double total = a.totalInvertido();
        if (total - p.presupuesto > 1e-6)
            throw new IllegalArgumentException("Excede presupuesto: " + total + " > " + p.presupuesto);

        // Mapas auxiliares de sumas por tipo/sector
        Map<String, Double> porTipo = new HashMap<>();
        Map<String, Double> porSector = new HashMap<>();

        // Recorremos activos del mercado para validar por-activo y acumular por tipo/sector
        for (var act : m.activos) {
            double monto = a.monto(act.ticker);
            if (monto > 0 && monto + 1e-9 < act.montoMin)
                throw new IllegalArgumentException("Monto < montoMin en " + act.ticker + ": " + monto + " < " + act.montoMin);

            if (monto - p.maxPorActivo * p.presupuesto > 1e-6)
                throw new IllegalArgumentException("Supera maxPorActivo en " + act.ticker);

            porTipo.merge(act.tipo, monto, Double::sum);
            porSector.merge(act.sector, monto, Double::sum);
        }

        // Límites por tipo
        for (var e : p.maxPorTipo.entrySet()) {
            double usado = porTipo.getOrDefault(e.getKey(), 0.0);
            double limite = e.getValue() * p.presupuesto;
            if (usado - limite > 1e-6)
                throw new IllegalArgumentException("Excede límite por tipo '" + e.getKey() + "': " + usado + " > " + limite);
        }

        // Límites por sector
        for (var e : p.maxPorSector.entrySet()) {
            double usado = porSector.getOrDefault(e.getKey(), 0.0);
            double limite = e.getValue() * p.presupuesto;
            if (usado - limite > 1e-6)
                throw new IllegalArgumentException("Excede límite por sector '" + e.getKey() + "': " + usado + " > " + limite);
        }

        // Riesgo de cartera
        double sigma = CalculadoraRiesgo.riesgoCartera(m, a, p.presupuesto);
        if (sigma - p.riesgoMax > 1e-9)
            throw new IllegalArgumentException("Riesgo de cartera excedido: σ=" + sigma + " > " + p.riesgoMax);
    }
}
