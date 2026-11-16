package pe.edu.idat.citas.dto;

/**
 * Cuerpo JSON de POST /api/citas.
 * nombre, telefono y direccion solo son necesarios si el cliente aun no esta registrado.
 */
public record CitaRequest(String dni, String tipoServicio, String fecha, String hora, String descripcion,
                          String nombre, String telefono, String direccion) {}
