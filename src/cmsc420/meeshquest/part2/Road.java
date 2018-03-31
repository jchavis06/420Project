package cmsc420.meeshquest.part2;

public class Road {

	private City cityA, cityB;
	private float distance;
	//private float angle;
	
	public Road(City cityA, City cityB) {
		this.cityA = cityA;
		this.cityB = cityB;
	}
	
	/**
	 * Not sure if I will need this method, but it will return true if 
	 * the parameter matches one of the two cities that are endpoints of this road.
	 * (Maybe in the future we will need to add if it intersects roads that are not endpoints.
	 */
	public boolean intersects(City city) {
		return (city.equals(this.cityA) || city.equals(this.cityB));
	}
	
	public float getDistance() {
		return this.distance;
	}
}
