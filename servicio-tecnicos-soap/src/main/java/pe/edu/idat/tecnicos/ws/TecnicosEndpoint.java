package pe.edu.idat.tecnicos.ws;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Endpoint SOAP del Servicio de Tecnicos.
 * Operaciones: consultarDisponibilidad, asignarTecnico y liberarHorario.
 * Turnos de 2 horas de lunes a sabado; los domingos no hay atencion.
 */
@Endpoint
public class TecnicosEndpoint {

    private static final String NAMESPACE = "http://idat.edu.pe/tecnicos";
    private static final List<String> TURNOS = List.of("08:00", "10:00", "12:00", "14:00", "16:00");
    private static final Set<String> TIPOS = Set.of("INSTALACION", "MANTENIMIENTO", "REPARACION");

    record Tecnico(String id, String nombre, Set<String> especialidades) {}

    private final List<Tecnico> tecnicos = List.of(
            new Tecnico("T01", "Juan Perez Lopez", Set.of("INSTALACION", "REPARACION")),
            new Tecnico("T02", "Maria Torres Vega", Set.of("MANTENIMIENTO", "REPARACION")),
            new Tecnico("T03", "Luis Ramos Castillo", Set.of("INSTALACION", "MANTENIMIENTO")));

    /** Turnos ocupados: "idTecnico|fecha|hora" */
    private final Set<String> ocupados = ConcurrentHashMap.newKeySet();

    @PayloadRoot(namespace = NAMESPACE, localPart = "consultarDisponibilidadRequest")
    @ResponsePayload
    public ConsultarDisponibilidadResponse consultarDisponibilidad(@RequestPayload ConsultarDisponibilidadRequest request) {
        String tipo = tipo(request.getTipoServicio());
        LocalDate fecha = fecha(request.getFecha());

        ConsultarDisponibilidadResponse response = new ConsultarDisponibilidadResponse();
        if (fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
            for (String hora : TURNOS) {
                if (buscarLibre(tipo, fecha.toString(), hora).isPresent()) response.getHorariosLibres().add(hora);
            }
        }
        response.setDisponible(!response.getHorariosLibres().isEmpty());
        System.out.println("[Tecnicos] consultarDisponibilidad(" + tipo + ", " + fecha + ") -> " + response.getHorariosLibres());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "asignarTecnicoRequest")
    @ResponsePayload
    public AsignarTecnicoResponse asignarTecnico(@RequestPayload AsignarTecnicoRequest request) {
        String tipo = tipo(request.getTipoServicio());
        LocalDate fecha = fecha(request.getFecha());
        String hora = request.getHora() == null ? "" : request.getHora().trim();

        AsignarTecnicoResponse response = new AsignarTecnicoResponse();
        response.setIdTecnico("");
        response.setNombreTecnico("");
        synchronized (ocupados) {
            Optional<Tecnico> libre = (TURNOS.contains(hora) && fecha.getDayOfWeek() != DayOfWeek.SUNDAY)
                    ? buscarLibre(tipo, fecha.toString(), hora) : Optional.empty();
            if (libre.isPresent()) {
                Tecnico t = libre.get();
                ocupados.add(clave(t.id(), fecha.toString(), hora));
                response.setExito(true);
                response.setIdTecnico(t.id());
                response.setNombreTecnico(t.nombre());
                response.setMensaje("Tecnico asignado para el " + fecha + " a las " + hora);
            } else {
                response.setExito(false);
                response.setMensaje("No hay tecnico de " + tipo + " disponible el " + fecha + " a las " + hora);
            }
        }
        System.out.println("[Tecnicos] asignarTecnico(" + tipo + ", " + fecha + ", " + hora + ") -> " + response.getMensaje());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "liberarHorarioRequest")
    @ResponsePayload
    public LiberarHorarioResponse liberarHorario(@RequestPayload LiberarHorarioRequest request) {
        boolean ok = ocupados.remove(clave(request.getIdTecnico(), request.getFecha(), request.getHora()));
        LiberarHorarioResponse response = new LiberarHorarioResponse();
        response.setExito(ok);
        response.setMensaje(ok ? "Horario liberado" : "El tecnico no tenia ese horario ocupado");
        System.out.println("[Tecnicos] liberarHorario(" + request.getIdTecnico() + ", " + request.getFecha() + ", "
                + request.getHora() + ") -> " + response.getMensaje());
        return response;
    }

    // ------------------------------------------------------------------ auxiliares

    private Optional<Tecnico> buscarLibre(String tipo, String fecha, String hora) {
        return tecnicos.stream()
                .filter(t -> t.especialidades().contains(tipo))
                .filter(t -> !ocupados.contains(clave(t.id(), fecha, hora)))
                .findFirst();
    }

    private static String clave(String idTecnico, String fecha, String hora) {
        return idTecnico + "|" + fecha + "|" + hora;
    }

    private static String tipo(String tipo) {
        String t = tipo == null ? "" : tipo.trim().toUpperCase();
        if (!TIPOS.contains(t)) throw new DatoInvalidoException("tipoServicio invalido: " + tipo + ". Use " + TIPOS);
        return t;
    }

    private static LocalDate fecha(String fecha) {
        try {
            return LocalDate.parse(fecha == null ? "" : fecha.trim());
        } catch (DateTimeParseException e) {
            throw new DatoInvalidoException("fecha invalida: " + fecha + ". Use el formato yyyy-MM-dd");
        }
    }

    /** Se devuelve al consumidor como SOAP Fault (soap:Client). */
    @SoapFault(faultCode = FaultCode.CLIENT)
    public static class DatoInvalidoException extends RuntimeException {
        public DatoInvalidoException(String msg) { super(msg); }
    }
}
