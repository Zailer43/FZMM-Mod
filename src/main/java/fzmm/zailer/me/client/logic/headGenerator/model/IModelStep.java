package fzmm.zailer.me.client.logic.headGenerator.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public interface IModelStep {

    void apply(Graphics2D graphics, HashMap<String, BufferedImage> textures, AtomicReference<BufferedImage> selectedTexture);
}
