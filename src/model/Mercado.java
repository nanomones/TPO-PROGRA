package model;

import java.util.*;

public final class Mercado {
    public final List<Activo> activos;
    public final double[][] rho; // matriz de correlaciones n x n
    public final Map<String, Integer> idxPorTicker;

    public Mercado(List<Activo> activos, double[][] rho) {
        this.activos = List.copyOf(activos);
        this.rho = rho;
        Map<String, Integer> mapa = new HashMap<>();
        for (int i = 0; i < activos.size(); i++) {
            mapa.put(activos.get(i).ticker, i);
        }
        this.idxPorTicker = Collections.unmodifiableMap(mapa);
    }

    public int indexOf(String ticker) {
        Integer idx = idxPorTicker.get(ticker);
        return idx != null ? idx : -1;
    }
}
