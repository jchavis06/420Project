package cmsc420.exception;

public class CityAlreadyExistsThrowable extends Throwable {

	public CityAlreadyExistsThrowable(){}
	
	public CityAlreadyExistsThrowable(String message) {
		super(message);
	}
}
