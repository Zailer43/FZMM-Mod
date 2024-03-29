package fzmm.zailer.me.client.logic.headGallery;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.FzmmClient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.http.client.HttpResponseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class HeadGalleryResources {
    public static final HashMap<String, ObjectArrayList<MinecraftHeadsData>> cache = new HashMap<>();
    public static final String MINECRAFT_HEADS_URL = "https://minecraft-heads.com";
    public static final String MINECRAFT_HEADS_API = MINECRAFT_HEADS_URL + "/scripts/api.php";
    public static final List<String> CATEGORY_LIST = List.of(
            "alphabet",
            "animals",
            "blocks",
            "decoration",
            "food-drinks",
            "humans",
            "humanoid",
            "miscellaneous",
            "monsters",
            "plants"
    );

    public static CompletableFuture<ObjectArrayList<MinecraftHeadsData>> getCategory(String category) {
        return CompletableFuture.supplyAsync(() -> {
            if (!CATEGORY_LIST.contains(category))
                throw new IllegalArgumentException("Invalid category. Please choose from the following list: " + CATEGORY_LIST);

            boolean cacheCategories = FzmmClient.CONFIG.headGallery.cacheCategories();
            if (cacheCategories && cache.containsKey(category))
                return cache.get(category);

            String url = MINECRAFT_HEADS_API + "?cat=" + category + "&tags=true";
            ObjectArrayList<MinecraftHeadsData> headsData = new ObjectArrayList<>();
            try {
                URL obj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                conn.setRequestProperty("User-Agent", FzmmClient.HTTP_USER_AGENT);
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                if ((responseCode / 100) == 2) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null)
                        response.append(inputLine);

                    in.close();
                    JsonArray jsonArray = JsonParser.parseString(response.toString()).getAsJsonArray();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                        headsData.add(MinecraftHeadsData.parse(jsonObject));
                    }

                    if (cacheCategories)
                        cache.put(category, headsData);

                    FzmmClient.LOGGER.info("[HeadGalleryResources] category '{}' successfully loaded", category);
                } else {
                    String errorReason = conn.getResponseMessage();
                    FzmmClient.LOGGER.error("[HeadGalleryResources] HTTP Error {} ({})", responseCode, errorReason);
                    throw new HttpResponseException(responseCode, errorReason == null ? "" : errorReason);
                }
            } catch (Exception e) {
                FzmmClient.LOGGER.error("The category '{}' of head gallery could not be loaded", category, e);
                throw new CompletionException(e);
            }
            return headsData;
        }).exceptionally(e -> {
            throw new RuntimeException(e.getCause());
        });
    }

}
