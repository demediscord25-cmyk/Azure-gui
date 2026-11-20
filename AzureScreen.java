package dev.azure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.lwjgl.glfw.GLFW;

public class AzureScreen extends Screen {
    private static final int W = 360;
    private static final int H = 236;

    private static final int PANEL = 0xF0060B1A;
    private static final int EDGE = 0xFF1E6BFF;
    private static final int CYAN = 0xFF00E5FF;
    private static final int SKY = 0xFF3DA5FF;
    private static final int TEXT = 0xFFE6F1FF;
    private static final int DIM = 0xFF7A93B8;
    private static final int ROW = 0xFF0A1530;
    private static final int ROW_HOVER = 0xFF0E2248;
    private static final int LINE = 0xFF1B3566;
    private static final int[] FLAG_COLORS = {0, 0xFF3DA5FF, 0xFFFFC857, 0xFFFF4D6D};
    private static final String[] FLAG_NAMES = {"", "DUD", "STOR", "BASE"};

    private Feature.Category tab = Feature.Category.BASE;
    private int px;
    private int py;

    public AzureScreen() {
        super(Component.literal("Azure"));
    }

    @Override
    protected void init() {
        px = (this.width - W) / 2;
        py = (this.height - H) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double x = event.x();
            double y = event.y();
            int i = 0;
            for (Feature.Category c : Feature.Category.values()) {
                if (inside(x, y, px + 12, py + 44 + i * 24, 88, 20)) {
                    tab = c;
                    return true;
                }
                i++;
            }
            int r = 0;
            for (Feature f : Modules.ALL) {
                if (f.category != tab) {
                    continue;
                }
                if (inside(x, y, px + 112, py + 44 + r * 28, 236, 24)) {
                    f.toggle();
                    return true;
                }
                r++;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        super.extractRenderState(g, mx, my, delta);

        box(g, px, py, px + W, py + H, PANEL, EDGE);
        g.fill(px + 1, py + 1, px + W - 1, py + 3, CYAN);
        g.text(this.font, "AZURE", px + 12, py + 12, CYAN, true);
        g.text(this.font, "base finding + visuals", px + 12, py + 24, DIM, false);
        g.text(this.font, "RSHIFT to close", px + W - 96, py + 12, DIM, false);

        int i = 0;
        for (Feature.Category c : Feature.Category.values()) {
            int x = px + 12;
            int y = py + 44 + i * 24;
            boolean on = c == tab;
            boolean hover = inside(mx, my, x, y, 88, 20);
            box(g, x, y, x + 88, y + 20, on ? 0xFF0F2A5E : (hover ? 0xFF0C1B3C : 0xFF08122A), on ? SKY : LINE);
            g.text(this.font, c.label, x + 8, y + 6, on ? 0xFFFFFFFF : 0xFFA9C4EA, false);
            i++;
        }

        int r = 0;
        for (Feature f : Modules.ALL) {
            if (f.category != tab) {
                continue;
            }
            int x = px + 112;
            int y = py + 44 + r * 28;
            boolean hover = inside(mx, my, x, y, 236, 24);
            box(g, x, y, x + 236, y + 24, hover ? ROW_HOVER : ROW, f.enabled ? 0xFF00C2FF : LINE);
            g.text(this.font, f.name, x + 8, y + 4, TEXT, false);
            g.text(this.font, f.desc, x + 8, y + 14, DIM, false);
            g.fill(x + 202, y + 6, x + 230, y + 18, f.enabled ? 0xFF00C2FF : 0xFF16284F);
            g.text(this.font, f.enabled ? "ON" : "OFF", f.enabled ? x + 210 : x + 207, y + 8, f.enabled ? 0xFF04122B : DIM, false);
            r++;
        }

        if (Modules.MAP.enabled) {
            drawMap(g, px + 112, py + 142);
        }
        if (Modules.LIST.enabled) {
            drawList(g, px + 212, py + 142);
        }
    }

    private void drawMap(GuiGraphicsExtractor g, int mapX, int mapY) {
        g.text(this.font, "Chunk map", mapX, mapY - 11, SKY, false);
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        int pcx = Mth.floor(this.minecraft.player.getX()) >> 4;
        int pcz = Mth.floor(this.minecraft.player.getZ()) >> 4;
        for (int dz = -5; dz <= 5; dz++) {
            for (int dx = -5; dx <= 5; dx++) {
                int x = mapX + (dx + 5) * 8;
                int y = mapY + (dz + 5) * 8;
                ChunkScanner.Result res = ChunkScanner.RESULTS.get(ChunkPos.asLong(pcx + dx, pcz + dz));
                int color = 0xFF0A1226;
                if (res != null) {
                    int flag = ChunkScanner.flag(res);
                    color = flag > 0 ? FLAG_COLORS[flag] : 0xFF14264A;
                }
                g.fill(x, y, x + 7, y + 7, color);
            }
        }
        int cx = mapX + 5 * 8;
        int cy = mapY + 5 * 8;
        g.fill(cx - 1, cy - 1, cx + 8, cy, 0xFFFFFFFF);
        g.fill(cx - 1, cy + 7, cx + 8, cy + 8, 0xFFFFFFFF);
        g.fill(cx - 1, cy, cx, cy + 7, 0xFFFFFFFF);
        g.fill(cx + 7, cy, cx + 8, cy + 7, 0xFFFFFFFF);
    }

    private void drawList(GuiGraphicsExtractor g, int lx, int ly) {
        g.text(this.font, "Flagged chunks", lx, ly - 11, SKY, false);
        if (!Modules.anyFinder()) {
            g.text(this.font, "Enable a finder", lx, ly, DIM, false);
            return;
        }
        List<int[]> flagged = new ArrayList<>();
        for (Map.Entry<Long, ChunkScanner.Result> e : ChunkScanner.RESULTS.entrySet()) {
            int flag = ChunkScanner.flag(e.getValue());
            if (flag > 0) {
                flagged.add(new int[] {flag, ChunkScanner.score(e.getValue()),
                        ChunkPos.getX(e.getKey()), ChunkPos.getZ(e.getKey())});
            }
        }
        if (flagged.isEmpty()) {
            g.text(this.font, "Scanning... none yet", lx, ly, DIM, false);
            return;
        }
        flagged.sort((a, b) -> a[0] != b[0] ? b[0] - a[0] : b[1] - a[1]);
        int shown = Math.min(8, flagged.size());
        for (int n = 0; n < shown; n++) {
            int[] f = flagged.get(n);
            String line = FLAG_NAMES[f[0]] + " " + (f[2] * 16 + 8) + ", " + (f[3] * 16 + 8) + " (" + f[1] + ")";
            g.text(this.font, line, lx, ly + n * 10, FLAG_COLORS[f[0]], false);
        }
    }

    private static void box(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int fill, int border) {
        g.fill(x0, y0, x1, y1, fill);
        g.fill(x0, y0, x1, y0 + 1, border);
        g.fill(x0, y1 - 1, x1, y1, border);
        g.fill(x0, y0, x0 + 1, y1, border);
        g.fill(x1 - 1, y0, x1, y1, border);
    }

    private static boolean inside(double x, double y, int rx, int ry, int w, int h) {
        return x >= rx && x < rx + w && y >= ry && y < ry + h;
    }
}
