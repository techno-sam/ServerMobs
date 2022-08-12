package com.slimeist.server_mobs.api.server_rendering.hologram;

import com.mojang.datafixers.util.Pair;
import eu.pb4.holograms.api.InteractionType;
import eu.pb4.holograms.api.elements.AbstractHologramElement;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import eu.pb4.holograms.mixin.accessors.EntityAccessor;
import eu.pb4.holograms.mixin.accessors.EntityTrackerUpdateS2CPacketAccessor;
import eu.pb4.holograms.utils.HologramHelper;
import eu.pb4.holograms.utils.PacketHelpers;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

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
        this.entity.requestTeleport(pos.x, pos.y - 0.00, pos.z);

        player.networkHandler.sendPacket(this.entity.createSpawnPacket());

        EntityTrackerUpdateS2CPacket packet = PacketHelpers.createEntityTrackerUpdate();
        EntityTrackerUpdateS2CPacketAccessor accessor = (EntityTrackerUpdateS2CPacketAccessor) packet;

        accessor.setId(this.entity.getId());
        List<DataTracker.Entry<?>> data = new ArrayList<>(this.entity.getDataTracker().getAllEntries());
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
        this.entity.requestTeleport(pos.x, pos.y - 0.00, pos.z);

        {
            EntityPositionS2CPacket posPacket = new EntityPositionS2CPacket(this.entity);
            player.networkHandler.sendPacket(posPacket);
        }
        {
            EntityTrackerUpdateS2CPacket packet = PacketHelpers.createEntityTrackerUpdate();
            EntityTrackerUpdateS2CPacketAccessor accessor = (EntityTrackerUpdateS2CPacketAccessor) packet;

            accessor.setId(this.entity.getId());
            List<DataTracker.Entry<?>> data = new ArrayList<>(this.entity.getDataTracker().getAllEntries());
            data.add(new DataTracker.Entry<>(EntityAccessor.getNoGravity(), true));
            accessor.setTrackedValues(data);

            player.networkHandler.sendPacket(packet);
        }

        if (this.equipmentDirty) {
            this.updateEquipment(player);
            this.equipmentDirty = false;
        }
        //*/
    }

    public void setOffset(Vec3d offset) {
        this.offset = offset;
    }

    public void markEquipmentDirty() {
        this.equipmentDirty = true;
    }

    //Since this element is intended for use to display a server-side entity, it is helpful to redirect attacks through to the entity beyond
    @Override
    public void onClick(AbstractHologram hologram, ServerPlayerEntity player, InteractionType type, @Nullable Hand hand, @Nullable Vec3d vec, int entityId) {
        float d = player.interactionManager.getGameMode().isCreative() ? 6.0f : 4.5f;
        Vec3d camPos = player.getCameraPosVec(0.5f);
        Vec3d rot = player.getRotationVec(1.0f);
        Vec3d endPos = camPos.add(rot.x * d, rot.y * d, rot.z * d);
        Box box = player.getBoundingBox().stretch(rot.multiply(d)).expand(1.0, 1.0, 1.0);
        EntityHitResult hitResult = ProjectileUtil.raycast(player, camPos, endPos, box, entity -> !entity.isSpectator() && entity.collides(), d * d);

        //ServerMobsMod.LOGGER.info("onClick: type = "+type);

        switch (type) {
            case ATTACK:
                if (hitResult != null) {
                    //ServerMobsMod.LOGGER.info("Player "+player.getName().getString()+" hit entity "+hitResult.getEntity().getEntityName());
                    player.attack(hitResult.getEntity());
                }
                break;
            case INTERACT:
                //ServerMobsMod.LOGGER.info("INTERACT");
                if (hitResult != null) {
                    //ServerMobsMod.LOGGER.info("Hit entity "+hitResult.getEntity().getEntityName());
                    if (vec != null && hitResult.getEntity() instanceof ArmorStandEntity) {
                        //ServerMobsMod.LOGGER.info("interact_at");
                        processInteract(player, hand, hitResult.getEntity(), (p, e, h) -> e.interactAt(p, hitResult.getPos(), h));
                    } else {
                        //ServerMobsMod.LOGGER.info("PE::interact");
                        processInteract(player, hand, hitResult.getEntity(), PlayerEntity::interact);
                    }
                }
                break;
            default:
                super.onClick(hologram, player, type, hand, vec, entityId);
        }
    }

    private void processInteract(ServerPlayerEntity player, Hand hand, Entity entity, Interaction action) {
        ItemStack itemStack = player.getStackInHand(hand).copy();
        ActionResult actionResult = action.run(player, entity, hand);
        if (actionResult.isAccepted()) {
            Criteria.PLAYER_INTERACTED_WITH_ENTITY.trigger(player, itemStack, entity);
            if (actionResult.shouldSwingHand()) {
                player.swingHand(hand, true);
            }
        }
    }

    @FunctionalInterface
    interface Interaction {
        ActionResult run(ServerPlayerEntity var1, Entity var2, Hand var3);
    }
}
