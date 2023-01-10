package fzmm.zailer.me.client.logic.imagetext;

import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public record ImagetextData(BufferedImage image, @Nullable String characters, int width, int height,
                            boolean smoothRescaling, double percentageOfSimilarityToCompress) {
}
