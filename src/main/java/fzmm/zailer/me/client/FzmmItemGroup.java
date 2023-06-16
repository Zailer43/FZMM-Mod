package fzmm.zailer.me.client;

import fzmm.zailer.me.builders.ArmorStandBuilder;
import fzmm.zailer.me.builders.BlockStateItemBuilder;
import fzmm.zailer.me.builders.CrossbowBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.utils.TagsConstant;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.raid.Raid;

import java.util.ArrayList;
import java.util.List;

public class FzmmItemGroup {
    public static final String OPERATOR_BASE_TRANSLATION_KEY = "itemGroup.op";
    public static final String USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY = "itemGroup.fzmm.useful_block_states";
    public static final Identifier USEFUL_BLOCK_STATES_IDENTIFIER = new Identifier(FzmmClient.MOD_ID, "useful_block_states");
    public static final Identifier LOOT_CHESTS_IDENTIFIER = new Identifier(FzmmClient.MOD_ID, "loot_chests");

    @SuppressWarnings("UnstableApiUsage")
    public static void register() {

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            ArrayList<ItemStack> newEntries = new ArrayList<>();

            newEntries.add(Items.DRAGON_EGG.getDefaultStack());
            newEntries.add(Items.FILLED_MAP.getDefaultStack());
            newEntries.add(Items.WRITTEN_BOOK.getDefaultStack());
            newEntries.add(Items.ENCHANTED_BOOK.getDefaultStack());
            newEntries.add(Items.KNOWLEDGE_BOOK.getDefaultStack());
            newEntries.add(Items.SUSPICIOUS_STEW.getDefaultStack());
            newEntries.add(Items.POTION.getDefaultStack());
            newEntries.add(Items.SPLASH_POTION.getDefaultStack());
            newEntries.add(Items.LINGERING_POTION.getDefaultStack());
            newEntries.add(Items.TIPPED_ARROW.getDefaultStack());

            addArmorStand(newEntries);
            addItemFrames(newEntries);
            addNameTags(newEntries);
            addCrossbows(newEntries);
            newEntries.add(Raid.getOminousBanner());

            ItemStack elytra = new ItemStack(Items.ELYTRA);
            elytra.setDamage(elytra.getMaxDamage() - 1);
            newEntries.add(elytra);

            entries.addAfter(Items.DEBUG_STICK, newEntries);
        });

        ItemGroup usefulBlockStatesItemGroup = FabricItemGroup.builder()
                .displayName(Text.translatable(USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY))
                .icon(() -> new ItemStack(Items.REDSTONE_LAMP))
                .entries((displayContext, entries) -> {

                    entries.add(new BlockStateItemBuilder(Items.REDSTONE_LAMP, "litRedstoneLamp").add("lit", true).get());
                    entries.add(new BlockStateItemBuilder(Items.FURNACE, "litFurnace").add("lit", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SMOKER, "litSmoker").add("lit", true).get());
                    entries.add(new BlockStateItemBuilder(Items.BLAST_FURNACE, "litBlastFurnace").add("lit", true).get());
                    entries.add(new BlockStateItemBuilder(Items.CAMPFIRE, "offCampfire").add("lit", false).get());
                    entries.add(new BlockStateItemBuilder(Items.CAMPFIRE, "signalFireOfCampfire").add("signal_fire", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SOUL_CAMPFIRE, "offSoulCampfire").add("lit", false).get());
                    entries.add(new BlockStateItemBuilder(Items.SOUL_CAMPFIRE, "signalFireOfSoulCampfire").add("signal_fire", true).get());
                    entries.add(new BlockStateItemBuilder(Items.GRASS_BLOCK, "snowyGrassBlock").add("snowy", true).get());
                    entries.add(new BlockStateItemBuilder(Items.MYCELIUM, "snowyMycelium").add("snowy", true).get());
                    entries.add(new BlockStateItemBuilder(Items.PODZOL, "snowyPodzol").add("snowy", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SNOW, "snowBlock").add("layers", 8).get());
                    entries.add(new BlockStateItemBuilder(Items.BARREL, "openBarrel").add("open", true).get());
                    entries.add(new BlockStateItemBuilder(Items.IRON_TRAPDOOR, "openIronTrapdoor").add("open", true).get());
                    entries.add(new BlockStateItemBuilder(Items.IRON_DOOR, "openIronDoor").add("open", true).get());
                    entries.add(new BlockStateItemBuilder(Items.END_PORTAL_FRAME, "endPortalFrameWithEye").add("eye", true).get());
                    entries.add(new BlockStateItemBuilder(Items.LANTERN, "hangingLantern").add("hanging", true).get());
                    entries.add(new BlockStateItemBuilder(Items.LANTERN, "lanternOnTheFloor").add("hanging", false).get());
                    entries.add(new BlockStateItemBuilder(Items.SOUL_LANTERN, "hangingSoulLantern").add("hanging", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SOUL_LANTERN, "soulLanternOnTheFloor").add("hanging", false).get());
                    entries.add(new BlockStateItemBuilder(Items.MANGROVE_PROPAGULE, "hangingMangrovePropagule").add("hanging", true).get());
                    // it is not possible to place it on faces of blocks other than the bottom one, it is useless
//                    stacks.add(new BlockStateTagItem(Items.MANGROVE_PROPAGULE, "Mangrove propagule on the floor").add("hanging", false).get());
                    entries.add(new BlockStateItemBuilder(Items.COMPOSTER, "fullComposter").add("level", 8).get());
                    entries.add(new BlockStateItemBuilder(Items.RESPAWN_ANCHOR, "fullRespawnAnchor").add("charges", 4).get());
                    entries.add(new BlockStateItemBuilder(Items.BAMBOO, "bambooWithLeaves").add("leaves", "large").get());
                    entries.add(new BlockStateItemBuilder(Items.WHEAT_SEEDS, "fullGrownWheat").add("age", 7).get());
                    entries.add(new BlockStateItemBuilder(Items.PUMPKIN_SEEDS, "fullGrownPumpkin").add("age", 7).get());
                    entries.add(new BlockStateItemBuilder(Items.MELON_SEEDS, "fullGrownMelon").add("age", 7).get());
                    entries.add(new BlockStateItemBuilder(Items.CARROT, "fullGrownCarrot").add("age", 7).get());
                    entries.add(new BlockStateItemBuilder(Items.POTATO, "fullGrownPotatoes").add("age", 7).get());
                    entries.add(new BlockStateItemBuilder(Items.BEETROOT_SEEDS, "fullGrownBeetroots").add("age", 3).get());
                    entries.add(new BlockStateItemBuilder(Items.COCOA_BEANS, "fullGrownCocoa").add("age", 2).get());
                    entries.add(new BlockStateItemBuilder(Items.GLOW_BERRIES, "glowBerries").add("berries", true).get());
                    entries.add(new BlockStateItemBuilder(Items.REPEATER, "repeaterTicks.2").add("delay", 2).get());
                    entries.add(new BlockStateItemBuilder(Items.REPEATER, "repeaterTicks.3").add("delay", 3).get());
                    entries.add(new BlockStateItemBuilder(Items.REPEATER, "repeaterTicks.4").add("delay", 4).get());
                    entries.add(new BlockStateItemBuilder(Items.REPEATER, "lockedRepeater").add("locked", true).get());
                    entries.add(new BlockStateItemBuilder(Items.HOPPER, "disabledHopper").add("enabled", false).get());
                    entries.add(new BlockStateItemBuilder(Items.BEE_NEST, "beeNestFilledWithHoney").add("honey_level", 5).get());
                    entries.add(new BlockStateItemBuilder(Items.BEEHIVE, "beehiveFilledWithHoney").add("honey_level", 5).get());
                    entries.add(new BlockStateItemBuilder(Items.SEA_PICKLE, "seaPickle4").add("pickles", 4).get());
                    entries.add(new BlockStateItemBuilder(Items.TURTLE_EGG, "turtleEgg4").add("eggs", 4).get());
                    entries.add(new BlockStateItemBuilder(Items.CAKE, "sliceOfCake").add("bites", 6).get());
                    entries.add(new BlockStateItemBuilder(Items.TNT, "unstableTnt").add("unstable", true).get());
                    entries.add(new BlockStateItemBuilder(Items.REDSTONE, "poweredRedstone").add("power", 15).get());
                    entries.add(new BlockStateItemBuilder(Items.SCULK_CATALYST, "sculkCatalystBloom").add("bloom", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SCULK_SHRIEKER, "sculkShriekerCanSummon").add("can_summon", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SCULK_SHRIEKER, "sculkShriekerLocked").add("shrieking", true).get());
                    entries.add(new BlockStateItemBuilder(Items.GLOW_LICHEN, "glowLichenBlock").add("down", true).add("east", true).add("north", true).add("south", true).add("up", true).add("west", true).get());
                    entries.add(new BlockStateItemBuilder(Items.SCULK_VEIN, "sculkVeinBlock").add("down", true).add("east", true).add("north", true).add("south", true).add("up", true).add("west", true).get());
                    entries.add(new BlockStateItemBuilder(Items.CHEST, "leftChest").add("type", "left").get());
                    entries.add(new BlockStateItemBuilder(Items.CHEST, "rightChest").add("type", "right").get());
                    entries.add(new BlockStateItemBuilder(Items.TRAPPED_CHEST, "leftTrappedChest").add("type", "left").get());
                    entries.add(new BlockStateItemBuilder(Items.TRAPPED_CHEST, "rightTrappedChest").add("type", "right").get());
                    addHalfDoors(entries);
                    addTallFlowers(entries);
                    addLeaves(entries);
                    addLitCandles(entries);
                    addHalfBed(entries);
                    addLockedBed(entries);
                    entries.add(new BlockStateItemBuilder(Items.MANGROVE_ROOTS, "waterloggedMangroveRoots").add("waterlogged", true).get());
                    addWaterloggedBlocks(entries);
                }).build();

        ItemGroup lootChestsItemGroup = FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.fzmm.loot_chests"))
                .icon(() -> new ItemStack(Items.CHEST))
                .entries((displayContext, entries) -> {
                    List<String> lootTablesPath = LootTables.getAll().stream()
                            .map(Identifier::getPath)
                            .sorted()
                            .toList();

                    for (String path : lootTablesPath) {
                        if (path.startsWith("entities"))
                            continue;

                        ItemStack chest = new ItemStack(Items.CHEST);
                        NbtCompound blockEntityTag = new NbtCompound();

                        blockEntityTag.putString("LootTable", path);

                        chest.setCustomName(Text.literal("LootChest: " + path));
                        chest.setSubNbt(TagsConstant.BLOCK_ENTITY, blockEntityTag);
                        entries.add(chest);
                    }
                }).build();

        Registry.register(Registries.ITEM_GROUP, USEFUL_BLOCK_STATES_IDENTIFIER, usefulBlockStatesItemGroup);
        Registry.register(Registries.ITEM_GROUP, LOOT_CHESTS_IDENTIFIER, lootChestsItemGroup);
    }

    private static void addArmorStand(List<ItemStack> entries) {
        String baseTranslation = "armorStand.";
        ItemStack armorStandWithArms = ArmorStandBuilder.builder()
                .setShowArms()
                .getItem(Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + "arms"));
        entries.add(armorStandWithArms);

        ItemStack smallArmorStand = ArmorStandBuilder.builder()
                .setSmall()
                .getItem(Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + "small"));
        entries.add(smallArmorStand);

        ItemStack smallArmorStandWithArms = ArmorStandBuilder.builder()
                .setSmall()
                .setShowArms()
                .getItem(Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + "smallWithArms"));
        entries.add(smallArmorStandWithArms);
    }

    private static void addItemFrames(List<ItemStack> entries) {
        ItemStack itemFrame = new ItemStack(Items.ITEM_FRAME);
        ItemStack glowItemFrame = new ItemStack(Items.GLOW_ITEM_FRAME);
        NbtCompound entityTag = new NbtCompound();

        entityTag.putBoolean("Invisible", true);
        itemFrame.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);
        glowItemFrame.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);

        String itemFrameName = Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + "invisibleItemFrame").getString();
        String glowItemFrameName = Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + "invisibleGlowItemFrame").getString();

        itemFrame.setCustomName(Text.literal(itemFrameName).setStyle(Style.EMPTY.withItalic(false)));
        glowItemFrame.setCustomName(Text.literal(glowItemFrameName).setStyle(Style.EMPTY.withItalic(false)));

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

        return Text.translatable(OPERATOR_BASE_TRANSLATION_KEY + "." + baseTranslation + value + commentTranslation + line).getString();
    }

    private static void addCrossbows(List<ItemStack> entries) {
        CrossbowBuilder crossbowArrow = CrossbowBuilder.builder()
                .setCharged(true)
                .putProjectile(new ItemStack(Items.ARROW));

        entries.add(crossbowArrow.get());

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);

        NbtCompound fireworkTag = new NbtCompound();
        fireworkTag.putInt(FireworkRocketItem.FLIGHT_KEY, 2);
        firework.setSubNbt(FireworkRocketItem.FIREWORKS_KEY, fireworkTag);

        CrossbowBuilder crossbowFirework = CrossbowBuilder.builder()
                .setCharged(true)
                .putProjectile(firework);

        entries.add(crossbowFirework.get());
    }

    private static void addLeaves(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.LEAVES))
                entries.add(new BlockStateItemBuilder(item, "nonPersistentLeaves", item).add("persistent", false).get());
        }
    }

    private static void addHalfDoors(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.DOORS))
                addHalfUpper(entries, item, "halfDoor");
        }
    }

    private static void addTallFlowers(ItemGroup.Entries entries) {
        String suffix = "tallFlowerSelfDestructs";
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.TALL_FLOWERS))
                addHalfUpper(entries, item, suffix);
        }
        addHalfUpper(entries, Items.TALL_GRASS, suffix);
        addHalfUpper(entries, Items.LARGE_FERN, suffix);
        addHalfUpper(entries, Items.SMALL_DRIPLEAF, suffix);
    }

    private static void addHalfUpper(ItemGroup.Entries entries, Item item, String translation) {
        entries.add(new BlockStateItemBuilder(item, translation, item).add("half", "upper").get());
    }

    private static void addLitCandles(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.CANDLES))
                entries.add(new BlockStateItemBuilder(item, "litCandle", item).add("lit", true).get());
        }
    }

    private static void addHalfBed(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.BEDS))
                entries.add(new BlockStateItemBuilder(item, "bedHeadPart", item).add("part", "head").get());
        }
    }

    private static void addLockedBed(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.BEDS))
                entries.add(new BlockStateItemBuilder(item, "lockedBed", item).add("occupied", true).get());
        }
    }

    private static void addWaterloggedBlocks(ItemGroup.Entries entries) {
        for (var item : Registries.ITEM) {
            if (contains(item, ItemTags.SLABS))
                entries.add(new BlockStateItemBuilder(item, "waterloggedBlock", item).add("type", "double").add("waterlogged", true).get());
        }
    }

    private static boolean contains(Item item, TagKey<Item> tag) {
        return ItemPredicate.Builder.create().tag(tag).build().test(new ItemStack(item));
    }
}
