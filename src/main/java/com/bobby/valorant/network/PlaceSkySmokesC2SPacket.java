package com.bobby.valorant.network;

import java.util.ArrayList;
import java.util.List;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import com.bobby.valorant.ability.Abilities;
import com.bobby.valorant.player.AbilityStateData;
import com.bobby.valorant.player.AgentData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlaceSkySmokesC2SPacket(List<BlockPos> positions) implements CustomPacketPayload {
    public static final Type<PlaceSkySmokesC2SPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "place_sky_smokes"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceSkySmokesC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, pkt) -> {
            buf.writeVarInt(pkt.positions.size());
            for (BlockPos p : pkt.positions) buf.writeBlockPos(p);
        },
        buf -> {
            int n = buf.readVarInt();
            List<BlockPos> list = new ArrayList<>(Math.max(0, n));
            for (int i = 0; i < n; i++) list.add(buf.readBlockPos());
            return new PlaceSkySmokesC2SPacket(list);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private static int findNearestVertexY(BlockPos point, com.bobby.valorant.skysmoke.SkySmokeArea area) {
        if (area.vertices().isEmpty()) return 64; // fallback

        double minDistanceSq = Double.MAX_VALUE;
        int nearestY = area.vertices().get(0).getY();

        for (BlockPos vertex : area.vertices()) {
            double dx = vertex.getX() - point.getX();
            double dz = vertex.getZ() - point.getZ();
            double distanceSq = dx * dx + dz * dz;

            if (distanceSq < minDistanceSq) {
                minDistanceSq = distanceSq;
                nearestY = vertex.getY();
            }
        }
        return nearestY;
    }

    public static void handle(PlaceSkySmokesC2SPacket packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sp)) return;
        ServerLevel level = (ServerLevel) sp.level();

        Valorant.LOGGER.info("[SkySmoke] Player {} placing {} smoke(s) in dimension {}",
            sp.getName().getString(), packet.positions().size(), level.dimension().location());

        var set = Abilities.getForAgent(AgentData.getSelectedAgent(sp));
        var brimE = set.e();
        if (brimE == null) return;

        int available = AbilityStateData.getCharges(sp, brimE);
        if (available <= 0) return;

        // Validate dimension
        String expectedDim = Config.COMMON.skySmokeMapDimensionId.get();
        if (!level.dimension().location().toString().equals(expectedDim)) {
            Valorant.LOGGER.warn("[SkySmoke] Player {} tried to place smokes in wrong dimension {} (expected {})",
                sp.getName().getString(), level.dimension().location(), expectedDim);
            return;
        }

        var areas = com.bobby.valorant.skysmoke.SkySmokeManager.get(level);

        boolean hasAllowedAreas = areas.hasAllowed();
        List<BlockPos> validPositions = new ArrayList<>();
        int filteredOut = 0;

        for (BlockPos pos : packet.positions) {
            if (!hasAllowedAreas) {
                boolean inBlockedArea = areas.isInsideAnyBlocked(pos.getX(), pos.getZ());
                if (!inBlockedArea) {
                    validPositions.add(pos);
                    Valorant.LOGGER.debug("[SkySmoke] Position ({},{}) is valid - no allowed areas defined, not in blocked areas",
                        pos.getX(), pos.getZ());
                } else {
                    filteredOut++;
                    Valorant.LOGGER.debug("[SkySmoke] Filtered out position ({},{}) - inside blocked area", pos.getX(), pos.getZ());
                }
            } else {
                var containingArea = areas.findContainingAllowedArea(pos.getX(), pos.getZ());
                if (containingArea.isPresent()) {
                    boolean inBlockedArea = areas.isInsideAnyBlocked(pos.getX(), pos.getZ());
                    if (!inBlockedArea) {
                        validPositions.add(pos);
                        Valorant.LOGGER.debug("[SkySmoke] Position ({},{}) is valid - inside allowed area '{}' and not in blocked areas",
                            pos.getX(), pos.getZ(), containingArea.get().id());
                    } else {
                        filteredOut++;
                        Valorant.LOGGER.debug("[SkySmoke] Filtered out position ({},{}) - inside blocked area", pos.getX(), pos.getZ());
                    }
                } else {
                    filteredOut++;
                    Valorant.LOGGER.debug("[SkySmoke] Filtered out position ({},{}) - not inside any allowed area", pos.getX(), pos.getZ());
                }
            }
        }

        if (filteredOut > 0) {
            Valorant.LOGGER.info("[SkySmoke] Filtered out {} position(s) - must be inside allowed areas and not in blocked areas", filteredOut);
        }

        int maxPerCast = Config.COMMON.skySmokeMaxPerCast.get();
        int count = Math.min(Math.min(validPositions.size(), available), maxPerCast);
        if (count <= 0) return;

        boolean placeOnGround = Config.COMMON.skySmokePlaceOnGround.get();
        double yOffset = Config.COMMON.skySmokeYOffset.get();
        double radius = Config.COMMON.skySmokeRadius.get();
        int duration = Config.COMMON.skySmokeDurationTicks.get();
        boolean applyBlindness = Config.COMMON.skySmokeApplyBlindness.get();
        int blindTicks = Config.COMMON.skySmokeBlindnessTicks.get();

        for (int i = 0; i < count; i++) {
            BlockPos in = validPositions.get(i);

            // Containing area
            var containingArea = areas.findContainingAllowedArea(in.getX(), in.getZ());
            int baseY;
            if (containingArea.isPresent()) {
                baseY = findNearestVertexY(in, containingArea.get());
                Valorant.LOGGER.debug("[SkySmoke] Smoke {}: Input({},{}) -> Area '{}' nearest vertex Y: {}",
                    i + 1, in.getX(), in.getZ(), containingArea.get().id(), baseY);
            } else {
                baseY = 64;
                Valorant.LOGGER.warn("[SkySmoke] Smoke {}: No containing area found, using fallback Y=64", i + 1);
            }

            // Use nearest vertex Y from allowed areas
            int finalY = baseY + Mth.floor(yOffset);

            // Apply spawn offsets after coordinate transformation
            double spawnXOffset = com.bobby.valorant.Config.COMMON.skySmokeSpawnXOffset.get();
            double spawnZOffset = com.bobby.valorant.Config.COMMON.skySmokeSpawnZOffset.get();
            BlockPos targetPos = BlockPos.containing(in.getX() + spawnXOffset, finalY, in.getZ() + spawnZOffset);
            Valorant.LOGGER.debug("[SkySmoke] Smoke {}: Final position ({},{},{})",
                i + 1, targetPos.getX(), targetPos.getY(), targetPos.getZ());

            // ArmorStand erstellen
            var stand = new ArmorStand(level,
                targetPos.getX() + 0.5D,
                targetPos.getY() + 0.1D,
                targetPos.getZ() + 0.5D);
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setNoGravity(true);
            stand.setSilent(true);
            stand.setShowArms(false);
            stand.setNoBasePlate(true);
            stand.setItemSlot(EquipmentSlot.HEAD,
                com.bobby.valorant.registry.ModItems.SKY_SMOKE_ITEM.get().getDefaultInstance());

            level.addFreshEntity(stand);

            // Rotator-Task inkl. Blindness
            if (applyBlindness && radius - 1 > 0 && blindTicks > 0) {
                com.bobby.valorant.util.ArmorStandRotator.addRotatingArmorStandWithBlindness(
                    stand, duration, (float) radius, blindTicks
                );
            } else {
                com.bobby.valorant.util.ArmorStandRotator.addRotatingArmorStand(stand, duration);
            }
        }

        // Charges updaten und syncen
        AbilityStateData.setCharges(sp, brimE, available - count);

        int c = AbilityStateData.getCharges(sp, set.c());
        int q = AbilityStateData.getCharges(sp, set.q());
        int e = AbilityStateData.getCharges(sp, set.e());
        int x = AbilityStateData.getUltPoints(sp);
        PacketDistributor.sendToPlayer(sp, new SyncAbilityStateS2CPacket(c, q, e, x));
    }
}
