package fzmm.zailer.me.client.gui.text_format.tabs;

import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import fzmm.zailer.me.compat.CompatMods;
import fzmm.zailer.me.compat.placeholder_api.PlaceholderApiCompat;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TextFormatPlaceholderApiTab implements ITextFormatTab {
    private static final String PLACEHOLDER_WIKI = "https://placeholders.pb4.eu/user/text-format";
    private FlowLayout infoLayout;

    @Override
    public String getId() {
        return "placeholder_api";
    }

    @Override
    public Text getText(TextFormatLogic logic) {
        String message = logic.message();
        return CompatMods.PLACEHOLDER_API_PRESENT ? PlaceholderApiCompat.parse(message) : Text.literal(message);
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.infoLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "placeholder-info");

        Component wikiInfo = Components.button(Text.translatable("fzmm.gui.textFormat.button.placeholderApiWiki"), buttonComponent -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ConfirmLinkScreen.open(client.currentScreen, PLACEHOLDER_WIKI);
        });
        this.infoLayout.child(wikiInfo);

        this.addUsageExamples();
    }

    private void addUsageExamples() {
        List<String> examples = new ArrayList<>();
        List<Component> componentList = new ArrayList<>();

        examples.add("<yellow>Yellow, <aqua>aqua, <light_purple>and light purple message");
        examples.add("<color:#AA0060>Custom <color:#80D000>colors <color:#00C0C0>message");
        examples.add("<strikethrough>strikethrough</strikethrough> <underline>underline</underline>");
        examples.add("<italic>italic</italic> <bold>bold</bold> <obfuscated>obfuscated</obfuscated>");
        examples.add("<st>strikethrough <underlined>underline <i>italic <b>bold <obf>obfuscated");
        examples.add("<font:default>Default minecraft font, <font:uniform>uniform font, <font:alt>alt font");
        examples.add("<green><underline><lang:" + Items.KNOWLEDGE_BOOK.getTranslationKey() + "></underline></green>");
        examples.add("<gr:#306ACF:#4530CF:#30BACF><b>gradient message</b></gr>");
        examples.add("<hgr:#306ACF:#4530CF:#30BACF><b>hard gradient message</b></hgr>");
        examples.add("<rainbow>Rainbow message</rainbow>");
        examples.add("<rb:0.8:0.7:0>parameters of rainbow are: frequency, saturation, offset</rb>");
        examples.add("<red><b><underline>Hello<r> world");

        componentList.add(EComponents.label(Text.translatable("fzmm.gui.textFormat.label.placeholderApi.examples")));

        for (var example : examples) {
            componentList.add(EComponents.label(PlaceholderApiCompat.parse(example)).tooltip(Text.literal(example)));
        }

        componentList.get(componentList.size() - 1).margins(Insets.bottom(6));

        this.infoLayout.children(componentList);
    }

    @Override
    public void setRandomValues() {
    }

    @Override
    public void componentsCallback(Consumer<Object> callback) {
    }

    @Override
    public boolean hasStyles() {
        return false;
    }
}
