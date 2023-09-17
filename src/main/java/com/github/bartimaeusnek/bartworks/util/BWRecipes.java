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

package com.github.bartimaeusnek.bartworks.util;

import static com.github.bartimaeusnek.bartworks.util.BW_Util.calculateSv;
import static com.github.bartimaeusnek.bartworks.util.BW_Util.specialToByte;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnegative;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import com.github.bartimaeusnek.bartworks.API.modularUI.BW_UITextures;
import com.github.bartimaeusnek.bartworks.MainMod;
import com.github.bartimaeusnek.bartworks.common.loaders.BioItemList;
import com.github.bartimaeusnek.bartworks.common.tileentities.multis.GT_TileEntity_BioVat;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.ProgressBar;

import gregtech.api.enums.GT_Values;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.nei.GT_NEI_DefaultHandler;
import gregtech.nei.NEIRecipeInfo;
import ic2.core.Ic2Items;
import ic2.core.item.ItemFluidCell;

public class BWRecipes {

    public static final BWRecipes instance = new BWRecipes();
    public static final byte BIOLABBYTE = 0;
    public static final byte BACTERIALVATBYTE = 1;
    public static final byte ACIDGENMAPBYTE = 2;
    public static final byte CIRCUITASSEMBLYLINE = 3;
    public static final byte RADHATCH = 4;

