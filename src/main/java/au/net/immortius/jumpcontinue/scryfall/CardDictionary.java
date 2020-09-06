package au.net.immortius.jumpcontinue.scryfall;

import au.net.immortius.jumpcontinue.scryfall.entity.CardObject;
import au.net.immortius.jumpcontinue.util.REST;
import com.google.gson.Gson;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CardDictionary {

    public static final Path CARD_HOME = Paths.get("cache", "cards");
    private static final String SCRYFALL_CARD_URL = "https://api.scryfall.com/cards/";

    private Client client;
    private Gson gson;

    public CardDictionary(Client client, Gson gson) {
        this.client = client;
        this.gson = gson;
    }

    public CardObject get(String set, int id) throws IOException {
        Path cardPath = CARD_HOME.resolve(set).resolve(id + ".json");
        if (!Files.exists(cardPath)) {
            if (!Files.exists(cardPath.getParent())) {
                Files.createDirectories(cardPath.getParent());
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String url = SCRYFALL_CARD_URL + set + "/" + id;
            System.out.println("Downloading " + url + "...");
            if (!REST.download(client, url, cardPath)) {
                throw new RuntimeException("Failed to download: " + url);
            }
        }
        try (Reader reader = Files.newBufferedReader(cardPath)) {
            return gson.fromJson(reader, CardObject.class);
        }
    }
}
