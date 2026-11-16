#!/bin/bash
# =====================================================================
#  Inicia los 3 servicios del sistema de citas (Mac / Linux).
#  En Mac: doble clic sobre este archivo. Para detenerlos, presiona Enter
#  en esta ventana (o ciérrala).
# =====================================================================
cd "$(dirname "$0")" || exit 1

echo "=============================================="
echo "  Sistema de citas de servicio tecnico (SOA)"
echo "=============================================="

# ---------- 1. Buscar Java 17 o superior ----------
version_de() { "$1" -version 2>&1 | awk -F'"' '/version/ {print $2}' | cut -d. -f1; }
JAVA_BIN=""
for candidato in \
    "$JAVA_HOME/bin/java" \
    "$(/usr/libexec/java_home -v 17+ 2>/dev/null)/bin/java" \
    "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java" \
    "/Applications/IntelliJ IDEA CE.app/Contents/jbr/Contents/Home/bin/java" \
    "/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home/bin/java" \
    "$(command -v java)"; do
  if [ -x "$candidato" ] && [ "$(version_de "$candidato")" -ge 17 ] 2>/dev/null; then
    JAVA_BIN="$candidato"; break
  fi
done
if [ -z "$JAVA_BIN" ]; then
  echo ""
  echo "No se encontro Java 17 o superior."
  echo "Instala 'Temurin 17' (gratis) desde https://adoptium.net y vuelve a abrir este archivo."
  open "https://adoptium.net/temurin/releases/?version=17" 2>/dev/null
  read -r -p "Presiona Enter para salir..."; exit 1
fi
export JAVA_HOME="$(cd "$(dirname "$JAVA_BIN")/.." && pwd)"
export PATH="$JAVA_HOME/bin:$PATH"
echo "Java encontrado: $JAVA_HOME"

# ---------- 2. Compilar (la primera vez descarga dependencias: 1-3 min) ----------
echo ""
echo "Compilando los 3 servicios... (la primera vez tarda unos minutos)"
chmod +x mvnw 2>/dev/null
if ! ./mvnw -q -B clean package -DskipTests; then
  echo "Error al compilar. Revisa tu conexion a internet y vuelve a intentarlo."
  read -r -p "Presiona Enter para salir..."; exit 1
fi

# ---------- 3. Levantar los servicios ----------
mkdir -p logs
detener() {
  echo ""; echo "Deteniendo servicios..."
  kill $PID_CLI $PID_TEC $PID_CIT 2>/dev/null
  wait 2>/dev/null; echo "Listo. Ya puedes cerrar esta ventana."
}
trap detener EXIT

"$JAVA_BIN" -jar servicio-clientes-soap/target/servicio-clientes-soap-1.0.0.jar > logs/clientes.log 2>&1 & PID_CLI=$!
"$JAVA_BIN" -jar servicio-tecnicos-soap/target/servicio-tecnicos-soap-1.0.0.jar > logs/tecnicos.log 2>&1 & PID_TEC=$!
echo "Iniciando Clientes (SOAP :8081) y Tecnicos (SOAP :8082)..."
sleep 2
"$JAVA_BIN" -jar servicio-citas-rest/target/servicio-citas-rest-1.0.0.jar > logs/citas.log 2>&1 & PID_CIT=$!
echo "Iniciando Citas (REST :8080)..."

for i in $(seq 1 60); do
  curl -s -o /dev/null http://localhost:8080/api/servicios && \
  curl -s -o /dev/null http://localhost:8081/ws/clientes.wsdl && \
  curl -s -o /dev/null http://localhost:8082/ws/tecnicos.wsdl && break
  sleep 1
done

echo ""
echo "=============================================="
echo "  LOS 3 SERVICIOS ESTAN CORRIENDO"
echo "=============================================="
echo "  REST Citas      http://localhost:8080/api/servicios"
echo "  WSDL Clientes   http://localhost:8081/ws/clientes.wsdl"
echo "  WSDL Tecnicos   http://localhost:8082/ws/tecnicos.wsdl"
echo ""
echo "  Para probar: doble clic en 'probar.command' o usa Postman"
echo "  (importa docs/Sistema_Citas_SOA.postman_collection.json)."
echo ""
echo "  Lo que va haciendo el orquestador (consola de Citas):"
echo "----------------------------------------------"
open "http://localhost:8080/api/servicios" 2>/dev/null
tail -n 0 -f logs/citas.log | grep --line-buffered "\[Citas\]" &
TAIL_PID=$!
read -r -p "" _
kill $TAIL_PID 2>/dev/null
