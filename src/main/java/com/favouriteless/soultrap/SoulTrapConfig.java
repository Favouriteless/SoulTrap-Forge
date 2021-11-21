/*
 * Copyright (c) 2021. Favouriteless
 * SoulTrap-Forge, a minecraft mod.
 * GNU GPLv3 License
 *
 *     This file is part of SoulTrap-Forge.
 *
 *     SoulTrap-Forge is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     SoulTrap-Forge is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with SoulTrap-Forge.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.favouriteless.soultrap;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;


public class SoulTrapConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> BLACKLIST_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_LIST;
    public static final ForgeConfigSpec.ConfigValue<Integer> XP_COST;

    static {
        BUILDER.push("Soul Trap Configuration");
        MOB_LIST = BUILDER.comment("creature_list").defineList("creature_list",
                Arrays.asList("minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:cave_spider", "minecraft:blaze", "minecraft:magma_cube", "minecraft:silverfish"),
                entry -> true);
        BLACKLIST_MODE = BUILDER.comment("Enable blacklist mode").define("blacklist", false);
        XP_COST = BUILDER.comment("XP Levels Consumed").define("cost", 0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}
