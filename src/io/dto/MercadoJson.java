package io.dto;

import java.util.List;

public class MercadoJson {
    public List<ActivoJson> activos;          // lista de activos
    public List<List<Double>> correlaciones;  // matriz n x n de correlaciones
}
