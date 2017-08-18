package ex02.entities.lights;

import java.util.List;

import ex02.blas.MathUtils;
import ex02.entities.IEntity;

public class LightPoint extends Light {

    private double[] attenuation = {1, 0, 0};
    private double[] color = {1, 1, 1};
	
	public LightPoint() {
	}
	
	// This constructor is called by LightArea
	public LightPoint(double[] pos, double[] attenuation, double[] color) {
		this.setPosition(pos);
		this.attenuation = attenuation;
		this.color = color;
	}

	@Override
	public void setParameter(String name, String[] args) throws Exception {
		if ("pos".equals(name)) setPosition(MathUtils.parseVector(args));
		if ("attenuation".equals(name)) attenuation = MathUtils.parseVector(args);
		if ("color".equals(name)) color = MathUtils.parseVector(args);
	}

	@Override
	public double[] getAmountOfLight(double[] point) {
		double d = MathUtils.norm(MathUtils.calcPointsDiff(getPosition(), point));
		
		double totalAttenuation = 1 / (attenuation[2] * d * d + attenuation[1] * d + attenuation[0]);
		
		double[] result = { color[0] * totalAttenuation, color[1] * totalAttenuation, color[2] * totalAttenuation };
		
		return result;
	}

	@Override
	public void postInit(List<IEntity> entities) throws Exception {
	}

	@Override
	public double[] getVectorToLight(double[] pointOfIntersection) throws Exception {
		double[] vec = MathUtils.calcPointsDiff(pointOfIntersection, getPosition());
		MathUtils.normalize(vec);
		return vec;
	}
	
}
