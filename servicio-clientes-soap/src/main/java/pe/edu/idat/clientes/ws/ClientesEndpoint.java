package pe.edu.idat.clientes.ws;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Endpoint SOAP del Servicio de Clientes.
 * Operaciones: validarCliente y registrarCliente. Los clientes se guardan en memoria.
 */
@Endpoint
public class ClientesEndpoint {

    private static final String NAMESPACE = "http://idat.edu.pe/clientes";

    record Cliente(String dni, String nombre, String telefono, String direccion) {}

    // Clientes de prueba ya registrados
    private final Map<String, Cliente> clientes = new ConcurrentHashMap<>(Map.of(
            "70123456", new Cliente("70123456", "Ana Quispe Rojas", "987654321", "Av. Arequipa 1520, Lince"),
            "45678912", new Cliente("45678912", "Jorge Huaman Soto", "912345678", "Jr. Los Pinos 340, San Juan de Lurigancho")));

    @PayloadRoot(namespace = NAMESPACE, localPart = "validarClienteRequest")
    @ResponsePayload
    public ValidarClienteResponse validarCliente(@RequestPayload ValidarClienteRequest request) {
        Cliente c = clientes.get(limpiar(request.getDni()));
        ValidarClienteResponse response = new ValidarClienteResponse();
        response.setExiste(c != null);
        response.setNombre(c != null ? c.nombre() : "");
        response.setDireccion(c != null ? c.direccion() : "");
        System.out.println("[Clientes] validarCliente(" + request.getDni() + ") -> existe=" + response.isExiste());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "registrarClienteRequest")
    @ResponsePayload
    public RegistrarClienteResponse registrarCliente(@RequestPayload RegistrarClienteRequest request) {
        RegistrarClienteResponse response = new RegistrarClienteResponse();
        String dni = limpiar(request.getDni());

        if (!dni.matches("\\d{8}")) {
            response.setExito(false);
            response.setMensaje("El DNI debe tener 8 digitos");
        } else if (vacio(request.getNombre()) || vacio(request.getDireccion())) {
            response.setExito(false);
            response.setMensaje("Nombre y direccion son obligatorios");
        } else if (clientes.putIfAbsent(dni, new Cliente(dni, request.getNombre().trim(),
                request.getTelefono(), request.getDireccion().trim())) != null) {
            response.setExito(false);
            response.setMensaje("El cliente con DNI " + dni + " ya esta registrado");
        } else {
            response.setExito(true);
            response.setMensaje("Cliente " + request.getNombre().trim() + " registrado correctamente");
        }
        System.out.println("[Clientes] registrarCliente(" + dni + ") -> " + response.getMensaje());
        return response;
    }

    private static String limpiar(String s) { return s == null ? "" : s.trim(); }
    private static boolean vacio(String s) { return s == null || s.isBlank(); }
}
