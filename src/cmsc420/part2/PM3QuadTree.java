package cmsc420.part2;

public class PM3QuadTree extends PMQuadTree{

	public PM3QuadTree(int spatialHeight, int spatialWidth) {
		super(new PM3Validator(), spatialHeight, spatialWidth);
	}
	
	private static class PM3Validator implements Validator {

		@Override
		public boolean isValid() {
			// TODO Auto-generated method stub
			return true;
		}
		
	}
}
