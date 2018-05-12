package cmsc420.exception;

public class AirportDoesNotExistThrowable extends Throwable {

	public AirportDoesNotExistThrowable() {
	}

	public AirportDoesNotExistThrowable(String message) {
		super(message);
	}
}
