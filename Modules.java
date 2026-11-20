package dev.azure;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class Modules {
    public static final Feature BASE = new Feature("Base finder", "Chunks full of built blocks", Feature.Category.BASE, false);
    public static final Feature STORAGE = new Feature("Storage finder", "Chunks with chests/furnaces", Feature.Category.BASE, false);
    public static final Feature DUD = new Feature("Dud chunk finder", "Chunks showing any activity", Feature.Category.BASE, false);
    public static final Feature FULLBRIGHT = new Feature("Fullbright", "Client-side night vision", Feature.Category.VISUAL, false);
    public static final Feature MAP = new Feature("Chunk map", "Scan map around you", Feature.Category.VISUAL, true);
    public static final Feature LIST = new Feature("Result list", "Top flagged chunk coords", Feature.Category.VISUAL, true);

    public static final List<Feature> ALL = List.of(BASE, STORAGE, DUD, FULLBRIGHT, MAP, LIST);

    private static boolean fullbrightWasOn;

    private Modules() {
    }

    public static boolean anyFinder() {
        return BASE.enabled || STORAGE.enabled || DUD.enabled;
    }

    public static void tick(Minecraft mc) {
        ChunkScanner.tick(mc);

        LocalPlayer player = mc.player;
        if (player == null) {
            fullbrightWasOn = false;
            return;
        }
        if (FULLBRIGHT.enabled) {
            MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
            if (current == null || current.getDuration() < 260) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false));
            }
            fullbrightWasOn = true;
        } else if (fullbrightWasOn) {
            player.removeEffect(MobEffects.NIGHT_VISION);
            fullbrightWasOn = false;
        }
    }
}
