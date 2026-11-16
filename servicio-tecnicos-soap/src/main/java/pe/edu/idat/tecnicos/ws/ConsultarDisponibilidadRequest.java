package pe.edu.idat.tecnicos.ws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "consultarDisponibilidadRequest", namespace = "http://idat.edu.pe/tecnicos")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConsultarDisponibilidadRequest {

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String tipoServicio;

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String fecha;

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
