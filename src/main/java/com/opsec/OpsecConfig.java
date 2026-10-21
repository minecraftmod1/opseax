package com.opsec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class OpsecConfig {
    public boolean spoofBrand = true;
    public String brand = "vanilla";
    public boolean filterPayloads = true;
    public List<String> allowedNamespaces = new ArrayList<>();
    public List<String> allowedChannels = new ArrayList<>();
    public boolean hideModTranslations = true;
    public boolean hideModKeybinds = true;
    public List<String> extraHiddenKeyWords = new ArrayList<>();
    public boolean logBlocked = true;
    public boolean blockLoginQueries = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> GENERIC = Set.of("minecraft", "java", "key", "keys", "block", "item",
            "entity", "gui", "options", "menu", "chat", "title", "effect", "biome", "category",
            "categories", "config", "misc");
    private static OpsecConfig instance;
    private transient Set<String> hidden;
    private final transient Set<String> logged = new HashSet<>();

    public static synchronized OpsecConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static OpsecConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("opsec.json");
        OpsecConfig cfg = new OpsecConfig();
        try {
            if (Files.exists(path)) {
                OpsecConfig read = GSON.fromJson(Files.readString(path), OpsecConfig.class);
                if (read != null) cfg = read;
            }
            Files.writeString(path, GSON.toJson(cfg));
        } catch (Exception e) {
            OpsecMod.LOGGER.warn("[opsec] config problem, using defaults", e);
        }
        return cfg;
    }

    public boolean shouldBlockPayload(Identifier id) {
        if (!filterPayloads) return false;
        String ns = id.getNamespace();
        if (ns.equals("minecraft") && id.getPath().equals("brand")) return false;
        if (allowedNamespaces.contains(ns)) return false;
        if (allowedChannels.contains(id.toString())) return false;
        log("blocked outgoing payload " + id);
        return true;
    }

    private static String norm(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    public boolean shouldHideKey(String key) {
        if (key == null) return false;
        Set<String> h = hidden();
        for (String part : key.toLowerCase(Locale.ROOT).split("[.:/]")) {
            if (h.contains(norm(part))) { log("hid probe key " + key); return true; }
            for (String sub : part.split("[-_]")) {
                if (sub.length() >= 4 && h.contains(norm(sub))) { log("hid probe key " + key); return true; }
            }
        }
        return false;
    }

    private synchronized Set<String> hidden() {
        if (hidden == null) {
            Set<String> ids = new HashSet<>();
            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                ids.add(norm(mod.getMetadata().getId()));
                mod.getMetadata().getProvides().forEach(p -> ids.add(norm(p)));
            }
            for (String w : extraHiddenKeyWords) ids.add(norm(w));
            ids.removeAll(GENERIC);
            ids.remove("");
            hidden = ids;
        }
        return hidden;
    }

    private synchronized void log(String msg) {
        if (logBlocked && logged.add(msg)) OpsecMod.LOGGER.info("[opsec] {}", msg);
    }
                             }
