package au.net.immortius.jumpcontinue.image;

import au.net.immortius.jumpcontinue.scryfall.ImageDictionary;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class AtlasBuilder {

    private static final int ICON_HEIGHT = 320;
    private static final int ICON_WIDTH = 240;
    private static final int ATLAS_WIDTH = 17;
    private static final int ATLAS_HEIGHT = 13;

    private Table<String, Integer, ImageInfo> imageTable;
    private ImageDictionary imageDictionary;
    private BufferedImage atlas;
    private Graphics atlasGraphics;
    private int nextX = 0;
    private int nextY = 0;

    public AtlasBuilder(ImageDictionary imageDictionary) {
        this.imageDictionary = imageDictionary;
        this.imageTable = HashBasedTable.create();

        atlas = new BufferedImage(ICON_WIDTH * ATLAS_WIDTH, ICON_HEIGHT * ATLAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
        atlasGraphics = atlas.createGraphics();
        atlasGraphics.setColor(Color.BLACK);
        atlasGraphics.fillRect(0,0,ICON_WIDTH * ATLAS_WIDTH, ICON_HEIGHT * ATLAS_HEIGHT);
    }

    public ImageInfo getImage(String set, int id) throws IOException {
        ImageInfo imageInfo = imageTable.get(set, id);
        if (imageInfo == null) {
            Path image = imageDictionary.getImage(set, id);
            imageInfo = new ImageInfo(nextX * ICON_WIDTH, nextY * ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
            BufferedImage icon = ImageIO.read(image.toFile());
            atlasGraphics.drawImage(icon, imageInfo.getxOffset(), imageInfo.getyOffset(), imageInfo.getxOffset() + imageInfo.getWidth(), imageInfo.getyOffset() + imageInfo.getHeight(), 0, 0, icon.getWidth(), icon.getHeight(), null);
            imageTable.put(set, id, imageInfo);

            nextX++;
            if (nextX == ATLAS_WIDTH) {
                nextX = 0;
                nextY++;
                if (nextY == ATLAS_HEIGHT) {
                    System.out.println("Atlas Full");
                }
            }
        }
        return imageInfo;
    }

    public void build(Path atlasPath) throws IOException {
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(0.7f);

        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        try (FileImageOutputStream out = new FileImageOutputStream(atlasPath.toFile())) {
            writer.setOutput(out);
            writer.write(null, new IIOImage(atlas, null, null), jpegParams);
        }

        atlasGraphics.dispose();
    }



}