    private final GT_Recipe.GT_Recipe_Map sBiolab = new GT_Recipe.GT_Recipe_Map(
            new HashSet<>(150),
            "bw.recipe.biolab",
            StatCollector.translateToLocal("tile.biolab.name"),
            null,
            "gregtech:textures/gui/basicmachines/BW.GUI.BioLab",
            6,
            2,
            1,
            1,
            1,
            "",
            1,
            "",
            true,
            false // special handler
    ) {

        @Nullable
        @Override
        public IDrawable getOverlayForSlot(boolean isFluid, boolean isOutput, int index, boolean isSpecial) {
            if (!isFluid && !isOutput && !isSpecial) {
                switch (index) {
                    case 0:
                        return BW_UITextures.OVERLAY_SLOT_DISH;
                    case 1:
                        return BW_UITextures.OVERLAY_SLOT_DNA_FLASK;
                    case 2:
                        return GT_UITextures.OVERLAY_SLOT_CIRCUIT;
                    case 3:
                        return GT_UITextures.OVERLAY_SLOT_MOLECULAR_1;
                    case 4:
                        return GT_UITextures.OVERLAY_SLOT_MOLECULAR_2;
                    case 5:
                        return GT_UITextures.OVERLAY_SLOT_DATA_ORB;
                }
            } else if (isFluid && !isOutput) {
                return GT_UITextures.OVERLAY_SLOT_VIAL_2;
            } else if (isSpecial) {
                return BW_UITextures.OVERLAY_SLOT_MODULE;
            }
            return super.getOverlayForSlot(isFluid, isOutput, index, false);
        }
    }.setProgressBar(GT_UITextures.PROGRESSBAR_ARROW_MULTIPLE).setLogo(BW_UITextures.PICTURE_BW_LOGO_47X21)
            .setLogoPos(125, 3).setLogoSize(47, 21);
    private final GT_Recipe.GT_Recipe_Map sBacteriaVat = new BacteriaVatRecipeMap(
            new HashSet<>(50),
            "bw.recipe.BacteriaVat",
            StatCollector.translateToLocal("tile.biovat.name"),
            null,
            "gregtech:textures/gui/basicmachines/Default",
            6,
            2,
            0,
            1,
            1,
            " Sievert: ",
            1,
            " Sv",
            true,
            false // special handler
    ).setProgressBar(GT_UITextures.PROGRESSBAR_ARROW_MULTIPLE)
            .setNEISpecialInfoFormatter((recipeInfo, applyPrefixAndSuffix) -> {
                int[] tSpecialA = GT_TileEntity_BioVat.specialValueUnpack(recipeInfo.recipe.mSpecialValue);
                return Arrays.asList(
                        StatCollector.translateToLocal("nei.biovat.0.name") + " " + tSpecialA[0],
                        (tSpecialA[2] == 1 ? StatCollector.translateToLocal("nei.biovat.1.name")
                                : StatCollector.translateToLocal("nei.biovat.2.name"))
                                + applyPrefixAndSuffix.apply(tSpecialA[3]));
            });
    private final GT_Recipe.GT_Recipe_Map sAcidGenFuels = new BW_Recipe_Map_LiquidFuel(
            new HashSet<>(10),
            "bw.fuels.acidgens",
            StatCollector.translateToLocal("tile.acidgenerator.name"),
            null,
            "gregtech:textures/gui/basicmachines/Default",
            1,
            1,
            1,
            1,
            1,
            "EU generated: ",
            1000,
            "",
            false,
            true).useModularUI(true);
    private final GT_Recipe.GT_Recipe_Map sCircuitAssemblyLineMap = new SpecialObjectSensitiveMap(
            new HashSet<>(60),
            "bw.recipe.cal",
            "Circuit Assembly Line",
            null,
            "gregtech:textures/gui/basicmachines/Default",
            6,
            1,
            1,
            1,
            1,
            "",
            1,
            "",
            true,
            true).setProgressBar(GT_UITextures.PROGRESSBAR_CIRCUIT_ASSEMBLER);
    private final GT_Recipe.GT_Recipe_Map sRadHatch = new GT_Recipe.GT_Recipe_Map(
            new HashSet<>(150),
            "bw.recipe.radhatch",
            "Radio Hatch Material List",
            null,
            "gregtech:textures/gui/basicmachines/BW.GUI.Radioactive",
            1,
            0,
            1,
            0,
            1,
            "",
            0,
            "",
            false,
            true) {

        @Override
        public void addProgressBarUI(ModularWindow.Builder builder, Supplier<Float> progressSupplier,
                Pos2d windowOffset) {
            builder.widget(
                    new DrawableWidget().setDrawable(BW_UITextures.PICTURE_RADIATION)
                            .setPos(new Pos2d(74, 20).add(windowOffset)).setSize(29, 27));
        }

        @Override
        protected void drawNEIEnergyInfo(NEIRecipeInfo recipeInfo) {}

        @Override
        protected void drawNEIDurationInfo(NEIRecipeInfo recipeInfo) {}
    }.setSlotOverlay(false, false, BW_UITextures.OVERLAY_SLOT_ROD).setLogo(BW_UITextures.PICTURE_BW_LOGO_47X21)
            .setLogoPos(118, 55).setLogoSize(47, 21).setNEISpecialInfoFormatter((recipeInfo, applyPrefixAndSuffix) -> {
                int radioLevel = recipeInfo.recipe.mEUt;
                int amount = recipeInfo.recipe.mDuration;
                long time = recipeInfo.recipe.mSpecialValue;
                return Arrays.asList(
                        StatCollector.translateToLocalFormatted("BW.NEI.display.radhatch.0", radioLevel),
                        StatCollector.translateToLocalFormatted("BW.NEI.display.radhatch.1", amount),
                        StatCollector.translateToLocalFormatted("BW.NEI.display.radhatch.2", time * amount / 20.0));
            });
    public final GT_Recipe.GT_Recipe_Map eicMap = new GT_Recipe.GT_Recipe_Map(
            new HashSet<>(GT_Recipe.GT_Recipe_Map.sImplosionRecipes.mRecipeList.size()),
            "gt.recipe.electricimplosioncompressor",
            "Electric Implosion Compressor",
            null,
            "gregtech:textures/gui/basicmachines/Default",
            6,
            2,
            0,
            0,
            1,
            "",
            1,
            "",
            true,
            true).setSlotOverlay(false, false, GT_UITextures.OVERLAY_SLOT_IMPLOSION)
                    .setProgressBar(GT_UITextures.PROGRESSBAR_COMPRESS, ProgressBar.Direction.RIGHT);

