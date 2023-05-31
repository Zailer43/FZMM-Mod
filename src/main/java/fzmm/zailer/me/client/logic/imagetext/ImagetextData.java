package fzmm.zailer.me.client.logic.imagetext;

import java.awt.image.BufferedImage;

public record ImagetextData(BufferedImage image, int width, int height,
                            boolean smoothRescaling, double percentageOfSimilarityToCompress) {
}
