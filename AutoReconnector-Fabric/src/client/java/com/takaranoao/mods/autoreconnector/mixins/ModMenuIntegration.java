package com.takaranoao.mods.autoreconnector;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> createConfigScreen(parent);
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("AutoReconnect Config"));

        AutoReconnectConfig config = AutoReconnectConfig.load();

        builder.setSavingRunnable(() -> {
            config.save();
        });

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General Settings"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        IntegerListEntry reconnectDelayEntry = entryBuilder.startIntField(Text.literal("Reconnect Delay (seconds)"), config.reconnectDelay)
                .setDefaultValue(15)
                .setSaveConsumer(newValue -> {
                    config.reconnectDelay = newValue;
                    AutoConnectorMod.MAX_TICK = 20 * newValue;
                })
                .build();

        general.addEntry(reconnectDelayEntry);

        return builder.build();
    }
}
