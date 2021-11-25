package fzmm.zailer.me.client;

import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.utils.*;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.raid.Raid;

import java.util.List;
import java.util.stream.Collectors;

public class FzmmItemGroup {

    public static void register() {

        FabricItemGroupBuilder.create(new Identifier("fzmm", "unobtainable_items"))
                .icon(() -> new ItemStack(Items.JIGSAW))
                .appendItems((stacks, itemGroup) -> {
                    for (Item item : Registry.ITEM) {
                        if (item.getGroup() == null && item != Items.AIR && item != Items.LIGHT) {
                            stacks.add(new ItemStack(item));
                        }
                    }
                    stacks.add(new ItemStack(Items.POTION));
                    stacks.add(new ItemStack(Items.SPLASH_POTION));
                    stacks.add(new ItemStack(Items.LINGERING_POTION));
                    stacks.add(new ItemStack(Items.TIPPED_ARROW));

                    addArmorStand(stacks);
                    addItemFrames(stacks);
                    addNameTags(stacks);
                    addCrossbows(stacks);
                    stacks.add(Raid.getOminousBanner());

                    ItemStack elytra = new ItemStack(Items.ELYTRA);
                    elytra.setDamage(431);
                    stacks.add(elytra);

                    addLightBlock(stacks);

                }).build();

        FabricItemGroupBuilder.create(new Identifier("fzmm", "useful_block_states"))
                .icon(() -> new ItemStack(Items.REDSTONE_LAMP))
                .appendItems((stacks, itemGroup) -> {

                    stacks.add(new BlockStateTagItem(Items.REDSTONE_LAMP, "Lit redstone lamp").add("lit", true).get());
                    stacks.add(new BlockStateTagItem(Items.FURNACE, "Lit furnace").add("lit", true).get());
                    stacks.add(new BlockStateTagItem(Items.SMOKER, "Lit smoker").add("lit", true).get());
                    stacks.add(new BlockStateTagItem(Items.BLAST_FURNACE, "Lit blast furnace").add("lit", true).get());
                    stacks.add(new BlockStateTagItem(Items.CAMPFIRE, "Off campfire").add("lit", false).get());
                    stacks.add(new BlockStateTagItem(Items.CAMPFIRE, "Signal fire of campfire").add("signal_fire", true).get());
                    stacks.add(new BlockStateTagItem(Items.SOUL_CAMPFIRE, "Off soul campfire").add("lit", false).get());
                    stacks.add(new BlockStateTagItem(Items.SOUL_CAMPFIRE, "Signal fire of soul campfire").add("signal_fire", true).get());
                    stacks.add(new BlockStateTagItem(Items.GRASS_BLOCK, "Snowy grass block").add("snowy", true).get());
                    stacks.add(new BlockStateTagItem(Items.MYCELIUM, "Snowy mycelium").add("snowy", true).get());
                    stacks.add(new BlockStateTagItem(Items.PODZOL, "Snowy podzol").add("snowy", true).get());
                    stacks.add(new BlockStateTagItem(Items.BARREL, "Open barrel").add("open", true).get());
                    stacks.add(new BlockStateTagItem(Items.IRON_TRAPDOOR, "Open iron trapdoor").add("open", true).get());
                    stacks.add(new BlockStateTagItem(Items.IRON_DOOR, "Open iron door").add("open", true).get());
                    stacks.add(new BlockStateTagItem(Items.END_PORTAL_FRAME, "End portal frame with eye").add("eye", true).get());
                    stacks.add(new BlockStateTagItem(Items.LANTERN, "Hanging lantern").add("hanging", true).get());
                    stacks.add(new BlockStateTagItem(Items.LANTERN, "Lantern on the floor").add("hanging", false).get());
                    stacks.add(new BlockStateTagItem(Items.SOUL_LANTERN, "Hanging soul lantern").add("hanging", true).get());
                    stacks.add(new BlockStateTagItem(Items.SOUL_LANTERN, "Soul lantern on the floor").add("hanging", false).get());
                    stacks.add(new BlockStateTagItem(Items.COMPOSTER, "Full composter").add("level", 8).get());
                    stacks.add(new BlockStateTagItem(Items.RESPAWN_ANCHOR, "Full respawn anchor").add("charges", 4).get());
                    stacks.add(new BlockStateTagItem(Items.WHEAT_SEEDS, "Full grown wheat").add("age", 7).get());
                    stacks.add(new BlockStateTagItem(Items.PUMPKIN_SEEDS, "Full grown pumpkin").add("age", 7).get());
                    stacks.add(new BlockStateTagItem(Items.MELON_SEEDS, "Full grown melon").add("age", 7).get());
                    stacks.add(new BlockStateTagItem(Items.CARROT, "Full grown carrot").add("age", 7).get());
                    stacks.add(new BlockStateTagItem(Items.POTATO, "Full grown potatoes").add("age", 7).get());
                    stacks.add(new BlockStateTagItem(Items.BEETROOT_SEEDS, "Full grown beetroots").add("age", 3).get());
                    stacks.add(new BlockStateTagItem(Items.COCOA_BEANS, "Full grown cocoa").add("age", 2).get());
                    stacks.add(new BlockStateTagItem(Items.REPEATER, "Repeater (2 ticks)").add("delay", 2).get());
                    stacks.add(new BlockStateTagItem(Items.REPEATER, "Repeater (3 ticks)").add("delay", 3).get());
                    stacks.add(new BlockStateTagItem(Items.REPEATER, "Repeater (4 ticks)").add("delay", 4).get());
                    stacks.add(new BlockStateTagItem(Items.BEE_NEST, "Bee nest filled with honey").add("honey_level", 5).get());
                    stacks.add(new BlockStateTagItem(Items.BEEHIVE, "Beehive filled with honey").add("honey_level", 5).get());
                    stacks.add(new BlockStateTagItem(Items.SEA_PICKLE, "Sea pickle (4)").add("pickles", 4).get());
                    stacks.add(new BlockStateTagItem(Items.TURTLE_EGG, "Turtle egg (4)").add("eggs", 4).get());
                    stacks.add(new BlockStateTagItem(Items.CAKE, "A slice of cake").add("bites", 6).get());
                    stacks.add(new BlockStateTagItem(Items.REDSTONE, "Powered redstone").add("power", 15).get());
                    addLeaves(stacks);
                    addLitCandles(stacks);
                }).build();

        FabricItemGroupBuilder.create(new Identifier("fzmm", "loot_chests"))
                .icon(() -> new ItemStack(Items.CHEST))
                .appendItems((stacks, itemGroup) -> {
                    List<String> lootTablesPath = LootTables.getAll().stream()
                            .map(Identifier::getPath)
                            .sorted()
                            .collect(Collectors.toList());

                    for (String path : lootTablesPath) {
                        if (path.startsWith("entities"))
                            continue;

                        ItemStack chest = new ItemStack(Items.CHEST);
                        NbtCompound blockEntityTag = new NbtCompound();

                        blockEntityTag.putString("LootTable", path);

                        chest.setCustomName(new LiteralText("LootChest: " + path));
                        chest.setSubNbt(TagsConstant.BLOCK_ENTITY, blockEntityTag);
                        stacks.add(chest);
                    }
                }).build();

        FabricItemGroupBuilder.create(new Identifier("fzmm", "player_heads"))
                .icon(() -> new ItemStack(Items.PLAYER_HEAD))
                .appendItems((stacks, itemGroup) -> {
                    stacks.clear();

                    ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
                    if (clientPlayer != null) {
                        List<GameProfile> profileList = clientPlayer.networkHandler.getPlayerList().stream()
                                .map(PlayerListEntry::getProfile)
                                .collect(Collectors.toList());

                        for (GameProfile profile : profileList) {
                            stacks.add(FzmmUtils.getPlayerHead(profile));
                        }
                    }

                }).build();

    }