    /**
     * @param machine 0 = biolab; 1 = BacterialVat; 2 = sAcidGenFuels; 3 = circuitAssemblyLine
     */
    public GT_Recipe.GT_Recipe_Map getMappingsFor(byte machine) {
        return switch (machine) {
            case 0 -> this.sBiolab;
            case 1 -> this.sBacteriaVat;
            case 2 -> this.sAcidGenFuels;
            case 3 -> this.sCircuitAssemblyLineMap;
            case 4 -> this.sRadHatch;
            default -> null;
        };
    }

    public static long calcDecayTicks(int x) {
        long ret;
        if (x == 43) ret = 5000;
        else if (x == 61) ret = 4500;
        else if (x <= 100) ret = MathUtils.ceilLong((8000D * Math.tanh(-x / 20D) + 8000D) * 1000D);
        else ret = MathUtils.ceilLong(8000D * Math.tanh(-x / 65D) + 8000D);
        return ret;
    }

    public boolean addRadHatch(ItemStack item, int radioLevel, int amount, short[] rgba) {
        return this.sRadHatch.addRecipe(
                new DynamicGTRecipe(
                        false,
                        new ItemStack[] { item },
                        null,
                        null,
                        new int[] { rgba[0], rgba[1], rgba[2] },
                        null,
                        null,
                        amount,
                        radioLevel,
                        (int) calcDecayTicks(radioLevel)))
                != null;
    }

    public boolean addRadHatch(ItemStack item, int radioLevel, int amount, int time, short[] rgba) {
        return this.sRadHatch.addRecipe(
                new DynamicGTRecipe(
                        false,
                        new ItemStack[] { item },
                        null,
                        null,
                        new int[] { rgba[0], rgba[1], rgba[2] },
                        null,
                        null,
                        amount,
                        radioLevel,
                        time))
                != null;
    }

    public boolean addBioLabRecipe(ItemStack[] aInputs, ItemStack aOutput, ItemStack aSpecialItems, int[] aChances,
            FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt, int aSpecialValue) {
        return this.sBiolab.addRecipe(
                new DynamicGTRecipe(
                        true,
                        aInputs,
                        new ItemStack[] { aOutput },
                        aSpecialItems,
                        aChances,
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSpecialValue))
                != null;
    }

    public boolean addBioLabRecipeIncubation(ItemStack aInput, BioCulture aOutput, int[] aChances,
            FluidStack[] aFluidInputs, int aDuration, int aEUt, int aSpecialValue) {
        return this.sBiolab.addRecipe(
                new DynamicGTRecipe(
                        true,
                        new ItemStack[] { BioItemList.getPetriDish(null), aInput },
                        new ItemStack[] { BioItemList.getPetriDish(aOutput) },
                        null,
                        aChances,
                        aFluidInputs,
                        new FluidStack[] { GT_Values.NF },
                        aDuration,
                        aEUt,
                        aSpecialValue))
                != null;
    }

    public boolean addBioLabRecipeIncubation(ItemStack aInput, BioCulture aOutput, int[] aChances,
            FluidStack aFluidInputs, int aDuration, int aEUt, int aSpecialValue) {
        return this.sBiolab.addRecipe(
                new DynamicGTRecipe(
                        true,
                        new ItemStack[] { BioItemList.getPetriDish(null), aInput },
                        new ItemStack[] { BioItemList.getPetriDish(aOutput) },
                        null,
                        aChances,
                        new FluidStack[] { aFluidInputs },
                        new FluidStack[] { GT_Values.NF },
                        aDuration,
                        aEUt,
                        aSpecialValue))
                != null;
    }

    @Deprecated
    public boolean addBioLabRecipeDNAExtraction(ItemStack[] aInputs, ItemStack aOutput, int[] aChances,
            FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt, int aSpecialValue) {
        return this.sBiolab.addRecipe(
                new DynamicGTRecipe(
                        true,
                        aInputs,
                        new ItemStack[] { aOutput },
                        BioItemList.mBioLabParts[0],
                        aChances,
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSpecialValue))
                != null;
    }

