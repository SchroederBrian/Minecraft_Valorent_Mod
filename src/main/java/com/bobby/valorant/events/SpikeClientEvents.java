package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.ModKeyBindings;
import com.bobby.valorant.round.RoundState;
import com.bobby.valorant.spawn.client.SpawnAreaClientState;
import com.bobby.valorant.client.hud.TitleOverlay;
import com.bobby.valorant.network.DefuseSpikePacket;
import com.bobby.valorant.network.EquipSpikePacket;
import com.bobby.valorant.network.PlantSpikePacket;
import com.bobby.valorant.registry.ModItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class SpikeClientEvents {
    private SpikeClientEvents() {}

    // Track client-side defusing state
    private static boolean isDefusing = false;
    // Require key release before allowing a new defuse start after cancellation
    private static boolean requireDefuseKeyRelease = false;
    // Track previous key state to edge-detect press/release
    private static boolean defuseKeyWasDown = false;

    // Track client-side planting state
    private static boolean isPlanting = false;

    // Track if spike has been defused this round (client-side knowledge)
    private static boolean spikeDefused = false;

    /**
     * Reset the spike defused flag (called when a new round starts)
     */
    public static void resetSpikeDefusedFlag() {
        spikeDefused = false;
        System.out.println("[SpikeClient] Reset spike defused flag for new round");
    }

    /**
     * Set the spike defused flag (called when defusing succeeds)
     */
    public static void setSpikeDefused(boolean defused) {
        spikeDefused = defused;
        if (defused) {
            System.out.println("[SpikeClient] Spike marked as defused");
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        var conn = mc.getConnection();
        if (player == null || conn == null) return;

        // Check if we should cancel defusing
        if (isDefusing) {
            boolean shouldCancel = false;

            // Check if defuser is no longer in slot 4
            ItemStack slot4Item = player.getInventory().getItem(3); // Hotbar slot 4 (index 3)
            if (!slot4Item.is(ModItems.DEFUSER.get())) {
                shouldCancel = true;
            }

            // Check if player is no longer in slot 4 (simplified check)
            // We can detect scrolling by checking if the player has switched away from the defuser
            // For now, rely on the key release and defuser removal checks

            // Check if '4' key is no longer pressed
            if (!ModKeyBindings.EQUIP_SPIKE_OR_DEFUSE.isDown()) {
                shouldCancel = true;
            }

            if (shouldCancel) {
                // Cancel defusing
                System.out.println("[SpikeClient] Canceling defusing - reason: " +
                    (!slot4Item.is(ModItems.DEFUSER.get()) ? "defuser missing" : "key released"));
                isDefusing = false;
                requireDefuseKeyRelease = true; // block re-starts until key fully released
                TitleOverlay.hide(); // Hide the overlay when defusing is cancelled
                ClientPacketDistributor.sendToServer(new DefuseSpikePacket(DefuseSpikePacket.Action.CANCEL));
            }
        }

        // Check if we should cancel planting
        if (isPlanting) {
            boolean shouldCancel = false;

            // Check if spike is no longer held
            ItemStack held = player.getMainHandItem();
            if (!held.is(ModItems.SPIKE.get())) {
                shouldCancel = true;
            }

            // Check if left mouse button is no longer pressed
            if (!mc.mouseHandler.isLeftPressed()) {
                shouldCancel = true;
            }

            // Cancel if player moved outside plant sites while holding
            if (!isInsideAnyClientSite(player)) {
                shouldCancel = true;
            }

            if (shouldCancel) {
                // Cancel planting
                System.out.println("[SpikeClient] Canceling planting - reason: " +
                    (!held.is(ModItems.SPIKE.get()) ? "spike not held" : "mouse released"));
                isPlanting = false;
                TitleOverlay.hide(); // Hide the overlay when planting is cancelled
                ClientPacketDistributor.sendToServer(new PlantSpikePacket(PlantSpikePacket.Action.CANCEL));
            }
        }

        // If we cancelled recently, wait for the key to be released before accepting another press
        if (requireDefuseKeyRelease) {
            if (!ModKeyBindings.EQUIP_SPIKE_OR_DEFUSE.isDown()) {
                requireDefuseKeyRelease = false; // key released, allow future presses
            }
        }

        // Edge-detect press/release for key 4
        boolean keyDown = ModKeyBindings.EQUIP_SPIKE_OR_DEFUSE.isDown();
        if (!requireDefuseKeyRelease && keyDown && !defuseKeyWasDown) {
            // Pressed this tick
            if (!isDefusing) {
                boolean startedDefuse = tryStartDefuse();
                if (!startedDefuse) {
                    int slot = findHotbarSlot(player, ModItems.SPIKE.get().getDefaultInstance());
                    if (slot >= 0) {
                        try {
                            java.lang.reflect.Field selectedField = Inventory.class.getDeclaredField("selected");
                            selectedField.setAccessible(true);
                            selectedField.set(player.getInventory(), slot);
                        } catch (Exception e) {
                            Valorant.LOGGER.error("[SPIKE CLIENT] Failed to set selected slot via reflection: {}", e.toString());
                        }
                        ClientPacketDistributor.sendToServer(new EquipSpikePacket());
                    }
                }
            }
        }
        if (defuseKeyWasDown && !keyDown && isDefusing) {
            // Released this tick while defusing -> cancel once
            System.out.println("[SpikeClient] Key released -> cancel defusing");
            isDefusing = false;
            requireDefuseKeyRelease = false;
            TitleOverlay.hide();
            ClientPacketDistributor.sendToServer(new DefuseSpikePacket(DefuseSpikePacket.Action.CANCEL));
        }
        defuseKeyWasDown = keyDown;

    }

    private static boolean tryStartDefuse() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return false;

        // Check if player is on defender team (V)
        var team = player.getTeam();
        if (team == null || !"V".equals(team.getName())) return false;

        // Check if spike has already been defused this round
        if (spikeDefused) {
            System.out.println("[SpikeClient] Spike already defused this round, cannot start defusing");
            return false;
        }

        double r = 1.2D;
        AABB box = new AABB(player.getX() - r, player.getY() - r, player.getZ() - r,
                player.getX() + r, player.getY() + r, player.getZ() + r);
        // Check for ArmorStand with planted spike item in head slot
        boolean nearPlanted = !player.level().getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class, box,
                stand -> stand.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(ModItems.PLANTEDSPIKE.get())).isEmpty();
        if (!nearPlanted) return false;
        System.out.println("[SpikeClient] Starting defusing - sending START packet");
        ClientPacketDistributor.sendToServer(new DefuseSpikePacket(DefuseSpikePacket.Action.START));
        // Defusing takes 7 seconds (7000ms), overlay stays visible until action completes
        TitleOverlay.showWithProgress("Defusing Spike", "Hold position", 10, 1000, 10, 0xFFFFD700, 0xFFFFFF00, 0.0f, 7000L, ModItems.DEFUSER.get().getDefaultInstance());
        isDefusing = true; // Track that we're now defusing
        return true;
    }

    @SubscribeEvent
    public static void onLeftClick(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!event.isAttack()) return;
        ItemStack held = player.getMainHandItem();
        if (!held.is(ModItems.SPIKE.get())) return;
        // Only show overlay and start if allowed: round running and not in buy, team A, inside site
        boolean roundRunning = RoundState.isRunning() && !RoundState.isBuyPhase();
        var team = player.getTeam();
        boolean isAttacker = team != null && "A".equals(team.getName());
        boolean inSite = isInsideAnyClientSite(player);
        if (roundRunning && isAttacker && inSite) {
            System.out.println("[SpikeClient] Starting planting - sending START packet");
            ClientPacketDistributor.sendToServer(new PlantSpikePacket(PlantSpikePacket.Action.START));
            int plantTicks = com.bobby.valorant.Config.COMMON.spikePlantHoldTicks.get();
            long plantDurationMs = plantTicks * 50L;
            TitleOverlay.showWithProgress("Planting Spike", "Hold position", 10, 1200, 10, 0xFFFFD700, 0xFFFFFF00, 0.0f, plantDurationMs, ModItems.SPIKE.get().getDefaultInstance());
            isPlanting = true;
            event.setCanceled(true);
        } else {
            // Not allowed: optionally show a brief client message
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Cannot plant here"), true);
            event.setCanceled(true);
        }
    }

    private static boolean isInsideAnyClientSite(LocalPlayer player) {
        java.util.List<net.minecraft.core.BlockPos> sa = SpawnAreaClientState.vertsSiteA;
        java.util.List<net.minecraft.core.BlockPos> sb = SpawnAreaClientState.vertsSiteB;
        java.util.List<net.minecraft.core.BlockPos> sc = SpawnAreaClientState.vertsSiteC;
        double x = player.getX();
        double z = player.getZ();
        return (sa != null && pointInPoly(sa, x, z))
                || (sb != null && pointInPoly(sb, x, z))
                || (sc != null && pointInPoly(sc, x, z));
    }

    private static boolean pointInPoly(java.util.List<net.minecraft.core.BlockPos> vertices, double x, double z) {
        if (vertices == null || vertices.size() < 3) return false;
        boolean inside = false;
        int n = vertices.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = vertices.get(i).getX();
            double zi = vertices.get(i).getZ();
            double xj = vertices.get(j).getX();
            double zj = vertices.get(j).getZ();
            boolean intersect = ((zi > z) != (zj > z)) &&
                    (x < (xj - xi) * (z - zi) / (zj - zi + 1e-9) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }

    private static int findHotbarSlot(LocalPlayer player, ItemStack match) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (ItemStack.isSameItemSameComponents(s, match)) return i;
        }
        return -1;
    }
}



