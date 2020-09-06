package au.net.immortius.jumpcontinue.output;

import java.util.ArrayList;
import java.util.List;

public class OutputTheme {
    private int id;
    private int variant;
    private String name;
    private List<OutputCard> keycards = new ArrayList<>();
    private OutputImage displayImage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVariant() {
        return variant;
    }

    public void setVariant(int variant) {
        this.variant = variant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<OutputCard> getKeycards() {
        return keycards;
    }

    public void setKeycards(List<OutputCard> keycards) {
        this.keycards = keycards;
    }

    public OutputImage getDisplayImage() {
        return displayImage;
    }

    public void setDisplayImage(OutputImage displayImage) {
        this.displayImage = displayImage;
    }
}
