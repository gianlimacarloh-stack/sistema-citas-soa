package pe.edu.idat.citas.service;

import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceIOException;
import pe.edu.idat.citas.cliente.ClientesClient;
import pe.edu.idat.citas.cliente.TecnicosClient;
import pe.edu.idat.citas.cliente.clientes.RegistrarClienteResponse;
import pe.edu.idat.citas.cliente.clientes.ValidarClienteResponse;
import pe.edu.idat.citas.cliente.tecnicos.AsignarTecnicoResponse;
import pe.edu.idat.citas.cliente.tecnicos.ConsultarDisponibilidadResponse;
import pe.edu.idat.citas.dto.CitaRequest;
import pe.edu.idat.citas.model.Cita;
import pe.edu.idat.citas.model.EstadoCita;
import pe.edu.idat.citas.model.TipoServicio;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Orquestador del proceso "Reservar cita de servicio tecnico" (ver docs/BPMN_Reservar_Cita.png).
 * Cada paso corresponde a una tarea o compuerta del diagrama BPMN.
 */
@Service
public class CitaService {

    private static final Map<String, TipoServicio> TIPOS = new LinkedHashMap<>();
    static {
        TIPOS.put("INSTALACION", new TipoServicio("INSTALACION", "Instalacion",
                "Instalacion de grupos electrogenos, generadores y tableros de transferencia"));
        TIPOS.put("MANTENIMIENTO", new TipoServicio("MANTENIMIENTO", "Mantenimiento preventivo",
                "Revision periodica de grupos electrogenos, motores y motobombas"));
        TIPOS.put("REPARACION", new TipoServicio("REPARACION", "Reparacion (mantenimiento correctivo)",
                "Diagnostico y reparacion de fallas en generadores, motores, compresoras e hidrolavadoras"));
    }

    private final ClientesClient clientes;
    private final TecnicosClient tecnicos;
    private final Map<Long, Cita> citas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(100);

    public CitaService(ClientesClient clientes, TecnicosClient tecnicos) {
        this.clientes = clientes;
        this.tecnicos = tecnicos;
    }

    public Collection<TipoServicio> tiposDeServicio() {
        return TIPOS.values();
    }

    /** Evento de inicio: el cliente solicita una cita (POST /api/citas). */
    public Cita reservar(CitaRequest req) {
        Cita cita = new Cita();
        cita.setId(secuencia.incrementAndGet());
        cita.setDni(req.dni());
        cita.setTipoServicio(req.tipoServicio() == null ? null : req.tipoServicio().trim().toUpperCase());
        cita.setFecha(req.fecha());
        cita.setHora(req.hora());
        cita.setDescripcion(req.descripcion());
        citas.put(cita.getId(), cita);
        log(cita, "inicio: solicitud de cita del DNI " + req.dni());

        // Validacion de datos de entrada (antes de llamar a los servicios SOAP)
        String error = validar(req, cita.getTipoServicio());
        if (error != null) return rechazar(cita, error);

        try {
            // Tarea de servicio (SOAP Clientes): Validar cliente  +  Compuerta: ¿Cliente registrado?
            ValidarClienteResponse validacion = clientes.validarCliente(req.dni());
            log(cita, "validarCliente(" + req.dni() + ") -> existe=" + validacion.isExiste());
            if (validacion.isExiste()) {
                cita.setNombreCliente(validacion.getNombre());
                cita.setDireccion(validacion.getDireccion());
            } else {
                // Tarea de servicio (SOAP Clientes): Registrar cliente
                if (vacio(req.nombre()) || vacio(req.direccion())) {
                    return rechazar(cita, "Cliente no registrado: envie nombre, telefono y direccion para registrarlo");
                }
                RegistrarClienteResponse registro = clientes.registrarCliente(req.dni(), req.nombre(), req.telefono(), req.direccion());
                log(cita, "registrarCliente(" + req.dni() + ") -> " + registro.getMensaje());
                if (!registro.isExito()) return rechazar(cita, registro.getMensaje());
                cita.setNombreCliente(req.nombre());
                cita.setDireccion(req.direccion());
            }
        } catch (WebServiceIOException e) {
            rechazar(cita, "Servicio de Clientes no disponible");
            throw new ServicioNoDisponibleException("Servicio de Clientes no disponible", e);
        }

        try {
            // Tarea de servicio (SOAP Tecnicos): Consultar disponibilidad  +  Compuerta: ¿Hay tecnico disponible?
            ConsultarDisponibilidadResponse disp = tecnicos.consultarDisponibilidad(cita.getTipoServicio(), cita.getFecha());
            log(cita, "consultarDisponibilidad(" + cita.getTipoServicio() + ", " + cita.getFecha() + ") -> " + disp.getHorariosLibres());
            if (!disp.getHorariosLibres().contains(cita.getHora())) {
                String libres = disp.getHorariosLibres().isEmpty() ? "ninguno" : String.join(", ", disp.getHorariosLibres());
                return rechazar(cita, "No hay tecnico disponible el " + cita.getFecha() + " a las " + cita.getHora()
                        + ". Horarios libres ese dia: " + libres);
            }

            // Tarea de servicio (SOAP Tecnicos): Asignar tecnico
            AsignarTecnicoResponse asignacion = tecnicos.asignarTecnico(cita.getTipoServicio(), cita.getFecha(), cita.getHora());
            log(cita, "asignarTecnico -> " + asignacion.getIdTecnico() + " " + asignacion.getNombreTecnico());
            if (!asignacion.isExito()) return rechazar(cita, asignacion.getMensaje());

            // Tarea: Confirmar cita  ->  Evento de fin "Cita confirmada"
            cita.setIdTecnico(asignacion.getIdTecnico());
            cita.setNombreTecnico(asignacion.getNombreTecnico());
            cita.setEstado(EstadoCita.CONFIRMADA);
            cita.setMensaje("Cita confirmada con el tecnico " + asignacion.getNombreTecnico());
            log(cita, "fin: CONFIRMADA");
            return cita;
        } catch (WebServiceIOException e) {
            rechazar(cita, "Servicio de Tecnicos no disponible");
            throw new ServicioNoDisponibleException("Servicio de Tecnicos no disponible", e);
        }
    }

