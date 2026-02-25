package com.decisiontree.backend.exception;

import java.util.List;


public class BusinessValidationException extends RuntimeException{

    private final List<FieldError> errores;

    public BusinessValidationException(String mensaje, List<FieldError> errores) {
        super(mensaje);
        this.errores = errores;
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

        public String getCampo() {return campo;}
        public String getMensaje() {return mensaje;}
    }

}
