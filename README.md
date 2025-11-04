# 💼 TPO — Optimización de Portafolio de Inversión  
**Materia:** Programación III  
**Lenguaje:** Java  
**Integrantes:** Ignacio Mones Ruiz — Francisco Gomez  
**Año:** 2025  

---

## 🧭 Descripción general

Este proyecto implementa un sistema que construye un **portafolio de inversión óptimo**, maximizando el **retorno esperado** y cumpliendo con las **restricciones de riesgo, presupuesto y diversificación** definidas por un cliente.

El algoritmo combina **Backtracking** con **Ramificación y Poda (Branch & Bound)** para encontrar la mejor combinación de activos según el perfil del inversor.

---

## 🎯 Objetivo del sistema

Diseñar un portafolio que:

- ✅ **Maximice el retorno esperado (ganancia)**.  
- ⚖️ **Respete el riesgo máximo permitido**, según el perfil del cliente.  
- 💰 **No supere el monto máximo disponible para invertir**.  
- 🧩 **Cumpla las reglas de diversificación** (por tipo de activo y sector).  
- 🔢 **Incluya entre 3 y 6 activos**.  
- 🔗 **Minimice la correlación entre activos**, mejorando la diversificación.  
- ⏱️ **Considere el plazo de inversión esperado** (plazos cortos → menos riesgo, plazos largos → mayor tolerancia).

---

## 📊 Datos utilizados

### Activos financieros (`data/activos.csv`)
Cada activo contiene:
| Campo | Descripción |
|--------|--------------|
| **Ticker** | Identificador (ej: AAPL, XOM, TLT) |
| **Tipo** | Acción, Bono, ETF, CEDEAR, ON, etc. |
| **Sector** | Tecnología, Energía, Finanzas, etc. |
| **Retorno esperado** | Rentabilidad anual esperada (en decimal) |
| **Riesgo (σ)** | Desvío estándar del rendimiento (en decimal) |
| **Monto mínimo** | Inversión mínima en ese activo |

### Correlaciones (`data/correlaciones.csv`)
Una **matriz n×n** con los coeficientes de correlación entre cada par de activos (de −1 a +1).  
La diagonal principal vale 1.

Ejemplo:
,ticker,AAPL,XOM,TLT
AAPL,1,0.35,-0.10
XOM,0.35,1,0.05
TLT,-0.10,0.05,1

yaml
Copiar código

---

## 👤 Parámetros del cliente

Cada cliente define:

| Parámetro | Descripción |
|------------|--------------|
| **Perfil** | Conservador / Moderado / Agresivo |
| **Riesgo máximo permitido** | σₚ ≤ límite del perfil |
| **Retorno mínimo deseado** | Rₚ ≥ umbral del perfil |
| **Monto máximo** | Capital disponible para invertir |
| **Plazo esperado (años)** | Horizonte temporal de inversión |
| **Diversificación** | Máximo por tipo y sector (ej: máx 2 acciones de Tecnología) |
| **Cantidad de activos** | Entre 3 y 6 |

---

## ⚙️ Funcionamiento del algoritmo

El algoritmo se basa en **Backtracking + Branch & Bound (Ramificación y Poda)**.

1. **Backtracking:**  
   Se construye el portafolio evaluando cada activo: *tomarlo o no tomarlo*.  
   El árbol de decisiones explora todas las combinaciones posibles (2ⁿ ramas).

2. **Branch & Bound:**  
   Se aplican **cotas** para **poda temprana**:
   - **Cota Inferior (LB):** solución *greedy* factible inicial (sin fracciones).  
   - **Cota Superior (UB):** solución *optimista* (greedy fraccional) con el presupuesto restante.  
   Si `UB ≤ mejorLB`, se **poda la rama**.

3. **Riesgo del portafolio:**  
   Se calcula usando la **matriz de covarianzas Σ**, derivada de las correlaciones:
   \[
   Σ_{ij} = ρ_{ij} · σ_i · σ_j
   \]
   \[
   σ_p = \sqrt{w^T · Σ · w}
   \]
   donde `w` son los pesos de inversión.

4. **Validaciones:**
   - Riesgo total ≤ riesgo máximo del perfil.  
   - Retorno total ≥ retorno mínimo.  
   - Monto total ≤ presupuesto.  
   - 3 ≤ activos ≤ 6.  
   - Cumplir límites por tipo y sector.

---

## 🧮 Estructura del código (Java)

src/
model/
Activo.java # Clase con datos de cada activo
Perfil.java # Define límites de riesgo y retorno
Cliente.java # Preferencias y presupuesto del cliente
Mercado.java # Lista de activos + matriz de correlaciones
Portafolio.java # Composición del portafolio (selección + pesos)

core/
Riesgo.java # Cálculo de riesgo total (σₚ = √(wᵀΣw))
Validacion.java # Reglas del sistema (riesgo, retorno, diversificación)
Greedy.java # Estrategia para LB (solución factible inicial)
Bound.java # Cálculo de UB (estimación optimista)
BranchAndBound.java# Algoritmo principal con poda

io/
CargadorDatos.java # Lee CSV (activos, correlaciones)
Reporte.java # Genera y muestra los resultados

App.java # Punto de entrada

yaml
Copiar código

---

## ▶️ Ejecución

### Compilación manual
```bash
javac -d bin $(find src -name "*.java")
