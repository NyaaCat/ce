package com.taiter.ce.Enchantments.Global;

/*
* This file is part of Custom Enchantments
* Copyright (C) Taiterio 2015
*
* This program is free software: you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as published by the
* Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
* for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import com.sk89q.worldguard.protection.flags.Flags;
import com.taiter.ce.Enchantments.CEnchantment;
import com.taiter.ce.Tools;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Shockwave extends CEnchantment {

    int cooldown;
    List<Material> ForbiddenMaterials;

    public Shockwave(EnchantmentTarget app) {
        super(app);
        configEntries.put("Cooldown", 200);
        configEntries.put("ForbiddenMaterials", Material.BEDROCK.name() + "," + Material.WATER.name() + "," + Material.LAVA.name() + "," +
                Material.CACTUS.name() + "," + Material.CAKE.name() + "," + Material.WHEAT.name() + "," + Material.END_PORTAL.name() + "," +
                Material.MOVING_PISTON.name() + "," + Material.MELON_STEM.name() + "," + Material.NETHER_WART.name() + "," + Material.SPAWNER.name() + "," +
                Material.SIGN + "," + Material.WALL_SIGN + "," + Material.PLAYER_HEAD.name() + "," + Material.PLAYER_WALL_HEAD);
        triggers.add(Trigger.DAMAGE_GIVEN);
    }

    @Override
    public void effect(Event e, ItemStack item, final int level) {

        final EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
        Player damager = (Player) event.getDamager();

        new BukkitRunnable() {
            @Override
            public void run() {
                event.getEntity().setVelocity(new Vector(0, 1 + (level / 4), 0));
            }
        }.runTaskLater(getPlugin(), 1l);

        Location loc = damager.getLocation();
        loc.setY(damager.getLocation().getY() - 1);
        List<Location> list = Tools.getCone(loc);
        this.generateCooldown(damager, getOriginalName(), cooldown);
        damager.getWorld().playEffect(damager.getLocation(), Effect.ZOMBIE_DESTROY_DOOR, 10);
        for (final Location l : list) {
            final org.bukkit.block.Block block = l.getBlock();
            Material blockMat = block.getType();
            if (!ForbiddenMaterials.contains(blockMat) && !(block.getState() instanceof Container) && checkSurrounding(block)) {
                if (!Tools.checkWorldGuard(l, damager, Flags.PVP, false))
                    return;
                final BlockData data = block.getBlockData();
                final FallingBlock b = l.getWorld().spawnFallingBlock(l, data);
                b.setDropItem(false);
                b.setVelocity(new Vector(0, (0.5 + 0.1 * (list.indexOf(l))) + (level / 4), 0));
                block.setType(Material.AIR);
                new BukkitRunnable() {
                    Location finLoc = l;

                    @Override
                    public void run() {
                        if (!b.isDead()) {
                            finLoc = b.getLocation();
                        } else {
                            //if(finLoc.getBlock().getLocation() != l) 
                            finLoc.getBlock().setType(Material.AIR);
                            block.setType(blockMat);
                            block.setBlockData(data);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(main, 0l, 5l);
            }
        }

    }

    @Override
    public void initConfigEntries() {
        cooldown = Integer.parseInt(getConfig().getString("Enchantments." + getOriginalName() + ".Cooldown"));
        makeList();
    }

    private boolean checkSurrounding(org.bukkit.block.Block block) {

        if (!block.getRelative(0, 1, 0).getType().isSolid())
            return false;
        if (!block.getRelative(1, 0, 0).getType().isSolid())
            return false;
        if (!block.getRelative(-1, 0, 0).getType().isSolid())
            return false;
        if (!block.getRelative(0, 0, 1).getType().isSolid())
            return false;
        if (!block.getRelative(0, 0, -1).getType().isSolid())
            return false;
        return true;
    }

    private void makeList() {
        ForbiddenMaterials = new ArrayList<Material>();
        String mS = getConfig().getString("Enchantments." + getOriginalName() + ".ForbiddenMaterials");
        mS = mS.replace(" ", "");

        String[] s = mS.split(",");

        for (int i = 0; i < s.length; i++)
            ForbiddenMaterials.add(Material.getMaterial(s[i]));
        if (ForbiddenMaterials.contains(Material.AIR))
            ForbiddenMaterials.remove(Material.AIR);
    }

}
