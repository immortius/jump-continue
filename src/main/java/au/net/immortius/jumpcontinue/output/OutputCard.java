package au.net.immortius.jumpcontinue.output;

public class OutputCard {

    private String name;
    private OutputImage image;

    public OutputCard() {
    }

    public OutputCard(String name, OutputImage image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OutputImage getImage() {
        return image;
    }

    public void setImage(OutputImage image) {
        this.image = image;
    }
}
