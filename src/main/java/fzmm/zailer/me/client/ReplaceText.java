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

        text = text.replaceAll("::fzmm_x::", String.valueOf(mcp.getX()));
        text = text.replaceAll("::fzmm_y::", String.valueOf(mcp.getY()));
        text = text.replaceAll("::fzmm_z::", String.valueOf(mcp.getZ()));

        text = text.replaceAll("::fzmm_x_round::", String.valueOf(Math.round(mcp.getX())));
        text = text.replaceAll("::fzmm_y_round::", String.valueOf(Math.round(mcp.getY())));
        text = text.replaceAll("::fzmm_z_round::", String.valueOf(Math.round(mcp.getZ())));

        text = text.replaceAll("::fzmm_yaw::", String.valueOf(mcp.getYaw()));
        text = text.replaceAll("::fzmm_pitch::", String.valueOf(mcp.getPitch()));

        text = text.replaceAll("::fzmm_yaw_round::", String.valueOf(Math.round(mcp.getYaw())));
        text = text.replaceAll("::fzmm_pitch_round::", String.valueOf(Math.round(mcp.getPitch())));

        text = text.replaceAll("::fzmm_uuid::", String.valueOf(mcp.getUuid()));
        text = text.replaceAll("::fzmm_item_name::", mcp.getInventory().getMainHandStack().getName().getString().replaceAll("§", "&"));

        return text;
    }
}