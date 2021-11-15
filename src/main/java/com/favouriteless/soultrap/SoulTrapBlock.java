/*
 * Copyright (c) 2021. Favouriteless
 * SoulTrap-Fabric, a minecraft mod.
 * GNU GPLv3 License
 *
 *     This file is part of SoulTrap-Fabric.
 *
 *     SoulTrap-Fabric is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     SoulTrap-Fabric is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with SoulTrap-Fabric.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.favouriteless.soultrap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

public class SoulTrapBlock extends Block {

    public static final Random RANDOM = new Random();

    public SoulTrapBlock(Properties settings) {
        super(settings);
    }


    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if(!world.isClientSide) {
            if(checkValidCage(world, pos)) {
                List<Entity> entities = getTrapEntities(world, pos);
                if(!entities.isEmpty()) {
                    if(entities.size() == 1) {
                        Entity entity = entities.get(0);
                        if(checkValidEntity(entity)) {
                            destroyCage(world, pos);
                            createSpawner(world, pos, (LivingEntity)entity);
                            entity.remove();
                            if(!player.isCreative() && player.experienceLevel >= SoulTrapConfig.XP_COST.get()) {
                                player.giveExperienceLevels(-SoulTrapConfig.XP_COST.get());
                            }
                            world.playSound(null, pos, SoundEvents.ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 1f, 0.3f);
                        } else {
                            player.displayClientMessage(new StringTextComponent("Creature is not valid.").withStyle(TextFormatting.RED), false);
                            world.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE, SoundCategory.BLOCKS, 1f, 0.5f);
                        }
                    } else {
                        player.displayClientMessage(new StringTextComponent("Too many creatures.").withStyle(TextFormatting.RED), false);
                        world.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE, SoundCategory.BLOCKS, 1f, 0.5f);
                    }
                } else {
                    player.displayClientMessage(new StringTextComponent("Creature not found.").withStyle(TextFormatting.RED), false);
                    world.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE, SoundCategory.BLOCKS, 1f, 0.5f);
                }
            } else {
                player.displayClientMessage(new StringTextComponent("Cage is not valid").withStyle(TextFormatting.RED), false);
                world.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE, SoundCategory.BLOCKS, 1f, 0.5f);
            }
            return ActionResultType.CONSUME;
        }
        return ActionResultType.SUCCESS;
    }

    public static boolean checkValidCage(World world, BlockPos pos) {
        BlockPos[] ironBlocks = new BlockPos[] {
                pos.relative(Direction.NORTH).relative(Direction.WEST), // NorthWest
                pos.relative(Direction.NORTH).relative(Direction.EAST), // NorthEast
                pos.relative(Direction.SOUTH).relative(Direction.WEST), // SouthWest
                pos.relative(Direction.SOUTH).relative(Direction.EAST), // SouthEast
                pos.relative(Direction.UP, 3) // Top
        };

        BlockPos[] airBlocks = new BlockPos[] {
                pos.relative(Direction.NORTH),
                pos.relative(Direction.EAST),
                pos.relative(Direction.SOUTH),
                pos.relative(Direction.WEST),
                pos.relative(Direction.UP),
                pos.relative(Direction.UP, 2)
        };

        for(BlockPos ironPos : ironBlocks) {
            if(world.getBlockState(ironPos).getBlock() != Blocks.IRON_BLOCK) {
                return false;
            }
        }

        for(BlockPos airPos : airBlocks) {
            if(!world.getBlockState(airPos).isAir()) {
                return false;
            }
        }


        BlockPos startPos = pos.offset(-1, 1, -1);

        for(int x = 0; x < 3; x++) {
            for(int z = 0; z < 3; z++) {
                for(int y = 0; y < 3; y++) {
                    Block block = world.getBlockState(startPos.offset(x, y, z)).getBlock();
                    if (!(x == 1 && z == 1)) {
                        if(block != Blocks.IRON_BARS) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public static boolean checkValidEntity(Entity entity) {
        String listString = SoulTrapConfig.MOB_LIST.get();
        String[] mobList = listString.split("\\s*,\\s*");

        for(String entityString : mobList) {
            EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityString));
            boolean matches = entity.getType() == entityType;

            return SoulTrapConfig.BLACKLIST_MODE.get() != matches;
        }
        return false;
    }

    public static List<Entity> getTrapEntities(World world, BlockPos pos) {
        List<Entity> entities =  world.getEntities(null, new AxisAlignedBB(pos.offset(new BlockPos(-1, 1, -1)), pos.offset(new BlockPos(1, 3, 1))));

        for(int i = entities.size() - 1; i >= 0; i--) {
            if(!(entities.get(i) instanceof LivingEntity)) {
                entities.remove(i);
            }
        }
        return entities;
    }

    public static void destroyCage(World world, BlockPos pos) {
        BlockPos startPos = pos.offset(-1, 0, -1);

        for(int x = 0; x < 3; x++) {
            for(int y = 0; y < 4; y++) {
                for(int z = 0; z < 3; z++) {
                    world.destroyBlock(startPos.offset(x,y,z), false);
                    spawnParticles(world, startPos.offset(x,y,z));
                }
            }
        }
    }

    public static void createSpawner(World world, BlockPos pos, LivingEntity entity) {
        world.setBlockAndUpdate(pos, Blocks.SPAWNER.defaultBlockState());
        MobSpawnerTileEntity blockEntity = (MobSpawnerTileEntity)world.getBlockEntity(pos);
        blockEntity.getSpawner().setEntityId(entity.getType());
    }

    public static void spawnParticles(World world, BlockPos pos) {
        if(!world.isClientSide) {
            for (int i = 0; i < 3; i++) {
                ((ServerWorld)world).sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.getX() + RANDOM.nextDouble(),
                        pos.getY() + RANDOM.nextDouble(),
                        pos.getZ() + RANDOM.nextDouble(),
                        1,
                        0D, 0D, 0D,
                        0f
                );
            }
        }
    }

}
