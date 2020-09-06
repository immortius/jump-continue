package au.net.immortius.jumpcontinue.input;

import java.util.Collections;
import java.util.List;

public class InputTheme {
    private int id;
    private int variant;
    private String name;
    private InputCard titleCard;
    private List<InputCard> keycards;


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

    public InputCard getTitleCard() {
        return titleCard;
    }

    public void setTitleCard(InputCard titleCard) {
        this.titleCard = titleCard;
    }

    public List<InputCard> getKeycards() {
        if (keycards == null) {
            return Collections.emptyList();
        }
        return keycards;
    }

    public void setKeycards(List<InputCard> keycards) {
        this.keycards = keycards;
    }
}
