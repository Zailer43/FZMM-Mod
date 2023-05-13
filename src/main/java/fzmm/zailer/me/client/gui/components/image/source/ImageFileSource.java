package fzmm.zailer.me.client.gui.components.image.source;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.toast.status.ImageStatus;
import fzmm.zailer.me.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ImageFileSource implements IImageLoaderFromText, IImageSuggestion {
    private static final int MAX_SUGGESTIONS = 25;
    private BufferedImage image;

    public ImageFileSource() {
        this.image = null;
    }

    @Override
    public ImageStatus loadImage(String value) {
        this.image = null;

        try {
            File file = Paths.get(value).toFile();
            if (!file.exists())
                return ImageStatus.FILE_DOES_NOT_EXIST;

            if (!file.isFile())
                return ImageStatus.PATH_DOES_NOT_HAVE_A_FILE;

            this.image = ImageUtils.getImageFromPath(value);
            return ImageStatus.IMAGE_LOADED;
        } catch (Exception e) {
            FzmmClient.LOGGER.error("Unexpected error loading an image", e);
            return ImageStatus.UNEXPECTED_ERROR;
        }
    }

    @Override
    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public boolean predicate(String value) {
        File file = new File(value);
        return file.exists() && file.isFile();
    }

    @Override
    public boolean hasTextField() {
        return true;
    }

    @Override
    public SuggestionProvider<?> getSuggestionProvider() {
        return (nul, builder) -> {
            try {
                File inputFile = new File(builder.getInput());
                File parentFolder;
                String prefix = "";

                if (inputFile.isDirectory()) {
                    parentFolder = inputFile;
                } else {
                    parentFolder = inputFile.getParentFile();
                    prefix = inputFile.getName();
                }

                if (parentFolder == null)
                    return CompletableFuture.completedFuture(builder.build());

                File[] files = parentFolder.listFiles();

                this.addSuggestions(files, prefix, builder);
            } catch (Exception e) {
                FzmmClient.LOGGER.error("[ImageFileSource] Failed to get suggestions", e);
            }

            return CompletableFuture.completedFuture(builder.build());
        };
    }

    private void addSuggestions(File[] files, String prefix, SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (suggestions.size() > MAX_SUGGESTIONS)
                    break;

                if (file.getName().startsWith(prefix)) {
                    suggestions.add(file.getPath());
                }
            }
        }

        suggestions.forEach(builder::suggest);
    }
}
