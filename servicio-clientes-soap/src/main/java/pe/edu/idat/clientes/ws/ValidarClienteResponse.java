package pe.edu.idat.clientes.ws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "validarClienteResponse", namespace = "http://idat.edu.pe/clientes")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidarClienteResponse {

    @XmlElement(namespace = "http://idat.edu.pe/clientes", required = true)
    private boolean existe;

    @XmlElement(namespace = "http://idat.edu.pe/clientes", required = true)
    private String nombre;

    @XmlElement(namespace = "http://idat.edu.pe/clientes", required = true)
    private String direccion;

    public boolean isExiste() { return existe; }
    public void setExiste(boolean existe) { this.existe = existe; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
