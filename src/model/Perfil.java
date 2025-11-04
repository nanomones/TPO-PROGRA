package model;

import java.util.Map;

public final class Perfil {
    // presupuesto total disponible para invertir
    public final double presupuesto;

    // cota de riesgo aceptada (desvío estándar máximo de la cartera)
    // rango razonable: (0, 1.5]
    public final double riesgoMax;

    // límite de exposición por activo (proporción del presupuesto) — ej: 0.15 = 15%
    // rango razonable: (0, 1]
    public final double maxPorActivo;

    // límites opcionales por TIPO (Accion, Bono, ETF, etc.) en proporciones 0..1
    public final Map<String, Double> maxPorTipo;

    // límites opcionales por SECTOR (Tecnologia, Energia, Salud, etc.) en proporciones 0..1
    public final Map<String, Double> maxPorSector;

    public Perfil(double presupuesto,
                  double riesgoMax,
                  double maxPorActivo,
                  Map<String, Double> maxPorTipo,
                  Map<String, Double> maxPorSector) {

        this.presupuesto = presupuesto;
        this.riesgoMax = riesgoMax;
        this.maxPorActivo = maxPorActivo;
        this.maxPorTipo = (maxPorTipo == null ? java.util.Map.of() : java.util.Map.copyOf(maxPorTipo));
        this.maxPorSector = (maxPorSector == null ? java.util.Map.of() : java.util.Map.copyOf(maxPorSector));
    }
}