    @Deprecated
    public boolean addBioLabRecipePCRThermoclycling(ItemStack[] aInputs, ItemStack aOutput, int[] aChances,
            FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt, int aSpecialValue) {
        return this.sBiolab.addRecipe(
                new DynamicGTRecipe(
                        true,
                        aInputs,
                        new ItemStack[] { aOutput },
                        BioItemList.mBioLabParts[1],
                        aChances,
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSpecialValue))
                != null;
    }

    @Deprecated
    public boolean addBioLabRecipePlasmidSynthesis(ItemStack[] aInputs, ItemStack aOutput, int[] aChances,
            FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt, int aSpecialValue) {
        return this.sBiolab.addRecipe(
                new DynamicGTRecipe(
                        true,
                        aInputs,
                        new ItemStack[] { aOutput },
                        BioItemList.mBioLabParts[2],
                        aChances,
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSpecialValue))
                != null;
    }

    @Deprecated
    public boolean addBioLabRecipeTransformation(ItemStack[] aInputs, ItemStack aOutput, int[] aChances,
            FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt, int aSpecialValue) {
        return this.sBiolab.addRecipe(
                new DynamicGTRecipe(
                        true,
                        aInputs,
                        new ItemStack[] { aOutput },
                        BioItemList.mBioLabParts[3],
                        aChances,
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSpecialValue))
                != null;
    }

    @Deprecated
    public boolean addBioLabRecipeClonalCellularSynthesis(ItemStack[] aInputs, ItemStack aOutput, int[] aChances,
            FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt, int aSpecialValue) {
        return this.sBiolab.addRecipe(
                new DynamicGTRecipe(
                        true,
                        aInputs,
                        new ItemStack[] { aOutput },
                        BioItemList.mBioLabParts[4],
                        aChances,
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSpecialValue))
                != null;
    }

    @Deprecated
    public boolean addBacterialVatRecipe(ItemStack[] aInputs, ItemStack[] aOutputs, FluidStack[] aFluidInputs,
            FluidStack[] aFluidOutputs, int aDuration, int aEUt, Materials material, @Nonnegative byte glasTier) {
        int aSievert = 0;
        if (material.getProtons() >= 83 || material.getProtons() == 61 || material.getProtons() == 43)
            aSievert += calculateSv(material);
        aSievert = aSievert << 6;
        aSievert = aSievert | glasTier;
        return this.sBacteriaVat.addRecipe(
                new BacteriaVatRecipe(
                        false,
                        aInputs,
                        aOutputs,
                        null,
                        new int[] {},
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSievert))
                != null;
    }

    public boolean addBacterialVatRecipe(ItemStack[] aInputs, BioCulture aCulture, FluidStack[] aFluidInputs,
            FluidStack[] aFluidOutputs, @Nonnegative int aDuration, @Nonnegative int aEUt, @Nonnegative int aSv,
            @Nonnegative int glasTier, int aSpecialValue, boolean exactSv) {
        int aSievert = 0;
        if (aSv >= 83 || aSv == 61 || aSv == 43) aSievert += aSv;
        aSievert = aSievert << 1;
        aSievert = aSievert | (exactSv ? 1 : 0);
        aSievert = aSievert << 2;
        aSievert = aSievert | specialToByte(aSpecialValue);
        aSievert = aSievert << 4;
        aSievert = aSievert | glasTier;
        return this.sBacteriaVat.addRecipe(
                new BacteriaVatRecipe(
                        false,
                        aInputs,
                        null,
                        BioItemList.getPetriDish(aCulture),
                        new int[] {},
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSievert))
                != null;
    }

    @Deprecated
    public boolean addBacterialVatRecipe(ItemStack[] aInputs, BioCulture aCulture, FluidStack[] aFluidInputs,
            FluidStack[] aFluidOutputs, int aDuration, int aEUt, Materials material, @Nonnegative int glasTier,
            int aSpecialValue, boolean exactSv) {
        byte gTier = (byte) glasTier;
        int aSievert = 0;
        if (material.getProtons() >= 83 || material.getProtons() == 61 || material.getProtons() == 43)
            aSievert += calculateSv(material);
        aSievert = aSievert << 1;
        aSievert = aSievert | (exactSv ? 1 : 0);
        aSievert = aSievert << 2;
        aSievert = aSievert | specialToByte(aSpecialValue);
        aSievert = aSievert << 4;
        aSievert = aSievert | gTier;
        return this.sBacteriaVat.addRecipe(
                new BacteriaVatRecipe(
                        false,
                        aInputs,
                        null,
                        BioItemList.getPetriDish(aCulture),
                        new int[] {},
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSievert))
                != null;
    }

