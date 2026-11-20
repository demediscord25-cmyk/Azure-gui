package dev.azure;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public final class ChunkScanner {
    public record Result(int base, int storage) {
    }

    public static final int BASE_MIN = 20;
    public static final int STORAGE_MIN = 2;
    public static final int DUD_MIN = 3;
    public static final int MAX_RADIUS = 16;
    public static final int CHUNKS_PER_TICK = 2;

    public static final Map<Long, Result> RESULTS = new HashMap<>();
    private static ClientLevel lastLevel;

    private ChunkScanner() {
    }

    public static void tick(Minecraft mc) {
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) {
            RESULTS.clear();
            lastLevel = null;
            return;
        }
        if (level != lastLevel) {
            RESULTS.clear();
            lastLevel = level;
        }
        if (!Modules.anyFinder()) {
            return;
        }

        int pcx = Mth.floor(player.getX()) >> 4;
        int pcz = Mth.floor(player.getZ()) >> 4;
        int radius = Math.min(mc.options.renderDistance().get(), MAX_RADIUS);
        int budget = CHUNKS_PER_TICK;

        for (int dx = -radius; dx <= radius && budget > 0; dx++) {
            for (int dz = -radius; dz <= radius && budget > 0; dz++) {
                int cx = pcx + dx;
                int cz = pcz + dz;
                long key = ChunkPos.asLong(cx, cz);
                if (RESULTS.containsKey(key) || !level.hasChunk(cx, cz)) {
                    continue;
                }
                RESULTS.put(key, scan(level.getChunk(cx, cz)));
                budget--;
            }
        }
    }

    private static Result scan(LevelChunk chunk) {
        int base = 0;
        int storage = 0;
        for (LevelChunkSection section : chunk.getSections()) {
            if (section.hasOnlyAir() || !section.maybeHas(ChunkScanner::interesting)) {
                continue;
            }
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (isStorage(state)) {
                            storage++;
                        } else if (isBuilt(state)) {
                            base++;
                        }
                    }
                }
            }
        }
        return new Result(base, storage);
    }

    private static boolean interesting(BlockState state) {
        return isStorage(state) || isBuilt(state);
    }

    private static boolean isStorage(BlockState s) {
        Block b = s.getBlock();
        return b instanceof ChestBlock
                || b instanceof EnderChestBlock
                || b instanceof BarrelBlock
                || b instanceof ShulkerBoxBlock
                || b instanceof AbstractFurnaceBlock
                || b instanceof HopperBlock
                || b instanceof DispenserBlock;
    }

    private static boolean isBuilt(BlockState s) {
        return s.is(BlockTags.PLANKS)
                || s.is(BlockTags.WOOL)
                || s.is(BlockTags.DOORS)
                || s.is(BlockTags.BEDS)
                || s.is(BlockTags.FENCES)
                || s.is(Blocks.CRAFTING_TABLE)
                || s.is(Blocks.TORCH)
                || s.is(Blocks.WALL_TORCH)
                || s.is(Blocks.LANTERN)
                || s.is(Blocks.GLASS)
                || s.is(Blocks.GLASS_PANE)
                || s.is(Blocks.BRICKS)
                || s.is(Blocks.STONE_BRICKS)
                || s.is(Blocks.LADDER);
    }

    public static int flag(Result r) {
        if (Modules.BASE.enabled && r.base() >= BASE_MIN) {
            return 3;
        }
        if (Modules.STORAGE.enabled && r.storage() >= STORAGE_MIN) {
            return 2;
        }
        if (Modules.DUD.enabled && r.base() + r.storage() >= DUD_MIN) {
            return 1;
        }
        return 0;
    }

    public static int score(Result r) {
        return r.base() + r.storage() * 4;
    }
}
