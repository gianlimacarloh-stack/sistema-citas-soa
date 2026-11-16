package pe.edu.idat.citas.cliente.tecnicos;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "liberarHorarioResponse", namespace = "http://idat.edu.pe/tecnicos")
@XmlAccessorType(XmlAccessType.FIELD)
public class LiberarHorarioResponse {

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private boolean exito;

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String mensaje;

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
