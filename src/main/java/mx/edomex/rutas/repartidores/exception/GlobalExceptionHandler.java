package mx.edomex.rutas.repartidores.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // cuando @valid falla, se activa este método
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarErroresDeValidacion(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();

        // recorriendo todos los errores que encontró Spring boot y moviéndolos a un archivo
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String nombreCampo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(nombreCampo, mensaje);
        });

        // devuelve la lista de errores limpia
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }
}