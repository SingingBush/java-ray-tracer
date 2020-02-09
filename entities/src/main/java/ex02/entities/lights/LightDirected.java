package ex02.entities.lights;

import java.util.List;

import ex02.blas.MathUtils;
import ex02.entities.IEntity;

public class LightDirected extends Light {

	private double[] position;
	private double[] direction;
	private double[] oppositeDirection;
	
	public LightDirected() {
		super();				
	}

	@Override
	public void setParameter(String name, String[] args) throws Exception {
		if ("color".equals(name)) {
            super.setColor(MathUtils.parseVector(args));
            return;
        }

		if ("direction".equals(name)) {
			direction = MathUtils.parseVector(args);
			MathUtils.normalize(direction);
			
			oppositeDirection = MathUtils.oppositeVector(direction);
		}
	}

	@Override
	public double[] getAmountOfLight(double[] point) {					
		return super.getColor(); // constant light, regardless of distance to target
	}
	
	@Override	
	public double[] getPosition() {		 		
		return position;
	}

	@Override
	public void postInit(List<IEntity> entities) throws Exception {
		position = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };		
	}

	@Override
	public double[] getVectorToLight(double[] pointOfIntersection) {
		return oppositeDirection;
	}

}
