package pe.edu.idat.citas.cliente.tecnicos;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "asignarTecnicoRequest", namespace = "http://idat.edu.pe/tecnicos")
@XmlAccessorType(XmlAccessType.FIELD)
public class AsignarTecnicoRequest {

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String tipoServicio;

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String fecha;

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String hora;

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
}
