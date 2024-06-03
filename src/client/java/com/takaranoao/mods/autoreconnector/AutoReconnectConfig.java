package com.takaranoao.mods.autoreconnector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AutoReconnectConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/autoreconnector.json");
    public int reconnectDelay = 15; // default to 15 seconds
    public String reconnectCommand = "/say Reconnected!"; // 추가된 부분: 기본 명령어 설정

    public static AutoReconnectConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, AutoReconnectConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new AutoReconnectConfig();
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
