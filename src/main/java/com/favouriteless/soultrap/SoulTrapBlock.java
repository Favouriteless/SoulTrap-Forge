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

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SoulTrapBlock extends Block {

    public static final Random RANDOM = new Random();

    public SoulTrapBlock(Properties settings) {
        super(settings);
    }
    
    

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!world.isClientSide) {
            if(checkValidCage(world, pos)) {
                List<Entity> entities = getTrapEntities(world, pos);
                if(!entities.isEmpty()) {
                    if(entities.size() == 1) {
                        Entity entity = entities.get(0);
                        if(checkValidEntity(entity)) {
                            destroyCage(world, pos);
                            createSpawner(world, pos, (LivingEntity)entity);
                            entity.remove(Entity.RemovalReason.DISCARDED);
                            if(!player.isCreative() && player.experienceLevel >= SoulTrapConfig.XP_COST.get()) {
                                player.giveExperienceLevels(-SoulTrapConfig.XP_COST.get());
                            }
                            world.playSound(null, pos, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.MASTER, 1f, 0.3f);
                        } else {
                            player.displayClientMessage(new TextComponent("Creature is not valid.").withStyle(ChatFormatting.RED), false);
                            world.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE, SoundSource.BLOCKS, 1f, 0.5f);
                        }
                    } else {
                        player.displayClientMessage(new TextComponent("Too many creatures.").withStyle(ChatFormatting.RED), false);
                        world.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE, SoundSource.BLOCKS, 1f, 0.5f);
                    }
                } else {
                    player.displayClientMessage(new TextComponent("Creature not found.").withStyle(ChatFormatting.RED), false);
                    world.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE, SoundSource.BLOCKS, 1f, 0.5f);
                }
            } else {
                player.displayClientMessage(new TextComponent("Cage is not valid").withStyle(ChatFormatting.RED), false);
                world.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE, SoundSource.BLOCKS, 1f, 0.5f);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean checkValidCage(Level world, BlockPos pos) {
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
        List<String> mobList = (List<String>) SoulTrapConfig.MOB_LIST.get();

        for(String entityString : mobList) {
            EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityString));
            boolean matches = entity.getType() == entityType;

            return SoulTrapConfig.BLACKLIST_MODE.get() != matches;
        }
        return false;
    }

    public static List<Entity> getTrapEntities(Level world, BlockPos pos) {
        List<Entity> entities =  world.getEntities(null, new AABB(pos.offset(new BlockPos(-1, 1, -1)), pos.offset(new BlockPos(1, 3, 1))));

        for(int i = entities.size() - 1; i >= 0; i--) {
            if(!(entities.get(i) instanceof LivingEntity)) {
                entities.remove(i);
            }
        }
        return entities;
    }

    public static void destroyCage(Level world, BlockPos pos) {
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

    public static void createSpawner(Level world, BlockPos pos, LivingEntity entity) {
        world.setBlockAndUpdate(pos, Blocks.SPAWNER.defaultBlockState());
        SpawnerBlockEntity blockEntity = (SpawnerBlockEntity)world.getBlockEntity(pos);
        blockEntity.getSpawner().setEntityId(entity.getType());
    }

    public static void spawnParticles(Level world, BlockPos pos) {
        if(!world.isClientSide) {
            for (int i = 0; i < 3; i++) {
                ((ServerLevel)world).sendParticles(ParticleTypes.LARGE_SMOKE,
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
