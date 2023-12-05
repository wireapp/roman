package com.wire.bots.roman.resources;

import com.wire.xenon.assets.ImagePreview;

import java.util.UUID;

public class Picture extends ImagePreview {

    private boolean aPublic;
    private final byte[] imageData;
    private int width;
    private int height;
    private String retention;

    public Picture(byte[] image, String mimeType)
    {
        super(UUID.randomUUID(), mimeType);
        this.imageData = image;
    }

    public void setPublic(boolean aPublic) {
        this.aPublic = aPublic;
    }

    public boolean isaPublic() {
        return aPublic;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
