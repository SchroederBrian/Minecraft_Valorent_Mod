package com.bobby.valorant.client;

import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import com.bobby.valorant.client.SkySmokeAreaClient;

import java.io.InputStream;
import java.util.Optional;

public class SkySmokeScreen extends Screen {
    public static final Logger LOGGER = LogManager.getLogger();

    // Sky Smoke GUI Texture
    private static final ResourceLocation MAP_TEX =
            ResourceLocation.parse("valorant:skysmoke_map");

    // echte PNG-Maße (werden in init() gelesen)
    private int texW = 256;
    private int texH = 256;
    private boolean hasPngTexture = false; // true if valorant:textures/gui/sprites/skysmoke_map.png exists

    // Zielrechteck
    private int drawW, drawH, drawX, drawY;
    // Follow-view window (UV sub-rect)
    private double viewU0 = 0.0, viewV0 = 0.0, viewUw = 1.0, viewVh = 1.0;

    // Sky Smoke marker management
    private final java.util.List<BlockPos> selectedPositions = new java.util.ArrayList<>();
    private static final int MARKER_COLOR = argb(128, 255, 100, 100); // Red color for markers with low opacity

    // Calibration mode
    private boolean isCalibrationMode = false;
    private int calibrationStep = 0;
    private String calibrationPrompt = "";

    public SkySmokeScreen() {
        super(Component.literal("Sky Smoke Map"));
    }

