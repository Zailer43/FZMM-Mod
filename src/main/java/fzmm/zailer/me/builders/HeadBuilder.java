package fzmm.zailer.me.builders;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class HeadBuilder {

    private String skinValue = "";
    @Nullable
    private String headName = null;
//    @Nullable
//    private String signature = null;
    @Nullable
    private UUID uuid = null;
    private boolean addToHeadHistory = true;

    private HeadBuilder() {
    }

    public static HeadBuilder builder() {
        return new HeadBuilder();
    }

    public ItemStack get() {
        ItemStack stack = Items.PLAYER_HEAD.getDefaultStack();
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }

        stack.apply(DataComponentTypes.PROFILE, null, component -> {

            Multimap<String, Property> properties = ImmutableMultimap.of("textures", new Property("textures", this.skinValue));
            PropertyMap propertiesMap = new PropertyMap(properties);

            return ProfileComponent.ofStatic(new GameProfile(this.uuid, safeHeadName(this.headName).orElse(""), propertiesMap));
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

    public static String toSkinValue(String url) {
        return TextUtils.encodeBase64("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}");
    }

    public static String toUrl(String urlValue) {
        return "http://textures.minecraft.net/texture/" + urlValue;
    }

    public static String urlValueToSkinValue(String urlValue) {
        return toSkinValue(toUrl(urlValue));
    }

    public HeadBuilder urlValue(String urlValue) {
        return this.skinValue(urlValueToSkinValue(urlValue));
    }

    public HeadBuilder skinValue(String skinValue) {
        this.skinValue = skinValue;
        return this;
    }

    public HeadBuilder headName(@Nullable String headName) {
        this.headName = headName;
        return this;
    }

//    public HeadBuilder signature(@Nullable String signature) {
//        this.signature = signature;
//        return this;
//    }

    public HeadBuilder id(UUID id) {
        this.uuid = id;
        return this;
    }

    public HeadBuilder notAddToHistory() {
        this.addToHeadHistory = false;
        return this;
    }

    public static ItemStack of(String username) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
        Optional<String> nameOptional = safeHeadName(username);
        if (nameOptional.isEmpty()) return head;

        head.apply(DataComponentTypes.PROFILE, null, component ->
                ProfileComponent.ofDynamic(nameOptional.get()));
        head = ItemUtils.process(head);

        FzmmHistory.addGeneratedHeads(head);
        return head;
    }

    public static ItemStack of(GameProfile profile) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();

        head.apply(DataComponentTypes.PROFILE, null, component -> ProfileComponent.ofStatic(profile));
        head = ItemUtils.process(head);

        FzmmHistory.addGeneratedHeads(head);
        return head;
    }
}
