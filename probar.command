#!/bin/bash
# =====================================================================
#  Prueba las funciones del sistema (los servicios deben estar corriendo:
#  primero abre 'iniciar.command'). En Mac: doble clic sobre este archivo.
# =====================================================================
API=http://localhost:8080/api

# Fechas de prueba: el proximo dia habil (lunes a sabado) y el proximo domingo
dia() { date -v+"$1"d +%F 2>/dev/null || date -d "+$1 day" +%F; }
dow() { date -j -f %F "$1" +%u 2>/dev/null || date -d "$1" +%u; }
for n in 1 2 3; do F=$(dia $n); [ "$(dow "$F")" != "7" ] && break; done
for n in 1 2 3 4 5 6 7; do D=$(dia $n); [ "$(dow "$D")" = "7" ] && break; done

if ! curl -s -o /dev/null "$API/servicios"; then
  echo "Los servicios no estan corriendo. Abre primero 'iniciar.command'."
  read -r -p "Presiona Enter para salir..."; exit 1
fi

paso() { echo ""; echo "=================================================="; echo "  $1"; echo "=================================================="; }
pedir() { echo "-> $1 $2"; [ -n "$3" ] && echo "   cuerpo: $3"; echo "<- respuesta:";
  if [ -n "$3" ]; then curl -s -w "\n   [HTTP %{http_code}]\n" -X "$1" "$API$2" -H "Content-Type: application/json" -d "$3";
  else curl -s -w "\n   [HTTP %{http_code}]\n" -X "$1" "$API$2"; fi; }

paso "1. Tipos de servicio que ofrece M&A Electric"
pedir GET /servicios

paso "2. Horarios libres para MANTENIMIENTO el $F (consulta SOAP a Tecnicos)"
pedir GET "/servicios/MANTENIMIENTO/disponibilidad?fecha=$F"

paso "3. Cliente registrado (DNI 70123456) reserva mantenimiento -> 201 CONFIRMADA"
R=$(curl -s -X POST "$API/citas" -H "Content-Type: application/json" -d "{\"dni\":\"70123456\",\"tipoServicio\":\"MANTENIMIENTO\",\"fecha\":\"$F\",\"hora\":\"10:00\",\"descripcion\":\"Mantenimiento de grupo electrogeno 10 kVA\"}")
echo "$R"
ID=$(echo "$R" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')

paso "4. Cliente NUEVO: se registra automaticamente y obtiene cita -> 201"
pedir POST /citas "{\"dni\":\"7$(date +%s | tail -c 8)\",\"nombre\":\"Rosa Mendoza\",\"telefono\":\"999111222\",\"direccion\":\"Av. Peru 450, SMP\",\"tipoServicio\":\"REPARACION\",\"fecha\":\"$F\",\"hora\":\"12:00\",\"descripcion\":\"El generador no arranca\"}"

paso "5. Cita en domingo ($D): no hay tecnicos -> 400 RECHAZADA"
pedir POST /citas "{\"dni\":\"70123456\",\"tipoServicio\":\"INSTALACION\",\"fecha\":\"$D\",\"hora\":\"10:00\"}"

paso "6. Datos invalidos (DNI de 3 digitos) -> 400"
pedir POST /citas '{"dni":"123","tipoServicio":"MANTENIMIENTO","fecha":"'"$F"'","hora":"10:00"}'

paso "7. Consultar la cita #$ID"
pedir GET "/citas/$ID"

paso "8. Cancelar la cita #$ID -> 200 CANCELADA (libera el horario del tecnico)"
pedir PUT "/citas/$ID/cancelar"

paso "9. Cancelarla otra vez -> 409 (ya no esta confirmada)"
pedir PUT "/citas/$ID/cancelar"

paso "10. Cita que no existe -> 404"
pedir GET /citas/9999

paso "11. Llamada SOAP directa a Clientes: validarCliente(70123456)"
curl -s -H "Content-Type: text/xml" -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:cli="http://idat.edu.pe/clientes"><soapenv:Body><cli:validarClienteRequest><cli:dni>70123456</cli:dni></cli:validarClienteRequest></soapenv:Body></soapenv:Envelope>' http://localhost:8081/ws
echo ""
echo ""
echo "Fin de las pruebas. En la ventana de 'iniciar.command' se ve cada paso del orquestador."
read -r -p "Presiona Enter para cerrar..."
