package ex02;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class Utils {
	
	// Converts an array of double in [0, 1] to SWT Color
	public static Color floatArrayToColor(double[] color) {
		int r = Math.min(255, (int)Math.round(color[0] * 255));
		int g = Math.min(255, (int)Math.round(color[1] * 255));
		int b = Math.min(255, (int)Math.round(color[2] * 255));
						
		return new Color(Display.getDefault(), r, g, b);
	}
	
	// Converts an array of double in [0, 1] to an Integer representing a color
	public static int floatArrayToColorInt(double[] color) {
		int colorInt = Math.min(255, (int)Math.round(color[0] * 255)) << 16 & 0xFF0000 |
					   Math.min(255, (int)Math.round(color[1] * 255)) << 8 & 0xFF00 |
					   Math.min(255, (int)Math.round(color[2] * 255));
						
		return colorInt;
	}
	
	// Loads a texture into a matrix (3rd dimension values are in [0, 1])
	public static double[][][] loadTexture(String textureFileName) {				
		ImageLoader imageLoader = new ImageLoader();
		ImageData[] imageDataArr = imageLoader.load(RayTracer.workingDirectory + textureFileName);
		ImageData imageData = imageDataArr[0];		
		
		int textureWidth = imageData.width;
		int textureHeight = imageData.height;
		
		double[][][] texture = new double[textureHeight][textureWidth][3];
		
		for (int i = 0; i < textureHeight; i++) {
			for (int j = 0; j < textureWidth; j++) {
				int pixel = imageData.getPixel(j, i);
				RGB rgb = imageData.palette.getRGB(pixel);
				
				texture[i][j][0] = rgb.red / 255F;
				texture[i][j][1] = rgb.green / 255F;
				texture[i][j][2] = rgb.blue / 255F;
			}
		}
		
		return texture;						
	}
}
