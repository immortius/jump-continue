package au.net.immortius.jumpcontinue.scryfall;

import au.net.immortius.jumpcontinue.scryfall.entity.CardObject;
import au.net.immortius.jumpcontinue.util.REST;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageDictionary {

    public static final Path IMAGE_HOME = Paths.get("cache", "images");

    private Client client;
    private CardDictionary cardDictionary;

    public ImageDictionary(Client client, CardDictionary cardDictionary) {
        this.client = client;
        this.cardDictionary = cardDictionary;
    }

    public Path getImage(String set, int id) throws IOException {
        Path imagePath = IMAGE_HOME.resolve(set).resolve(id + ".png");
        if (!Files.exists(imagePath)) {
            if (!Files.exists(imagePath.getParent())) {
                Files.createDirectories(imagePath.getParent());
            }

            CardObject card = cardDictionary.get(set, id);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!REST.download(client, card.getImages().getSmall(), imagePath)) {
                throw new RuntimeException("Failed to download " + card.getImages().getSmall());
            }
        }
        return imagePath;
    }
}
