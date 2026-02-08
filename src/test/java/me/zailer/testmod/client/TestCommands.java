package me.zailer.testmod.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.zailer.testmod.client.test_command.HeadGeneratorTest;
import me.zailer.testmod.client.test_command.ParityComponentTest;
import me.zailer.testmod.client.test_command.SnackBarTest;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public class TestCommands {

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        var testCommand = ClientCommandManager.literal("fzmm:test");

        testCommand.then(ClientCommandManager.literal("head_generator:write")
                .executes(ctx -> {
                    HeadGeneratorTest.writeSkins();
                    return 0;
                })
        );

        testCommand.then(ClientCommandManager.literal("head_generator:check_format")
                .executes(ctx -> {
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Missing arguments"));
                    return 0;
                }).then(ClientCommandManager.argument("isSlim", BoolArgumentType.bool()).executes(ctx -> {
                    var isSlim = ctx.getArgument("isSlim", Boolean.class);

                    HeadGeneratorTest.checkFormat(isSlim);
                    return 0;
                }))
        );

        testCommand.then(ClientCommandManager.literal("head_generator:check_pixel")
                .executes(ctx -> {
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Missing arguments"));
                    return 0;
                }).then(ClientCommandManager.argument("x", IntegerArgumentType.integer(0, 63)).executes(ctx -> {
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Missing arguments"));

                    return 0;
                }).then(ClientCommandManager.argument("y", IntegerArgumentType.integer(0, 63)).executes(ctx -> {
                    var x = ctx.getArgument("x", Integer.class);
                    var y = ctx.getArgument("y", Integer.class);

                    HeadGeneratorTest.checkPixel(x, y, false);
                    return 0;
                })))
        );

        testCommand.then(ClientCommandManager.literal("head_generator:time")
                .executes(context -> {
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Missing arguments"));
                    return 0;
                }).then(ClientCommandManager.argument("loops", IntegerArgumentType.integer(1)).executes(ctx -> {

                    HeadGeneratorTest.time(ctx.getArgument("loops", Integer.class));
                    return 0;
                }))
        );

        testCommand.then(ClientCommandManager.literal("snack_bar:timer")
                .executes(ctx -> {
                    SnackBarTest.showTimer();
                    return 0;
                })
        );

        testCommand.then(ClientCommandManager.literal("snack_bar:color")
                .executes(ctx -> {
                    SnackBarTest.showColor();
                    return 0;
                })
        );

        testCommand.then(ClientCommandManager.literal("snack_bar:button")
                .executes(ctx -> {
                    SnackBarTest.showButton();
                    return 0;
                })
        );

        testCommand.then(ClientCommandManager.literal("parity_component")
                .executes(ctx -> {
                    ParityComponentTest.testAll();
                    return 0;
                })
        );

        dispatcher.register(testCommand);
    }
}