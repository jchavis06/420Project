package cmsc420.meeshquest.part2;

public class PM1QuadTree extends PMQuadTree{
	
	public PM1QuadTree() {
		super(new PM1Validator());
	}
	
	private static class PM1Validator implements Validator {

		@Override
		public boolean isValid() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
