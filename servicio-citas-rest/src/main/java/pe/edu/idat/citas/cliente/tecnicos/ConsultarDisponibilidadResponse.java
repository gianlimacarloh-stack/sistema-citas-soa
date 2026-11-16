package pe.edu.idat.citas.cliente.tecnicos;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "consultarDisponibilidadResponse", namespace = "http://idat.edu.pe/tecnicos")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConsultarDisponibilidadResponse {

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos", required = true)
    private boolean disponible;

    @XmlElement(namespace = "http://idat.edu.pe/tecnicos")
    private List<String> horariosLibres = new ArrayList<>();

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public List<String> getHorariosLibres() { return horariosLibres; }
    public void setHorariosLibres(List<String> horariosLibres) { this.horariosLibres = horariosLibres; }
}
