package fzmm.zailer.me.client.gui.utils.context_menu;

import com.mojang.serialization.JsonOps;
import fzmm.zailer.me.builders.ContainerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmIcons;
import fzmm.zailer.me.client.command.fzmm.NbtCommand;
import fzmm.zailer.me.client.gui.banner_editor.BannerEditorScreen;
import fzmm.zailer.me.client.gui.converters.tabs.ConverterUuidToArrayTab;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentOverlay;
import fzmm.zailer.me.client.gui.utils.CopyTextScreen;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.model.InternalModels;
import fzmm.zailer.me.utils.*;
import fzmm.zailer.me.utils.skin.CacheSkinGetter;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class CtxMenuActions {
    public static List<CtxElement<List<ItemStack>>> itemActions() {
        return List.of(giveEntry(), copyEntry(), screenEntry(), headGeneratorEntry(), giveWrappedEntry(), givePackagedEntry());
    }

    // allow multiple if player has more or equals hotbar empty?
    private static CtxElement<List<ItemStack>> giveEntry() {
        return CtxEntry.<List<ItemStack>>single("give", stacks -> ItemUtils.give(stacks.get(0)))
                .condition((stack, type) -> type != CtxType.INVENTORY && !ItemUtils.isNotAllowedToGive())
                .component(ICtxComponent.simple(Component.translatable("fzmm.gui.contextMenu.give")));
    }

    private static CtxElement<List<ItemStack>> copyEntry() {
        String translationKey = "fzmm.gui.contextMenu.copy";
        BiPredicate<List<ItemStack>, CtxType> requireNbt = (stacks, type) -> ItemUtils.getNbtIfModifiedCompounds(stacks.get(0)).isPresent();

        // copy - name related
        var nameText = CtxEntry.<List<ItemStack>>single("copy_name_text", stacks -> SnackBarManager.copyToClipboard(stacks.get(0).getHoverName().getString()))
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".name.text")));

        var nameJson = CtxEntry.<List<ItemStack>>single("copy_name_json", stacks -> ComponentSerialization.CODEC
                        .encodeStart(FzmmUtils.getRegistryOps(JsonOps.INSTANCE), stacks.get(0).getHoverName())
                        .result().ifPresent(jsonElement -> SnackBarManager.copyToClipboard(jsonElement.toString())))
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".name.json")));

        var nameAllOptions = CtxEntry.<List<ItemStack>>single("copy_name_all_options", stacks ->
                Minecraft.getInstance().execute(() -> {
                    var screen = new CopyTextScreen(Minecraft.getInstance().gui.screen(), stacks.get(0).getHoverName());
                    Minecraft.getInstance().gui.setScreen(screen);
                })
        ).component(ICtxComponent.simple(Component.translatable(translationKey + ".name.allOptions")));

        // copy - raw nbt - nbt related
        var rawNbt = initCopyRawNbt(translationKey + ".rawNbt", requireNbt);

        // copy - give related
        var vanillaGive = CtxEntry.<List<ItemStack>>single("copy_vanilla_give", stacks -> ItemUtils.toIdWithComponents(stacks.get(0))
                        .ifPresent(value -> {
                            String command = String.format("/give @s %s %s", value, stacks.get(0).getCount()); // avoid hardcoded?
                            SnackBarManager.copyToClipboard(command);
                        }))
                .condition(requireNbt)
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".vanillaGive"), Icon.of(Items.COMMAND_BLOCK)));

        var fzmmGive = CtxEntry.<List<ItemStack>>single("copy_fzmm_give", stacks -> ItemUtils.toIdWithComponents(stacks.get(0))
                        .ifPresent(value -> {
                            String command = String.format("/fzmm give %s %s", value, stacks.get(0).getCount()); // avoid hardcoded?
                            SnackBarManager.copyToClipboard(command);
                        }))
                .condition(requireNbt)
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".fzmmGive"), Icon.of(Items.COMMAND_BLOCK)));

        // copy - player head
        var playerHead = initCopyPlayerHead(translationKey + ".playerHead");

        return CtxNode.of("copy", List.of(nameText, nameJson, nameAllOptions, CtxEntry.divider(), vanillaGive, fzmmGive, CtxEntry.divider(), rawNbt, playerHead))
                .component(ICtxComponent.simple(Component.translatable(translationKey)));
    }

    private static CtxElement<List<ItemStack>> initCopyRawNbt(String translationKey, BiPredicate<List<ItemStack>, CtxType> requireNbt) {
        var components = CtxEntry.<List<ItemStack>>single("copy_components", stacks ->
                        ItemUtils.getNbtIfModifiedCompounds(stacks.get(0))
                                .map(nbt -> NbtCommand.toFormatedComponent(nbt, false).getString())
                                .ifPresent(SnackBarManager::copyToClipboard)
                ).condition(requireNbt)
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".components")));

        var componentsAsNbt = CtxEntry.<List<ItemStack>>single("copy_components_nbt", stacks ->
                        ItemUtils.getNbtIfModifiedCompounds(stacks.get(0))
                                .map(CompoundTag::toString)
                                .ifPresent(SnackBarManager::copyToClipboard)
                ).condition(requireNbt)
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".componentsAsNbt")));

        var itemAsNbt = CtxEntry.<List<ItemStack>>single("copy_item_nbt", stacks ->
                        ItemUtils.toNbt(stacks.get(0))
                                .map(CompoundTag::toString)
                                .ifPresent(SnackBarManager::copyToClipboard)
                ).condition(requireNbt)
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".itemAsNbt")));

        // copy - raw nbt - item related
        var itemWithComponents = CtxEntry.<List<ItemStack>>single("copy_item_components", stacks ->
                        ItemUtils.toIdWithComponents(stacks.get(0)).ifPresent(SnackBarManager::copyToClipboard))
                .condition(requireNbt)
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".itemWithComponents")));

        // copy - raw nbt
        return CtxNode.of("copy_nbts", List.of(components, componentsAsNbt, itemAsNbt, itemWithComponents))
                .component(ICtxComponent.simple(Component.translatable(translationKey)));
    }

    private static CtxElement<List<ItemStack>> initCopyPlayerHead(String translationKey) {
        var name = CtxEntry.<List<ItemStack>>single("copy_head_name", stacks -> {
            var profile = stacks.get(0).getComponents().get(DataComponents.PROFILE);
            if (profile == null) return;
            profile.name().ifPresent(SnackBarManager::copyToClipboard);
        }).component(ICtxComponent.simple(Component.translatable(translationKey + ".name")));

        var uuidAsString = CtxEntry.<List<ItemStack>>single("copy_head_uuid_str", stacks -> {
            var profile = stacks.get(0).getComponents().get(DataComponents.PROFILE);
            if (profile == null) return;
            SnackBarManager.copyToClipboard(profile.partialProfile().id().toString());
        }).component(ICtxComponent.simple(Component.translatable(translationKey + ".uuidAsString")));

        var uuidAsArray = CtxEntry.<List<ItemStack>>single("copy_head_uuid_arr", stacks -> {
            var profile = stacks.get(0).getComponents().get(DataComponents.PROFILE);
            if (profile == null) return;
            int[] uuid = ConverterUuidToArrayTab.UUIDtoArray(profile.partialProfile().id());
            SnackBarManager.copyToClipboard(ConverterUuidToArrayTab.toString(uuid));
        }).component(ICtxComponent.simple(Component.translatable(translationKey + ".uuidAsArray")));

        var url = CtxEntry.<List<ItemStack>>single("copy_head_url", stacks -> {
            var profile = stacks.get(0).getComponents().get(DataComponents.PROFILE);
            if (profile == null) return;
            HeadUtils.unwrapUrl(profile).ifPresent(SnackBarManager::copyToClipboard);
        }).component(ICtxComponent.simple(Component.translatable(translationKey + ".url")));

        var value = CtxEntry.<List<ItemStack>>single("copy_head_value", stacks -> {
            var profile = stacks.get(0).getComponents().get(DataComponents.PROFILE);
            if (profile == null) return;
            HeadUtils.skinValue(profile).ifPresent(SnackBarManager::copyToClipboard);
        }).component(ICtxComponent.simple(Component.translatable(translationKey + ".value")));

        return CtxNode.of("copy_head", List.of(name, uuidAsString, uuidAsArray, url, value))
                .component(ICtxComponent.simple(Component.translatable(translationKey), Icon.of(Items.PLAYER_HEAD)))
                .condition((stacks, type) -> stacks.get(0).getComponents().get(DataComponents.PROFILE) != null);
    }

    private static CtxElement<List<ItemStack>> screenEntry() {
        String translationKey = "fzmm.gui.contextMenu.openScreen";

        var bannerEditor = CtxEntry.<List<ItemStack>>single("screen_banner_editor", stacks ->
                        Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.setScreen(new BannerEditorScreen(Minecraft.getInstance().gui.screen(), stacks.get(0))))
                ).component(ICtxComponent.simple(Component.translatable("fzmm.gui.title.bannerEditor"), FzmmIcons.of(FzmmIcons.BANNER_EDITOR)))
                .condition((stacks, type) -> BannerEditorScreen.condition(stacks.get(0)));

        var headGenerator = CtxEntry.<List<ItemStack>>single("screen_head_generator", stacks ->
                        HeadUtils.getSkinTextures(stacks.get(0)).whenComplete((playerSkin, throwable) -> {
                            if (playerSkin.isEmpty() || throwable != null) return;

                            var image = new CacheSkinGetter().getSkin(playerSkin.get());
                            if (image.isEmpty()) return;

                            Minecraft.getInstance().execute(() -> {
                                var screen = new HeadGeneratorScreen(Minecraft.getInstance().gui.screen());
                                Minecraft.getInstance().gui.setScreen(screen);
                                screen.skinCallback(image.get());
                            });
                        })
                ).component(ICtxComponent.simple(Component.translatable("fzmm.gui.title.headGenerator"), FzmmIcons.of(FzmmIcons.HEAD_GENERATOR)))
                .condition((stacks, type) -> HeadUtils.hasSkin(stacks.get(0)));

        return CtxNode.of("screen", List.of(bannerEditor, headGenerator))
                .component(ICtxComponent.simple(Component.translatable(translationKey)));
    }

    private static CtxElement<List<ItemStack>> headGeneratorEntry() {
        var entries = new ArrayList<CtxElement<List<ItemStack>>>();

        for (int i = 0; i != 6; i++) {
            var modelEntry = InternalModels.ROTATE.get(i % InternalModels.ROTATE.size());
            boolean negative = i >= InternalModels.ROTATE.size();

            entries.add(CtxEntry.<List<ItemStack>>single("head_generator_shortcut_rotate_" + i, stacks ->
                            uploadHead(stacks.get(0), modelEntry, negative))
                    .component(ICtxComponent.button(FzmmIcons.of(new int[]{FzmmIcons.ROTATE_U, i * 16})))
                    .condition((stacks, type) -> HeadUtils.hasSkin(stacks.get(0))));
        }

        // TODO: replace favorite with pinned, use pre-edit, replace icon with head preview component
//        var pinnedConfig = FzmmClient.CONFIG.headGenerator.favoriteSkins();
//        var pinnedEntries = HeadResourcesLoader.getAllLoaded().stream()
//                .filter(entry -> pinnedConfig.contains(entry.getKey()))
//                .sorted(Comparator.comparing(AbstractHeadEntry::getKey))
//                .limit(9)
//                .toList();
//
//        for (var entry : pinnedEntries) {
//            entries.add(CtxEntry.<ItemStack>single("head_generator_shortcut_pin_", stacks -> uploadHead(stacks, entry, false))
//                    .component(ICtxEntryComponent.button(Icon.of(Items.PLAYER_HEAD)))
//                    .condition((stacks, type) -> HeadUtils.hasSkin(stacks)));
//            );
//        }

        return CtxNode.of("head_generator_shortcut", entries)
                .nodeComponent(ICtxComponent.ltrNode(Sizing.fixed(20 * 3 + 2))) // 20 = button size + padding, 2 = ltr layout padding
                .component(ICtxComponent.simple(Component.translatable("fzmm.gui.contextMenu.headGeneratorShortcut"), FzmmIcons.of(FzmmIcons.HEAD_GENERATOR)))
                .condition((stacks, type) -> !ItemUtils.isNotAllowedToGive());
    }

    private static void uploadHead(ItemStack stack, AbstractHeadEntry entry, boolean applyMultipleTimes) {
        HeadUtils.getSkinTextures(stack).whenComplete((skinOptional, throwable) -> Minecraft.getInstance().execute(() -> {
            if (throwable != null || skinOptional.isEmpty()) return;

            var skin = new CacheSkinGetter().getSkin(skinOptional.get()).orElse(null);
            if (skin == null) return;
            uploadHead(stack, HeadComponentOverlay.apply(skin, entry, applyMultipleTimes ? 3 : 1, ImageUtils.hasUnusedPixel(skin)));
        }));
    }

    private static void uploadHead(ItemStack stack, BufferedImage skin) {
        FzmmClient.MINESKIN_API.upload(skin).whenComplete((response, throwable1) -> {
            var profile = stack.getOrDefault(DataComponents.PROFILE, ResolvableProfile.createUnresolved(""));
            var generated = HeadGeneratorScreen.giveItem(response, throwable1, profile.name().orElse(""));
            FzmmClient.MINESKIN_API.showComplete(response, generated, buttonComponent -> uploadHead(stack, skin));
        });
    }

    // allow multiple if player has more or equals hotbar empty?
    private static CtxElement<List<ItemStack>> giveWrappedEntry() {
//        var containers = new ArrayList<Item>();
        var entries = new ArrayList<CtxElement<List<ItemStack>>>();
        var sortedDye = FzmmUtils.getDyeColorsInOrder();

//        containers.add(Items.SHULKER_BOX);
        entries.add(wrapper("wrapped_shulker", Items.SHULKER_BOX, stack -> ItemUtils.wrapInContainer(stack, Items.SHULKER_BOX)));
        for (var dye : sortedDye) {
            var shulker = Items.DYED_SHULKER_BOX.pick(dye);
//            containers.add(shulker);
            entries.add(wrapper("wrapped_shulker_" + dye.getName(), shulker, stack -> ItemUtils.wrapInContainer(stack, shulker)));
        }
        entries.add(wrapper("wrapped_item_frame", Items.ITEM_FRAME, stack -> ItemUtils.wrapInItemFrame(stack, Items.ITEM_FRAME)));
        entries.add(wrapper("wrapped_glow_item_frame", Items.GLOW_ITEM_FRAME, stack -> ItemUtils.wrapInItemFrame(stack, Items.GLOW_ITEM_FRAME)));
        entries.add(wrapper("wrapped_bundle", Items.BUNDLE, stack -> ItemUtils.wrapInBundle(stack, Items.BUNDLE)));
        for (var dye : sortedDye) {
            var bundle = Items.DYED_BUNDLE.pick(dye);
            entries.add(wrapper("wrapped_bundle_" + dye.getName(), bundle, stack -> ItemUtils.wrapInBundle(stack, bundle)));
        }

        // I don't like this: it feels disorganized, its size should be limited in case it gets modded,
        // and it makes it harder to choose quickly. Move to config?
//        for (var item : BuiltInRegistries.ITEM.stream().toList()) {
//            if (containers.contains(item) || item.getDefaultInstance().get(DataComponents.CONTAINER) == null) continue;
//
//            entries.add(wrapper(item, stack -> ItemUtils.wrapInContainer(stack, item.getDefaultInstance())));
//        }

        return CtxNode.of("wrapped", entries)
                .nodeComponent(ICtxComponent.ltrNode(Sizing.fixed(20 * 9 + 2))) // 20 = button size + padding, 2 = ltr layout padding, 9 = column count
                .component(ICtxComponent.simple(Component.translatable("fzmm.gui.contextMenu.wrapped"), Icon.of(Items.CHEST)))
                .condition((stacks, type) -> !ItemUtils.isNotAllowedToGive());
    }

    private static CtxElement<List<ItemStack>> wrapper(String id, Item icon, Function<ItemStack, ItemStack> wrapper) {
        return CtxEntry.<List<ItemStack>>single(id, stacks -> ItemUtils.give(wrapper.apply(stacks.get(0))))
                .component(ICtxComponent.button(Icon.of(icon)));
    }

    private static CtxElement<List<ItemStack>> givePackagedEntry() {
        String translationKey = "fzmm.gui.contextMenu.packaged";
        var item = Items.DYED_SHULKER_BOX.white();
        var icon = Icon.of(item);
        //add container variants?

        var shulker = CtxEntry.<List<ItemStack>>multiple("packed_shulker", stacks -> {
                    var container = ContainerBuilder.builder().containerItem(item).addAll(stacks);
                    ItemUtils.give(container.getAsList().get(0));
                }).condition((value, type) -> value.size() <= ShulkerBoxBlockEntity.CONTAINER_SIZE)
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".shulker"), icon));

        var oneShulker = CtxEntry.<List<ItemStack>>multiple("packed_one_shulker", stacks -> {
                    var container = ContainerBuilder.builder().containerItem(item).addAll(stacks);
                    ItemUtils.give(ContainerBuilder.builder().addAll(container.getAsList()).getAsList().get(0));
                }).condition((value, type) -> value.size() > ShulkerBoxBlockEntity.CONTAINER_SIZE)
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".oneShulker"), icon));

        var severalShulkers = CtxEntry.<List<ItemStack>>multiple("packed_several_shulker", stacks -> {
                    var container = ContainerBuilder.builder().containerItem(item).addAll(stacks);
                    for (var stack : container.getAsList()) {
                        ItemUtils.give(stack);
                    }
                }).condition((value, type) -> value.size() > ShulkerBoxBlockEntity.CONTAINER_SIZE)
                .component(ICtxComponent.simple(Component.translatable(translationKey + ".severalShulkers"), icon));

        return CtxNode.of("packed", List.of(shulker, oneShulker, severalShulkers))
                .component(ICtxComponent.simple(Component.translatable(translationKey)));
    }
}
