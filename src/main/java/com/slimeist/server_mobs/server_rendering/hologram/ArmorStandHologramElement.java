package com.slimeist.server_mobs.server_rendering.hologram;

import com.mojang.datafixers.util.Pair;
import eu.pb4.holograms.api.elements.AbstractHologramElement;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import eu.pb4.holograms.mixin.accessors.EntityAccessor;
import eu.pb4.holograms.mixin.accessors.EntityTrackerUpdateS2CPacketAccessor;
import eu.pb4.holograms.utils.HologramHelper;
import eu.pb4.holograms.utils.PacketHelpers;
import eu.pb4.polymer.mixin.client.item.packet.EntityEquipmentUpdateS2CPacketMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ArmorStandHologramElement extends AbstractHologramElement {
    protected final ArmorStandEntity entity;
    protected boolean equipmentDirty = false;

    public ArmorStandHologramElement(ArmorStandEntity entity) {
        this(entity, false);
    }

    public ArmorStandHologramElement(ArmorStandEntity entity, boolean zeroHeight) {
        this.height = zeroHeight ? 0 : entity.getHeight() + 0.1;
        this.entityIds.add(entity.getId());
        this.entity = entity;

        if (this.entity.world.getEntityById(this.entity.getId()) != null) {
            throw new IllegalArgumentException("Entity can't exist in world!");
        }
        this.entity.setUuid(HologramHelper.getUUID());
    }

    @Override
    public void createSpawnPackets(ServerPlayerEntity player, AbstractHologram hologram) {
        Vec3d pos = hologram.getElementPosition(this).add(this.offset);
        this.entity.setPos(pos.x, pos.y - 0.00, pos.z);

        player.networkHandler.sendPacket(this.entity.createSpawnPacket());

        EntityTrackerUpdateS2CPacket packet = PacketHelpers.createEntityTrackerUpdate();
        EntityTrackerUpdateS2CPacketAccessor accessor = (EntityTrackerUpdateS2CPacketAccessor) packet;

        accessor.setId(this.entity.getId());
        List<DataTracker.Entry<?>> data = new ArrayList<>();
        data.addAll(this.entity.getDataTracker().getAllEntries());
        data.add(new DataTracker.Entry<>(EntityAccessor.getNoGravity(), true));
        accessor.setTrackedValues(data);

        player.networkHandler.sendPacket(packet);
        updateEquipment(player);
        player.networkHandler.sendPacket(TeamS2CPacket.changePlayerTeam(HologramHelper.getFakeTeam(), this.entity.getUuidAsString(), TeamS2CPacket.Operation.ADD));
    }

    public void updateEquipment(ServerPlayerEntity player) {
        ArrayList<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.add(Pair.of(slot, this.entity.getEquippedStack(slot)));
        }
        EntityEquipmentUpdateS2CPacket armorPacket = new EntityEquipmentUpdateS2CPacket(this.entity.getId(), equipment);
        player.networkHandler.sendPacket(armorPacket);
    }

    @Override
    public void createRemovePackets(ServerPlayerEntity player, AbstractHologram hologram) {
        player.networkHandler.sendPacket(TeamS2CPacket.changePlayerTeam(HologramHelper.getFakeTeam(), this.entity.getUuidAsString(), TeamS2CPacket.Operation.REMOVE));
        super.createRemovePackets(player, hologram);
    }

    @Override
    public void updatePosition(ServerPlayerEntity player, AbstractHologram hologram) {
        Vec3d pos = hologram.getElementPosition(this).add(this.offset);
        this.entity.updatePosition(pos.x, pos.y - 0.00, pos.z);

        //this.createRemovePackets(player, hologram);
        //this.createSpawnPackets(player, hologram);

        {
            EntityPositionS2CPacket posPacket = new EntityPositionS2CPacket(this.entity);
            player.networkHandler.sendPacket(posPacket);
        }
        {
            EntityTrackerUpdateS2CPacket packet = PacketHelpers.createEntityTrackerUpdate();
            EntityTrackerUpdateS2CPacketAccessor accessor = (EntityTrackerUpdateS2CPacketAccessor) packet;

            accessor.setId(this.entity.getId());
            List<DataTracker.Entry<?>> data = new ArrayList<>();
            data.addAll(this.entity.getDataTracker().getAllEntries());
            data.add(new DataTracker.Entry<>(EntityAccessor.getNoGravity(), true));
            accessor.setTrackedValues(data);

            player.networkHandler.sendPacket(packet);
        }
        if (this.equipmentDirty) {
            this.updateEquipment(player);
            this.equipmentDirty = false;
        }
    }

    public void setOffset(Vec3d offset) {
        this.offset = offset;
    }

    public void markEquipmentDirty() {
        this.equipmentDirty = true;
    }
}
