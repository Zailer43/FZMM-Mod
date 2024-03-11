package fzmm.zailer.me.client.gui.item_editor.skull_editor;

import com.mojang.authlib.yggdrasil.TextureUrlChecker;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.skull_editor.components.PlaySoundButtonComponent;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.utils.skin.GetSkinFromMojang;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class SkullEditor implements IItemEditorScreen {
    private RequestedItem skullRequested = null;
    private List<RequestedItem> requestedItems = null;
    private final HeadBuilder builder = HeadBuilder.builder();
    private ConfigTextBox noteBlockSoundTextBox;
    private ConfigTextBox headNameTextBox;
    private ConfigTextBox headUuidTextBox;
    private BooleanButton headUuidFormatButton;
    private TextAreaComponent headValueTextBox;
    private boolean ignoreHeadValueCallback;
    private ConfigTextBox headTextureTextBox;
    private boolean ignoreHeadTextureCallback;
    private LabelComponent textureStatusLabel;

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.skullRequested = new RequestedItem(
                itemStack -> itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SkullBlock,
                this::selectItemAndUpdateParameters,
                null,
                Items.PLAYER_HEAD.getDefaultStack(),
                Text.translatable("fzmm.gui.itemEditor.skull.title"),
                true
        );

        this.requestedItems = List.of(this.skullRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.SKELETON_SKULL.getDefaultStack();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        // skull editor
        FlowLayout playNoteBlockSoundLayout = editorLayout.childById(FlowLayout.class, "play-note-block-sound-layout");
        BaseFzmmScreen.checkNull(playNoteBlockSoundLayout, "flow-layout", "play-note-block-sound-layout");
        PlaySoundButtonComponent playNoteBlockSoundButton = new PlaySoundButtonComponent();
        playNoteBlockSoundLayout.child(playNoteBlockSoundButton);

        this.noteBlockSoundTextBox = editorLayout.childById(ConfigTextBox.class, "note-block-sound");
        BaseFzmmScreen.checkNull(this.noteBlockSoundTextBox, "text-box", "note-block-sound");
        this.noteBlockSoundTextBox.valueParser(Identifier::tryParse);
        this.noteBlockSoundTextBox.applyPredicate(s -> Registries.SOUND_EVENT.getOrEmpty(new Identifier(s)).isPresent());
        this.noteBlockSoundTextBox.setMaxLength(255);
        this.noteBlockSoundTextBox.onChanged().subscribe(value -> {
            if (this.noteBlockSoundTextBox.isValid())
                playNoteBlockSoundButton.setSound((Identifier) this.noteBlockSoundTextBox.parsedValue());
            else
                playNoteBlockSoundButton.setSound((Identifier) null);

            this.builder.noteBlockSound((Identifier) this.noteBlockSoundTextBox.parsedValue());
            this.updateItemPreview();
        });

        ButtonComponent selectNoteBlockSoundButton = editorLayout.childById(ButtonComponent.class, "select-note-block-sound");
        BaseFzmmScreen.checkNull(selectNoteBlockSoundButton, "button", "select-note-block-sound");
        selectNoteBlockSoundButton.onPress(button -> this.setSelectNoteBlockSoundOverlay(editorLayout));

        // player head editor
        this.headNameTextBox = editorLayout.childById(ConfigTextBox.class, "head-name");
        BaseFzmmScreen.checkNull(this.headNameTextBox, "text-box", "head-name");
        this.headNameTextBox.setMaxLength(255);
        this.headNameTextBox.onChanged().subscribe(value -> {
            this.builder.headName(value);
            this.updateItemPreview();
        });

        ButtonComponent loadFromNameButton = editorLayout.childById(ButtonComponent.class, "load-from-name");
        BaseFzmmScreen.checkNull(loadFromNameButton, "button", "load-from-name");
        loadFromNameButton.onPress(this::loadFromNameExecute);

        this.headUuidTextBox = editorLayout.childById(ConfigTextBox.class, "head-uuid");
        BaseFzmmScreen.checkNull(this.headUuidTextBox, "text-box", "head-uuid");
        this.headUuidTextBox.applyPredicate(s -> this.parseUUid(s, this.headUuidFormatButton.enabled()).isPresent());
        this.headUuidTextBox.valueParser(s -> this.parseUUid(s, this.headUuidFormatButton.enabled()).orElse(null));
        this.headUuidTextBox.setMaxLength(70);
        this.headUuidTextBox.onChanged().subscribe(value -> {
            this.builder.id((UUID) this.headUuidTextBox.parsedValue());
            this.updateItemPreview();
        });

        ButtonComponent randomUuid = editorLayout.childById(ButtonComponent.class, "random-uuid");
        BaseFzmmScreen.checkNull(randomUuid, "button", "random-uuid");
        randomUuid.onPress(button -> this.setRandomUuid());

        this.headUuidFormatButton = editorLayout.childById(BooleanButton.class, "uuid-format");
        BaseFzmmScreen.checkNull(this.headUuidFormatButton, "boolean-button", "uuid-format");
        this.headUuidFormatButton.enabled(true);
        this.headUuidFormatButton.onPress(this::changeUuidFormat);

        this.ignoreHeadValueCallback = false;
        this.headValueTextBox = editorLayout.childById(TextAreaComponent.class, "head-value");
        BaseFzmmScreen.checkNull(this.headValueTextBox, "text-box", "head-value");
        this.headValueTextBox.setMaxLength(1024);
        this.headValueTextBox.onChanged().subscribe(this::headValueCallback);

        this.ignoreHeadTextureCallback = false;
        this.headTextureTextBox = editorLayout.childById(ConfigTextBox.class, "head-texture");
        BaseFzmmScreen.checkNull(this.headTextureTextBox, "text-box", "head-texture");
        this.headTextureTextBox.setMaxLength(150);
        this.headTextureTextBox.onChanged().subscribe(value -> this.headTextureCallback());


        this.textureStatusLabel = editorLayout.childById(LabelComponent.class, "texture-status");
        BaseFzmmScreen.checkNull(this.textureStatusLabel, "label", "texture-status");

        return editorLayout;
    }

    @Override
    public String getId() {
        return "skull";
    }

    @Override
    public void updateItemPreview() {
        this.skullRequested.setStack(this.builder.get());
        this.skullRequested.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.builder.of(stack);

        Optional<Identifier> noteBlockSoundOptional = this.builder.noteBlockSound();
        if (noteBlockSoundOptional.isEmpty()) {
            this.noteBlockSoundTextBox.text("");
        } else {
            Identifier noteBlockSound = noteBlockSoundOptional.get();
            this.noteBlockSoundTextBox.text(noteBlockSound.toString());
        }

        this.headNameTextBox.text(this.builder.headName());

        Optional<UUID> headUuidOptional = this.builder.idUuid();
        if (headUuidOptional.isEmpty())
            this.headUuidTextBox.text("");
        else
            this.headUuidTextBox.text(headUuidOptional.get().toString());

        this.headValueTextBox.text(this.builder.skinValue());
        this.headTextureTextBox.text(this.builder.skinUrl());
    }

    private void setSelectNoteBlockSoundOverlay(FlowLayout editorLayout) {
        FlowLayout overlayLayout = Containers.verticalFlow(Sizing.expand(90), Sizing.expand(90));
        Set<RegistryKey<SoundEvent>> soundEvents = Registries.SOUND_EVENT.getKeys();
        OverlayContainer<FlowLayout> overlay = Containers.overlay(overlayLayout);

        List<SoundEvent> soundEventList = soundEvents.stream()
                .map(Registries.SOUND_EVENT::get)
                .sorted(Comparator.comparing(t -> {
                    assert t != null;
                    return t.getId().toString();
                })).toList();

        FlowLayout contentLayout = Containers.ltrTextFlow(Sizing.expand(100), Sizing.content());
        contentLayout.gap(15);
        contentLayout.verticalAlignment(VerticalAlignment.CENTER);

        int maxWidth = 0;
        List<LabelComponent> labelList = new ArrayList<>();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Text selectTranslation = Text.translatable("fzmm.gui.itemEditor.skull.button.selectSound");

        List<Pair<LabelComponent, FlowLayout>> componentsList = new ArrayList<>();
        for (var sound : soundEventList) {
            FlowLayout soundLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            soundLayout.gap(4);

            Text labelText = Text.literal(sound.getId().toString());
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(labelText));
            LabelComponent soundLabel = Components.label(labelText);
            labelList.add(soundLabel);

            PlaySoundButtonComponent soundPlayButton = new PlaySoundButtonComponent();
            soundPlayButton.setSound(sound);

            ButtonComponent selectButton = Components.button(selectTranslation, button -> {
                this.noteBlockSoundTextBox.text(sound.getId().toString());
                overlay.remove();
            });

            soundLayout.child(soundLabel);
            soundLayout.child(soundPlayButton);
            soundLayout.child(selectButton);

            soundLayout.mouseEnter().subscribe(() -> soundLayout.surface(Surface.flat(0x40000000)));
            soundLayout.mouseLeave().subscribe(() -> soundLayout.surface(Surface.flat(0)));
            // it seems that there is a bug in owo-lib where it
            // does not detect the mouse enter if there are components on top of it.
            for (var child : soundLayout.children())
                child.mouseEnter().subscribe(() -> soundLayout.surface(Surface.flat(0x40000000)));

            for (var child : soundLayout.children())
                child.mouseLeave().subscribe(() -> soundLayout.surface(Surface.flat(0)));

            componentsList.add(new Pair<>(soundLabel, soundLayout));
        }

        maxWidth = Math.min(maxWidth, 250);
        for (var label : labelList)
            label.horizontalSizing(Sizing.fixed(maxWidth));

        FlowLayout searchLayout = Containers.horizontalFlow(Sizing.expand(100), Sizing.fixed(24));
        searchLayout.gap(4);
        searchLayout.verticalAlignment(VerticalAlignment.CENTER);
        searchLayout.child(Components.label(Text.translatable("fzmm.gui.label.search")));
        TextBoxComponent searchTextBox = Components.textBox(Sizing.fixed(150));
        searchTextBox.onChanged().subscribe(value -> this.searchNoteBlockSoundOverlay(value, contentLayout, componentsList));
        searchLayout.child(searchTextBox);

        this.searchNoteBlockSoundOverlay("", contentLayout, componentsList);

        ScrollContainer<FlowLayout> contentScroll = Containers.verticalScroll(Sizing.expand(100), Sizing.expand(100), contentLayout);

        overlayLayout.padding(Insets.of(6));
        overlayLayout.surface(Surface.DARK_PANEL);
        overlayLayout.zIndex(500);
        overlayLayout.child(searchLayout);
        overlayLayout.child(contentScroll);

        if (editorLayout.root() instanceof FlowLayout rootLayout)
            rootLayout.child(overlay);
    }

    private void searchNoteBlockSoundOverlay(String search, FlowLayout contentLayout, List<Pair<LabelComponent, FlowLayout>> componentsList) {
        contentLayout.clearChildren();
        List<Pair<LabelComponent, FlowLayout>> resultList = new ArrayList<>(componentsList);

        search = search.toLowerCase();
        for (var component : componentsList) {
            if (!component.getLeft().text().getString().toLowerCase().contains(search))
                resultList.remove(component);
        }

        contentLayout.children(resultList.stream().map(Pair::getRight).collect(Collectors.toList()));
    }

    private void loadFromNameExecute(ButtonComponent loadFromNameButton) {
        loadFromNameButton.active = false;
        this.builder.setUnloadedHead(true);
        Optional<ItemStack> stackFromHeadOptional = new GetSkinFromMojang().getHead(this.headNameTextBox.getText());
        stackFromHeadOptional.ifPresent(this.builder::of);
        this.updateItemPreview();

        // I don't currently have an event or something that lets me know
        // when the postProcessNbt is finished, so I use a schedule
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            this.builder.setUnloadedHead(false);
            MinecraftClient.getInstance().execute(() -> this.selectItemAndUpdateParameters(this.skullRequested.stack()));
            loadFromNameButton.active = true;
        }, 2500, TimeUnit.MILLISECONDS);
        scheduler.shutdown();
    }

    /**
     * @param isUuidFormat true if value is String UUID format, false if is NbtIntArray String
     */
    private Optional<UUID> parseUUid(String value, boolean isUuidFormat) {
        UUID uuid = null;
        try {
            if (isUuidFormat) {
                uuid = UUID.fromString(value);
            } else {
                String[] intArr = value.replace("[I;", "").replace("]", "").split(",");
                int[] intList = new int[intArr.length];
                for (int i = 0; i < intArr.length; i++)
                    intList[i] = Integer.parseInt(intArr[i]);

                NbtIntArray nbtIntArray = new NbtIntArray(intList);
                uuid = NbtHelper.toUuid(nbtIntArray);
            }
        } catch (Exception ignored) {
        }

        return Optional.ofNullable(uuid);
    }

    private void headValueCallback(String value) {
        if (this.ignoreHeadValueCallback)
            return;
        this.builder.skinValue(value);

        this.ignoreHeadTextureCallback = true;
        this.headTextureTextBox.text(this.builder.skinUrl());
        this.ignoreHeadTextureCallback = false;

        this.updateStatusLabel();
        this.updateItemPreview();
    }

    private void headTextureCallback() {
        if (this.ignoreHeadTextureCallback)
            return;

        this.updateStatusLabel();
        this.ignoreHeadValueCallback = true;
        this.headValueTextBox.text(this.builder.skinValue());
        this.ignoreHeadValueCallback = false;
        this.updateItemPreview();
    }

    private void updateStatusLabel() {
        this.builder.skinUrl(this.headTextureTextBox.getText());
        String url = this.builder.skinUrl();
        boolean isValid = !url.isEmpty() && TextureUrlChecker.isAllowedTextureDomain(url);

        String translationKey = "fzmm.gui.itemEditor.skull.label.headTexture." + (isValid ? "valid" : "invalid");
        int color = isValid ? this.headTextureTextBox.validColor() : this.headTextureTextBox.invalidColor();
        this.textureStatusLabel.text(Text.translatable(translationKey).setStyle(Style.EMPTY.withColor(color)));
    }

    private void changeUuidFormat(ButtonComponent buttonComponent) {
        String result;
        UUID uuid = this.builder.idUuid().orElse(UUID.randomUUID());

        if (this.headUuidFormatButton.enabled())
            result = uuid.toString();
        else
            result = NbtHelper.fromUuid(uuid).toString();

        this.headUuidTextBox.text(result);
    }

    private void setRandomUuid() {
        String result;
        UUID randomUUID = UUID.randomUUID();
        if (this.headUuidFormatButton.enabled())
            result = randomUUID.toString();
        else
            result = NbtHelper.fromUuid(randomUUID).toString();

        this.headUuidTextBox.text(result);
    }
}
