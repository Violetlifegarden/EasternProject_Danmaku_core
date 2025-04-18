package com.huashanlunjian.amara.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

public class EasternProjectBlock extends AbstractNote{
    private static final int WARMUP_TIME = 20;
    private static final EntityDataAccessor<Direction> MOVE_DIRECTION = SynchedEntityData.defineId(EasternProjectBlock.class, EntityDataSerializers.DIRECTION);

    private BlockState myState;
    private int slideTime;


    public EasternProjectBlock(EntityType<? extends EasternProjectBlock> type, Level world) {
        super(type, world);
        this.blocksBuilding = true;
    }

    public EasternProjectBlock(EntityType<? extends EasternProjectBlock> type, Level world, double x, double y, double z, BlockState state) {
        super(type, world);

        this.myState = state;
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(new Vec3(0, 0, 0));
        this.xo = x;
        this.yo = y;
        this.zo = z;

        this.determineMoveDirection();
    }

    private void determineMoveDirection() {
        BlockPos pos = new BlockPos(this.blockPosition());

        Direction[] toCheck = switch (myState.getValue(RotatedPillarBlock.AXIS)) {
            case X -> // horizontal blocks will go up or down if there is a block on one side and air on the other
                    new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};
            case Z -> // horizontal blocks will go up or down if there is a block on one side and air on the other
                    new Direction[]{Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST};
            case Y -> // vertical blocks priority is -x, +x, -z, +z
                    new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
        };

        for (Direction e : toCheck) {
            if (this.level().isEmptyBlock(pos.relative(e)) && !this.level().isEmptyBlock(pos.relative(e.getOpposite()))) {
                this.getEntityData().set(MOVE_DIRECTION, e);
                return;
            }
        }

        // if no wall, travel towards open air
        for (Direction e : toCheck) {
            if (this.level().isEmptyBlock(pos.relative(e))) {
                this.getEntityData().set(MOVE_DIRECTION, e);
                return;
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MOVE_DIRECTION, Direction.DOWN);
    }

    @Override
    public boolean isSteppingCarefully() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return this.isAlive();
    }



    @Override
    public void tick() {
        if (this.myState == null || this.myState.isAir()) {
            this.myState = Blocks.SAND.defaultBlockState();//discard();
        } else {
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            ++this.slideTime;
            // start moving after warmup
            if (this.slideTime > WARMUP_TIME) {
                final double moveAcceleration = 0.04;
                Direction moveDirection = this.getEntityData().get(MOVE_DIRECTION);
                this.setDeltaMovement(this.getDeltaMovement().add(moveDirection.getStepX() * moveAcceleration, moveDirection.getStepY() * moveAcceleration, moveDirection.getStepZ() * moveAcceleration));
                this.move(MoverType.SELF, new Vec3(this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z()));
            }
            this.getDeltaMovement().multiply(0.98, 0.98, 0.98);

            if (!this.level().isClientSide()) {
                //if (this.slideTime % 5 == 0) {
                //this.playSound(TFSounds.SLIDER.get(), 1.0F, 0.9F + (this.random.nextFloat() * 0.4F));
                //}

                BlockPos pos = new BlockPos(this.blockPosition());

                if (this.slideTime == 1) {
                    if (this.level().getBlockState(pos) != this.myState) {
                        //this.discard();
                        return;
                    }

                    this.level().removeBlock(pos, false);
                }

                if (this.slideTime == WARMUP_TIME + 40) {
                    this.setDeltaMovement(new Vec3(0, 0, 0));

                    this.getEntityData().set(MOVE_DIRECTION, getEntityData().get(MOVE_DIRECTION).getOpposite());
                }

                if (this.verticalCollision || this.horizontalCollision) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, 0.7D, 0.7D));

                    //this.discard();

                    if (this.level().isUnobstructed(this.myState, pos, CollisionContext.empty())) {
                        this.level().setBlockAndUpdate(pos, this.myState);
                    } else {
                        this.spawnAtLocation(new ItemStack(this.myState.getBlock()), 0.0F);
                    }
                } else if (this.slideTime > 100 && (pos.getY() < this.level().getMinBuildHeight() + 1 || pos.getY() > this.level().getMaxBuildHeight()) || this.slideTime > 600) {
                    this.spawnAtLocation(new ItemStack(this.myState.getBlock()), 0.0F);
                    this.discard();
                }

                // push things out and damage them
                this.damageKnockbackEntities(this.level().getEntities(this, this.getBoundingBox()));
            }
        }
    }

    private void damageKnockbackEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                //living.hurt(TFDamageTypes.getDamageSource(this.level(), TFDamageTypes.SLIDER), 5.0F);

                double kx = (this.getX() - entity.getX()) * 2.0D;
                double kz = (this.getZ() - entity.getZ()) * 2.0D;

                living.knockback(2.0F, kx, kz);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        this.slideTime = compound.getInt("Time");
        this.getEntityData().set(MOVE_DIRECTION, Direction.from3DDataValue(compound.getByte("Direction")));
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        compound.putInt("Time", this.slideTime);
        compound.putByte("Direction", (byte) this.getEntityData().get(MOVE_DIRECTION).get3DDataValue());
    }

//    @Override
//    public void writeSpawnData(FriendlyByteBuf buffer) {
//        buffer.writeInt(Block.getId(this.myState));
//    }
//
//    @Override
//    public void readSpawnData(FriendlyByteBuf additionalData) {
//        this.myState = Block.stateById(additionalData.readInt());
//    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected boolean canRide(Entity entityIn) {
        return false;
    }

    public BlockState getBlockState() {
        return this.myState;
    }
}
