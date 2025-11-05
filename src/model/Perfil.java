package model;

import java.util.Map;

/**
 * Representa el perfil de riesgo de un cliente inversor.
 * Define presupuesto, límites de riesgo, diversificación y retorno mínimo esperado.
 */
public final class Perfil {

    private final double presupuesto;
    private final double riesgoMax;               // sigma máximo permitido (decimal: 0.20 = 20%)
    private final double maxPorActivo;
    private final Map<String, Double> maxPorTipo;
    private final Map<String, Double> maxPorSector;

    private final String tipoPerfil;              // Conservador, Moderadamente conservador, Moderado, Moderadamente agresivo, Agresivo
    private final double retornoMin;              // mínimo del perfil (decimal: 0.10 = 10%)
    private final double retornoMinDeseado;       // mínimo pedido por el cliente (>= retornoMin del perfil)
    private final int plazoAnios;                 // en años

    /**
     * Constructor que configura riesgoMax y retornoMin automáticamente según el tipo de perfil.
     *
     * @param presupuesto       Presupuesto total (moneda)
     * @param maxPorActivo      Tope por activo (fracción 0..1)
     * @param maxPorTipo        Topes por tipo (fracción 0..1)
     * @param maxPorSector      Topes por sector (fracción 0..1)
     * @param tipoPerfil        Uno de: "Conservador", "Moderadamente conservador", "Moderado",
     *                          "Moderadamente agresivo", "Agresivo"
     * @param retornoDeseado    Retorno mínimo deseado por el cliente (decimal). Se forzará a ser >= retornoMin del perfil.
     * @param plazoAnios        Plazo de inversión en años
     */
    public Perfil(double presupuesto,
                  double maxPorActivo,
                  Map<String, Double> maxPorTipo,
                  Map<String, Double> maxPorSector,
                  String tipoPerfil,
                  double retornoDeseado,
                  int plazoAnios) {

        this.presupuesto = presupuesto;
        this.maxPorActivo = maxPorActivo;
        this.maxPorTipo = maxPorTipo;
        this.maxPorSector = maxPorSector;
        this.tipoPerfil = tipoPerfil;
        this.plazoAnios = plazoAnios;

        String t = tipoPerfil.trim().toLowerCase();
        if (t.equals("conservador")) {
            this.riesgoMax = 0.20;
            this.retornoMin = 0.10;
        } else if (t.equals("moderadamente conservador")) {
            this.riesgoMax = 0.30;
            this.retornoMin = 0.12;
        } else if (t.equals("moderado")) {
            this.riesgoMax = 0.40;
            this.retornoMin = 0.14;
        } else if (t.equals("moderadamente agresivo")) {
            this.riesgoMax = 0.50;
            this.retornoMin = 0.16;
        } else if (t.equals("agresivo")) {
            this.riesgoMax = 0.60;
            this.retornoMin = 0.18;
        } else {
            throw new IllegalArgumentException("Tipo de perfil no reconocido: " + tipoPerfil);
        }

        this.retornoMinDeseado = Math.max(retornoDeseado, this.retornoMin);
    }

    // --- Getters públicos para usar en App.java y otros módulos ---
    public double getPresupuesto() { return presupuesto; }
    public double getRiesgoMax() { return riesgoMax; }
    public double getMaxPorActivo() { return maxPorActivo; }
    public Map<String, Double> getMaxPorTipo() { return maxPorTipo; }
    public Map<String, Double> getMaxPorSector() { return maxPorSector; }
    public String getTipoPerfil() { return tipoPerfil; }
    public double getRetornoMin() { return retornoMin; }
    public double getRetornoMinDeseado() { return retornoMinDeseado; }
    public int getPlazoAnios() { return plazoAnios; }
}
