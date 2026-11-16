package pe.edu.idat.citas.cliente;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import pe.edu.idat.citas.cliente.tecnicos.*;

/** Cliente del Servicio SOAP de Tecnicos. */
@Component
public class TecnicosClient {

    private final WebServiceTemplate template;

    public TecnicosClient(@Qualifier("tecnicosTemplate") WebServiceTemplate template) {
        this.template = template;
    }

    public ConsultarDisponibilidadResponse consultarDisponibilidad(String tipoServicio, String fecha) {
        ConsultarDisponibilidadRequest req = new ConsultarDisponibilidadRequest();
        req.setTipoServicio(tipoServicio);
        req.setFecha(fecha);
        return (ConsultarDisponibilidadResponse) template.marshalSendAndReceive(req);
    }

    public AsignarTecnicoResponse asignarTecnico(String tipoServicio, String fecha, String hora) {
        AsignarTecnicoRequest req = new AsignarTecnicoRequest();
        req.setTipoServicio(tipoServicio);
        req.setFecha(fecha);
        req.setHora(hora);
        return (AsignarTecnicoResponse) template.marshalSendAndReceive(req);
    }

    public LiberarHorarioResponse liberarHorario(String idTecnico, String fecha, String hora) {
        LiberarHorarioRequest req = new LiberarHorarioRequest();
        req.setIdTecnico(idTecnico);
        req.setFecha(fecha);
        req.setHora(hora);
        return (LiberarHorarioResponse) template.marshalSendAndReceive(req);
    }
}
