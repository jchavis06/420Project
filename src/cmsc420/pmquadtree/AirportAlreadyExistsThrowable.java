package cmsc420.pmquadtree;

public class AirportAlreadyExistsThrowable extends Throwable {
	private static final long serialVersionUID = 1L;
	
	public AirportAlreadyExistsThrowable() {
    }

    public AirportAlreadyExistsThrowable(String msg) {
    	super(msg);
    } 
}
