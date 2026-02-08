package fzmm.zailer.me.client.gui.encrypt_book.translation_file_saver;

import fzmm.zailer.me.client.logic.enycrpt_book.TranslationEncryptProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class TranslationWriteLang implements ITranslationFileSaver{
    @Override
    public Component getMessage() {
        return Component.translatable("fzmm.gui.encryptbook.getDecryptor.option.writeLang");
    }

    @Override
    public CompletableFuture<Boolean> save(TranslationEncryptProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            String langPath = TinyFileDialogs.tinyfd_saveFileDialog(
                    "Save language file",
                    Minecraft.getInstance().getResourcePackDirectory().resolve("en_us.json").toString(),
                    null,
                    null
            );
            boolean cancelled = langPath == null;

            if (!cancelled) {
                try {
                    Files.writeString(Path.of(langPath), profile.toJson().toString(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return cancelled;
        }, Util.backgroundExecutor());
    }
}
