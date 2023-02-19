package fzmm.zailer.me.client.gui.textformat.tabs;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.utils.IMementoObject;
import fzmm.zailer.me.client.gui.textformat.ITextFormatTab;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import fzmm.zailer.me.compat.CompatMods;
import fzmm.zailer.me.compat.placeholderApi.PlaceholderApiCompat;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TextFormatPlaceholderApiTab implements ITextFormatTab {

    private static final String PLACEHOLDER_WIKI = "https://placeholders.pb4.eu/user/text-format";
    private static final String INFO_ID = "placeholder-info";
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
    public void setupComponents(FlowLayout rootComponent) {
        this.infoLayout = rootComponent.childById(FlowLayout.class, INFO_ID);
        BaseFzmmScreen.checkNull(this.infoLayout, "flow-layout", INFO_ID);

        Component wikiInfo = Components.button(Text.translatable("fzmm.gui.textFormat.button.placeholderApiWiki"), buttonComponent -> {
            MinecraftClient client = MinecraftClient.getInstance();
            Screen currentScreen = client.currentScreen;
            client.setScreen(new ConfirmLinkScreen(bool -> {
                if (bool)
                    Util.getOperatingSystem().open(PLACEHOLDER_WIKI);

                client.setScreen(currentScreen);
            }, PLACEHOLDER_WIKI, true));
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

        componentList.add(Components.label(Text.translatable("fzmm.gui.textFormat.label.placeholderApi.examples")));

        for (var example : examples)
            componentList.add(Components.label(PlaceholderApiCompat.parse(example)).tooltip(Text.literal(example)));

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

    @Override
    public IMementoObject createMemento() {
        return null;
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {

    }
}
