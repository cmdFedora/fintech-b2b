package com.fintech.b2b.domain.model.exception;

/**
 * (Ej. Transferencias a la misma cuenta, operaciones duplicadas).
 */
public class OperacionInvalidaException extends BusinessException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OperacionInvalidaException(String message) {
        super(message);
    }
}