    public Optional<Cita> buscar(long id) {
        return Optional.ofNullable(citas.get(id));
    }

    public Collection<Cita> listar() {
        return citas.values();
    }

    /** Horarios libres para un tipo de servicio y una fecha (consulta directa al Servicio SOAP de Tecnicos). */
    public List<String> disponibilidad(String tipo, String fecha) {
        String t = tipo == null ? "" : tipo.trim().toUpperCase();
        if (!TIPOS.containsKey(t)) throw new IllegalArgumentException("tipoServicio invalido. Use " + TIPOS.keySet());
        if (!fechaValida(fecha)) throw new IllegalArgumentException("fecha invalida. Use el formato yyyy-MM-dd");
        try {
            return tecnicos.consultarDisponibilidad(t, fecha).getHorariosLibres();
        } catch (WebServiceIOException e) {
            throw new ServicioNoDisponibleException("Servicio de Tecnicos no disponible", e);
        }
    }

    /** Cancelacion: solo una cita CONFIRMADA se puede cancelar; se libera el horario del tecnico. */
    public Cita cancelar(Cita cita) {
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new IllegalStateException("Solo se puede cancelar una cita CONFIRMADA (estado actual: " + cita.getEstado() + ")");
        }
        try {
            tecnicos.liberarHorario(cita.getIdTecnico(), cita.getFecha(), cita.getHora());
        } catch (WebServiceIOException e) {
            throw new ServicioNoDisponibleException("Servicio de Tecnicos no disponible", e);
        }
        cita.setEstado(EstadoCita.CANCELADA);
        cita.setMensaje("Cita cancelada; el horario del tecnico quedo libre");
        log(cita, "liberarHorario(" + cita.getIdTecnico() + ") -> fin: CANCELADA");
        return cita;
    }

    // ---------------------------------------------------------------- pasos internos

    private String validar(CitaRequest req, String tipo) {
        if (vacio(req.dni()) || !req.dni().trim().matches("\\d{8}")) return "dni es obligatorio y debe tener 8 digitos";
        if (tipo == null || !TIPOS.containsKey(tipo)) return "tipoServicio invalido. Use " + TIPOS.keySet();
        if (!fechaValida(req.fecha())) return "fecha invalida. Use el formato yyyy-MM-dd";
        if (LocalDate.parse(req.fecha()).isBefore(LocalDate.now())) return "La fecha no puede ser pasada";
        if (vacio(req.hora()) || !req.hora().matches("\\d{2}:\\d{2}")) return "hora invalida. Use el formato HH:mm (ej. 10:00)";
        return null;
    }

    /** Tarea: Rechazar cita -> Evento de fin "Cita rechazada". */
    private Cita rechazar(Cita cita, String motivo) {
        cita.setEstado(EstadoCita.RECHAZADA);
        cita.setMensaje(motivo);
        log(cita, "fin: RECHAZADA - " + motivo);
        return cita;
    }

    private static boolean fechaValida(String fecha) {
        try { LocalDate.parse(fecha); return true; } catch (Exception e) { return false; }
    }

    private static boolean vacio(String s) { return s == null || s.isBlank(); }

    private void log(Cita cita, String texto) {
        System.out.println("[Citas][#" + cita.getId() + "] " + texto);
    }
}
