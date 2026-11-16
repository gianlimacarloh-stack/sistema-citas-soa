package pe.edu.idat.tecnicos.ws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "asignarTecnicoResponse", namespace = "http://idat.edu.pe/tecnicos")
@XmlAccessorType(XmlAccessType.FIELD)
public class AsignarTecnicoResponse {

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private boolean exito;

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String idTecnico;

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String nombreTecnico;

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private String mensaje;

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
    public String getIdTecnico() { return idTecnico; }
    public void setIdTecnico(String idTecnico) { this.idTecnico = idTecnico; }
    public String getNombreTecnico() { return nombreTecnico; }
    public void setNombreTecnico(String nombreTecnico) { this.nombreTecnico = nombreTecnico; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
