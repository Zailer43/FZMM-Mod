package fzmm.zailer.me.client.gui.components.image.source;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.awt.image.BufferedImage;
import java.util.Optional;

public interface IImageSuggestion {

    SuggestionProvider<?> getSuggestionProvider();
}
