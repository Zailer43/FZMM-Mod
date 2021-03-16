package fzmm.zailer.me.client;

import fzmm.zailer.me.config.FzmmConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.Stack;

public class ReplaceText {


    public static String replace (String msg) {

        FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();

        Stack<String[]> normalTexts = new Stack<String[]>() {};
        for (String text : config.replaceTexts.texts) if (text.contains("==")) normalTexts.add(text.split("=="));

        for (String[] text : normalTexts) msg = msg.replaceAll(text[0], text[1]);
        msg = setVariables(msg);

        return msg;
    }

    public static String setVariables(String text) {

        Stack<String[]> texts = new Stack<String[]>() {};
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