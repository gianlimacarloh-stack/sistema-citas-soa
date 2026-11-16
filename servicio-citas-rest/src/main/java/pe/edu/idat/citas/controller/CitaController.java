package pe.edu.idat.citas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.idat.citas.dto.CitaRequest;
import pe.edu.idat.citas.model.Cita;
import pe.edu.idat.citas.model.EstadoCita;
import pe.edu.idat.citas.service.CitaService;

import java.util.Collection;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService service;

    public CitaController(CitaService service) {
        this.service = service;
    }

    /** POST /api/citas -> 201 si se confirma, 400 si se rechaza (datos invalidos o sin tecnico disponible). */
    @PostMapping
    public ResponseEntity<Cita> reservar(@RequestBody CitaRequest request) {
        Cita cita = service.reservar(request);
        HttpStatus status = cita.getEstado() == EstadoCita.CONFIRMADA ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(cita);
    }

    /** GET /api/citas -> 200 con todas las citas. */
    @GetMapping
    public Collection<Cita> listar() {
        return service.listar();
    }

    /** GET /api/citas/{id} -> 200 o 404. */
    @GetMapping("/{id}")
    public Cita obtener(@PathVariable long id) {
        return buscar(id);
    }

    /** PUT /api/citas/{id}/cancelar -> 200 (CANCELADA), 404 o 409 si no se puede cancelar. */
    @PutMapping("/{id}/cancelar")
    public Cita cancelar(@PathVariable long id) {
        return service.cancelar(buscar(id));
    }

    private Cita buscar(long id) {
        return service.buscar(id).orElseThrow(() -> new NoSuchElementException("Cita " + id + " no encontrada"));
    }
}
