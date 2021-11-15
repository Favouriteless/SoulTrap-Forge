package com.favouriteless.soultrap;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(SoulTrap.MOD_ID)
public class SoulTrap
{
    public static final String MOD_ID = "soultrap";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Block> SOUL_TRAP_BLOCK = BLOCKS.register("soul_trap", () -> new SoulTrapBlock(AbstractBlock.Properties.copy(Blocks.OBSIDIAN).lightLevel((state) -> 3).noOcclusion()));
    public static final RegistryObject<Item> SOUL_TRAP_ITEM = ITEMS.register("soul_trap", () -> new BlockItem(SOUL_TRAP_BLOCK.get(), new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_REDSTONE)));

    public SoulTrap() {

        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SoulTrapConfig.SPEC, "soultrap-common.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }
}
