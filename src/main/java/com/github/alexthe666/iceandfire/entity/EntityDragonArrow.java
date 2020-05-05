package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

public class EntityDragonArrow extends EntityArrow {

    public EntityDragonArrow(World worldIn) {
        super(worldIn);
        this.setDamage(10);
    }

    public EntityDragonArrow(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
        this.setDamage(10);
    }

    public EntityDragonArrow(World worldIn, EntityLivingBase shooter) {
        super(worldIn, shooter);
    }

    @Override
    public void writeEntityToNBT(CompoundNBT tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setDouble("damage", 10);
    }

    @Override
    public void readEntityFromNBT(CompoundNBT tagCompund) {
        super.readEntityFromNBT(tagCompund);
        tagCompund.setDouble("damage", 10);
    }

    @Override
    protected ItemStack getArrowStack() {
        return new ItemStack(IafItemRegistry.DRAGONBONE_ARROW);
    }

}