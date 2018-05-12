package cmsc420.part2;

public class PM1QuadTree extends PMQuadTree {

	public PM1QuadTree(int spatialHeight, int spatialWidth) {
		super(new PM1Validator(), spatialHeight, spatialWidth);
	}
	
	private static class PM1Validator implements Validator {

		@Override
		public boolean isValid() {
			// TODO Auto-generated method stub
			return true;
		}
		
	}
}
