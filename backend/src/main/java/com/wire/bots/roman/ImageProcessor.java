package com.wire.bots.roman;

import com.wire.bots.roman.resources.Picture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ImageProcessor {

    private static final int MEDIUM_DIMENSION = 2896;
    public static Picture getMediumImage(Picture picture) throws Exception {
        return getScaledImage(picture, MEDIUM_DIMENSION);
    }

    private static Boolean shouldScaleOriginalSize(double width, double height, double dimension) {
        final double maxPixelCount = 1.3 * dimension * dimension;
        return (width > 1.3 * dimension || height > 1.3 * dimension)
                && width * height > maxPixelCount;
    }

    private static Size getScaledSize(double origWidth, double origHeight, double dimension) {
        Size ret = new Size();
        double op1 = Math.min(dimension / origWidth, dimension / origHeight);
        double op2 = dimension / Math.sqrt(origWidth * origHeight);
        double scale = Math.max(op1, op2);
        double width = Math.ceil(scale * origWidth);
        ret.width = width;
        ret.height = width / origWidth * origHeight;
        return ret;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage,
                                             int origWidth, int origHeight) {
        final int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
                : originalImage.getType();
        BufferedImage resizedImage = new BufferedImage(origWidth, origHeight,
                type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, origWidth, origHeight, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        return resizedImage;
    }

    private static Picture getScaledImage(Picture image, int dimension) throws Exception {
        String resultImageType;
        switch (image.getMimeType()) {
            case "image/jpeg":
                resultImageType = "jpg";
                break;
            case "image/png":
                resultImageType = "png";
                break;
            default:
                throw new Exception("Unsupported mime type: " + image.getMimeType());
        }

        int origWidth = image.getWidth();
        int origHeight = image.getHeight();

        BufferedImage resultImage = ImageIO.read(new ByteArrayInputStream(image.getImageData()));

        if (shouldScaleOriginalSize(origWidth, origHeight, dimension)) {
            Size scaledSize = getScaledSize(origWidth, origHeight, dimension);
            resultImage = resizeImage(resultImage, (int) scaledSize.width,
                    (int) scaledSize.height);
        }

        try (ByteArrayOutputStream resultStream = new ByteArrayOutputStream()) {
            ImageIO.write(resultImage, resultImageType, resultStream);
            resultStream.flush();
            return new Picture(resultStream.toByteArray(), image.getMimeType());
        }
    }

    private static class Size {
        double width;
        double height;
    }
}
