package com.fintech.b2b.domain.model.exception;

/**
 * Clase base para todas las excepciones de reglas de negocio del dominio.
*/
public abstract class BusinessException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BusinessException(String message) {
        super(message);
    }
}