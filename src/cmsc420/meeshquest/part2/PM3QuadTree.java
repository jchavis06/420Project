package cmsc420.meeshquest.part2;

public class PM3QuadTree extends PMQuadTree{

	public PM3QuadTree() {
		super(new PM3Validator());
		// TODO Auto-generated constructor stub
	}
	
	private static class PM3Validator implements Validator {

		@Override
		public boolean isValid() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
