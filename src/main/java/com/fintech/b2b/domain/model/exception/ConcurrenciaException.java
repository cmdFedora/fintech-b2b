package com.fintech.b2b.domain.model.exception;

public class ConcurrenciaException extends BusinessException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConcurrenciaException(String message) {
        super(message);
    }
}