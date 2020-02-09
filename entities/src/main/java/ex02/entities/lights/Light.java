package ex02.entities.lights;

import ex02.entities.IEntity;

public abstract class Light implements IEntity {	

	private double[] position;
	private double[] color = {1, 1, 1};
	
	public abstract double[] getAmountOfLight(double[] point);

	public abstract double[] getVectorToLight(double[] pointOfIntersection);
	
	public double[] getPosition() {
		return position;
	}	
	
	void setPosition(double[] position) {
		this.position = position;
	} 		
	
	double[] getColor() {
		return color;
	}
	
	public void setColor(double[] color) {
		this.color = color;
	}
}