    @Deprecated
    public boolean addBacterialVatRecipe(ItemStack[] aInputs, ItemStack[] aOutputs, FluidStack[] aFluidInputs,
            FluidStack[] aFluidOutputs, int aDuration, int aEUt, Materials material, boolean exactSv) {
        int aSievert = 0;
        if (material.getProtons() >= 83 || material.getProtons() == 61 || material.getProtons() == 43)
            aSievert += calculateSv(material);
        aSievert = aSievert << 1;
        aSievert = aSievert | (exactSv ? 1 : 0);
        aSievert = aSievert << 6;
        return this.sBacteriaVat.addRecipe(
                new BacteriaVatRecipe(
                        false,
                        aInputs,
                        aOutputs,
                        null,
                        new int[] {},
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSievert))
                != null;
    }

    @Deprecated
    public boolean addBacterialVatRecipe(ItemStack[] aInputs, BioCulture culture, FluidStack[] aFluidInputs,
            FluidStack[] aFluidOutputs, int aDuration, int aEUt, Materials material, int aSpecialValue,
            boolean exactSv) {
        int aSievert = 0;
        if (material.getProtons() >= 83 || material.getProtons() == 61 || material.getProtons() == 43)
            aSievert += calculateSv(material);
        aSievert = aSievert << 1;
        aSievert = aSievert | (exactSv ? 1 : 0);
        aSievert = aSievert << 2;
        aSievert = aSievert | specialToByte(aSpecialValue);
        aSievert = aSievert << 4;
        return this.sBacteriaVat.addRecipe(
                new BacteriaVatRecipe(
                        false,
                        aInputs,
                        null,
                        BioItemList.getPetriDish(culture),
                        new int[] {},
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSievert))
                != null;
    }

    /**
     * Adds a Vat recipe without Rad requirements but with Glas requirements
     */
    public boolean addBacterialVatRecipe(ItemStack[] aInputs, BioCulture culture, FluidStack[] aFluidInputs,
            FluidStack[] aFluidOutputs, int aDuration, int aEUt, byte glasTier) {
        int aSievert = 0;
        aSievert = aSievert | glasTier;
        return this.sBacteriaVat.addRecipe(
                new BacteriaVatRecipe(
                        false,
                        aInputs,
                        null,
                        BioItemList.getPetriDish(culture),
                        new int[] {},
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSievert))
                != null;
    }

    /**
     * Adds a Vat recipe without Rad or Glas requirements
     */
    public boolean addBacterialVatRecipe(ItemStack[] aInputs, FluidStack[] aFluidInputs, BioCulture culture,
            FluidStack[] aFluidOutputs, int aDuration, int aEUt) {
        return this.sBacteriaVat.addRecipe(
                new BacteriaVatRecipe(
                        false,
                        aInputs,
                        null,
                        BioItemList.getPetriDish(culture),
                        new int[] {},
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        0))
                != null;
    }

    public boolean addTrimmedBacterialVatRecipe(ItemStack[] aInputs, BioCulture aCulture, FluidStack[] aFluidInputs,
            FluidStack[] aFluidOutputs, int aDuration, int aEUt, Materials material, @Nonnegative int glasTier,
            int aSpecialValue, boolean exactSv) {
        byte gTier = (byte) glasTier;
        int aSievert = 0;
        if (material.getProtons() >= 83 || material.getProtons() == 61 || material.getProtons() == 43)
            aSievert += material.getProtons();
        aSievert = aSievert << 1;
        aSievert = aSievert | (exactSv ? 1 : 0);
        aSievert = aSievert << 2;
        aSievert = aSievert | specialToByte(aSpecialValue);
        aSievert = aSievert << 4;
        aSievert = aSievert | gTier;
        return this.sBacteriaVat.addRecipe(
                new BacteriaVatRecipe(
                        true,
                        aInputs,
                        null,
                        BioItemList.getPetriDish(aCulture),
                        new int[] {},
                        aFluidInputs,
                        aFluidOutputs,
                        aDuration,
                        aEUt,
                        aSievert))
                != null;
    }

