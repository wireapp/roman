package com.wire.bots.roman.resources;

import com.wire.xenon.assets.ImagePreview;

import java.util.UUID;

public class Picture  {
    private final String mimeType;
    private final byte[] imageData;
    private int width;
    private int height;

    public Picture(byte[] image, String mimeType)
    {
        this.imageData = image;
        this.mimeType = mimeType;
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

    public String getMimeType() {
        return mimeType;
    }
}
