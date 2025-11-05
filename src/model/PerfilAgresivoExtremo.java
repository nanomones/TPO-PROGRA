package model;

import java.util.*;

public class PerfilAgresivoExtremo extends Perfil {

    public PerfilAgresivoExtremo() {
        super();

        this.nombre = "Agresivo extremo";

        // Presupuesto
        this.presupuesto = 100000.0;

        // Objetivo de retorno mínimo
        this.retornoMin = 0.180; // 18%

        // Riesgo máximo permitido
        this.riesgoMax = 0.8; // más alto que el moderado

        // Límites de concentración
        this.maxPorActivo = 0.5; // hasta 50% en un activo

        // Límites por tipo y sector (flexibles)
        this.maxPorTipo   = new HashMap<>();
        this.maxPorSector = new HashMap<>();

        this.maxPorTipo.put("Accion", 1.0);
        this.maxPorTipo.put("ETF", 1.0);
        this.maxPorTipo.put("Bono Soberano", 1.0);
        this.maxPorTipo.put("Obligacion Negociable", 1.0);

        this.maxPorSector.put("Tecnologia", 1.0);
        this.maxPorSector.put("Salud", 1.0);
        this.maxPorSector.put("Consumo", 1.0);
        this.maxPorSector.put("Finanzas", 1.0);
        this.maxPorSector.put("Energia", 1.0);
    }
}
