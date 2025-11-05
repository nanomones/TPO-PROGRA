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

        // Semilla inicial con Greedy
        Asignacion best = heuristicas.GreedyInicial.construir(m, p);
        double bestRet  = CalculadoraRetorno.retornoCartera(m, best, p.presupuesto);
        double bestRisk = CalculadoraRiesgo.riesgoCartera(m, best, p.presupuesto);

        // Orden de exploración: score agresivo (más peso al retorno)
        List<Activo> orden = new ArrayList<>(m.activos);
        orden.sort((a1, a2) -> {
            double s1 = scoreAgresivo(a1);
            double s2 = scoreAgresivo(a2);
            return Double.compare(s2, s1);
        });

        // Estado mutable
        LinkedHashMap<String,Double> asig = new LinkedHashMap<>();
        Map<String,Double> usoTipo   = new HashMap<>();
        Map<String,Double> usoSector = new HashMap<>();

        int[] nodos = new int[]{0};
        backtrack(0, m, p, orden, asig, p.presupuesto, usoTipo, usoSector,
                  new double[]{bestRet}, new Asignacion[]{best}, new double[]{bestRisk}, nodos);

        return new Resultado(best, bestRet, bestRisk, nodos[0]);
    }

    private static void backtrack(int k, Mercado m, Perfil p, List<Activo> ord,
                                  LinkedHashMap<String,Double> asig, double presupuestoRest,
                                  Map<String,Double> usoTipo, Map<String,Double> usoSector,
                                  double[] bestRet, Asignacion[] best, double[] bestRisk, int[] nodos) {
        nodos[0]++;

        if (k == ord.size() || presupuestoRest < 1e-6) {
            evaluarYActualizar(m, p, asig, bestRet, best, bestRisk);
            return;
        }

        // Bound optimista más permisivo
        double ub = boundOptimista(m, p, ord, k, asig, usoTipo, usoSector, presupuestoRest);
        if (ub <= bestRet[0] + 1e-12) return;

        Activo act = ord.get(k);
        double topeActivoAbs = p.maxPorActivo * p.presupuesto;
        double unit = act.montoMin;

        int qmax = (int)Math.floor(Math.min(presupuestoRest, topeActivoAbs) / unit);

        for (int q = qmax; q >= 0; q--) {
            double delta = q * unit;

            if (delta < 1e-9) {
                backtrack(k+1, m, p, ord, asig, presupuestoRest, usoTipo, usoSector, bestRet, best, bestRisk, nodos);
                continue;
            }

            double nuevoTipo   = usoTipo.getOrDefault(act.tipo, 0.0) + delta;
            double nuevoSector = usoSector.getOrDefault(act.sector, 0.0) + delta;
            double limTipo     = p.maxPorTipo.getOrDefault(act.tipo, 1.0)*p.presupuesto;
            double limSector   = p.maxPorSector.getOrDefault(act.sector,1.0)*p.presupuesto;
            if (nuevoTipo > limTipo + 1e-9 || nuevoSector > limSector + 1e-9) continue;

            double prevActual = asig.getOrDefault(act.ticker, 0.0);
            asig.put(act.ticker, prevActual + delta);
            usoTipo.put(act.tipo, nuevoTipo);
            usoSector.put(act.sector, nuevoSector);

            // 🚨 Cambio: ya no podo por riesgo parcial aquí, dejo explorar
            backtrack(k+1, m, p, ord, asig, presupuestoRest - delta,
                      usoTipo, usoSector, bestRet, best, bestRisk, nodos);

            // Deshacer
            if (prevActual <= 1e-12 && delta > 0) {
                asig.remove(act.ticker);
            } else {
                asig.put(act.ticker, prevActual);
            }
            usoTipo.put(act.tipo, nuevoTipo - delta);
            usoSector.put(act.sector, nuevoSector - delta);
        }
    }

    private static double boundOptimista(Mercado m, Perfil p, List<Activo> ord, int k,
                                         Map<String,Double> asig,
                                         Map<String,Double> usoTipo, Map<String,Double> usoSector,
                                         double presupuestoRest) {
        double retParcial = 0.0;
        for (Map.Entry<String,Double> e : asig.entrySet()) {
            int idx = m.indexOf(e.getKey());
            double w = e.getValue() / p.presupuesto;
            retParcial += w * m.activos.get(idx).retorno;
        }

        double retExtra = 0.0;
        double resto = presupuestoRest;

        for (int i=k;i<ord.size() && resto>1e-9;i++){
            Activo a = ord.get(i);
            double limTipoRest   = p.maxPorTipo.getOrDefault(a.tipo, 1.0)*p.presupuesto - usoTipo.getOrDefault(a.tipo, 0.0);
            double limSectorRest = p.maxPorSector.getOrDefault(a.sector,1.0)*p.presupuesto - usoSector.getOrDefault(a.sector,0.0);
            double topeActivo    = p.maxPorActivo * p.presupuesto;

            double cap = Math.max(0.0, Math.min(Math.min(resto, topeActivo), Math.min(limTipoRest, limSectorRest)));
            if (cap <= 1e-12) continue;

            double w = cap / p.presupuesto;
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
            double r = CalculadoraRetorno.retornoCartera(m, a, p.presupuesto);
            double risk = CalculadoraRiesgo.riesgoCartera(m, a, p.presupuesto);

            boolean mejor = false;
            if (r > bestRet[0] + 1e-12 && risk <= p.riesgoMax + 1e-9) {
                mejor = true;
            } else if (Math.abs(r - bestRet[0]) <= 1e-12) {
                double corrA = correlacionMedia(m, a);
                double corrBest = (best[0] == null) ? Double.POSITIVE_INFINITY : correlacionMedia(m, best[0]);
                if (corrA < corrBest - 1e-12) mejor = true;
            }

            if (mejor) {
                bestRet[0]  = r;
                best[0]     = a;
                bestRisk[0] = risk;
            }
        } catch (IllegalArgumentException ignore) {}
    }

    private static double correlacionMedia(Mercado m, Asignacion a){
        List<Integer> idx = new ArrayList<>();
        for (String t : a.getMontos().keySet()){
            if (a.getMonto(t) > 0.0) idx.add(m.indexOf(t));
        }
        if (idx.size() < 2) return 0.0;
        double sum=0.0; int cnt=0;
        for (int i=0;i<idx.size();i++){
            for (int j=i+1;j<idx.size();j++){
                sum += m.rho[idx.get(i)][idx.get(j)];
                cnt++;
            }
        }
        return cnt==0?0.0:sum/cnt;
    }

    // Score agresivo: más peso al retorno puro, menos al sigma
    private static double scoreAgresivo(Activo a){
        double base = a.retorno;
        double penal = (a.sigma > 1e-12) ? a.sigma : 1.0;
        return base / Math.sqrt(penal); // menos castigo al riesgo
    }
}
