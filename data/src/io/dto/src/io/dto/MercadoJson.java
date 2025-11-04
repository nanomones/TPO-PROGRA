package io.dto;

import java.util.List;

public class MercadoJson {
    public List<ActivoJson> activos;
    public List<List<Double>> correlaciones; // matriz n x n (mismo orden que activos)
}
