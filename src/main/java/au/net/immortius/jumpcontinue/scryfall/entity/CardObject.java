package au.net.immortius.jumpcontinue.scryfall.entity;

public class CardObject {
    private String object;
    private String name;
    private boolean highres_image;
    private ImageLocations image_uris;

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHighres_image() {
        return highres_image;
    }

    public void setHighres_image(boolean highres_image) {
        this.highres_image = highres_image;
    }

    public ImageLocations getImages() {
        return image_uris;
    }

    public void setImages(ImageLocations images) {
        this.image_uris = images;
    }
}
