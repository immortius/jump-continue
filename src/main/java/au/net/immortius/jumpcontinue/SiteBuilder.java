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
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiteBuilder {

    private static final Pattern CARD_PATTERN = Pattern.compile("(\\d+) (.*) \\((\\S+)\\) (\\d+)");

    public static void main(String... args) throws Exception {
        new SiteBuilder().build();
    }

    private void build() throws IOException {
        Gson gson = new GsonFireBuilder().createGson();
        Client client = ClientBuilder.newClient();
        client.register(GsonJsonProvider.class);

        Map<String, String> decklists = processDecklists();

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

                    String deckname = inputTheme.getName().toLowerCase(Locale.ENGLISH);
                    if (inputTheme.getVariant() > 0) {
                        deckname = deckname + " (" + inputTheme.getVariant() + ")";
                    }
                    String decklist = decklists.get(deckname);
                    if (decklist != null) {
                        checkDecklist(deckname, decklist, cardDictionary);
                        decklist = decklist.replaceAll("AJMP", "JMP");
                        theme.setMtgaDecklist(decklist);
                    } else {
                        System.out.println("Missing decklist for " + deckname);
                    }

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


    private void checkDecklist(String decklistName, String decklist, CardDictionary cardDictionary) throws IOException {
        String[] lines = decklist.split("\n");

        int cardCount = 0;
        for (String line : lines) {
            Matcher match = CARD_PATTERN.matcher(line);
            if (match.matches()) {
                cardCount += Integer.parseInt(match.group(1));
                String cardName = match.group(2);
                String set = match.group(3).toLowerCase(Locale.ENGLISH);
                if (!set.equals("ajmp") && !set.equals("jmp") && !set.equals("m21")) {
                    System.out.println("Card from wrong set in " + decklistName + " - " + cardName);
                }
                int cardNumber = Integer.parseInt(match.group(4));

                try {
                    CardObject card = cardDictionary.get(set, cardNumber);
                    if (!cardName.equals(card.getName())) {
                        System.out.println("Wrong card in " + decklistName + "? Expected '" + cardName + "', found '" + card.getName() + "'");
                    }
                } catch (RuntimeException e) {
                    if (e.getCause() instanceof NotFoundException) {
                        System.out.println("Could not find " + set + " " + cardNumber + " in theme " + decklistName);
                    } else {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Bad card definition in " + decklistName + " - " + line);
            }
        }
        if (cardCount != 20) {
            System.out.println("Incorrect card count in " + decklistName + " - found " + cardCount);
        }
    }

    private Map<String, String> processDecklists() throws IOException {
        Map<String, String> decklists = new HashMap<>();
        List<String> lines;
        try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/decklists.txt"))) {
            lines = CharStreams.readLines(reader);
        }

        String currentTheme = null;
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                if (currentTheme != null) {
                    decklists.put(currentTheme.toLowerCase(Locale.ENGLISH), builder.toString());
                    currentTheme = null;
                    builder.setLength(0);
                }
            } else if (currentTheme == null) {
                currentTheme = line;
            } else {
                builder.append(line);
                builder.append('\n');
            }
        }
        return decklists;

    }

}
