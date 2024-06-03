package com.takaranoao.mods.autoreconnector;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoConnectorMod implements ClientModInitializer {
    public static final String MODID = "autoreconnector";
    public static int MAX_TICK;
    @Environment(EnvType.CLIENT)
    public static ServerInfo lastestServerEntry;
    public static int disconnectTick = 0;
    private static AutoReconnectConfig config;
    private static boolean shouldSendCommand = false;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void clientTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null && mc.getCurrentServerEntry() != null) {
            lastestServerEntry = mc.getCurrentServerEntry();
        }
        if (mc.currentScreen instanceof DisconnectedScreen) {
            disconnectTick++;
            if (disconnectTick == MAX_TICK && lastestServerEntry != null) {
                System.out.println(disconnectTick);
                mc.disconnect();
                ConnectScreen.connect(new TitleScreen(), mc, ServerAddress.parse(lastestServerEntry.address), lastestServerEntry, false);
                disconnectTick = 0;
                shouldSendCommand = true; // 자동 재접속 시 명령어 실행 플래그 설정
            }
        } else {
            disconnectTick = 0;
        }
    }

    @Override
    public void onInitializeClient() {
        config = AutoReconnectConfig.load();
        MAX_TICK = 20 * config.reconnectDelay;
        config.save();

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> clientTick());
        LogManager.getLogger().info("Loading Auto Reconnect");

        // 디버깅: 로드된 명령어 출력
        LogManager.getLogger().info("Reconnect Command: " + config.reconnectCommand);

        // 서버 접속 시 명령어 입력
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (shouldSendCommand) {
                MinecraftClient.getInstance().execute(() -> {
                    scheduler.schedule(() -> {
                        if (client.player != null) {
                            LogManager.getLogger().info("Executing Command: " + config.reconnectCommand);
                            sendCommand(client.player.networkHandler, config.reconnectCommand);
                            shouldSendCommand = false; // 명령어 실행 후 플래그 해제
                        }
                    }, 3, TimeUnit.SECONDS); // 3초 대기
                });
            }
        });
    }

    private void sendCommand(ClientPlayNetworkHandler networkHandler, String command) {
        if (command.startsWith("/")) {
            command = command.substring(1); // 슬래시 제거
        }
        networkHandler.sendChatCommand(command);
    }

    public static AutoReconnectConfig getConfig() {
        return config;
    }

    public static void reloadConfig() {
        config = AutoReconnectConfig.load();
        MAX_TICK = 20 * config.reconnectDelay;
        LogManager.getLogger().info("Config reloaded: " + config.reconnectCommand);
    }
}
