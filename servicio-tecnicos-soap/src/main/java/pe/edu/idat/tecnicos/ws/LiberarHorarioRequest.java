package pe.edu.idat.tecnicos.ws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "liberarHorarioRequest", namespace = "http://idat.edu.pe/tecnicos")
@XmlAccessorType(XmlAccessType.FIELD)
public class LiberarHorarioRequest {

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String idTecnico;

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String fecha;

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String hora;

    public String getIdTecnico() { return idTecnico; }
    public void setIdTecnico(String idTecnico) { this.idTecnico = idTecnico; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
}
