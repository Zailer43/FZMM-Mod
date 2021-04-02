package fzmm.zailer.me.client;

import fzmm.zailer.me.config.FzmmConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class ReplaceText {


    public static String replace (String msg) {

        FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();

        for (FzmmConfig.Pair text : config.replaceTexts.texts) msg = msg.replaceAll(text.getOriginal(), text.getReplace());
        msg = setVariables(msg);

        return msg;
    }

    public static String setVariables(String text) {

        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;
        ClientPlayerEntity mcp = mc.player;

        text = text.replaceAll("::fzmm_x::", mcp.getX() + "");
        text = text.replaceAll("::fzmm_y::", mcp.getY() + "");
        text = text.replaceAll("::fzmm_z::", mcp.getZ() + "");

        text = text.replaceAll("::fzmm_x_round::", Math.round(mcp.getX()) + "");
        text = text.replaceAll("::fzmm_y_round::", Math.round(mcp.getY()) + "");
        text = text.replaceAll("::fzmm_z_round::", Math.round(mcp.getZ()) + "");

        text = text.replaceAll("::fzmm_yaw::", mcp.yaw % 360 + "");
        text = text.replaceAll("::fzmm_pitch::", mcp.pitch + "");

        text = text.replaceAll("::fzmm_yaw_round::", Math.round(mcp.yaw % 360) + "");
        text = text.replaceAll("::fzmm_pitch_round::", Math.round(mcp.pitch) + "");

        text = text.replaceAll("::fzmm_uuid::", mcp.getUuid() + "");
        text = text.replaceAll("::fzmm_item_name::", mcp.inventory.getMainHandStack().getName().getString().replaceAll("§", "&"));

        return text;
    }
}