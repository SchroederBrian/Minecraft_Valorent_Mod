package com.bobby.valorant.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.bobby.valorant.skysmoke.SkySmokeArea;

import net.minecraft.core.BlockPos;

public final class SkySmokeRecordingClient {
    private SkySmokeRecordingClient() {}

    // Client-side cache for recording points visualization
    private static final Map<UUID, RecordingData> RECORDING_DATA = new ConcurrentHashMap<>();

    public static void setRecordingPoints(UUID playerId, SkySmokeArea.Type type, List<BlockPos> points) {
        RECORDING_DATA.put(playerId, new RecordingData(type, points));
    }

    public static void clearRecordingPoints(UUID playerId) {
        RECORDING_DATA.remove(playerId);
    }

    public static RecordingData getRecordingData(UUID playerId) {
        return RECORDING_DATA.get(playerId);
    }

    public record RecordingData(SkySmokeArea.Type type, List<BlockPos> points) {}
}
