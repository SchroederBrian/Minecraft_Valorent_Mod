package com.bobby.valorant.world.agent;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.bobby.valorant.Valorant;

public enum Agent {
    OMEN("omen", Component.translatable("agent.valorant.omen"), "Controller"),
    RAZE("raze", Component.translatable("agent.valorant.raze"), "Duelist"),
    JETT("jett", Component.translatable("agent.valorant.jett"), "Duelist"),
    BRIMSTONE("brimstone", Component.translatable("agent.valorant.brimstone"), "Controller"),
    PHOENIX("phoenix", Component.translatable("agent.valorant.phoenix"), "Duelist"),
    SAGE("sage", Component.translatable("agent.valorant.sage"), "Sentinel"),
    SOVA("sova", Component.translatable("agent.valorant.sova"), "Initiator");
    
    private final String id;
    private final Component displayName;
    private final String role;
    
    Agent(String id, Component displayName, String role) {
        this.id = id;
        this.displayName = displayName;
        this.role = role;
    }
    
    public String getId() {
        return id;
    }
    
    public Component getDisplayName() {
        return displayName;
    }
    
    public String getRole() {
        return role;
    }
    
    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "textures/agent/" + id + ".png");
    }
    
    public static Agent byId(String id) {
        for (Agent agent : values()) {
            if (agent.id.equals(id)) {
                return agent;
            }
        }
        return JETT; // Default fallback
    }
}

