package pe.edu.idat.clientes.ws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "registrarClienteRequest", namespace = "http://idat.edu.pe/clientes")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegistrarClienteRequest {

    @XmlElement(namespace = "http://idat.edu.pe/clientes", required = true)
    private String dni;

    @XmlElement(namespace = "http://idat.edu.pe/clientes", required = true)
    private String nombre;

    @XmlElement(namespace = "http://idat.edu.pe/clientes", required = true)
    private String telefono;

    @XmlElement(namespace = "http://idat.edu.pe/clientes", required = true)
    private String direccion;

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
