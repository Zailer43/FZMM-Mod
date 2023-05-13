package fzmm.zailer.me.client.gui.components.image.source;

import com.mojang.brigadier.suggestion.SuggestionProvider;

public interface IImageSuggestion {

    SuggestionProvider<?> getSuggestionProvider();
}
