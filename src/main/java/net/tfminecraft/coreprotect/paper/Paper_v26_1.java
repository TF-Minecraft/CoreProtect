package net.tfminecraft.coreprotect.paper;

import java.util.List;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;

import net.tfminecraft.coreprotect.bukkit.BukkitAdapter;

public class Paper_v26_1 extends Paper_26_0 {

    @Override
    public boolean getEntityMeta(LivingEntity entity, List<Object> info) {
        if (entity instanceof Chicken) {
            return BukkitAdapter.getRegistryVariant(BukkitAdapter.ADAPTER, entity, info, "getSoundVariant");
        }
        else if (entity.getType() == EntityType.COW) {
            return BukkitAdapter.getRegistryVariant(BukkitAdapter.ADAPTER, entity, info, "getSoundVariant");
        }
        else if (entity instanceof Pig) {
            return BukkitAdapter.getRegistryVariant(BukkitAdapter.ADAPTER, entity, info, "getSoundVariant");
        }

        return super.getEntityMeta(entity, info);
    }

    @Override
    public boolean setEntityMeta(Entity entity, Object value, int count) {
        if (entity instanceof Chicken && count == 1) {
            return BukkitAdapter.setRegistryVariant(BukkitAdapter.ADAPTER, entity, value, "getSoundVariant", "setSoundVariant");
        }
        else if (entity.getType() == EntityType.COW && count == 1) {
            return BukkitAdapter.setRegistryVariant(BukkitAdapter.ADAPTER, entity, value, "getSoundVariant", "setSoundVariant");
        }
        else if (entity instanceof Pig && count == 2) {
            return BukkitAdapter.setRegistryVariant(BukkitAdapter.ADAPTER, entity, value, "getSoundVariant", "setSoundVariant");
        }

        return super.setEntityMeta(entity, value, count);
    }

}