    public static class DynamicGTRecipe extends GT_Recipe {

        public DynamicGTRecipe(boolean aOptimize, ItemStack[] aInputs, ItemStack[] aOutputs, Object aSpecialItems,
                int[] aChances, FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt,
                int aSpecialValue, GT_Recipe originalRecipe) {
            super(
                    aOptimize,
                    aInputs,
                    aOutputs,
                    aSpecialItems,
                    aChances,
                    aFluidInputs,
                    aFluidOutputs,
                    aDuration,
                    aEUt,
                    aSpecialValue);
            if (originalRecipe != null) {
                this.owners = new ArrayList<>(originalRecipe.owners);
                this.stackTraces = new ArrayList<>(originalRecipe.stackTraces);
                this.setOwner(MainMod.MOD_ID);
            }
        }

        public DynamicGTRecipe(boolean aOptimize, ItemStack[] aInputs, ItemStack[] aOutputs, Object aSpecialItems,
                int[] aChances, FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt,
                int aSpecialValue) {
            this(
                    aOptimize,
                    aInputs,
                    aOutputs,
                    aSpecialItems,
                    aChances,
                    aFluidInputs,
                    aFluidOutputs,
                    aDuration,
                    aEUt,
                    aSpecialValue,
                    null);
        }
    }

    public static class BW_Recipe_Map_LiquidFuel extends GT_Recipe.GT_Recipe_Map_Fuel {

        public BW_Recipe_Map_LiquidFuel(Collection<GT_Recipe> aRecipeList, String aUnlocalizedName, String aLocalName,
                String aNEIName, String aNEIGUIPath, int aUsualInputCount, int aUsualOutputCount,
                int aMinimalInputItems, int aMinimalInputFluids, int aAmperage, String aNEISpecialValuePre,
                int aNEISpecialValueMultiplier, String aNEISpecialValuePost, boolean aShowVoltageAmperageInNEI,
                boolean aNEIAllowed) {
            super(
                    aRecipeList,
                    aUnlocalizedName,
                    aLocalName,
                    aNEIName,
                    aNEIGUIPath,
                    aUsualInputCount,
                    aUsualOutputCount,
                    aMinimalInputItems,
                    aMinimalInputFluids,
                    aAmperage,
                    aNEISpecialValuePre,
                    aNEISpecialValueMultiplier,
                    aNEISpecialValuePost,
                    aShowVoltageAmperageInNEI,
                    aNEIAllowed);
        }

        public GT_Recipe addLiquidFuel(Materials M, int burn) {
            return super.addFuel(M.getCells(1), Materials.Empty.getCells(1), burn);
        }

        public GT_Recipe addMoltenFuel(Materials M, int burn) {
            return super.addFuel(
                    GT_OreDictUnificator.get(OrePrefixes.cellMolten, M, 1),
                    Materials.Empty.getCells(1),
                    burn);
        }

        public GT_Recipe addLiquidFuel(FluidStack fluidStack, int burn) {
            return super.addFuel(ItemFluidCell.getUniversalFluidCell(fluidStack), Ic2Items.FluidCell.copy(), burn);
        }
    }

    public static class BacteriaVatRecipe extends GT_Recipe {

        public BacteriaVatRecipe(boolean aOptimize, ItemStack[] aInputs, ItemStack[] aOutputs, ItemStack aSpecialItems,
                int[] aChances, FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt,
                int aSpecialValue) {
            super(
                    aOptimize,
                    aInputs,
                    aOutputs,
                    aSpecialItems,
                    aChances,
                    aFluidInputs,
                    aFluidOutputs,
                    aDuration,
                    aEUt,
                    aSpecialValue);
        }
    }

