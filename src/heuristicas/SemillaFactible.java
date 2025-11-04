package heuristicas;

import model.*;
import java.util.*;

public final class SemillaFactible {
    private SemillaFactible(){}

    public static Asignacion construir(Mercado m, Perfil p){
        var asign = new LinkedHashMap<String, Double>();
        double restante = p.presupuesto;

        for (var act : m.activos) {
            double topeActivo = p.maxPorActivo * p.presupuesto;
            double monto = act.montoMin;

            // Debe caber en presupuesto y bajo el tope por activo
            if (monto <= restante && monto <= topeActivo) {
                asign.put(act.ticker, monto);
                restante -= monto;
            }
            if (restante < 1e-6) break; // sin más presupuesto
        }

        var a = new Asignacion(asign);
        validacion.ValidadorAsignacion.validar(m, p, a); // lanza si algo no cumple
        return a;
    }
}
