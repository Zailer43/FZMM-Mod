package me.zailer.testmod.client.test_command;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BookComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;

import java.util.ArrayList;
import java.util.List;

public class ParityComponentTest {


    public static void testAll() {

        testBook();
    }

    private static void assertText(String label, List<String> customWrappedText, List<String> vanillaWrappedText) {
        var chatHud = Minecraft.getInstance().gui.getChat();
        var isEqual = true;

        if (customWrappedText.size() != vanillaWrappedText.size()) {
            FzmmClient.LOGGER.warn("[ParityComponentTest] {}: line count is not equals custom: '{}', vanilla: '{}'",
                    label, customWrappedText.size(), vanillaWrappedText.size()
            );
            isEqual = false;
        } else {
            for (int i = 0; i < customWrappedText.size(); i++) {
                String customLine = customWrappedText.get(i);
                String vanillaLine = vanillaWrappedText.get(i);

                if (!customLine.equals(vanillaLine)) {
                    isEqual = false;

                    FzmmClient.LOGGER.warn("[ParityComponentTest] {}: line {} is not equals custom: '{}', vanilla: '{}'",
                            label, i, customWrappedText.get(i), vanillaWrappedText.get(i)
                    );
                    break;
                }
            }
        }

        chatHud.addClientSystemMessage(Component.literal(label + ": " + (isEqual ? "equals" : "not equals")).withStyle(isEqual ? ChatFormatting.GRAY : ChatFormatting.RED));
    }

    private static void assertBookText(String label, String message) {
        assertText(label, testBookCustom(message), testBookVanilla(message));
    }

    private static void testBook() {
        assertBookText("Book ('▏' + '┊') x 2000", "▏┊".repeat(2000));
        assertBookText("Book ('▏' + '☐') x 2000", "▏☐".repeat(2000));
        assertBookText("Book ('☐' + Invisible char) x 2000", "☐\u200C".repeat(2000));
        assertBookText("Book (Lorem Ipsum)", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod laoreet ipsum, a ullamcorper dui ornare vel. Curabitur semper diam ut augue dictum suscipit. Aenean quis suscipit lectus. Vestibulum lacinia libero vel purus malesuada, sit amet dictum lacus tempus. Nullam in sagittis felis, id imperdiet tortor. Morbi ac hendrerit tortor. Aliquam faucibus sagittis metus eget bibendum. Sed ac erat luctus, aliquam tortor ac, tincidunt urna. Cras facilisis turpis a magna consequat, et molestie lacus mattis. Nam vel velit neque. Morbi vitae elementum turpis. Integer nec tincidunt ligula, eu ultrices ante. Aliquam dapibus sit amet orci et varius. Sed tristique massa sed enim molestie consequat. Fusce sit amet sem nunc. Nulla orci mi, euismod et pretium non, ultrices eget elit.");
        assertBookText("Book (empty lines)", "a\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\na");
        assertBookText("Book (random message)", """
                iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii asdaslkdmwedeweow  .
                .........................................................
                
                ___________________
                ######################################
                
                  test   \
                +++++++++++++++++++
                
                kmdfomasdoifmoiemneqofnqifnqo fqemqo meoirew mrqoiermoe         ire  123              .\s"""
        );
    }

    private static List<String> testBookVanilla(String testStr) {
        assert Minecraft.getInstance().player != null;
        var bookEditScreen = new BookEditScreen(Minecraft.getInstance().player,
                Items.WRITABLE_BOOK.getDefaultInstance(),
                InteractionHand.MAIN_HAND,
                new WritableBookContent(new ArrayList<>())
        );
        // text renderer is initialised in setScreen
        Minecraft.getInstance().setScreen(bookEditScreen);

        for (int i = 0; i < testStr.length(); i++) {
            bookEditScreen.keyPressed(new KeyEvent(testStr.charAt(i), 0, 0));
        }

        Minecraft.getInstance().setScreen(null);
        return new ArrayList<>(bookEditScreen.pages);
    }

    private static List<String> testBookCustom(String testStr) {
        var component = new BookComponent();

        // it is necessary to initialize the screen for edit box to work properly (otherwise it has 1 letter per line)
        var screen = new BaseFzmmScreen("main", "main", null) {

            @Override
            protected void setup(EFlowLayout rootComponent) {
                rootComponent.child(component);
                component.setFocused(true);
            }
        };
        Minecraft.getInstance().setScreen(screen);

        for (int i = 0; i < testStr.length(); i++) {
            component.setFocused(true);
            component.keyPressed(new KeyEvent(testStr.charAt(i), 0, 0));
        }

        Minecraft.getInstance().setScreen(null);
        return component.getWrappedText();
    }
}
