package com.takaranoao.mods.autoreconnector;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.apache.logging.log4j.LogManager;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class AutoConnectorMod implements ClientModInitializer {
    public static final String MODID = "autoreconnector";
    public static int MAX_TICK;
    @Environment(EnvType.CLIENT)
    public static ServerInfo lastestServerEntry;
    public static int disconnectTick = 0;
    private static AutoReconnectConfig config;

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

                // Create an instance of CookieStorage with an empty map
                Map<Identifier, byte[]> cookieMap = new HashMap<>();
                CookieStorage cookieStorage = new CookieStorage(cookieMap);

                // Use the cookieStorage in the connect method
                ConnectScreen.connect(new TitleScreen(), mc, ServerAddress.parse(lastestServerEntry.address), lastestServerEntry, false, cookieStorage);
                disconnectTick = 0;
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
    }
}
