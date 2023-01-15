package fzmm.zailer.me.client.logic.headGenerator.model;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface IModelStep {

    void apply(Graphics2D graphics, BufferedImage baseSkin);
}
