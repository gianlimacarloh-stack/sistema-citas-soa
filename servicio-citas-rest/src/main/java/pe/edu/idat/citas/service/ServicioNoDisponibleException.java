package pe.edu.idat.citas.service;

/** Un servicio SOAP no respondio (esta apagado o fallo). Se traduce a HTTP 503. */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
