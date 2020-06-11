package com.github.alexthe666.iceandfire.entity.util;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.entity.*;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.gen.Heightmap;

import javax.annotation.Nullable;
import java.util.List;

public class DragonUtils {

    public static BlockPos getBlockInViewEscort(EntityDragonBase dragon) {
        float radius = 12 * (0.7F * dragon.getRenderSize() / 3);
        float neg = dragon.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = dragon.renderYawOffset;
        BlockPos escortPos = dragon.getEscortPosition();
        BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, escortPos);
        int distFromGround = escortPos.getY() - ground.getY();
        for (int i = 0; i < 10; i++) {
            BlockPos pos = new BlockPos(escortPos.getX() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance) - IafConfig.dragonWanderFromHomeDistance / 2,
                    (distFromGround > 16 ? escortPos.getY() : escortPos.getY() + 8 + dragon.getRNG().nextInt(16)),
                    (escortPos.getZ() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance) - IafConfig.dragonWanderFromHomeDistance / 2));
            if (!dragon.isTargetBlocked(new Vec3d(pos)) && dragon.getDistanceSquared(new Vec3d(pos)) > 6) {
                return pos;
            }
        }
        return null;
    }

    public static BlockPos getBlockInView(EntityDragonBase dragon) {
        float radius = 12 * (0.7F * dragon.getRenderSize() / 3);
        float neg = dragon.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = dragon.renderYawOffset;
        if (dragon.hasHomePosition && dragon.homePos != null) {
            BlockPos dragonPos = new BlockPos(dragon);
            BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, dragonPos);
            int distFromGround = (int) dragon.getPosY() - ground.getY();
            for (int i = 0; i < 10; i++) {
                BlockPos pos = new BlockPos(dragon.homePos.getX() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance * 2) - IafConfig.dragonWanderFromHomeDistance, (distFromGround > 16 ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getPosY() + dragon.getRNG().nextInt(16) - 8) : (int) dragon.getPosY() + dragon.getRNG().nextInt(16) + 1), (dragon.homePos.getZ() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance * 2) - IafConfig.dragonWanderFromHomeDistance));
                if (!dragon.isTargetBlocked(new Vec3d(pos)) && dragon.getDistanceSquared(new Vec3d(pos)) > 6) {
                    return pos;
                }
            }
        }
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (dragon.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(dragon.getPosX() + extraX, 0, dragon.getPosZ() + extraZ);
        BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, radialPos);
        int distFromGround = (int) dragon.getPosY() - ground.getY();
        BlockPos newPos = radialPos.up(distFromGround > 16 ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getPosY() + dragon.getRNG().nextInt(16) - 8) : (int) dragon.getPosY() + dragon.getRNG().nextInt(16) + 1);
        BlockPos pos = dragon.doesWantToLand() ? ground : newPos;
        if (!dragon.isTargetBlocked(new Vec3d(newPos)) && dragon.getDistanceSquared(new Vec3d(newPos)) > 6) {
            return pos;
        }
        return null;
    }

    public static BlockPos getWaterBlockInView(EntityDragonBase dragon) {
        float radius = 0.75F * (0.7F * dragon.getRenderSize() / 3) * -7 - dragon.getRNG().nextInt(dragon.getDragonStage() * 6);
        float neg = dragon.getRNG().nextBoolean() ? 1 : -1;
        float angle = (0.01745329251F * dragon.renderYawOffset) + 3.15F + (dragon.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(dragon.getPosX() + extraX, 0, dragon.getPosZ() + extraZ);
        BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, radialPos);
        int distFromGround = (int) dragon.getPosY() - ground.getY();
        BlockPos newPos = radialPos.up(distFromGround > 16 ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getPosY() + dragon.getRNG().nextInt(16) - 8) : (int) dragon.getPosY() + dragon.getRNG().nextInt(16) + 1);
        BlockPos pos = dragon.doesWantToLand() ? ground : newPos;
        BlockPos surface = dragon.world.getBlockState(newPos.down(2)).getMaterial() != Material.WATER ? newPos.down(dragon.getRNG().nextInt(10) + 1) : newPos;
        if (dragon.getDistanceSquared(new Vec3d(surface)) > 6 && dragon.world.getBlockState(surface).getMaterial() == Material.WATER) {
            return surface;
        }
        return null;
    }

    public static LivingEntity riderLookingAtEntity(LivingEntity dragon, LivingEntity rider, double dist) {
        Vec3d vec3d = rider.getEyePosition(1.0F);
        Vec3d vec3d1 = rider.getLook(1.0F);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
        double d1 = dist;
        Entity pointedEntity = null;
        List<Entity> list = rider.world.getEntitiesInAABBexcluding(rider, rider.getBoundingBox().expand(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).grow(1.0D, 1.0D, 1.0D), new Predicate<Entity>() {
            public boolean apply(@Nullable Entity entity) {
                if (onSameTeam(dragon, entity)) {
                    return false;
                }
                return entity != null && entity.canBeCollidedWith() && entity instanceof LivingEntity && !entity.isEntityEqual(dragon) && !entity.isOnSameTeam(dragon) && (!(entity instanceof IDeadMob) || !((IDeadMob) entity).isMobDead());
            }
        });
        double d2 = d1;
        for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = list.get(j);
            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow((double) entity1.getCollisionBorderSize() + 2F);
            Vec3d raytraceresult = axisalignedbb.rayTrace(vec3d, vec3d2).orElse(Vec3d.ZERO);

            if (axisalignedbb.contains(vec3d)) {
                if (d2 >= 0.0D) {
                    pointedEntity = entity1;
                    d2 = 0.0D;
                }
            } else if (raytraceresult != null) {
                double d3 = vec3d.distanceTo(raytraceresult);

                if (d3 < d2 || d2 == 0.0D) {
                    if (entity1.getLowestRidingEntity() == rider.getLowestRidingEntity() && !rider.canRiderInteract()) {
                        if (d2 == 0.0D) {
                            pointedEntity = entity1;
                        }
                    } else {
                        pointedEntity = entity1;
                        d2 = d3;
                    }
                }
            }
        }
        return (LivingEntity) pointedEntity;
    }

    public static BlockPos getBlockInViewHippogryph(EntityHippogryph hippo) {
        float radius = 0.75F * (0.7F * 8) * -3 - hippo.getRNG().nextInt(48);
        float neg = hippo.getRNG().nextBoolean() ? 1 : -1;
        float angle = (0.01745329251F * hippo.renderYawOffset) + 3.15F + (hippo.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        if (hippo.hasHomePosition && hippo.homePos != null) {
            BlockPos dragonPos = new BlockPos(hippo);
            BlockPos ground = hippo.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, dragonPos);
            int distFromGround = (int) hippo.getPosY() - ground.getY();
            for (int i = 0; i < 10; i++) {
                BlockPos pos = new BlockPos(hippo.homePos.getX() + hippo.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance) - IafConfig.dragonWanderFromHomeDistance, (distFromGround > 16 ? (int) Math.min(IafConfig.maxDragonFlight, hippo.getPosY() + hippo.getRNG().nextInt(16) - 8) : (int) hippo.getPosY() + hippo.getRNG().nextInt(16) + 1), (hippo.homePos.getZ() + hippo.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance * 2) - IafConfig.dragonWanderFromHomeDistance));
                if (!hippo.isTargetBlocked(new Vec3d(pos)) && hippo.getDistanceSquared(new Vec3d(pos)) > 6) {
                    return pos;
                }
            }
        }
        BlockPos radialPos = new BlockPos(hippo.getPosX() + extraX, 0, hippo.getPosZ() + extraZ);
        BlockPos ground = hippo.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, radialPos);
        int distFromGround = (int) hippo.getPosY() - ground.getY();
        BlockPos newPos = radialPos.up(distFromGround > 16 ? (int) Math.min(IafConfig.maxDragonFlight, hippo.getPosY() + hippo.getRNG().nextInt(16) - 8) : (int) hippo.getPosY() + hippo.getRNG().nextInt(16) + 1);
        BlockPos pos = hippo.doesWantToLand() ? ground : newPos;
        if (!hippo.isTargetBlocked(new Vec3d(newPos)) && hippo.getDistanceSquared(new Vec3d(newPos)) > 6) {
            return newPos;
        }
        return null;
    }

    public static BlockPos getBlockInViewStymphalian(EntityStymphalianBird bird) {
        float radius = 0.75F * (0.7F * 6) * -3 - bird.getRNG().nextInt(24);
        float neg = bird.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = bird.flock != null && !bird.flock.isLeader(bird) ? getStymphalianFlockDirection(bird) : bird.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (bird.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = getStymphalianFearPos(bird, new BlockPos(bird.getPosX() + extraX, 0, bird.getPosZ() + extraZ));
        BlockPos ground = bird.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, radialPos);
        int distFromGround = (int) bird.getPosY() - ground.getY();
        int flightHeight = Math.min(IafConfig.stymphalianBirdFlightHeight, bird.flock != null && !bird.flock.isLeader(bird) ? ground.getY() + bird.getRNG().nextInt(16) : ground.getY() + bird.getRNG().nextInt(16));
        BlockPos newPos = radialPos.up(distFromGround > 16 ? flightHeight : (int) bird.getPosY() + bird.getRNG().nextInt(16) + 1);
        BlockPos pos = bird.doesWantToLand() ? ground : newPos;
        if (!bird.isTargetBlocked(new Vec3d(newPos)) && bird.getDistanceSquared(new Vec3d(newPos)) > 6) {
            return newPos;
        }
        return null;
    }

    private static BlockPos getStymphalianFearPos(EntityStymphalianBird bird, BlockPos fallback) {
        if (bird.getVictor() != null && bird.getVictor() instanceof CreatureEntity) {
            Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom((CreatureEntity) bird.getVictor(), 16, IafConfig.stymphalianBirdFlightHeight, new Vec3d(bird.getVictor().getPosX(), bird.getVictor().getPosY(), bird.getVictor().getPosZ()));
            if (vec3d != null) {
                BlockPos pos = new BlockPos(vec3d);
                return new BlockPos(pos.getX(), 0, pos.getZ());
            }
        }
        return fallback;
    }

    private static float getStymphalianFlockDirection(EntityStymphalianBird bird) {
        EntityStymphalianBird leader = bird.flock.getLeader();
        if (bird.getDistanceSq(leader) > 2) {
            double d0 = leader.getPosX() - bird.getPosX();
            double d2 = leader.getPosZ() - bird.getPosZ();
            double d1 = leader.getPosY() + (double) leader.getEyeHeight() - (bird.getPosY() + (double) bird.getEyeHeight());
            double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
            float f = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
            float degrees = MathHelper.wrapDegrees(f - bird.rotationYaw);

            return bird.rotationYaw + degrees;
        } else {
            return leader.renderYawOffset;
        }
    }

    public static BlockPos getBlockInTargetsViewCockatrice(EntityCockatrice cockatrice, LivingEntity target) {
        float radius = 10 + cockatrice.getRNG().nextInt(10);
        float neg = cockatrice.getRNG().nextBoolean() ? 1 : -1;
        float angle = (0.01745329251F * target.rotationYawHead);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(target.getPosX() + extraX, 0, target.getPosZ() + extraZ);
        BlockPos ground = target.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, radialPos);
        if (!cockatrice.isTargetBlocked(new Vec3d(ground)) && cockatrice.getDistanceSq(new Vec3d(ground)) > 30) {
            return ground;
        }
        return target.getPosition();
    }

    public static BlockPos getBlockInTargetsViewSeaSerpent(EntitySeaSerpent serpent, LivingEntity target) {
        float radius = 10 * serpent.getSeaSerpentScale() + serpent.getRNG().nextInt(10);
        float neg = serpent.getRNG().nextBoolean() ? 1 : -1;
        float angle = (0.01745329251F * target.rotationYawHead);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(target.getPosX() + extraX, 0, target.getPosZ() + extraZ);
        BlockPos ground = target.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, radialPos);
        if (serpent.getDistanceSq(new Vec3d(ground)) > 30) {
            return ground;
        }
        return target.getPosition();
    }

    public static boolean canTameDragonAttack(TameableEntity dragon, Entity entity) {
        String className = entity.getClass().getSimpleName();
        if (className.contains("VillagerMCA") || className.contains("MillVillager") || className.contains("Citizen")) {
            return false;
        }
        if (entity instanceof AbstractVillagerEntity || entity instanceof GolemEntity || entity instanceof PlayerEntity) {
            return false;
        }
        if (entity instanceof TameableEntity) {
            return !((TameableEntity) entity).isTamed();
        }
        return true;
    }

    public static boolean isVillager(Entity entity) {
        String className = entity.getClass().getSimpleName();
        return entity instanceof IMerchant || className.contains("VillagerMCA") || className.contains("MillVillager") || className.contains("Citizen");
    }

    public static boolean isAnimaniaMob(Entity entity) {
        String className = entity.getClass().getCanonicalName().toLowerCase();
        return className.contains("animania");
    }

    public static boolean isLivestock(Entity entity) {
        String className = entity.getClass().getSimpleName();
        return entity instanceof CowEntity || entity instanceof SheepEntity || entity instanceof PigEntity || entity instanceof ChickenEntity
                || entity instanceof RabbitEntity || entity instanceof AbstractHorseEntity
                || className.contains("Cow") || className.contains("Sheep") || className.contains("Pig") || className.contains("Chicken")
                || className.contains("Rabbit") || className.contains("Peacock") || className.contains("Goat") || className.contains("Ferret")
                || className.contains("Hedgehog") || className.contains("Peahen") || className.contains("Peafowl") || className.contains("Sow")
                || className.contains("Hog") || className.contains("Hog");
    }

    public static boolean canDragonBreak(Block block) {
        return block != Blocks.BARRIER &&
                block != Blocks.OBSIDIAN &&
                block != Blocks.END_STONE &&
                block != Blocks.BEDROCK &&
                block != Blocks.END_PORTAL &&
                block != Blocks.END_PORTAL_FRAME &&
                block != Blocks.COMMAND_BLOCK &&
                block != Blocks.REPEATING_COMMAND_BLOCK &&
                block != Blocks.CHAIN_COMMAND_BLOCK &&
                block != Blocks.IRON_BARS &&
                block != Blocks.END_GATEWAY &&
                !isBlacklistedBlock(block);
    }

    public static boolean hasSameOwner(TameableEntity cockatrice, Entity entity) {
        if (entity instanceof TameableEntity) {
            TameableEntity tameable = (TameableEntity) entity;
            return tameable.getOwnerId() != null && cockatrice.getOwnerId() != null && tameable.getOwnerId().equals(cockatrice.getOwnerId());
        }
        return false;
    }

    public static boolean isAlive(LivingEntity entity) {
        return (!(entity instanceof IDeadMob) || !((IDeadMob) entity).isMobDead()) && !EntityGorgon.isStoneMob(entity);
    }


    public static boolean canGrief(boolean weak) {
        if (weak) {
            return IafConfig.dragonGriefing == 0;

        } else {
            return IafConfig.dragonGriefing < 2;
        }
    }

    public static boolean isBlacklistedBlock(Block block) {
        if (IafConfig.blacklistBreakBlocksIsWhiteList) {
            for (String name : IafConfig.blacklistedBreakBlocks) {
                if (name.equalsIgnoreCase(block.getRegistryName().toString())) {
                    return false;
                }
            }
            return true;
        } else {
            for (String name : IafConfig.blacklistedBreakBlocks) {
                if (name.equalsIgnoreCase(block.getRegistryName().toString())) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean canHostilesTarget(Entity entity) {
        if (entity instanceof PlayerEntity && entity.world.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        } else {
            return entity instanceof LivingEntity && isAlive((LivingEntity) entity);
        }
    }

    public static boolean onSameTeam(Entity entity1, Entity entity2) {
        Entity owner1 = null;
        Entity owner2 = null;
        boolean def = entity1.isOnSameTeam(entity2);
        if (entity1 instanceof TameableEntity) {
            owner1 = ((TameableEntity) entity1).getOwner();
        }
        if (entity2 instanceof TameableEntity) {
            owner2 = ((TameableEntity) entity2).getOwner();
        }
        if (entity1 instanceof EntityMutlipartPart) {
            Entity multipart = ((EntityMutlipartPart) entity1).getParent();
            if (multipart != null && multipart instanceof TameableEntity) {
                owner1 = ((TameableEntity) multipart).getOwner();
            }
        }
        if (entity2 instanceof EntityMutlipartPart) {
            Entity multipart = ((EntityMutlipartPart) entity2).getParent();
            if (multipart != null && multipart instanceof TameableEntity) {
                owner2 = ((TameableEntity) multipart).getOwner();
            }
        }
        if (owner1 != null && owner2 != null) {
            return owner1.isEntityEqual(owner2);
        }
        return def;
    }

    public static boolean canDropFromDragonBlockBreak(BlockState state) {
        for (String name : IafConfig.noDropBreakBlocks) {
            if (name.equalsIgnoreCase(state.getBlock().getRegistryName().toString())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDreadBlock(BlockState state) {
        Block block = state.getBlock();
        return block == IafBlockRegistry.DREAD_STONE || block == IafBlockRegistry.DREAD_STONE_BRICKS || block == IafBlockRegistry.DREAD_STONE_BRICKS_CHISELED ||
                block == IafBlockRegistry.DREAD_STONE_BRICKS_CRACKED || block == IafBlockRegistry.DREAD_STONE_BRICKS_MOSSY || block == IafBlockRegistry.DREAD_STONE_TILE ||
                block == IafBlockRegistry.DREAD_STONE_FACE || block == IafBlockRegistry.DREAD_TORCH || block == IafBlockRegistry.DREAD_STONE_BRICKS_STAIRS ||
                block == IafBlockRegistry.DREAD_STONE_BRICKS_SLAB || block == IafBlockRegistry.DREADWOOD_LOG ||
                block == IafBlockRegistry.DREADWOOD_PLANKS || block == IafBlockRegistry.DREADWOOD_PLANKS_LOCK || block == IafBlockRegistry.DREAD_PORTAL ||
                block == IafBlockRegistry.DREAD_SPAWNER;
    }
}