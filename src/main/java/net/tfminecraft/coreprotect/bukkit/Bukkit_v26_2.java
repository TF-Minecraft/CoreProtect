package net.tfminecraft.coreprotect.bukkit;

import org.bukkit.Material;

import net.tfminecraft.coreprotect.model.BlockGroup;

/**
 * Bukkit adapter implementation for Minecraft 26.2.
 */
public class Bukkit_v26_2 extends Bukkit_v1_21_5 {

    public Bukkit_v26_2() {
        initializeBlockGroups();
    }

    private void initializeBlockGroups() {
        Material sulfurSpike = Material.matchMaterial("SULFUR_SPIKE");
        if (sulfurSpike != null) {
            BlockGroup.TRACK_TOP_BOTTOM.add(sulfurSpike);
        }
    }

}
