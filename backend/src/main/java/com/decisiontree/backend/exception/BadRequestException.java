package com.decisiontree.backend.exception;

import java.util.Collections;
import java.util.List;

public class BadRequestException extends RuntimeException {

    private final List<FieldError> errores;

    public BadRequestException(String message) {
        super(message);
        this.errores = Collections.emptyList();
    }

    public BadRequestException(String message, List<FieldError> errores) {
        super(message);
        this.errores = (errores == null) ? Collections.emptyList() : errores;
    }

    public List<FieldError> getErrores() {
        return errores;
    }

    public static class FieldError {
        private final String campo;
        private final String mensaje;

        public FieldError(String campo, String mensaje) {
            this.campo = campo;
            this.mensaje = mensaje;
        }

        public String getCampo() { return campo; }
        public String getMensaje() { return mensaje; }
    }
}