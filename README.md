# Sistema distribuido de reserva de citas de servicio técnico (SOA): M&A Electric

Proyecto grupal del curso **Aplicaciones con Enfoque Orientado a Servicios** (IDAT).

M&A Electric S.A.C. vende grupos electrógenos, generadores y motores, y brinda servicios de **instalación**,
**mantenimiento preventivo** y **reparación**. Este sistema permite que un cliente reserve una cita de servicio
técnico. Tres servicios independientes colaboran para lograrlo:

| Servicio | Tipo | Puerto | Contrato / URL base | Responsable |
|---|---|---|---|---|
| `servicio-clientes-soap` | SOAP (WSDL/XSD) | 8081 | http://localhost:8081/ws/clientes.wsdl | Alvaro de Jesus Valenzuela Osco |
| `servicio-tecnicos-soap` | SOAP (WSDL/XSD) | 8082 | http://localhost:8082/ws/tecnicos.wsdl | Gian Carlo Lima Huachaca |
| `servicio-citas-rest` | REST (JSON) | 8080 | http://localhost:8080/api | Carlos Alejandro Máximo Diaz Lapa |

El servicio REST de **Citas** recibe la solicitud del cliente y **orquesta** las llamadas a los dos servicios SOAP
siguiendo el diagrama BPMN [`docs/BPMN_Reservar_Cita.png`](docs/BPMN_Reservar_Cita.png).

```
                 ┌─────────────────────────┐   SOAP/XML   ┌──────────────────────────────┐
 Cliente ──JSON──▶  servicio-citas-rest    ├─────────────▶ servicio-clientes-soap  :8081 │
 (Postman)       │  :8080  (orquestador)   │              └──────────────────────────────┘
                 │                         │   SOAP/XML   ┌──────────────────────────────┐
                 │                         ├─────────────▶ servicio-tecnicos-soap  :8082 │
                 └─────────────────────────┘              └──────────────────────────────┘
```

## Requisitos

- **Java 17** o superior: `java -version`
- **Maven**: no hace falta instalarlo; el proyecto incluye `./mvnw` (Maven Wrapper).
- **Postman** y/o **SoapUI** para las pruebas

## Cómo ejecutarlo

### Forma más fácil (Mac): doble clic

Solo necesitas tener **Java 17 o superior**. Si tienes Android Studio, el script usa el Java que trae incluido.

1. Doble clic en **`iniciar.command`**. Compila y levanta los 3 servicios; la primera vez tarda unos minutos porque descarga las dependencias.
   Deja esa ventana abierta: ahí verás cada paso del orquestador.
2. Doble clic en **`probar.command`**. Ejecuta 11 pruebas (reservar, rechazar, cancelar, llamada SOAP directa...) y muestra las respuestas.
3. Para detener todo, presiona **Enter** en la ventana de `iniciar.command`.

> Si macOS dice que no puede abrir el archivo, haz clic derecho sobre él → **Abrir** → **Abrir**. También puedes
> ejecutarlo en Terminal con `bash iniciar.command`.

No hace falta instalar Maven: el proyecto incluye el *Maven Wrapper* (`./mvnw`).

### Opción A: terminal (3 terminales, una por servicio)

```bash
# 1) Clonar y compilar los 3 servicios desde la raíz
git clone https://github.com/gianlimacarloh-stack/sistema-citas-soa.git
cd sistema-citas-soa
./mvnw clean package -DskipTests      # o: mvn clean package -DskipTests

# 2) Terminal 1: Clientes (SOAP)
java -jar servicio-clientes-soap/target/servicio-clientes-soap-1.0.0.jar

# 3) Terminal 2: Técnicos (SOAP)
java -jar servicio-tecnicos-soap/target/servicio-tecnicos-soap-1.0.0.jar

# 4) Terminal 3: Citas (REST)
java -jar servicio-citas-rest/target/servicio-citas-rest-1.0.0.jar
```

También se puede levantar cada servicio con `mvn spring-boot:run` dentro de su carpeta.

### Opción B: IntelliJ IDEA

1. *File → Open* y elegir la carpeta raíz (se reconoce como proyecto Maven).
2. Ejecutar en este orden: `ServicioClientesApplication`, `ServicioTecnicosApplication` y `ServicioCitasApplication`.

> Conviene iniciar primero los dos servicios SOAP. Si Citas no puede comunicarse con alguno, responde
> **HTTP 503** indicando qué servicio no está disponible.

### Verificar que están corriendo

- WSDL de Clientes: http://localhost:8081/ws/clientes.wsdl
- WSDL de Técnicos: http://localhost:8082/ws/tecnicos.wsdl
- REST de Citas: http://localhost:8080/api/servicios

## Operaciones

### SOAP: Clientes (`http://idat.edu.pe/clientes`)

| Operación | Entrada | Salida |
|---|---|---|
| `validarCliente` | dni | existe, nombre, direccion |
| `registrarCliente` | dni, nombre, telefono, direccion | exito, mensaje |

### SOAP: Técnicos (`http://idat.edu.pe/tecnicos`)

| Operación | Entrada | Salida |
|---|---|---|
| `consultarDisponibilidad` | tipoServicio, fecha | disponible, horariosLibres (lista) |
| `asignarTecnico` | tipoServicio, fecha, hora | exito, idTecnico, nombreTecnico, mensaje |
| `liberarHorario` *(nueva)* | idTecnico, fecha, hora | exito, mensaje |