    public static class BacteriaVatRecipeMap extends BWRecipes.SpecialObjectSensitiveMap {

        public BacteriaVatRecipeMap(Collection<GT_Recipe> aRecipeList, String aUnlocalizedName, String aLocalName,
                String aNEIName, String aNEIGUIPath, int aUsualInputCount, int aUsualOutputCount,
                int aMinimalInputItems, int aMinimalInputFluids, int aAmperage, String aNEISpecialValuePre,
                int aNEISpecialValueMultiplier, String aNEISpecialValuePost, boolean aShowVoltageAmperageInNEI,
                boolean aNEIAllowed) {
            super(
                    aRecipeList,
                    aUnlocalizedName,
                    aLocalName,
                    aNEIName,
                    aNEIGUIPath,
                    aUsualInputCount,
                    aUsualOutputCount,
                    aMinimalInputItems,
                    aMinimalInputFluids,
                    aAmperage,
                    aNEISpecialValuePre,
                    aNEISpecialValueMultiplier,
                    aNEISpecialValuePost,
                    aShowVoltageAmperageInNEI,
                    aNEIAllowed);
        }

        @Override
        protected GT_Recipe addRecipe(GT_Recipe aRecipe, boolean aCheckForCollisions, boolean aFakeRecipe,
                boolean aHidden) {
            aRecipe.mHidden = aHidden;
            aRecipe.mFakeRecipe = aFakeRecipe;
            GT_Recipe isthere = this
                    .findRecipe(null, false, false, 9223372036854775807L, aRecipe.mFluidInputs, aRecipe.mInputs);

            if (aRecipe.mFluidInputs.length < this.mMinimalInputFluids
                    && aRecipe.mInputs.length < this.mMinimalInputItems) {
                return null;
            }
            return aCheckForCollisions && isthere != null
                    && BW_Util
                            .areStacksEqualOrNull((ItemStack) isthere.mSpecialItems, (ItemStack) aRecipe.mSpecialItems)
                                    ? null
                                    : this.add(aRecipe);
        }

        public GT_Recipe addRecipe(GT_Recipe aRecipe, boolean VanillaGT) {
            if (VanillaGT) return this.addRecipe(aRecipe, true, false, false);
            return this.addRecipe(aRecipe);
        }

        @Override
        public GT_Recipe addRecipe(GT_Recipe aRecipe) {

            if (aRecipe.mInputs.length > 0 && GT_Utility.areStacksEqual(
                    aRecipe.mInputs[aRecipe.mInputs.length - 1],
                    GT_Utility.getIntegratedCircuit(32767)))
                return aRecipe;
            ItemStack[] nu1 = Arrays.copyOf(aRecipe.mInputs, aRecipe.mInputs.length + 1);
            nu1[nu1.length - 1] = GT_Utility.getIntegratedCircuit(9 + nu1.length);
            aRecipe.mInputs = nu1;
            if (this.findRecipe(null, false, 9223372036854775807L, aRecipe.mFluidInputs, aRecipe.mInputs) != null) {
                ItemStack[] nu = Arrays.copyOf(aRecipe.mInputs, aRecipe.mInputs.length + 1);
                int i = 9 + nu.length;
                do {
                    nu[nu.length - 1] = GT_Utility.getIntegratedCircuit(i);
                    i++;
                    aRecipe.mInputs = nu;
                    if (i > 24) i = 1;
                    if (i == 9 + nu.length) return null;
                } while (this.findRecipe(null, false, 9223372036854775807L, aRecipe.mFluidInputs, aRecipe.mInputs)
                        != null);
            }
            return this.addRecipe(aRecipe, false, false, false);
        }

        @Override
        protected List<String> handleNEIItemInputTooltip(List<String> currentTip,
                GT_NEI_DefaultHandler.FixedPositionedStack pStack) {
            if (pStack.isFluid()) {
                currentTip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("nei.biovat.input.tooltip"));
                return currentTip;
            }
            return super.handleNEIItemInputTooltip(currentTip, pStack);
        }

