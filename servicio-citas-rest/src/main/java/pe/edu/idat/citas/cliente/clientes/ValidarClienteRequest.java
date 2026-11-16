package pe.edu.idat.citas.cliente.clientes;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "validarClienteRequest", namespace = "http://idat.edu.pe/clientes")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidarClienteRequest {

    @XmlElement(namespace = "http://idat.edu.pe/clientes", required = true)
    private String dni;

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
}
