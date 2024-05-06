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
        return getScaledImage(picture);
    }

    private static Boolean shouldScaleOriginalSize(double width, double height) {
        final double maxPixelCount = 1.3 * (double) ImageProcessor.MEDIUM_DIMENSION * (double) ImageProcessor.MEDIUM_DIMENSION;
        return (width > 1.3 * (double) ImageProcessor.MEDIUM_DIMENSION || height > 1.3 * (double) ImageProcessor.MEDIUM_DIMENSION)
                && width * height > maxPixelCount;
    }

    private static Size getScaledSize(double origWidth, double origHeight) {
        Size ret = new Size();
        double op1 = Math.min((double) ImageProcessor.MEDIUM_DIMENSION / origWidth, (double) ImageProcessor.MEDIUM_DIMENSION / origHeight);
        double op2 = (double) ImageProcessor.MEDIUM_DIMENSION / Math.sqrt(origWidth * origHeight);
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

    private static Picture getScaledImage(Picture image) throws Exception {
        String resultImageType = switch (image.getMimeType()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> throw new IllegalArgumentException("Unsupported mime type");
        };

        int origWidth = image.getWidth();
        int origHeight = image.getHeight();

        BufferedImage resultImage = ImageIO.read(new ByteArrayInputStream(image.getImageData()));

        if (shouldScaleOriginalSize(origWidth, origHeight)) {
            Size scaledSize = getScaledSize(origWidth, origHeight);
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
