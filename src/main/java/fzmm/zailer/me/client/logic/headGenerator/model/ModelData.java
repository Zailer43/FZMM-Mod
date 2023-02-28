package fzmm.zailer.me.client.logic.headGenerator.model;

import io.wispforest.owo.ui.core.Color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public record ModelData(Graphics2D graphics, HashMap<String, BufferedImage> textures, HashMap<String, Color> colors,
                        AtomicReference<BufferedImage> selectedTexture, AtomicReference<Color> selectedColor) {
}
