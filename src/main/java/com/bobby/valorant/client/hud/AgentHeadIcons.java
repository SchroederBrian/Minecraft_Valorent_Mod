package com.bobby.valorant.client.hud;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import com.bobby.valorant.world.agent.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

/**
 * Creates ItemStack player-head icons for agents using developer texture URLs.
 */
public final class AgentHeadIcons {
    private AgentHeadIcons() {}

    private static final Map<Agent, ItemStack> CACHE = new EnumMap<>(Agent.class);

    private static String urlFor(Agent agent) {
        return switch (agent) {
            case OMEN -> "http://textures.minecraft.net/texture/4f87189638ccefbadcc11adda65af465a0d3eb63eecdeb85462e743305e9789"; // Omen
            case RAZE -> "http://textures.minecraft.net/texture/8e043a67f69f95197df356ebae881f3d58d3ed657b4efd5702b8231c1267249f"; // Raze
            case JETT -> "http://textures.minecraft.net/texture/cc1362098fecb01dd833bdde36de808df39010c275f18bbbccfc8db68593ffe1"; // Jett
            case BRIMSTONE -> "http://textures.minecraft.net/texture/662cb4ad0a8827012e8553e6e6a7d89ec80f940d42456580fb13392a1dbac25"; // Brimstone
            case PHOENIX -> "http://textures.minecraft.net/texture/d996fdab74c1a4db60acadcf090f2105453478b37b28570030ccffbcc437099f"; // Phoenix
            case SAGE -> "http://textures.minecraft.net/texture/c6704181b8ef1847f0a57d35a89c5f47f1f121b03e969175077e981d48e87622"; // Sage
            case SOVA -> "http://textures.minecraft.net/texture/17f9731edf5c460a708f6404733e5cf33b9576fc45996f8f1cd052b02e4465fb"; // Sova
            default -> null; // Add more as you collect URLs
        };
    }

    public static ItemStack get(Agent agent) {
        return CACHE.computeIfAbsent(agent, AgentHeadIcons::createHead);
    }

    private static ItemStack createHead(Agent agent) {
        String url = urlFor(agent);
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        if (url == null) {
            return head; // default plain head
        }
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
        String value = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(("valorant:" + agent.getId()).getBytes(StandardCharsets.UTF_8)), agent.getId());
        profile.getProperties().put("textures", new Property("textures", value));
        head.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        return head;
    }
}