    private static void addArmorStand(List<ItemStack> stacks) {
        ItemStack armorStandWithArms = new ItemStack(Items.ARMOR_STAND);
        armorStandWithArms.setNbt(new ArmorStandUtils().setShowArms().getItemNbt("Armor stand with arms"));
        stacks.add(armorStandWithArms);

        ItemStack smallArmorStand = new ItemStack(Items.ARMOR_STAND);
        smallArmorStand.setNbt(new ArmorStandUtils().setSmall().getItemNbt("Small armor stand"));
        stacks.add(smallArmorStand);

        ItemStack smallArmorStandWithArms = new ItemStack(Items.ARMOR_STAND);
        smallArmorStandWithArms.setNbt(new ArmorStandUtils().setSmall().setShowArms().getItemNbt("Small armor stand with arms"));
        stacks.add(smallArmorStandWithArms);
    }

    private static void addItemFrames(List<ItemStack> stacks) {
        ItemStack itemFrame = new ItemStack(Items.ITEM_FRAME);
        ItemStack glowItemFrame = new ItemStack(Items.GLOW_ITEM_FRAME);
        NbtCompound entityTag = new NbtCompound();

        entityTag.putBoolean("Invisible", true);
        itemFrame.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);
        glowItemFrame.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);

        itemFrame.setCustomName(new LiteralText("Invisible item frame").setStyle(Style.EMPTY.withItalic(false)));
        glowItemFrame.setCustomName(new LiteralText("Invisible glow item frame").setStyle(Style.EMPTY.withItalic(false)));

        stacks.add(itemFrame);
        stacks.add(glowItemFrame);
    }

    private static void addNameTags(List<ItemStack> stacks) {
        ItemStack emptyNameTag = new ItemStack(Items.NAME_TAG),
                dinnerbone = new ItemStack(Items.NAME_TAG),
                grumm = new ItemStack(Items.NAME_TAG),
                toast = new ItemStack(Items.NAME_TAG),
                jeb_ = new ItemStack(Items.NAME_TAG),
                johnny = new ItemStack(Items.NAME_TAG);
        final int LORE_COLOR = 0x1ecbe1;

        emptyNameTag.setCustomName(new LiteralText(""));
        LoreUtils.addLore(emptyNameTag, "Empty name tag", LORE_COLOR);
        stacks.add(emptyNameTag);

        dinnerbone.setCustomName(new LiteralText("Dinnerbone"));
        LoreUtils.addLore(dinnerbone, "Any mob to receive this name is rendered upside down.", LORE_COLOR);
        stacks.add(dinnerbone);

        grumm.setCustomName(new LiteralText("Grumm"));
        LoreUtils.addLore(grumm, "Any mob to receive this name is rendered upside down.", LORE_COLOR);
        stacks.add(grumm);

        toast.setCustomName(new LiteralText("Toast"));
        LoreUtils.addLore(toast, "Naming a rabbit \"Toast\" causes it to have a special memorial", LORE_COLOR);
        LoreUtils.addLore(toast, "skin of user xyzen420's girlfriend's missing rabbit.", LORE_COLOR);
        stacks.add(toast);

        jeb_.setCustomName(new LiteralText("jeb_"));
        LoreUtils.addLore(jeb_, "Naming a sheep \"jeb_\" causes its wool to fade between", LORE_COLOR);
        LoreUtils.addLore(jeb_, "the dye colors, producing a rainbow effect.", LORE_COLOR);
        stacks.add(jeb_);

        johnny.setCustomName(new LiteralText("Johnny"));
        LoreUtils.addLore(johnny, "Naming a vindicator \"Johnny\" causes it to be aggressive and attack", LORE_COLOR);
        LoreUtils.addLore(johnny, " all mobs including the wither (except ghasts and other illagers).", LORE_COLOR);
        stacks.add(johnny);
    }

    private static void addCrossbows(List<ItemStack> stacks) {
        CrossbowUtils crossbowArrow = new CrossbowUtils();
        CrossbowUtils crossbowFirework = new CrossbowUtils();
        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        NbtCompound fireworkTag = new NbtCompound();

        stacks.add(crossbowArrow.setCharged(true).putProjectile(new ItemStack(Items.ARROW)).get());

        fireworkTag.putInt(FireworkRocketItem.FLIGHT_KEY, 2);
        firework.setSubNbt(FireworkRocketItem.FIREWORKS_KEY, fireworkTag);
        stacks.add(crossbowFirework.setCharged(true).putProjectile(firework).get());

    }

    private static void addLightBlock(List<ItemStack> stacks) {
        for (int i = 0; i != 16; i++) {
            stacks.add(new BlockStateTagItem(Items.LIGHT).add("level", String.valueOf(i)).get());
        }
    }

    private static void addLeaves(List<ItemStack> stacks) {
        for (Item item : Registry.ITEM) {
            if (item.toString().contains("leave"))
                stacks.add(new BlockStateTagItem(item, item.getName().getString() + " (persistent: false)").add("persistent", false).get());
        }
    }

    private static void addLitCandles(List<ItemStack> stacks) {
        for (Item item : Registry.ITEM) {
            if (item.toString().contains("candle"))
                stacks.add(new BlockStateTagItem(item, item.getName().getString() + " (lit)").add("lit", true).get());
        }
    }
}