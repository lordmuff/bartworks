/*
 * Copyright (c) 2018-2020 bartimaeusnek Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.github.bartimaeusnek.bartworks.system.material.werkstoff_loaders.recipe;

import static gregtech.api.enums.OrePrefixes.block;
import static gregtech.api.enums.OrePrefixes.crushedPurified;
import static gregtech.api.enums.OrePrefixes.dust;
import static gregtech.api.enums.OrePrefixes.dustSmall;
import static gregtech.api.enums.OrePrefixes.dustTiny;
import static gregtech.api.enums.OrePrefixes.gem;
import static gregtech.api.enums.OrePrefixes.gemChipped;
import static gregtech.api.enums.OrePrefixes.gemExquisite;
import static gregtech.api.enums.OrePrefixes.gemFlawed;
import static gregtech.api.enums.OrePrefixes.gemFlawless;
import static gregtech.api.enums.OrePrefixes.lens;
import static gregtech.api.enums.OrePrefixes.ore;
import static gregtech.api.enums.OrePrefixes.plate;
import static gregtech.api.util.GT_Recipe.GT_Recipe_Map.sCompressorRecipes;
import static gregtech.api.util.GT_Recipe.GT_Recipe_Map.sHammerRecipes;
import static gregtech.api.util.GT_Recipe.GT_Recipe_Map.sImplosionRecipes;
import static gregtech.api.util.GT_Recipe.GT_Recipe_Map.sLaserEngraverRecipes;
import static gregtech.api.util.GT_Recipe.GT_Recipe_Map.sLatheRecipes;
import static gregtech.api.util.GT_Recipe.GT_Recipe_Map.sMaceratorRecipes;
import static gregtech.api.util.GT_Recipe.GT_Recipe_Map.sSifterRecipes;
import static gregtech.api.util.GT_RecipeBuilder.MINUTES;
import static gregtech.api.util.GT_RecipeBuilder.SECONDS;
import static gregtech.api.util.GT_RecipeBuilder.TICKS;
import static gregtech.api.util.GT_RecipeConstants.ADDITIVE_AMOUNT;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.github.bartimaeusnek.bartworks.system.material.Werkstoff;
import com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader;
import com.github.bartimaeusnek.bartworks.system.material.werkstoff_loaders.IWerkstoffRunnable;
import com.github.bartimaeusnek.bartworks.util.BW_ColorUtil;

import gregtech.api.GregTech_API;
import gregtech.api.enums.GT_Values;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Textures;
import gregtech.api.enums.TierEU;
import gregtech.api.interfaces.ITexture;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_OreDictUnificator;

public class GemLoader implements IWerkstoffRunnable {

    @Override
    public void run(Werkstoff werkstoff) {
        if (werkstoff.hasItemType(gem)) {
            if (werkstoff.getGenerationFeatures().hasSifterRecipes()
                    || werkstoff.hasItemType(ore) && werkstoff.hasItemType(dust)) {

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gem, 9)).itemOutputs(werkstoff.get(block))
                        .noFluidInputs().noFluidOutputs().duration(15 * SECONDS).eut(2).addTo(sCompressorRecipes);

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(block)).itemOutputs(werkstoff.get(gem, 9))
                        .noFluidInputs().noFluidOutputs().duration(5 * SECONDS).eut(24).addTo(sHammerRecipes);

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(crushedPurified))
                        .itemOutputs(
                                werkstoff.get(gemExquisite),
                                werkstoff.get(gemFlawless),
                                werkstoff.get(gem),
                                werkstoff.get(gemFlawed),
                                werkstoff.get(gemChipped),
                                werkstoff.get(dust))
                        .outputChances(200, 1000, 2500, 2000, 4000, 5000).noFluidInputs().noFluidOutputs()
                        .duration(40 * SECONDS).eut(16).addTo(sSifterRecipes);

            }

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemExquisite)).itemOutputs(werkstoff.get(dust, 4))
                    .noFluidInputs().noFluidOutputs().duration(20 * SECONDS).eut(2).addTo(sMaceratorRecipes);

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemFlawless)).itemOutputs(werkstoff.get(dust, 2))
                    .noFluidInputs().noFluidOutputs().duration(20 * SECONDS).eut(2).addTo(sMaceratorRecipes);

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gem)).itemOutputs(werkstoff.get(dust)).noFluidInputs()
                    .noFluidOutputs().duration(20 * SECONDS).eut(2).addTo(sMaceratorRecipes);

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemFlawed)).itemOutputs(werkstoff.get(dustSmall, 2))
                    .noFluidInputs().noFluidOutputs().duration(20 * SECONDS).eut(2).addTo(sMaceratorRecipes);

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemChipped)).itemOutputs(werkstoff.get(dustSmall))
                    .noFluidInputs().noFluidOutputs().duration(20 * SECONDS).eut(2).addTo(sMaceratorRecipes);

            GT_ModHandler.addCraftingRecipe(
                    werkstoff.get(gemFlawless, 2),
                    0,
                    new Object[] { "h  ", "W  ", 'W', werkstoff.get(gemExquisite) });
            GT_ModHandler.addCraftingRecipe(
                    werkstoff.get(gem, 2),
                    0,
                    new Object[] { "h  ", "W  ", 'W', werkstoff.get(gemFlawless) });
            GT_ModHandler.addCraftingRecipe(
                    werkstoff.get(gemFlawed, 2),
                    0,
                    new Object[] { "h  ", "W  ", 'W', werkstoff.get(gem) });
            GT_ModHandler.addCraftingRecipe(
                    werkstoff.get(gemChipped, 2),
                    0,
                    new Object[] { "h  ", "W  ", 'W', werkstoff.get(gemFlawed) });

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemExquisite)).itemOutputs(werkstoff.get(gemFlawless, 2))
                    .noFluidInputs().noFluidOutputs().duration(3 * SECONDS + 4 * TICKS).eut(16).addTo(sHammerRecipes);

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemFlawless)).itemOutputs(werkstoff.get(gem, 2))
                    .noFluidInputs().noFluidOutputs().duration(3 * SECONDS + 4 * TICKS).eut(16).addTo(sHammerRecipes);

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gem)).itemOutputs(werkstoff.get(gemFlawed, 2))
                    .noFluidInputs().noFluidOutputs().duration(3 * SECONDS + 4 * TICKS).eut(16).addTo(sHammerRecipes);

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemFlawed)).itemOutputs(werkstoff.get(gemChipped, 2))
                    .noFluidInputs().noFluidOutputs().duration(3 * SECONDS + 4 * TICKS).eut(16).addTo(sHammerRecipes);

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemChipped)).itemOutputs(werkstoff.get(dustTiny))
                    .noFluidInputs().noFluidOutputs().duration(3 * SECONDS + 4 * TICKS).eut(16).addTo(sHammerRecipes);

            if (!werkstoff.contains(WerkstoffLoader.NO_BLAST)) {
                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemFlawless, 3))
                        .itemOutputs(
                                werkstoff.get(gemExquisite),
                                GT_OreDictUnificator.get(dustTiny, Materials.DarkAsh, 2))
                        .noFluidInputs().noFluidOutputs().duration(20 * TICKS).eut(TierEU.RECIPE_LV)
                        .metadata(ADDITIVE_AMOUNT, 8).addTo(sImplosionRecipes);

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gem, 3))
                        .itemOutputs(
                                werkstoff.get(gemFlawless),
                                GT_OreDictUnificator.get(dustTiny, Materials.DarkAsh, 2))
                        .noFluidInputs().noFluidOutputs().duration(20 * TICKS).eut(TierEU.RECIPE_LV)
                        .metadata(ADDITIVE_AMOUNT, 8).addTo(sImplosionRecipes);

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemFlawed, 3))
                        .itemOutputs(werkstoff.get(gem), GT_OreDictUnificator.get(dustTiny, Materials.DarkAsh, 2))
                        .noFluidInputs().noFluidOutputs().duration(20 * TICKS).eut(TierEU.RECIPE_LV)
                        .metadata(ADDITIVE_AMOUNT, 8).addTo(sImplosionRecipes);

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemChipped, 3))
                        .itemOutputs(werkstoff.get(gemFlawed), GT_OreDictUnificator.get(dustTiny, Materials.DarkAsh, 2))
                        .noFluidInputs().noFluidOutputs().duration(20 * TICKS).eut(TierEU.RECIPE_LV)
                        .metadata(ADDITIVE_AMOUNT, 8).addTo(sImplosionRecipes);

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(dust, 4))
                        .itemOutputs(werkstoff.get(gem, 3), GT_OreDictUnificator.get(dustTiny, Materials.DarkAsh, 8))
                        .noFluidInputs().noFluidOutputs().duration(20 * TICKS).eut(TierEU.RECIPE_LV)
                        .metadata(ADDITIVE_AMOUNT, 24).addTo(sImplosionRecipes);
            }

            if (werkstoff.hasItemType(plate)) {

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(plate))
                        .itemOutputs(werkstoff.get(lens), werkstoff.get(dustSmall)).noFluidInputs().noFluidOutputs()
                        .duration(60 * SECONDS).eut(TierEU.RECIPE_MV).addTo(sLatheRecipes);

            }

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemExquisite))
                    .itemOutputs(werkstoff.get(lens), werkstoff.get(dust, 2)).noFluidInputs().noFluidOutputs()
                    .duration(2 * MINUTES).eut(TierEU.RECIPE_LV).addTo(sLatheRecipes);

            final ITexture texture = TextureFactory.of(
                    Textures.BlockIcons.MACHINE_CASINGS[2][0],
                    TextureFactory.of(Textures.BlockIcons.OVERLAY_LENS, werkstoff.getRGBA(), false));
            GregTech_API.registerCover(
                    werkstoff.get(lens),
                    texture,
                    new gregtech.common.covers.GT_Cover_Lens(
                            BW_ColorUtil.getDyeFromColor(werkstoff.getRGBA()).mIndex,
                            texture));

            GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(lens)).itemOutputs(werkstoff.get(dustSmall, 3))
                    .noFluidInputs().noFluidOutputs().duration(20 * SECONDS).eut(2).addTo(sMaceratorRecipes);

            for (ItemStack is : OreDictionary.getOres(
                    "craftingLens" + BW_ColorUtil.getDyeFromColor(werkstoff.getRGBA()).mName.replace(" ", ""))) {
                is.stackSize = 0;

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemChipped, 3), is)
                        .itemOutputs(werkstoff.get(gemFlawed, 1)).noFluidInputs().noFluidOutputs()
                        .duration(30 * SECONDS).eut(TierEU.RECIPE_LV).addTo(sLaserEngraverRecipes);

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemFlawed, 3), is).itemOutputs(werkstoff.get(gem, 1))
                        .noFluidInputs().noFluidOutputs().duration(30 * SECONDS).eut(TierEU.RECIPE_MV)
                        .addTo(sLaserEngraverRecipes);

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gem, 3), is)
                        .itemOutputs(werkstoff.get(gemFlawless, 1)).noFluidInputs().noFluidOutputs()
                        .duration(60 * SECONDS).eut(TierEU.RECIPE_HV).addTo(sLaserEngraverRecipes);

                GT_Values.RA.stdBuilder().itemInputs(werkstoff.get(gemFlawless, 3), is)
                        .itemOutputs(werkstoff.get(gemExquisite, 1)).noFluidInputs().noFluidOutputs()
                        .duration(2 * MINUTES).eut(2000).addTo(sLaserEngraverRecipes);

            }
        }
    }
}
