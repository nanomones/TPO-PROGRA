package model;

import java.util.*;

public final class Asignacion {
    // Monto invertido por ticker (en la misma moneda que el presupuesto)
    public final Map<String, Double> montoPorTicker;

    public Asignacion(Map<String, Double> montoPorTicker) {
        // Normalizamos: sin nulls, sin negativos
        var tmp = new LinkedHashMap<String, Double>();
        for (var e : montoPorTicker.entrySet()) {
            double v = e.getValue() == null ? 0.0 : e.getValue();
            if (v < 0) throw new IllegalArgumentException("Monto negativo en " + e.getKey());
            tmp.put(e.getKey(), v);
        }
        this.montoPorTicker = Collections.unmodifiableMap(tmp);
    }

    public double totalInvertido() {
        double s = 0.0;
        for (double v : montoPorTicker.values()) s += v;
        return s;
    }

    public double monto(String ticker) {
        return montoPorTicker.getOrDefault(ticker, 0.0);
    }
}
