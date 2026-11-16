package pe.edu.idat.citas.controller;

import org.springframework.web.bind.annotation.*;
import pe.edu.idat.citas.model.TipoServicio;
import pe.edu.idat.citas.service.CitaService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final CitaService service;

    public ServicioController(CitaService service) {
        this.service = service;
    }

    /** GET /api/servicios -> 200 con los tipos de servicio tecnico. */
    @GetMapping
    public Collection<TipoServicio> listar() {
        return service.tiposDeServicio();
    }

    /** GET /api/servicios/{tipo}/disponibilidad?fecha=2026-11-20 -> horarios libres (via SOAP Tecnicos). */
    @GetMapping("/{tipo}/disponibilidad")
    public Map<String, Object> disponibilidad(@PathVariable String tipo, @RequestParam String fecha) {
        List<String> libres = service.disponibilidad(tipo, fecha);
        return Map.of("tipoServicio", tipo.toUpperCase(), "fecha", fecha, "disponible", !libres.isEmpty(), "horariosLibres", libres);
    }
}
