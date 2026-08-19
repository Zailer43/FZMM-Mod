package fzmm.zailer.me.client;

import fzmm.zailer.me.builders.ArmorStandBuilder;
import fzmm.zailer.me.builders.BlockStateItemBuilder;
import fzmm.zailer.me.builders.CrossbowBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.utils.FzmmUtils;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FzmmItemGroup {
    public static final String OPERATOR_BASE_TRANSLATION_KEY = "itemGroup.op";
    public static final String USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY = "itemGroup.fzmm.useful_block_states";
    public static final Identifier USEFUL_BLOCK_STATES_IDENTIFIER = Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "useful_block_states");
    public static final Identifier LOOT_CHESTS_IDENTIFIER = Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "loot_chests");

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.OP_BLOCKS).register(entries -> {
            if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;
            RegistryAccess registryManager = Minecraft.getInstance().player.registryAccess();
            ArrayList<ItemStack> newEntries = new ArrayList<>();

            newEntries.add(new ItemStack(Items.FILLED_MAP));
            newEntries.add(new ItemStack(Items.WRITTEN_BOOK));
            newEntries.add(new ItemStack(Items.ENCHANTED_BOOK));
            newEntries.add(Items.KNOWLEDGE_BOOK.getDefaultInstance());
            newEntries.add(new ItemStack(Items.SUSPICIOUS_STEW));
            newEntries.add(new ItemStack(Items.POTION));
            newEntries.add(new ItemStack(Items.SPLASH_POTION));
            newEntries.add(new ItemStack(Items.LINGERING_POTION));
            newEntries.add(new ItemStack(Items.TIPPED_ARROW));
            newEntries.add(Items.DRAGON_EGG.getDefaultInstance());
            newEntries.add(Items.PETRIFIED_OAK_SLAB.getDefaultInstance());

            addSpawnEggs(newEntries);
            addArmorStand(newEntries);
            addItemFrames(newEntries);
            addNameTags(newEntries);
            addCrossbows(newEntries);
            Optional<Registry<BannerPattern>> patternRegistry = registryManager.lookup(Registries.BANNER_PATTERN);
            if (patternRegistry.isPresent()) {
                newEntries.add(Raid.getOminousBannerInstance(patternRegistry.get()));
            } else {
                FzmmClient.LOGGER.warn("[FzmmItemGroup] Failed to add OminousBanner");
            }

            ItemStack elytra = new ItemStack(Items.ELYTRA);
            elytra.setDamageValue(elytra.getMaxDamage() - 1);
            newEntries.add(elytra);

            entries.insertAfter(Items.DEBUG_STICK, newEntries);
        });
        // TODO: this need be sorted/organized
        CreativeModeTab usefulBlockStatesItemGroup = FabricCreativeModeTab.builder()
                .title(Component.translatable(USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY))
                .icon(() -> new ItemStack(Items.REDSTONE_LAMP))
                .displayItems((displayContext, entries) -> {

                    entries.accept(new BlockStateItemBuilder(Items.REDSTONE_LAMP, "litRedstoneLamp").add("lit", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.FURNACE, "litFurnace").add("lit", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.SMOKER, "litSmoker").add("lit", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.BLAST_FURNACE, "litBlastFurnace").add("lit", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.CAMPFIRE, "offCampfire").add("lit", false).get());
                    entries.accept(new BlockStateItemBuilder(Items.CAMPFIRE, "signalFireOfCampfire").add("signal_fire", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.SOUL_CAMPFIRE, "offSoulCampfire").add("lit", false).get());
                    entries.accept(new BlockStateItemBuilder(Items.SOUL_CAMPFIRE, "signalFireOfSoulCampfire").add("signal_fire", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.GRASS_BLOCK, "snowyGrassBlock").add("snowy", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.MYCELIUM, "snowyMycelium").add("snowy", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.PODZOL, "snowyPodzol").add("snowy", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.SNOW, "snowBlock").add("layers", 8).get());
                    entries.accept(new BlockStateItemBuilder(Items.BARREL, "openBarrel").add("open", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.IRON_TRAPDOOR, "openIronTrapdoor").add("open", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.IRON_DOOR, "openIronDoor").add("open", true).get());
                    addItemTag(ItemTags.WOODEN_SHELVES, item -> entries.accept(new BlockStateItemBuilder(item, "poweredShelf", item).add("powered", true).get()));
                    entries.accept(new BlockStateItemBuilder(Items.END_PORTAL_FRAME, "endPortalFrameWithEye").add("eye", true).get());
                    addItemTag(BlockItemTags.LANTERNS.item(), item -> {
                        entries.accept(new BlockStateItemBuilder(item, "hangingLantern", item).add("hanging", true).get());
                        entries.accept(new BlockStateItemBuilder(item, "lanternOnTheFloor", item).add("hanging", false).get());
                    });
                    entries.accept(new BlockStateItemBuilder(Items.MANGROVE_PROPAGULE, "hangingMangrovePropagule").add("hanging", true).get());
                    // it is not possible to place it on faces of blocks other than the bottom one, it is useless
//                    stacks.add(new BlockStateTagItem(Items.MANGROVE_PROPAGULE, "Mangrove propagule on the floor").add("hanging", false).get());
                    entries.accept(new BlockStateItemBuilder(Items.COMPOSTER, "fullComposter").add("level", 8).get());
                    entries.accept(new BlockStateItemBuilder(Items.RESPAWN_ANCHOR, "fullRespawnAnchor").add("charges", 4).get());
                    entries.accept(new BlockStateItemBuilder(Items.BAMBOO, "bambooWithLeaves").add("leaves", "large").get());
                    entries.accept(new BlockStateItemBuilder(Items.WHEAT_SEEDS, "fullGrownWheat").add("age", 7).get());
                    entries.accept(new BlockStateItemBuilder(Items.PUMPKIN_SEEDS, "fullGrownPumpkin").add("age", 7).get());
                    entries.accept(new BlockStateItemBuilder(Items.MELON_SEEDS, "fullGrownMelon").add("age", 7).get());
                    entries.accept(new BlockStateItemBuilder(Items.CARROT, "fullGrownCarrot").add("age", 7).get());
                    entries.accept(new BlockStateItemBuilder(Items.POTATO, "fullGrownPotatoes").add("age", 7).get());
                    entries.accept(new BlockStateItemBuilder(Items.BEETROOT_SEEDS, "fullGrownBeetroots").add("age", 3).get());
                    entries.accept(new BlockStateItemBuilder(Items.COCOA_BEANS, "fullGrownCocoa").add("age", 2).get());
                    entries.accept(new BlockStateItemBuilder(Items.GLOW_BERRIES, "glowBerries").add("berries", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.REPEATER, "repeaterTicks.2").add("delay", 2).get());
                    entries.accept(new BlockStateItemBuilder(Items.REPEATER, "repeaterTicks.3").add("delay", 3).get());
                    entries.accept(new BlockStateItemBuilder(Items.REPEATER, "repeaterTicks.4").add("delay", 4).get());
                    entries.accept(new BlockStateItemBuilder(Items.REPEATER, "lockedRepeater").add("locked", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.HOPPER, "disabledHopper").add("enabled", false).get());
                    entries.accept(new BlockStateItemBuilder(Items.BEE_NEST, "beeNestFilledWithHoney").add("honey_level", 5).get());
                    entries.accept(new BlockStateItemBuilder(Items.BEEHIVE, "beehiveFilledWithHoney").add("honey_level", 5).get());
                    entries.accept(new BlockStateItemBuilder(Items.SEA_PICKLE, "seaPickle4").add("pickles", 4).get());
                    entries.accept(new BlockStateItemBuilder(Items.TURTLE_EGG, "turtleEgg4").add("eggs", 4).get());
                    entries.accept(new BlockStateItemBuilder(Items.CAKE, "sliceOfCake").add("bites", 6).get());
                    entries.accept(new BlockStateItemBuilder(Items.TNT, "unstableTnt").add("unstable", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.REDSTONE, "poweredRedstone").add("power", 15).get());
                    entries.accept(new BlockStateItemBuilder(Items.SCULK_CATALYST, "sculkCatalystBloom").add("bloom", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.SCULK_SHRIEKER, "sculkShriekerCanSummon").add("can_summon", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.SCULK_SHRIEKER, "sculkShriekerLocked").add("shrieking", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.GLOW_LICHEN, "glowLichenBlock").add("down", true).add("east", true).add("north", true).add("south", true).add("up", true).add("west", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.SCULK_VEIN, "sculkVeinBlock").add("down", true).add("east", true).add("north", true).add("south", true).add("up", true).add("west", true).get());
                    entries.accept(new BlockStateItemBuilder(Items.CHEST, "leftChest").add("type", "left").get());
                    entries.accept(new BlockStateItemBuilder(Items.CHEST, "rightChest").add("type", "right").get());
                    entries.accept(new BlockStateItemBuilder(Items.TRAPPED_CHEST, "leftTrappedChest").add("type", "left").get());
                    entries.accept(new BlockStateItemBuilder(Items.TRAPPED_CHEST, "rightTrappedChest").add("type", "right").get());
                    addItemTag(BlockItemTags.DOORS.item(), item -> addHalfUpper(entries, item, "halfDoor"));
                    addTallFlowers(entries);
                    addItemTag(ItemTags.LEAVES, item -> entries.accept(new BlockStateItemBuilder(item, "nonPersistentLeaves", item).add("persistent", false).get()));
                    addItemTag(ItemTags.CANDLES, item -> entries.accept(new BlockStateItemBuilder(item, "litCandle", item).add("lit", true).get()));
                    addItemTag(ItemTags.BEDS, item -> entries.accept(new BlockStateItemBuilder(item, "bedHeadPart", item).add("part", "head").get()));
                    addItemTag(ItemTags.BEDS, item -> entries.accept(new BlockStateItemBuilder(item, "lockedBed", item).add("occupied", true).get()));
                    entries.accept(new BlockStateItemBuilder(Items.MANGROVE_ROOTS, "waterloggedMangroveRoots").add("waterlogged", true).get());
                    addItemTag(BlockItemTags.SLABS.item(), item -> entries.accept(new BlockStateItemBuilder(item, "waterloggedBlock", item).add("type", "double").add("waterlogged", true).get()));
                }).build();

        CreativeModeTab lootChestsItemGroup = FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.fzmm.loot_chests"))
                .icon(() -> new ItemStack(Items.CHEST))
                .displayItems((displayContext, entries) -> {
                    List<ResourceKey<LootTable>> lootTablesPath = BuiltInLootTables.all().stream()
                            .sorted(Comparator.comparing(t -> t.identifier().getPath()))
                            .collect(Collectors.toList());

                    List<ResourceKey<LootTable>> archeologyLootTablesPath = BuiltInLootTables.all().stream()
                            .sorted(Comparator.comparing(t -> t.identifier().getPath()))
                            .collect(Collectors.toList());

                    archeologyLootTablesPath.removeIf(lootTable -> !lootTable.identifier().getPath().startsWith("archaeology"));

                    lootTablesPath.removeIf(lootTable -> lootTable.identifier().getPath().startsWith("entities"));
                    lootTablesPath.removeIf(archeologyLootTablesPath::contains);

                    addLootChest(entries, Items.SUSPICIOUS_SAND, archeologyLootTablesPath, true);
                    addLootChest(entries, Items.SUSPICIOUS_GRAVEL, archeologyLootTablesPath, true);
                    addLootChest(entries, Items.CHEST, lootTablesPath, false);
                }).build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, USEFUL_BLOCK_STATES_IDENTIFIER, usefulBlockStatesItemGroup);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, LOOT_CHESTS_IDENTIFIER, lootChestsItemGroup);
    }

    public static void populateItemGroups() {
        // since 1.21 the item groups and the search bar are initialized from the CreativeInventory constructor,
        // not initializing it here will cause that searching in the item groups will have no results if it was
        // initialized for the first time with ItemGroups#updateDisplayContext
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        new CreativeModeInventoryScreen(player, player.connection.enabledFeatures(), true);
    }

    private static void addSpawnEggs(List<ItemStack> entries) {
        Predicate<ItemStack> hasGroup = stack -> {
            for (var group : BuiltInRegistries.CREATIVE_MODE_TAB) {
                if (group.contains(stack)) {
                    return true;
                }
            }
            return false;
        };
        for (var item : BuiltInRegistries.ITEM) {
            if (item instanceof SpawnEggItem && !hasGroup.test(item.getDefaultInstance())) {
                entries.add(item.getDefaultInstance());
            }
        }
    }

    private static void addArmorStand(List<ItemStack> entries) {
        String baseTranslation = "armorStand.";
        ItemStack armorStandWithArms = ArmorStandBuilder.builder()
                .setShowArms()
                .getItem(FzmmUtils.disableItalicConfig(Component.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + "arms"), true));
        entries.add(armorStandWithArms);

        ItemStack smallArmorStand = ArmorStandBuilder.builder()
                .setSmall()
                .getItem(FzmmUtils.disableItalicConfig(Component.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + "small"), true));
        entries.add(smallArmorStand);

        ItemStack smallArmorStandWithArms = ArmorStandBuilder.builder()
                .setSmall()
                .setShowArms()
                .getItem(FzmmUtils.disableItalicConfig(Component.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + "smallWithArms"), true));
        entries.add(smallArmorStandWithArms);
    }

    private static void addItemFrames(List<ItemStack> entries) {
        ItemStack itemFrame = new ItemStack(Items.ITEM_FRAME);
        ItemStack glowItemFrame = new ItemStack(Items.GLOW_ITEM_FRAME);
        CompoundTag entityTag = new CompoundTag();
        entityTag.putBoolean("Invisible", true);

        itemFrame.update(DataComponents.ENTITY_DATA, null, entityData -> {
            CompoundTag result = entityTag.copy();
            return TypedEntityData.of(EntityTypes.ITEM_FRAME, result);
        });
        glowItemFrame.update(DataComponents.ENTITY_DATA, null, entityData -> {
            CompoundTag result = entityTag.copy();
            return TypedEntityData.of(EntityTypes.ITEM_FRAME, result);
        });

        itemFrame.update(DataComponents.CUSTOM_NAME, null, component -> {
            Component translation = Component.translatable(OPERATOR_BASE_TRANSLATION_KEY + ".invisibleItemFrame");
            return FzmmUtils.disableItalicConfig(translation.getString(), true);
        });
        glowItemFrame.update(DataComponents.CUSTOM_NAME, null, component -> {
            Component translation = Component.translatable(OPERATOR_BASE_TRANSLATION_KEY + ".invisibleGlowItemFrame");
            return FzmmUtils.disableItalicConfig(translation.getString(), true);
        });

        entries.add(itemFrame);
        entries.add(glowItemFrame);
    }

    private static void addNameTags(List<ItemStack> entries) {
        final int LORE_COLOR = 0x1ecbe1;

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("")
                .addLore(getNameTagTranslation("empty", 1), LORE_COLOR).get());

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("Dinnerbone")
                .addLore(getNameTagTranslation("dinnerbone", 1), LORE_COLOR).get());

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("Grumm")
                .addLore(getNameTagTranslation("grumm", 1), LORE_COLOR).get());

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("Toast")
                .addLore(getNameTagTranslation("toast", 1), LORE_COLOR)
                .addLore(getNameTagTranslation("toast", 2), LORE_COLOR).get());

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("jeb_")
                .addLore(getNameTagTranslation("jeb_", 1), LORE_COLOR)
                .addLore(getNameTagTranslation("jeb_", 2), LORE_COLOR).get());

        entries.add(DisplayBuilder.builder().item(Items.NAME_TAG).setName("Johnny")
                .addLore(getNameTagTranslation("johnny", 1), LORE_COLOR)
                .addLore(getNameTagTranslation("johnny", 2), LORE_COLOR)
                .addLore(getNameTagTranslation("johnny", 3), LORE_COLOR).get());
    }

    private static String getNameTagTranslation(String value, int line) {
        String baseTranslation = "nameTag.";
        String commentTranslation = ".comment.";

        return Component.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + value + commentTranslation + line).getString();
    }

    private static void addCrossbows(List<ItemStack> entries) {
        CrossbowBuilder crossbowArrow = CrossbowBuilder.builder().putProjectile(new ItemStack(Items.ARROW));

        entries.add(crossbowArrow.get());

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);

        firework.update(DataComponents.FIREWORKS, null,
                component -> new Fireworks(2, new ArrayList<>())
        );

        CrossbowBuilder crossbowFirework = CrossbowBuilder.builder().putProjectile(firework);

        entries.add(crossbowFirework.get());
    }

    private static void addTallFlowers(CreativeModeTab.Output entries) {
        String suffix = "tallFlowerSelfDestructs";
        // TallFlowerBlock
        addHalfUpper(entries, Items.SUNFLOWER, suffix);
        addHalfUpper(entries, Items.LILAC, suffix);
        addHalfUpper(entries, Items.ROSE_BUSH, suffix);
        addHalfUpper(entries, Items.PEONY, suffix);
        // TallPlantBlock
        addHalfUpper(entries, Items.TALL_GRASS, suffix);
        addHalfUpper(entries, Items.LARGE_FERN, suffix);
        addHalfUpper(entries, Items.PITCHER_PLANT, suffix);
        // SmallDripleafBlock
        addHalfUpper(entries, Items.SMALL_DRIPLEAF, suffix);
    }

    private static void addHalfUpper(CreativeModeTab.Output entries, Item item, String translation) {
        entries.accept(new BlockStateItemBuilder(item, translation, item).add("half", "upper").get());
    }

    private static void addItemTag(TagKey<Item> tag, Consumer<Item> consumer) {
        Optional<ItemPredicate> predicate = itemPredicate(tag);
        if (predicate.isEmpty()) return;

        for (var item : BuiltInRegistries.ITEM) {
            if (predicate.get().test(new ItemStack(item))) {
                consumer.accept(item);
            }
        }
    }

    private static Optional<ItemPredicate> itemPredicate(TagKey<Item> tag) {
        try {
            return Optional.of(ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, tag).build());
        } catch (Exception ignored) {
            FzmmClient.LOGGER.warn("[FzmmItemGroup] Missing tag '{}' (this can be ignored in multiplayer)", tag.location());
            return Optional.empty();
        }
    }

    private static void addLootChest(CreativeModeTab.Output entries, Item item, List<ResourceKey<LootTable>> lootTableList, boolean isBrushable) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;
        for (var lootTable : lootTableList) {
            ItemStack stack = new ItemStack(item);

            String identifierString = lootTable.identifier().toString();

            // Brushable blocks (suspicious sand and suspicious gravel) do not use the
            // container_loot component like other lootable blocks in 1.20.5
            // https://bugs.mojang.com/browse/MC-271530
            if (isBrushable) {
                stack.update(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BlockEntityTypes.BRUSHABLE_BLOCK, new CompoundTag()), entityData -> {
                    CompoundTag result = entityData.copyTagWithoutId();

                    result.putString("LootTable", identifierString);

                    return TypedEntityData.of(BlockEntityTypes.BRUSHABLE_BLOCK, result);
                });
            } else {
                stack.update(DataComponents.CONTAINER_LOOT, null,
                        containerLootComponent -> new SeededContainerLoot(lootTable, 0));
            }

            stack.update(DataComponents.CUSTOM_NAME, null, text -> Component.literal(identifierString));

            entries.accept(stack);
        }
    }
}
