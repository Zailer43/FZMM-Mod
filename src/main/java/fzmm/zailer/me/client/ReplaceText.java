package fzmm.zailer.me.client;

import fzmm.zailer.me.config.FzmmConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.Stack;

public class ReplaceText {


    public static String replace (String msg) {

        Stack<String[]> variableTexts = getVariableTexts();

        FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();

        Stack<String[]> normalTexts = new Stack<String[]>() {};
        for (String text : config.replaceTexts.texts) if (text.contains("==")) normalTexts.add(text.split("=="));

        for (String[] text : normalTexts) msg = msg.replaceAll(text[0], text[1]);
        for (String[] text : variableTexts) msg = msg.replaceAll(text[0], text[1]);
        return msg;
    }

    public static Stack<String[]> getVariableTexts() {

        Stack<String[]> texts = new Stack<String[]>() {};
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;
        ClientPlayerEntity mcp = mc.player;

        texts.push(new String[]{"::xyz::", Math.round(mcp.getX()) + " " + Math.round(mcp.getY()) + " " +  Math.round(mcp.getZ())});
        texts.push(new String[]{"::xz::", Math.round(mcp.getX()) + " " +  Math.round(mcp.getZ())});
        texts.push(new String[]{"::uuid::", mc.player.getUuid() + ""});
        texts.push(new String[]{"::item:name::", mcp.inventory.getMainHandStack().getName().getString().replaceAll("§", "&")});

        return texts;
    }
}