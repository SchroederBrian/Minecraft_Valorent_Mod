package com.bobby.valorant.world.agent;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.bobby.valorant.Valorant;

public enum Agent {
    JETT("jett", Component.translatable("agent.valorant.jett"), "Duelist"),
    REYNA("reyna", Component.translatable("agent.valorant.reyna"), "Duelist"),
    RAZE("raze", Component.translatable("agent.valorant.raze"), "Duelist"),
    YORU("yoru", Component.translatable("agent.valorant.yoru"), "Duelist"),
    PHOENIX("phoenix", Component.translatable("agent.valorant.phoenix"), "Duelist"),
    NEON("neon", Component.translatable("agent.valorant.neon"), "Duelist"),
    
    SAGE("sage", Component.translatable("agent.valorant.sage"), "Sentinel"),
    CYPHER("cypher", Component.translatable("agent.valorant.cypher"), "Sentinel"),
    KILLJOY("killjoy", Component.translatable("agent.valorant.killjoy"), "Sentinel"),
    CHAMBER("chamber", Component.translatable("agent.valorant.chamber"), "Sentinel"),
    DEADLOCK("deadlock", Component.translatable("agent.valorant.deadlock"), "Sentinel"),
    
    BRIMSTONE("brimstone", Component.translatable("agent.valorant.brimstone"), "Controller"),
    VIPER("viper", Component.translatable("agent.valorant.viper"), "Controller"),
    OMEN("omen", Component.translatable("agent.valorant.omen"), "Controller"),
    ASTRA("astra", Component.translatable("agent.valorant.astra"), "Controller"),
    HARBOR("harbor", Component.translatable("agent.valorant.harbor"), "Controller"),
    CLOVE("clove", Component.translatable("agent.valorant.clove"), "Controller"),
    
    SOVA("sova", Component.translatable("agent.valorant.sova"), "Initiator"),
    BREACH("breach", Component.translatable("agent.valorant.breach"), "Initiator"),
    SKYE("skye", Component.translatable("agent.valorant.skye"), "Initiator"),
    KAYO("kayo", Component.translatable("agent.valorant.kayo"), "Initiator"),
    FADE("fade", Component.translatable("agent.valorant.fade"), "Initiator"),
    GECKO("gekko", Component.translatable("agent.valorant.gekko"), "Initiator"),
    ISO("iso", Component.translatable("agent.valorant.iso"), "Initiator");
    
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

