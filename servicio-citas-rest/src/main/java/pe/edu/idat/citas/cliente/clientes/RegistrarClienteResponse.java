package pe.edu.idat.citas.cliente.clientes;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "registrarClienteResponse", namespace = "http://idat.edu.pe/clientes")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegistrarClienteResponse {

    @XmlElement(namespace = "http://idat.edu.pe/clientes", required = true)
    private boolean exito;

    @XmlElement(namespace = "http://idat.edu.pe/clientes", required = true)
    private String mensaje;

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
