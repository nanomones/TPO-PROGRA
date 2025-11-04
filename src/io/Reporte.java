package io;

import model.*;

public final class Reporte {
    private Reporte(){}

    public static void imprimirResumen(Mercado m, Perfil p, Asignacion a){
        double ret = CalculadoraRetorno.retornoCartera(m, a, p.presupuesto);
        double sig = CalculadoraRiesgo.riesgoCartera(m, a, p.presupuesto);

        System.out.println("=== CARTERA ===");
        System.out.printf(java.util.Locale.US, "Presupuesto: %.2f%n", p.presupuesto);
        System.out.printf(java.util.Locale.US, "Invertido:   %.2f%n", a.totalInvertido());
        System.out.printf(java.util.Locale.US, "Retorno esp: %.3f%n", ret);
        System.out.printf(java.util.Locale.US, "Riesgo (sigma): %.3f (max %.3f)%n", sig, p.riesgoMax);

        System.out.println("\nDetalle (ticker, monto, % del presupuesto):");
        for (var act : m.activos){
            double monto = a.monto(act.ticker);
            if (monto <= 0) continue;
            double w = monto / p.presupuesto * 100.0;
            System.out.printf(java.util.Locale.US, " - %s: %.2f  (%.2f%%)%n", act.ticker, monto, w);
        }
    }
}