    public static void openCalibrationMode(int step, String prompt) {
        SkySmokeScreen screen = new SkySmokeScreen();
        screen.isCalibrationMode = true;
        screen.calibrationStep = step;
        screen.calibrationPrompt = prompt;
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    protected void init() {
        super.init();

        // PNG-Maße zuverlässig einlesen
        readTextureSize();

        // Zielgröße berechnen (80% der kleineren Fensterkante, Aspect-Ratio beibehalten)
        int maxW = (int) (this.width * 0.8);
        int maxH = (int) (this.height * 0.8);
        double scale = Math.min(maxW / (double) texW, maxH / (double) texH);
        if (scale <= 0) scale = 1.0;

        drawW = (int) Math.round(texW * scale);
        drawH = (int) Math.round(texH * scale);
        drawX = (this.width - drawW) / 2;
        drawY = (this.height - drawH) / 2;
    }

    private void readTextureSize() {
        try {
            LOGGER.info("Versuche die Kartentextur zu laden: " + MAP_TEX);
            var rm = Minecraft.getInstance().getResourceManager();
            Optional<Resource> opt = rm.getResource(MAP_TEX);
    
            if (opt.isPresent()) {
                // Die Ressource wurde gefunden, versuche sie zu lesen
                LOGGER.info("Ressource gefunden! Lese Bild-Dimensionen...");
                try (InputStream in = opt.get().open()) {
                    NativeImage img = NativeImage.read(in);
                    this.texW = img.getWidth();
                    this.texH = img.getHeight();
                    LOGGER.info("Bild geladen mit den Dimensionen: " + texW + "x" + texH);
                    hasPngTexture = true;
                }
            } else {
                // Dieser Fall wird jetzt protokolliert!
                LOGGER.error("FEHLER: Ressource NICHT gefunden unter dem Pfad: " + MAP_TEX);
                hasPngTexture = false;
            }
    
        } catch (Throwable t) {
            LOGGER.error("FEHLER beim Lesen der Bilddatei (sie könnte korrupt sein): " + MAP_TEX, t);
        }
        if (texW <= 0 || texH <= 0) {
            texW = 256;
            texH = 256;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 1. Hintergrund und Rahmen (Ihr Code, unverändert)
        int top = argb(200, 12, 14, 16);
        int bottom = argb(200, 8, 9, 11);
        g.fillGradient(0, 0, this.width, this.height, top, bottom);
    
        int border = 4;
        int accent = argb(255, 100, 220, 255);
        int inner = argb(180, 20, 22, 28);
        g.fill(drawX - border, drawY - border, drawX + drawW + border, drawY + drawH + border, accent);
        g.fill(drawX, drawY, drawX + drawW, drawY + drawH, inner);

        // Compute follow view window centered on player (if enabled and texture available)
        boolean follow = com.bobby.valorant.Config.COMMON.skySmokeUiFollowPlayer.get() && hasPngTexture;
        if (follow) {
            computeFollowViewWindow();
        } else {
            viewU0 = 0.0; viewV0 = 0.0; viewUw = 1.0; viewVh = 1.0;
        }

        // Draw either sub-rect (PNG) or full sprite fallback
        int rDrawW = drawW;
        int rDrawH = drawH;
        int rDrawX = drawX;
        int rDrawY = drawY;
        if (hasPngTexture) {
            // Use PNG rendering with UV coordinates for proper marker/clicking functionality
            int srcU = (int)Math.floor(viewU0 * texW);
            int srcV = (int)Math.floor(viewV0 * texH);
            int srcW = (int)Math.ceil(viewUw * texW);
            int srcH = (int)Math.ceil(viewVh * texH);
            // Clamp
            if (srcU < 0) srcU = 0; if (srcV < 0) srcV = 0;
            if (srcU + srcW > texW) srcW = texW - srcU;
            if (srcV + srcH > texH) srcH = texH - srcV;
            g.blit(MAP_TEX, rDrawX, rDrawY, srcU, srcV, rDrawW, rDrawH, texW, texH);
        } else {
            // Fallback to sprite rendering if PNG not available
            g.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                ResourceLocation.parse("valorant:skysmoke_map"),
                rDrawX, rDrawY,
                rDrawW, rDrawH
            );
        }
    
        // 3. Draw Sky Smoke markers
        drawMarkers(g);

        // 4. Draw hint text
        drawHints(g);

        // 2. Titel und super.render (Ihr Code, unverändert)
        g.drawCenteredString(this.font, this.title, this.width / 2, Math.max(10, rDrawY - 14), 0xFFFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
        // Update active draw rect for input mapping
        this.drawX = rDrawX;
        this.drawY = rDrawY;
        this.drawW = rDrawW;
        this.drawH = rDrawH;

        // Draw player marker if enabled
        if (com.bobby.valorant.Config.COMMON.skySmokeUiShowPlayerMarker.get()) {
            drawPlayerMarker(g);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC oder Inventar-Key schließt ohne senden
        if (keyCode == 256 || Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        // Enter bestätigt und sendet
        if (keyCode == 257) { // Enter key
            confirmAndSend();
            return true;
        }
        // Backspace entfernt letzten Marker
        if (keyCode == 259 && !selectedPositions.isEmpty()) { // Backspace key
            selectedPositions.remove(selectedPositions.size() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle calibration mode
        if (isCalibrationMode && button == 0) { // Left click in calibration mode
            // Convert screen coordinates to UV coordinates (0-1)
            double u = Mth.clamp((mouseX - drawX) / (double) drawW, 0.0, 1.0);
            double v = Mth.clamp((mouseY - drawY) / (double) drawH, 0.0, 1.0);

            // Optional Ctrl snap to pixel centers
            if (com.bobby.valorant.Config.COMMON.skySmokeCalibrationUiEnableSnap.get() && this.hasControlDown()) {
                u = (Math.round(u * texW) + 0.5) / Math.max(1, texW);
                v = (Math.round(v * texH) + 0.5) / Math.max(1, texH);
                u = Mth.clamp(u, 0.0, 1.0);
                v = Mth.clamp(v, 0.0, 1.0);
            }

            // Send UV coordinates to server
            var packet = new com.bobby.valorant.network.SubmitCalibrationPointC2SPacket(calibrationStep, u, v);
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(packet);

            // Close the screen
            this.onClose();
            return true;
        }

        int maxPerCast = com.bobby.valorant.Config.COMMON.skySmokeMaxPerCast.get();
        if (button == 0) { // Left click - add marker
            if (selectedPositions.size() < maxPerCast) {
                BlockPos worldPos = toWorld((int) mouseX, (int) mouseY);
                // Check if the point is inside an allowed area
                if (!SkySmokeAreaClient.isInsideAllowedArea(worldPos.getX(), worldPos.getZ())) {
                    // Do not add marker if not in allowed area
                    return true;
                }
                selectedPositions.add(worldPos);
                com.bobby.valorant.Valorant.LOGGER.debug("[SkySmoke] Click at screen({},{}) -> world({},{})",
                    (int) mouseX, (int) mouseY, worldPos.getX(), worldPos.getZ());
                return true;
            }
        } else if (button == 1) { // Right click - remove last marker
            if (!selectedPositions.isEmpty()) {
                selectedPositions.remove(selectedPositions.size() - 1);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void confirmAndSend() {
        if (!selectedPositions.isEmpty()) {
            // Debug logging
            com.bobby.valorant.Valorant.LOGGER.info("[SkySmoke] Client sending {} smoke position(s):", selectedPositions.size());
            for (int i = 0; i < selectedPositions.size(); i++) {
                BlockPos pos = selectedPositions.get(i);
                com.bobby.valorant.Valorant.LOGGER.info("[SkySmoke] Position {}: ({},{})", i + 1, pos.getX(), pos.getZ());
            }

            // Send packet to server
            com.bobby.valorant.network.PlaceSkySmokesC2SPacket packet =
                new com.bobby.valorant.network.PlaceSkySmokesC2SPacket(selectedPositions);
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(packet);
        }
        // Close screen regardless
        onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    private void drawMarkers(GuiGraphics g) {
        for (BlockPos worldPos : selectedPositions) {
            // Convert world pos back to screen coordinates (accounting for rotation)
            var screenCoords = worldToScreen(worldPos.getX(), worldPos.getZ());
            int screenX = screenCoords.getFirst();
            int screenY = screenCoords.getSecond();

            // Draw filled marker circle
            int radius = Math.max(4, drawW / 45); // Scale with map size, slightly larger for circles
            drawFilledCircle(g, screenX, screenY, radius, MARKER_COLOR);

            // Draw center dot (white border)
            g.fill(screenX - 1, screenY - 1, screenX + 1, screenY + 1, 0xFFFFFFFF);
        }
    }

    private void drawPlayerMarker(GuiGraphics g) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int color = com.bobby.valorant.Config.COMMON.skySmokeUiPlayerMarkerColor.get();
        int size = com.bobby.valorant.Config.COMMON.skySmokeUiPlayerMarkerSize.get();
        double wx = mc.player.getX();
        double wz = mc.player.getZ();
        var screen = worldToScreen((int)Math.round(wx), (int)Math.round(wz));
        int cx = screen.getFirst();
        int cy = screen.getSecond();
        drawFilledCircle(g, cx, cy, Math.max(2, size/2), color);
    }

    /**
     * Draws a filled circle using GuiGraphics by approximating with small rectangles.
     */
    private void drawFilledCircle(GuiGraphics g, int centerX, int centerY, int radius, int color) {
        int radiusSq = radius * radius;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                if (x * x + y * y <= radiusSq) {
                    g.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
                }
            }
        }
    }

    private void drawHints(GuiGraphics g) {
        int y = drawY + drawH + 20;

        if (isCalibrationMode) {
            // Calibration mode hints
            String hint1 = "Calibration Point " + calibrationStep;
            String hint2 = calibrationPrompt;
            String hint3 = "Left-click: select location on map";

            g.drawString(this.font, hint1, drawX, y, 0xFFFFFF00); // Yellow for calibration
            g.drawString(this.font, hint2, drawX, y + 12, 0xFFFFFFFF);
            g.drawString(this.font, hint3, drawX, y + 24, 0xFFFFFFFF);
        } else {
            // Normal Sky Smoke placement hints
            int maxPerCast = com.bobby.valorant.Config.COMMON.skySmokeMaxPerCast.get();
            String hint1 = "Left-click: place smoke (" + selectedPositions.size() + "/" + maxPerCast + ")";
            String hint2 = "Right-click/Backspace: remove last | Enter: confirm | ESC: cancel";

            g.drawString(this.font, hint1, drawX, y, 0xFFFFFFFF);
            g.drawString(this.font, hint2, drawX, y + 12, 0xFFFFFFFF);
        }
    }

    /**
     * Converts world X coordinate to screen X coordinate.
     * Accounts for map rotation to properly position markers.
     */
    private int worldToScreenX(int worldX) {
        return worldToScreen(worldX, 0).getFirst();
    }

    /**
     * Converts world Z coordinate to screen Y coordinate.
     * Accounts for map rotation to properly position markers.
     */
    private int worldToScreenY(int worldZ) {
        return worldToScreen(0, worldZ).getSecond();
    }

    /**
     * Converts world coordinates to screen UV coordinates, accounting for rotation.
     * Returns a Pair of (screenX, screenY).
     */
    private com.mojang.datafixers.util.Pair<Integer, Integer> worldToScreen(int worldX, int worldZ) {
        // Check if we have a calibrated transform - use it if available (homography aware)
        if (SkySmokeCalibrationClient.hasTransform()) {
            double[] uv = SkySmokeCalibrationClient.worldToUv(worldX, worldZ);
            double u = uv[0];
            double v = uv[1];
            // Map using current view sub-rect
            int screenX = drawX + (int) (((u - viewU0) / viewUw) * drawW);
            int screenY = drawY + (int) (((v - viewV0) / viewVh) * drawH);
            return com.mojang.datafixers.util.Pair.of(screenX, screenY);
        }

        // Fallback to bounds-based method (legacy behavior)
        int minX = SkySmokeCalibrationClient.getMinX();
        int minZ = SkySmokeCalibrationClient.getMinZ();
        int maxX = SkySmokeCalibrationClient.getMaxX();
        int maxZ = SkySmokeCalibrationClient.getMaxZ();
        double rotationDegrees = SkySmokeCalibrationClient.getRotationDegrees();

        // Convert world coordinates to position relative to map center
        double mapWidth = maxX - minX;
        double mapHeight = maxZ - minZ;
        double mapCenterX = minX + mapWidth / 2.0;
        double mapCenterZ = minZ + mapHeight / 2.0;

        double relX = (worldX - mapCenterX) / mapWidth;
        double relZ = (worldZ - mapCenterZ) / mapHeight;

        // Apply inverse rotation
        double u = relX;
        double v = relZ;
        if (rotationDegrees != 0.0) {
            double radians = Math.toRadians(-rotationDegrees); // Negative for inverse
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double rotatedU = u * cos - v * sin;
            double rotatedV = u * sin + v * cos;
            u = rotatedU;
            v = rotatedV;
        }

        // Convert to screen coordinates with sub-rect view
        double uu = (u + 0.5);
        double vv = (v + 0.5);
        int screenX = drawX + (int) (((uu - viewU0) / viewUw) * drawW);
        int screenY = drawY + (int) (((vv - viewV0) / viewVh) * drawH);

        return com.mojang.datafixers.util.Pair.of(screenX, screenY);
    }

    /**
     * Converts mouse coordinates to world BlockPos using Sky Smoke map bounds.
     * Accounts for map rotation to properly align GUI clicks with world positions.
     * The Y coordinate is set to 0.0 as it's resolved server-side.
     */
    private BlockPos toWorld(int mouseX, int mouseY) {
        // Convert screen coordinates to normalized UV coordinates (0-1)
        double relU = Mth.clamp((mouseX - drawX) / (double) drawW, 0.0, 1.0);
        double relV = Mth.clamp((mouseY - drawY) / (double) drawH, 0.0, 1.0);
        // If we are viewing a sub-rect, map back to full UV
        double u = viewU0 + relU * viewUw;
        double v = viewV0 + relV * viewVh;

        // Check if we have a calibrated transform - use it if available (homography aware)
        if (SkySmokeCalibrationClient.hasTransform()) {
            double[] w = SkySmokeCalibrationClient.uvToWorld(u, v);
            return BlockPos.containing(w[0], 0.0, w[1]); // y resolved server-side
        }

        // Fallback to bounds-based method
        int minX = SkySmokeCalibrationClient.getMinX();
        int minZ = SkySmokeCalibrationClient.getMinZ();
        int maxX = SkySmokeCalibrationClient.getMaxX();
        int maxZ = SkySmokeCalibrationClient.getMaxZ();
        double rotationDegrees = SkySmokeCalibrationClient.getRotationDegrees();

        // Convert UV to map coordinates (relative to map bounds)
        double mapWidth = maxX - minX;
        double mapHeight = maxZ - minZ;
        double mapCenterX = minX + mapWidth / 2.0;
        double mapCenterZ = minZ + mapHeight / 2.0;

        // Convert UV to position relative to map center (-0.5 to 0.5)
        double relUC = u - 0.5;
        double relVC = v - 0.5;

        // Apply rotation if needed
        double rotatedU = relUC;
        double rotatedV = relVC;
        if (rotationDegrees != 0.0) {
            double radians = Math.toRadians(rotationDegrees);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            rotatedU = relUC * cos - relVC * sin;
            rotatedV = relUC * sin + relVC * cos;
        }

        // Convert back to world coordinates (no client-side flooring to preserve precision)
        double x = mapCenterX + rotatedU * mapWidth;
        double z = mapCenterZ + rotatedV * mapHeight;

        return BlockPos.containing(x, 0.0, z); // y resolved server-side
    }

    private void computeFollowViewWindow() {
        var mc = Minecraft.getInstance();
        if (mc.player == null) { viewU0=0; viewV0=0; viewUw=1; viewVh=1; return; }
        double wx = mc.player.getX();
        double wz = mc.player.getZ();
        double u, v;
        if (SkySmokeCalibrationClient.hasTransform()) {
            double[] uv = SkySmokeCalibrationClient.worldToUv(wx, wz);
            u = uv[0]; v = uv[1];
        } else {
            // Bounds fallback
            int minX = SkySmokeCalibrationClient.getMinX();
            int minZ = SkySmokeCalibrationClient.getMinZ();
            int maxX = SkySmokeCalibrationClient.getMaxX();
            int maxZ = SkySmokeCalibrationClient.getMaxZ();
            double mapWidth = maxX - minX;
            double mapHeight = maxZ - minZ;
            double mapCenterX = minX + mapWidth / 2.0;
            double mapCenterZ = minZ + mapHeight / 2.0;
            double relX = (wx - mapCenterX) / mapWidth;
            double relZ = (wz - mapCenterZ) / mapHeight;
            u = relX + 0.5; v = relZ + 0.5;
        }
        double zoom = Math.max(1.0, com.bobby.valorant.Config.COMMON.skySmokeUiFollowZoomFactor.get());
        double w = 1.0 / zoom;
        double h = 1.0 / zoom;
        double u0 = u - w / 2.0;
        double v0 = v - h / 2.0;
        // Clamp to [0,1]
        if (u0 < 0) u0 = 0; if (v0 < 0) v0 = 0;
        if (u0 + w > 1.0) u0 = 1.0 - w;
        if (v0 + h > 1.0) v0 = 1.0 - h;
        viewU0 = u0; viewV0 = v0; viewUw = w; viewVh = h;
    }

    private static int argb(int a, int r, int g, int b) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
