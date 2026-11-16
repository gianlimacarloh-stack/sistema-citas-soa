package pe.edu.idat.citas.cliente;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import pe.edu.idat.citas.cliente.clientes.*;

/** Cliente del Servicio SOAP de Clientes. */
@Component
public class ClientesClient {

    private final WebServiceTemplate template;

    public ClientesClient(@Qualifier("clientesTemplate") WebServiceTemplate template) {
        this.template = template;
    }

    public ValidarClienteResponse validarCliente(String dni) {
        ValidarClienteRequest req = new ValidarClienteRequest();
        req.setDni(dni);
        return (ValidarClienteResponse) template.marshalSendAndReceive(req);
    }

    public RegistrarClienteResponse registrarCliente(String dni, String nombre, String telefono, String direccion) {
        RegistrarClienteRequest req = new RegistrarClienteRequest();
        req.setDni(dni);
        req.setNombre(nombre);
        req.setTelefono(telefono == null ? "" : telefono);
        req.setDireccion(direccion);
        return (RegistrarClienteResponse) template.marshalSendAndReceive(req);
    }
}
