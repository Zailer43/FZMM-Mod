package fzmm.zailer.me.builders;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class HeadBuilder {

    private String skinValue;
    @Nullable
    private String headName;
    @Nullable
    private String signature;
    private UUID uuid;
    private boolean addToHeadHistory;

    private HeadBuilder() {
        this.skinValue = "";
        this.headName = null;
        this.addToHeadHistory = true;
        this.uuid = UUID.randomUUID();
    }

    public static HeadBuilder builder() {
        return new HeadBuilder();
    }

    public ItemStack get() {
        ItemStack stack = Items.PLAYER_HEAD.getDefaultInstance();

        stack.update(DataComponents.PROFILE, null, component -> {

            Multimap<String, Property> properties = ImmutableMultimap.of("textures", new Property("textures", this.skinValue));
            PropertyMap propertiesMap = new PropertyMap(properties);

            return ResolvableProfile.createResolved(new GameProfile(this.uuid, safeHeadName(this.headName).orElse(""), propertiesMap));
        });
        stack = ItemUtils.process(stack);

        if (this.addToHeadHistory)
            FzmmHistory.addGeneratedHeads(stack);
        return stack;
    }

    private static Optional<String> safeHeadName(@Nullable String headName) {
        if (headName == null) {
            return Optional.empty();
        }

        String headNameCopy = headName;
        // ProfileComponent.PACKET_CODEC max size is 16
        if (headNameCopy.length() > 16) {
            headNameCopy = headNameCopy.substring(0, 16);
        }

        return Optional.of(headNameCopy);
    }

    public HeadBuilder skinValue(String skinValue) {
        this.skinValue = skinValue;
        return this;
    }

    public HeadBuilder headName(@Nullable String headName) {
        this.headName = headName;
        return this;
    }

    public HeadBuilder signature(@Nullable String signature) {
        this.signature = signature;
        return this;
    }

    public HeadBuilder id(UUID id) {
        this.uuid = id;
        return this;
    }

    public HeadBuilder notAddToHistory() {
        this.addToHeadHistory = false;
        return this;
    }

    public static ItemStack of(String username) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultInstance();
        Optional<String> nameOptional = safeHeadName(username);
        if (nameOptional.isEmpty()) return head;

        head.update(DataComponents.PROFILE, null, component ->
                ResolvableProfile.createUnresolved(nameOptional.get()));
        head = ItemUtils.process(head);

        FzmmHistory.addGeneratedHeads(head);
        return head;
    }

    public static ItemStack of(GameProfile profile) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultInstance();

        head.update(DataComponents.PROFILE, null, component -> ResolvableProfile.createResolved(profile));
        head = ItemUtils.process(head);

        FzmmHistory.addGeneratedHeads(head);
        return head;
    }
}
