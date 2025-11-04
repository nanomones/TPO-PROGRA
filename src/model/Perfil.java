package model;

import java.util.Map;

public final class Perfil {

    public final double presupuesto;
    public final double riesgoMax;
    public final double maxPorActivo;
    public final Map<String, Double> maxPorTipo;
    public final Map<String, Double> maxPorSector;

    public final String tipoPerfil;       // Conservador, Moderado, Agresivo, etc.
    public final double retornoMin;       // Retorno mínimo del perfil base
    public final double retornoMinDeseado;// Retorno mínimo pedido por el cliente
    public final int plazoAnios = 1;      //  siempre 1 año

    public Perfil(
            double presupuesto,
            double maxPorActivo,
            Map<String, Double> maxPorTipo,
            Map<String, Double> maxPorSector,
            String tipoPerfil,
            double retornoMinDeseado
    ) {
        this.presupuesto = presupuesto;
        this.maxPorActivo = maxPorActivo;
        this.maxPorTipo = maxPorTipo;
        this.maxPorSector = maxPorSector;
        this.tipoPerfil = tipoPerfil;

        // Configuración automática según tipo de perfil 
        switch (tipoPerfil.toLowerCase()) {
            case "conservador":
                this.riesgoMax = 0.15;
                this.retornoMin = 0.08;
                break;
            case "moderado":
                this.riesgoMax = 0.25;
                this.retornoMin = 0.14;
                break;
            case "agresivo":
                this.riesgoMax = 0.40;
                this.retornoMin = 0.20;
                break;
            case "arriesgado":
            case "muy agresivo":
                this.riesgoMax = 0.55;
                this.retornoMin = 0.28;
                break;
            default:
                throw new IllegalArgumentException("Tipo de perfil no reconocido: " + tipoPerfil);
        }

        // El cliente puede pedir más retorno del que su perfil base garantiza
        this.retornoMinDeseado = Math.max(retornoMinDeseado, this.retornoMin);
    }
}

