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

import java.io.InputStream;
import java.util.Optional;

public class HavenMapScreen extends Screen {
    public static final Logger LOGGER = LogManager.getLogger();

    // Deine Textur
    private static final ResourceLocation MAP_TEX =
            ResourceLocation.parse("valorant:textures/gui/haven_map.png");

    // echte PNG-Maße (werden in init() gelesen)
    private int texW = 256;
    private int texH = 256;

    // Zielrechteck
    private int drawW, drawH, drawX, drawY;

    // Sky Smoke marker management
    private final java.util.List<BlockPos> selectedPositions = new java.util.ArrayList<>();
    private static final int MARKER_COLOR = argb(128, 255, 100, 100); // Red color for markers with low opacity

    // Calibration mode
    private boolean isCalibrationMode = false;
    private int calibrationStep = 0;
    private String calibrationPrompt = "";

    public HavenMapScreen() {
        super(Component.literal("Haven Map"));
    }

    public static void openCalibrationMode(int step, String prompt) {
        HavenMapScreen screen = new HavenMapScreen();
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
                }
            } else {
                // Dieser Fall wird jetzt protokolliert!
                LOGGER.error("FEHLER: Ressource NICHT gefunden unter dem Pfad: " + MAP_TEX);
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

        // --- DER FINALE BLIT-AUFRUF FÜR SKALIERUNG ---
        // Dieser Aufruf behebt das Kachel-Problem, indem er die richtigen Parameter verwendet.
        g.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            ResourceLocation.parse("valorant:haven_map"),  // no folders, no .png
            drawX, drawY,
            drawW, drawH
        );
    
        // 3. Draw Sky Smoke markers
        drawMarkers(g);

        // 4. Draw hint text
        drawHints(g);

        // 2. Titel und super.render (Ihr Code, unverändert)
        g.drawCenteredString(this.font, this.title, this.width / 2, Math.max(10, drawY - 14), 0xFFFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
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
            int radius = Math.max(4, drawW / 60); // Scale with map size, slightly larger for circles
            drawFilledCircle(g, screenX, screenY, radius, MARKER_COLOR);

            // Draw center dot (white border)
            g.fill(screenX - 1, screenY - 1, screenX + 1, screenY + 1, 0xFFFFFFFF);
        }
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
        // Check if we have a calibrated transform - use it if available
        if (SkySmokeCalibrationClient.hasTransform()) {
            // Apply inverse similarity transform: mapUV = R^(-1)·(world - t)/s
            double a = SkySmokeCalibrationClient.getTransformA();
            double b = SkySmokeCalibrationClient.getTransformB();
            double tx = SkySmokeCalibrationClient.getTransformTx();
            double tz = SkySmokeCalibrationClient.getTransformTz();

            // Inverse transform: [u, v] = R^(-1) · ([wx, wz] - [tx, tz]) / s
            double wx = worldX - tx;
            double wz = worldZ - tz;

            // R^(-1) where R = [a, -b; b, a], so R^(-1) = [a, b; -b, a] / det
            double det = a * a + b * b; // Scale squared
            double u = (a * wx + b * wz) / det;
            double v = (-b * wx + a * wz) / det;

            // Convert to screen coordinates
            int screenX = drawX + (int) (u * drawW);
            int screenY = drawY + (int) (v * drawH);

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

        // Convert to screen coordinates
        int screenX = drawX + (int) ((u + 0.5) * drawW);
        int screenY = drawY + (int) ((v + 0.5) * drawH);

        return com.mojang.datafixers.util.Pair.of(screenX, screenY);
    }

    /**
     * Converts mouse coordinates to world BlockPos using Sky Smoke map bounds.
     * Accounts for map rotation to properly align GUI clicks with world positions.
     * The Y coordinate is set to 0.0 as it's resolved server-side.
     */
    private BlockPos toWorld(int mouseX, int mouseY) {
        // Convert screen coordinates to normalized UV coordinates (0-1)
        double u = Mth.clamp((mouseX - drawX) / (double) drawW, 0.0, 1.0);
        double v = Mth.clamp((mouseY - drawY) / (double) drawH, 0.0, 1.0);

        // Check if we have a calibrated transform - use it if available
        if (SkySmokeCalibrationClient.hasTransform()) {
            // Apply the similarity transform: world = s·R·uv + t
            double a = SkySmokeCalibrationClient.getTransformA();
            double b = SkySmokeCalibrationClient.getTransformB();
            double tx = SkySmokeCalibrationClient.getTransformTx();
            double tz = SkySmokeCalibrationClient.getTransformTz();

            double wx = a * u - b * v + tx;
            double wz = b * u + a * v + tz;

            return BlockPos.containing(wx, 0.0, wz); // y resolved server-side
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
        double relU = u - 0.5;
        double relV = v - 0.5;

        // Apply rotation if needed
        double rotatedU = relU;
        double rotatedV = relV;
        if (rotationDegrees != 0.0) {
            double radians = Math.toRadians(rotationDegrees);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            rotatedU = relU * cos - relV * sin;
            rotatedV = relU * sin + relV * cos;
        }

        // Convert back to world coordinates
        int x = Mth.floor(mapCenterX + rotatedU * mapWidth);
        int z = Mth.floor(mapCenterZ + rotatedV * mapHeight);

        return BlockPos.containing(x + 0.5, 0.0, z + 0.5); // y resolved server-side
    }

    private static int argb(int a, int r, int g, int b) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
