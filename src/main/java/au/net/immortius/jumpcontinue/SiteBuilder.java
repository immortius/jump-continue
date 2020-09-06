package au.net.immortius.jumpcontinue;

import au.net.immortius.jumpcontinue.image.AtlasBuilder;
import au.net.immortius.jumpcontinue.image.ImageInfo;
import au.net.immortius.jumpcontinue.input.InputCard;
import au.net.immortius.jumpcontinue.input.InputData;
import au.net.immortius.jumpcontinue.input.InputTheme;
import au.net.immortius.jumpcontinue.output.OutputCard;
import au.net.immortius.jumpcontinue.output.OutputData;
import au.net.immortius.jumpcontinue.output.OutputImage;
import au.net.immortius.jumpcontinue.output.OutputTheme;
import au.net.immortius.jumpcontinue.scryfall.CardDictionary;
import au.net.immortius.jumpcontinue.scryfall.ImageDictionary;
import au.net.immortius.jumpcontinue.scryfall.entity.CardObject;
import au.net.immortius.jumpcontinue.util.GsonJsonProvider;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SiteBuilder {

    public static void main(String... args) throws Exception {
        new SiteBuilder().build();
    }

    private void build() throws IOException {
        Gson gson = new GsonFireBuilder().createGson();
        Client client = ClientBuilder.newClient();
        client.register(GsonJsonProvider.class);

        CardDictionary cardDictionary = new CardDictionary(client, gson);
        ImageDictionary imageDictionary = new ImageDictionary(client, cardDictionary);
        AtlasBuilder atlasBuilder = new AtlasBuilder(imageDictionary);

        try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/content.json"))) {
            InputData inputData = gson.fromJson(reader, InputData.class);

            OutputData outputData = new OutputData();
            for (InputTheme inputTheme : inputData.getThemes()) {

                try {
                    OutputTheme theme = new OutputTheme();
                    theme.setId(inputTheme.getId());
                    theme.setName(inputTheme.getName());
                    theme.setVariant(inputTheme.getVariant());

                    if (inputTheme.getTitleCard() == null) {
                        System.out.println("Missing card for " + inputTheme.getName());
                    } else {
                        ImageInfo image = atlasBuilder.getImage(inputTheme.getTitleCard().getSet(), inputTheme.getTitleCard().getId());
                        theme.setDisplayImage(new OutputImage(image.getxOffset(), image.getyOffset(), image.getWidth(), image.getHeight()));
                    }

                    for (InputCard card : inputTheme.getKeycards()) {
                        CardObject cardInfo = cardDictionary.get(card.getSet(), card.getId());
                        ImageInfo image = atlasBuilder.getImage(card.getSet(), card.getId());
                        theme.getKeycards().add(new OutputCard(cardInfo.getName(), new OutputImage(image.getxOffset(), image.getyOffset(), image.getWidth(), image.getHeight())));

                    }

                    outputData.getThemes().add(theme);
                } catch (IOException | RuntimeException e) {
                    throw new RuntimeException("Failed processing theme: " + inputTheme.getName() + ", " + inputTheme.getVariant());
                }
            }

            try (Writer writer = Files.newBufferedWriter(Paths.get("site", "data", "content.json"))) {
                gson.toJson(outputData, writer);
            }
        }
        atlasBuilder.build(Paths.get("site", "img", "atlas.jpg"));
    }

}
