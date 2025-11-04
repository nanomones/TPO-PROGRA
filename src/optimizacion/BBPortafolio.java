package optimizacion;

import model.*;
import validacion.ValidadorAsignacion;
import java.util.*;

public final class BBPortafolio {

    public static final class Resultado {
        public final Asignacion mejor;
        public final double retorno;
        public final double riesgo;
        public final int nodosVisitados;
        public Resultado(Asignacion a, double r, double s, int nv){
            this.mejor=a; this.retorno=r; this.riesgo=s; this.nodosVisitados=nv;
        }
    }

    private BBPortafolio(){}

    public static Resultado maximizarRetorno(Mercado m, Perfil p){
        final int n = m.activos.size();

        // Orden de exploración por score (ret/sigma) desc
        List<Integer> orden = new ArrayList<>();
        for (int i=0;i<n;i++) orden.add(i);
        orden.sort((i,j)->{
            var ai=m.activos.get(i), aj=m.activos.get(j);
            double si = ai.sigma>0? ai.retorno/ai.sigma : ai.retorno;
            double sj = aj.sigma>0? aj.retorno/aj.sigma : aj.retorno;
            return Double.compare(sj, si);
        });

        // Cota inferior inicial con Greedy
        Asignacion best = heuristicas.GreedyInicial.construir(m, p);
        double bestRet = model.CalculadoraRetorno.retornoCartera(m, best, p.presupuesto);
        double bestRisk= model.CalculadoraRiesgo.riesgoCartera(m, best, p.presupuesto);

        // Estado mutable para backtracking
        var asig = new LinkedHashMap<String,Double>();
        Map<String,Double> usoTipo   = new HashMap<>();
        Map<String,Double> usoSector = new HashMap<>();

        int[] nodos = new int[]{0};
        // DFS con podas
        backtrack(0, m, p, orden, asig, p.presupuesto, usoTipo, usoSector,
                  new double[]{bestRet}, new Asignacion[]{best}, new double[]{bestRisk}, nodos);

        return new Resultado(best, bestRet, bestRisk, nodos[0]);
    }

    private static void backtrack(int k, Mercado m, Perfil p, List<Integer> ord,
                                  LinkedHashMap<String,Double> asig, double presupuestoRest,
                                  Map<String,Double> usoTipo, Map<String,Double> usoSector,
                                  double[] bestRet, Asignacion[] best, double[] bestRisk, int[] nodos) {
        nodos[0]++;

        // Si no hay más candidatos o no queda presupuesto útil → evaluar
        if (k == ord.size() || presupuestoRest < 1e-6) {
            evaluarYActualizar(m, p, asig, bestRet, best, bestRisk);
            return;
        }

        // Poda por cota superior (bound optimista)
        double ub = boundOptimista(m, p, ord, k, asig, usoTipo, usoSector, presupuestoRest);
        if (ub <= bestRet[0] + 1e-12) return;

        var act = m.activos.get(ord.get(k));
        double topeActivo = p.maxPorActivo * p.presupuesto;

        // BRANCH 1: TOMAR (montoMin) si cabe por presupuesto/tope/limites
        if (act.montoMin <= presupuestoRest + 1e-9 && act.montoMin <= topeActivo + 1e-9) {
            double nuevoTipo   = usoTipo.getOrDefault(act.tipo, 0.0) + act.montoMin;
            double nuevoSector = usoSector.getOrDefault(act.sector, 0.0) + act.montoMin;
            double limTipo   = p.maxPorTipo.getOrDefault(act.tipo, 1.0)*p.presupuesto;
            double limSector = p.maxPorSector.getOrDefault(act.sector,1.0)*p.presupuesto;

            if (nuevoTipo <= limTipo + 1e-9 && nuevoSector <= limSector + 1e-9) {
                // aplicar
                asig.put(act.ticker, asig.getOrDefault(act.ticker, 0.0) + act.montoMin);
                usoTipo.put(act.tipo, nuevoTipo);
                usoSector.put(act.sector, nuevoSector);

                // poda por riesgo (tentativa)
                Asignacion parcial = new Asignacion(asig);
                double risk = model.CalculadoraRiesgo.riesgoCartera(m, parcial, p.presupuesto);
                if (risk <= p.riesgoMax + 1e-9) {
                    backtrack(k+1, m, p, ord, asig, presupuestoRest - act.montoMin,
                              usoTipo, usoSector, bestRet, best, bestRisk, nodos);
                }

                // deshacer
                double prev = asig.get(act.ticker) - act.montoMin;
                if (prev <= 1e-12) asig.remove(act.ticker); else asig.put(act.ticker, prev);
                usoTipo.put(act.tipo, nuevoTipo - act.montoMin);
                usoSector.put(act.sector, nuevoSector - act.montoMin);
            }
        }

        // BRANCH 2: NO TOMAR
        backtrack(k+1, m, p, ord, asig, presupuestoRest, usoTipo, usoSector, bestRet, best, bestRisk, nodos);
    }

    // Cota: retorno parcial + retorno máximo fraccional (optimista) con presupuesto restante y límites tipo/sector.
    private static double boundOptimista(Mercado m, Perfil p, List<Integer> ord, int k,
                                         Map<String,Double> asig,
                                         Map<String,Double> usoTipo, Map<String,Double> usoSector,
                                         double presupuestoRest) {
        double retParcial = 0.0;
        for (var e : asig.entrySet()) {
            int idx = m.indexOf(e.getKey());
            double w = e.getValue() / p.presupuesto;
            retParcial += w * m.activos.get(idx).retorno;
        }
        double retExtra = 0.0;
        double resto = presupuestoRest;

        for (int i=k;i<ord.size() && resto>1e-9;i++){
            var a = m.activos.get(ord.get(i));
            double limTipoRest   = p.maxPorTipo.getOrDefault(a.tipo, 1.0)*p.presupuesto - usoTipo.getOrDefault(a.tipo, 0.0);
            double limSectorRest = p.maxPorSector.getOrDefault(a.sector,1.0)*p.presupuesto - usoSector.getOrDefault(a.sector,0.0);
            double topeActivo    = p.maxPorActivo * p.presupuesto;

            double cap = Math.max(0.0, Math.min(Math.min(resto, topeActivo), Math.min(limTipoRest, limSectorRest)));
            if (cap <= 1e-12) continue;

            double w = cap / p.presupuesto;           // fraccional optimista
            retExtra += w * a.retorno;
            resto -= cap;
        }
        return retParcial + retExtra;
    }

    private static void evaluarYActualizar(Mercado m, Perfil p,
                                           Map<String,Double> asig,
                                           double[] bestRet, Asignacion[] best, double[] bestRisk){
        Asignacion a = new Asignacion(asig);
        try {
            ValidadorAsignacion.validar(m, p, a);
            double r = model.CalculadoraRetorno.retornoCartera(m, a, p.presupuesto);
            if (r > bestRet[0]) {
                bestRet[0] = r;
                best[0] = a;
                bestRisk[0] = model.CalculadoraRiesgo.riesgoCartera(m, a, p.presupuesto);
            }
        } catch (IllegalArgumentException ignore) {
            // nodo hoja no factible → descartado
        }
    }
}