        @Override
        protected List<String> handleNEIItemOutputTooltip(List<String> currentTip,
                GT_NEI_DefaultHandler.FixedPositionedStack pStack) {
            if (pStack.isFluid()) {
                currentTip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("nei.biovat.output.tooltip"));
                return currentTip;
            }
            return super.handleNEIItemOutputTooltip(currentTip, pStack);
        }

        @Override
        protected void drawNEIOverlayForInput(GT_NEI_DefaultHandler.FixedPositionedStack stack) {
            drawFluidOverlay(stack);
        }

        @Override
        protected void drawNEIOverlayForOutput(GT_NEI_DefaultHandler.FixedPositionedStack stack) {
            drawFluidOverlay(stack);
        }

        private void drawFluidOverlay(GT_NEI_DefaultHandler.FixedPositionedStack stack) {
            if (stack.isFluid()) {
                drawNEIOverlayText(
                        "+",
                        stack,
                        colorOverride.getTextColorOrDefault("nei_overlay_yellow", 0xFDD835),
                        0.5f,
                        true,
                        Alignment.TopRight);
                return;
            }
            super.drawNEIOverlayForOutput(stack);
        }
    }

    public static class SpecialObjectSensitiveMap extends GT_Recipe.GT_Recipe_Map {

        public SpecialObjectSensitiveMap(Collection<GT_Recipe> aRecipeList, String aUnlocalizedName, String aLocalName,
                String aNEIName, String aNEIGUIPath, int aUsualInputCount, int aUsualOutputCount,
                int aMinimalInputItems, int aMinimalInputFluids, int aAmperage, String aNEISpecialValuePre,
                int aNEISpecialValueMultiplier, String aNEISpecialValuePost, boolean aShowVoltageAmperageInNEI,
                boolean aNEIAllowed) {
            super(
                    aRecipeList,
                    aUnlocalizedName,
                    aLocalName,
                    aNEIName,
                    aNEIGUIPath,
                    aUsualInputCount,
                    aUsualOutputCount,
                    aMinimalInputItems,
                    aMinimalInputFluids,
                    aAmperage,
                    aNEISpecialValuePre,
                    aNEISpecialValueMultiplier,
                    aNEISpecialValuePost,
                    aShowVoltageAmperageInNEI,
                    aNEIAllowed);
            this.setSpecialSlotSensitive(true);
        }
    }

    public static class BWNBTDependantCraftingRecipe implements IRecipe {

        ItemStack result;
        Map<Character, ItemStack> charToStackMap = new HashMap<>(9, 1);
        String[] shape;

        @SuppressWarnings({ "SuspiciousSystemArraycopy" })
        public BWNBTDependantCraftingRecipe(ItemStack result, Object... recipe) {
            this.result = result;
            this.shape = new String[3];
            System.arraycopy(recipe, 0, this.shape, 0, 3);
            this.charToStackMap.put(' ', null);
            for (int i = 3; i < recipe.length; i += 2) {
                this.charToStackMap.put((char) recipe[i], ((ItemStack) recipe[i + 1]).copy());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BWRecipes.BWNBTDependantCraftingRecipe that)) return false;

            if (!Objects.equals(this.result, that.result) || !Objects.equals(this.charToStackMap, that.charToStackMap))
                return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(this.shape, that.shape);
        }

        @Override
        public int hashCode() {
            int result1 = this.result != null ? this.result.hashCode() : 0;
            result1 = 31 * result1 + (this.charToStackMap != null ? this.charToStackMap.hashCode() : 0);
            return 31 * result1 + Arrays.hashCode(this.shape);
        }

        @Override
        public boolean matches(InventoryCrafting p_77569_1_, World p_77569_2_) {
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    ItemStack toCheck = p_77569_1_.getStackInRowAndColumn(y, x);
                    ItemStack ref = this.charToStackMap.get(this.shape[x].toCharArray()[y]);
                    if (!BW_Util.areStacksEqualOrNull(toCheck, ref)) return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting p_77572_1_) {
            return this.result.copy();
        }

        @Override
        public int getRecipeSize() {
            return 10;
        }

        @Override
        public ItemStack getRecipeOutput() {
            return this.result;
        }
    }
}
