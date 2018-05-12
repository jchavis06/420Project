package cmsc420.exception;

import cmsc420.geometry.Metropole;

/**
 * Thrown if city is already in the spatial map upon attempted insertion.
 */
public class MetropoleAlreadyMappedException extends Throwable {
	public MetropoleAlreadyMappedException() {
	}

	public MetropoleAlreadyMappedException(String message) {
		super(message);
	}
}