Si se envía un `tipoServicio` o una `fecha` inválidos, el servicio responde con un **SOAP Fault** (`soap:Client`).

### REST: Citas

| Método y ruta | Descripción | Respuesta |
|---|---|---|
| `GET /api/servicios` | Lista los tipos de servicio | 200 |
| `GET /api/servicios/{tipo}/disponibilidad?fecha=` | Horarios libres (vía SOAP) *(nuevo)* | 200 / 400 |
| `POST /api/citas` | Reserva una cita y orquesta Clientes y Técnicos | 201 confirmada / 400 rechazada / 503 |
| `GET /api/citas` | Lista las citas *(nuevo)* | 200 |
| `GET /api/citas/{id}` | Consulta una cita | 200 / 404 |
| `PUT /api/citas/{id}/cancelar` | Cancela la cita y libera el horario del técnico | 200 / 404 / 409 |

## Datos de prueba

- **Tipos de servicio:** `INSTALACION`, `MANTENIMIENTO` y `REPARACION`.
- **Turnos:** 08:00, 10:00, 12:00, 14:00 y 16:00, de lunes a sábado. Los domingos no hay atención. La fecha debe ser futura y tener formato `yyyy-MM-dd`.
- **Clientes ya registrados:** DNI `70123456` (Ana Quispe Rojas) y `45678912` (Jorge Huaman Soto).
- **Técnicos:**
  - T01 Juan Perez: instalación y reparación.
  - T02 Maria Torres: mantenimiento y reparación.
  - T03 Luis Ramos: instalación y mantenimiento.

## Pruebas rápidas

```bash
# Cita de un cliente registrado (201)
curl -X POST http://localhost:8080/api/citas -H "Content-Type: application/json" \
  -d '{"dni":"70123456","tipoServicio":"MANTENIMIENTO","fecha":"2026-11-20","hora":"10:00","descripcion":"Mantenimiento de grupo electrogeno 10 kVA"}'

# Cita de un cliente nuevo: se registra automáticamente (201)
curl -X POST http://localhost:8080/api/citas -H "Content-Type: application/json" \
  -d '{"dni":"71234567","nombre":"Rosa Mendoza","telefono":"999111222","direccion":"Av. Peru 450, SMP","tipoServicio":"REPARACION","fecha":"2026-11-20","hora":"10:00","descripcion":"El generador no arranca"}'

# Cita en domingo: no hay técnico (400)
curl -X POST http://localhost:8080/api/citas -H "Content-Type: application/json" \
  -d '{"dni":"70123456","tipoServicio":"INSTALACION","fecha":"2026-11-22","hora":"10:00"}'

curl "http://localhost:8080/api/servicios/MANTENIMIENTO/disponibilidad?fecha=2026-11-20"
curl http://localhost:8080/api/citas/101
curl -X PUT http://localhost:8080/api/citas/101/cancelar
```

Respuesta de una cita confirmada:

```json
{
  "id": 101,
  "dni": "70123456",
  "nombreCliente": "Ana Quispe Rojas",
  "tipoServicio": "MANTENIMIENTO",
  "fecha": "2026-11-20",
  "hora": "10:00",
  "idTecnico": "T02",
  "nombreTecnico": "Maria Torres Vega",
  "estado": "CONFIRMADA",
  "mensaje": "Cita confirmada con el tecnico Maria Torres Vega"
}
```

- **Postman:** importar `docs/Sistema_Citas_SOA.postman_collection.json`, que trae las peticiones REST y SOAP.
- **SoapUI:** *New SOAP Project* con la URL del WSDL. En `docs/soapui/` hay peticiones XML de ejemplo.

En la consola de Citas se ve la orquestación paso a paso:

```
[Citas][#102] inicio: solicitud de cita del DNI 71234567
[Citas][#102] validarCliente(71234567) -> existe=false
[Citas][#102] registrarCliente(71234567) -> Cliente Rosa Mendoza registrado correctamente
[Citas][#102] consultarDisponibilidad(REPARACION, 2026-11-20) -> [08:00, 10:00, 12:00, 14:00, 16:00]
[Citas][#102] asignarTecnico -> T01 Juan Perez Lopez
[Citas][#102] fin: CONFIRMADA
```

## Estructura

```
├── iniciar.command / probar.command          levantar y probar todo con doble clic (Mac)
├── mvnw, .mvn/                               Maven Wrapper
├── docs/
│   ├── BPMN_Reservar_Cita.png / .svg         BPMN actualizado
│   ├── Sistema_Citas_SOA.postman_collection.json
│   └── soapui/                               peticiones SOAP de ejemplo
├── servicio-clientes-soap/                   Spring Web Services, contrato clientes.xsd
├── servicio-tecnicos-soap/                   Spring Web Services, contrato tecnicos.xsd
├── servicio-citas-rest/                      Spring Web + clientes SOAP (WebServiceTemplate)
└── pom.xml                                   POM agregador (compila los 3 módulos)
```

## Tecnologías

- Java 17 y Spring Boot 3.3
- Spring Web Services: SOAP *contract-first*, del XSD se genera el WSDL
- JAXB
- Spring Web: REST con JSON
- Maven, Postman y SoapUI

**Fuera de alcance:** interfaz gráfica (app móvil o web), pagos en línea, notificaciones y WS-Security. Los datos se guardan
en memoria y se reinician al detener los servicios.
