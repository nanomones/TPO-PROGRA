@echo off
echo Compilando...

javac -cp "lib\gson-2.10.1.jar" -d bin ^
  src\App.java ^
  src\io\CargadorDatosJson.java ^
  src\io\Reporte.java ^
  src\io\dto\ActivoJson.java ^
  src\io\dto\MercadoJson.java ^
  src\model\Activo.java ^
  src\model\Cliente.java ^
  src\model\Portafolio.java ^
  src\validacion\Validador.java ^
  src\heuristicas\SemillaFactible.java ^
  src\heuristicas\GreedyInicial.java ^
  src\optimizacion\BBPortafolio.java

echo Ejecutando...
java -cp "bin;lib\gson-2.10.1.jar" App
pause
