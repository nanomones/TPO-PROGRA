# TPO — Optimización de Portafolio (Programación III)

Proyecto en Java para construir un **portafolio de inversión** que **maximiza retorno** respetando **riesgo máximo**, **presupuesto**, **diversificación** y **tamaño** (3 a 6 activos).  
Se implementa **Backtracking + Branch & Bound (Ramificación y Poda)** con **cotas** (LB/UB) y cálculo de riesgo con **matriz de correlaciones**.

---

##  Objetivo
- Elegir 3–6 activos que:
  - Maximicen el **retorno esperado**.
  - Cumplan **riesgo de portafolio ≤ riesgoMáximo** (según perfil).
  - No superen **monto máximo** del cliente.
  - Respeten **diversificación** por sector/tipo.
  - Minimicen **correlación** (mejor diversificación).

---

##  Estructura del proyecto
src/
model/
Activo.java
Perfil.java
Cliente.java
Mercado.java
Portafolio.java
core/
Riesgo.java
Validacion.java
Greedy.java
Bound.java
BranchAndBound.java
io/
CargadorDatos.java
Reporte.java
App.java
data/
activos.csv
correlaciones.csv
docs/
Informe_TPO.docx

---

## 📥 Datos de entrada

### `data/activos.csv` (ejemplo de columnas)
ticker,tipo,sector,retorno,sigma,montoMin
AAPL,Accion,Tecnologia,0.12,0.18,1000
XOM,Accion,Energia,0.09,0.16,1000
TLT,Bono,Bonos,0.05,0.08,1000

### `data/correlaciones.csv` (matriz n×n, diagonal=1)
- Primera fila: encabezados (tickers).
- Cada fila: `ticker_i` seguido de n valores `rho(i, j)` en [−1, 1].

Ejemplo:
,tickers,AAPL,XOM,TLT
AAPL,1,0.35,-0.10
XOM,0.35,1,0.05
TLT,-0.10,0.05,1
> **Importante:** `retorno` y `sigma` en **decimales** (12% → 0.12).

---

## ⚙️ Configuración del cliente (ejemplo)
- **Perfil**: Conservador / Moderado / Agresivo (define `riesgoMax`, `retornoMin`).
- **Parámetros**:
  - `montoMax`, `plazoAnios`
  - `minActivos=3`, `maxActivos=6`
  - `maxPorSector` (p. ej. Tecnología ≤ 2)
  - `maxPorTipo` (p. ej. Acción ≤ 4)

*(Se cargan en `App.java` o `CargadorDatos.java`, según implementación.)*

---

## 🧠 Algoritmo (resumen técnico)

- **Backtracking + Branch & Bound**:
  - Cada nivel decide **tomar/no tomar** el activo *i*.
  - **LB (cota inferior)**: solución **greedy factible** inicial (sin fracciones).
  - **UB (cota superior)**: **greedy fraccional** con presupuesto remanente (optimista).
  - **Poda**: si `UB ≤ mejor`, se descarta la rama.
- **Riesgo de cartera**:
  - Covarianza: `Σᵢⱼ = ρᵢⱼ · σᵢ · σⱼ`
  - Varianza: `varP = wᵀ Σ w`
  - Riesgo: `σₚ = sqrt(varP)`

---

## ▶️ Compilar y ejecutar

### Requisitos
- Java 17+ (o la versión que use su cátedra)
- (Opcional) Maven/Gradle si usan build tool

### Sin build tool
```bash
# compilar
javac -d bin $(find src -name "*.java")

# ejecutar
java -cp bin App
