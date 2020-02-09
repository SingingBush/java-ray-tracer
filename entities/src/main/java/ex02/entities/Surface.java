package ex02.entities;

import ex02.blas.MathUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class Surface {

    private static final int TYPE_FLAT = 1;
    public static final int TYPE_CHECKERS = 2;
    public static final int TYPE_TEXTURE = 3;

    private int typeId;
    private String type;
    private double[] diffuse = {0.8F, 0.8F, 0.8F};
    private double[] specular = {1.0F, 1.0F, 1.0F};
    private double[] ambient = {0.1F, 0.1F, 0.1F};
    private double[] emission = {0, 0, 0};
    private double shininess = 100.0F;
    private double checkersSize = 0.1F;
    private double[] checkersDiffuse1 = {1.0F, 1.0F, 1.0F};
    private double[] checkersDiffuse2 = {0.1F, 0.1F, 0.1F};
    private double reflectance = 0.0F;
    private String textureFileName;
    private Raster texture;

    // Returns the texture color for a given 2D point in [0, 1] coordinates
    public double[] getTextureColor(double[] point2D) {
        if(texture == null) {
            // fallback to using the diffuse colour if texture missing
            return getDiffuse();
        }

        final int textureWidth = texture.getWidth();
        final int textureHeight = texture.getHeight();

        int textureX = textureWidth > 0 ?
                Math.abs((int) Math.round(point2D[0] * textureWidth)) % textureWidth :
                Math.abs((int) Math.round(point2D[0] * 0));

        int textureY = textureHeight > 0 ?
                Math.abs((int) Math.round(point2D[1] * textureHeight)) % textureHeight :
                Math.abs((int) Math.round(point2D[1] * 0));

        final double[] rgb = this.texture.getPixel(textureX, textureY, new double[]{0, 0, 0});

        rgb[0] = rgb[0] / 255.0f;
        rgb[1] = rgb[1] / 255.0f;
        rgb[2] = rgb[2] / 255.0f;

        return rgb;
    }

    // Returns the checkers color for a given 2D point in [0, 1] coordinates
    public double[] getCheckersColor(double[] point2D) {
        double checkersX = Math.abs(Math.floor(point2D[0] / checkersSize) % 2);
        double checkersY = Math.abs(Math.floor(point2D[1] / checkersSize) % 2);

        if (checkersX == 0 && checkersY == 0) return checkersDiffuse2;
        if (checkersX == 0 && checkersY == 1) return checkersDiffuse1;
        if (checkersX == 1 && checkersY == 0) return checkersDiffuse1;
        if (checkersX == 1 && checkersY == 1) return checkersDiffuse2;

        return null; // should never happen, perhaps better to return [0,0,0]
    }

    public void postInit() {

    }

    // Read parameters into members
    public boolean parseParameter(String name, String[] args) throws Exception {
        boolean parsed = false;

        if ("mtl-type".equals(name)) {
            type = args[0];
            parsed = true;

            if ("flat".equals(args[0])) typeId = TYPE_FLAT;
            if ("checkers".equals(args[0])) typeId = TYPE_CHECKERS;
            if ("texture".equals(args[0])) typeId = TYPE_TEXTURE;
        }

        if ("mtl-diffuse".equals(name)) {
            diffuse = MathUtils.parseVector(args);
            parsed = true;
        }
        if ("mtl-specular".equals(name)) {
            specular = MathUtils.parseVector(args);
            parsed = true;
        }
        if ("mtl-ambient".equals(name)) {
            ambient = MathUtils.parseVector(args);
            parsed = true;
        }
        if ("mtl-emission".equals(name)) {
            emission = MathUtils.parseVector(args);
            parsed = true;
        }
        if ("mtl-shininess".equals(name)) {
            shininess = Double.parseDouble(args[0]);
            parsed = true;
        }
        if ("checkers-size".equals(name)) {
            checkersSize = Double.parseDouble(args[0]);
            parsed = true;
        }
        if ("checkers-diffuse1".equals(name)) {
            checkersDiffuse1 = MathUtils.parseVector(args);
            parsed = true;
        }
        if ("checkers-diffuse2".equals(name)) {
            checkersDiffuse2 = MathUtils.parseVector(args);
            parsed = true;
        }
        if ("texture".equals(name)) {
            textureFileName = args[0];
            parsed = true;

            try {
                final File textureFile = Paths.get(textureFileName).toFile();

                if(textureFile.canRead()) {
                    final BufferedImage image = ImageIO.read(textureFile);
                    this.texture = image.getData();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        if ("reflectance".equals(name)) {
            reflectance = Double.parseDouble(args[0]);
            parsed = true;
        }

        return parsed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[] getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(double[] diffuse) {
        this.diffuse = diffuse;
    }

    public double[] getSpecular() {
        return specular;
    }

    public void setSpecular(double[] specular) {
        this.specular = specular;
    }

    public double[] getAmbient() {
        return ambient;
    }

    public void setAmbient(double[] ambient) {
        this.ambient = ambient;
    }

    public double[] getEmission() {
        return emission;
    }

    public void setEmission(double[] emission) {
        this.emission = emission;
    }

    public double getShininess() {
        return shininess;
    }

    public void setShininess(double shininess) {
        this.shininess = shininess;
    }

    public double getCheckersSize() {
        return checkersSize;
    }

    public void setCheckersSize(double checkersSize) {
        this.checkersSize = checkersSize;
    }

    public double[] getCheckersDiffuse1() {
        return checkersDiffuse1;
    }

    public void setCheckersDiffuse1(double[] checkersDiffuse1) {
        this.checkersDiffuse1 = checkersDiffuse1;
    }

    public double[] getCheckersDiffuse2() {
        return checkersDiffuse2;
    }

    public void setCheckersDiffuse2(double[] checkersDiffuse2) {
        this.checkersDiffuse2 = checkersDiffuse2;
    }

    public double getReflectance() {
        return reflectance;
    }

    public void setReflectance(double reflectance) {
        this.reflectance = reflectance;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Surface{");
        sb.append("typeId=").append(typeId);
        sb.append(", type='").append(type).append('\'');
        sb.append(", diffuse=").append(Arrays.toString(diffuse));
        sb.append(", specular=").append(Arrays.toString(specular));
        sb.append(", ambient=").append(Arrays.toString(ambient));
        sb.append(", emission=").append(Arrays.toString(emission));
        sb.append(", shininess=").append(shininess);
        sb.append(", checkersSize=").append(checkersSize);
        sb.append(", checkersDiffuse1=").append(Arrays.toString(checkersDiffuse1));
        sb.append(", checkersDiffuse2=").append(Arrays.toString(checkersDiffuse2));
        sb.append(", reflectance=").append(reflectance);
        sb.append(", textureFileName='").append(textureFileName).append('\'');
        //sb.append(", texture=").append(texture);
        sb.append('}');
        return sb.toString();
    }
}
