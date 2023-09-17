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

package com.github.bartimaeusnek.bartworks.common.loaders;

import static com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader.ANAEROBE_GAS;
import static com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader.NOBLE_GAS;
import static com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader.Oganesson;
import static com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader.fluids;
import static com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader.molten;
import static gregtech.api.enums.GT_Values.VN;
import static gregtech.api.enums.Mods.TinkerConstruct;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.StringUtils;

import com.github.bartimaeusnek.bartworks.MainMod;
import com.github.bartimaeusnek.bartworks.system.material.Werkstoff;
import com.github.bartimaeusnek.bartworks.util.BWRecipes;
import com.github.bartimaeusnek.bartworks.util.BW_Util;
import com.github.bartimaeusnek.bartworks.util.StreamUtils;
import com.github.bartimaeusnek.bartworks.util.log.DebugLog;
import com.github.bartimaeusnek.crossmod.BartWorksCrossmod;
import com.google.common.collect.ArrayListMultimap;

import cpw.mods.fml.common.registry.GameRegistry;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gregtech.api.enums.Element;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.SubTag;
import gregtech.api.enums.TierEU;
import gregtech.api.objects.GT_ItemStack;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Recipe.GT_Recipe_Map;
import gregtech.api.util.GT_Utility;

public class StaticRecipeChangeLoaders {

    private static TObjectDoubleHashMap<Materials> gtEbfGasRecipeTimeMultipliers = null;
    private static TObjectDoubleHashMap<Materials> gtEbfGasRecipeConsumptionMultipliers = null;

    public static final List<ItemStack> whitelistForEBFNoGasRecipeDontCheckItemData = Arrays
            .asList(GT_ModHandler.getModItem(TinkerConstruct.ID, "materials", 1L, 12) // Raw Aluminum -> Aluminium Ingot
            // (coremod)
            );

    private StaticRecipeChangeLoaders() {}

    public static void addEBFGasRecipes() {
        if (gtEbfGasRecipeTimeMultipliers == null) {
            // For Werkstoff gases, use Werkstoff.Stats.setEbfGasRecipeTimeMultiplier
            gtEbfGasRecipeTimeMultipliers = new TObjectDoubleHashMap<>(10, 0.5F, -1.0D); // keep default value as -1
            // Example to make Argon cut recipe times into a third of the original:
            // gtEbfGasRecipeTimeMultipliers.put(Materials.Argon, 1.0D / 3.0D);

            gtEbfGasRecipeTimeMultipliers.put(Materials.Nitrogen, 1.0D);
            gtEbfGasRecipeTimeMultipliers.put(Materials.Helium, 0.9D);
            gtEbfGasRecipeTimeMultipliers.put(Materials.Argon, 0.8D);
            gtEbfGasRecipeTimeMultipliers.put(Materials.Radon, 0.7D);
        }
        if (gtEbfGasRecipeConsumptionMultipliers == null) {
            // For Werkstoff gases, use Werkstoff.Stats.setEbfGasRecipeConsumedAmountMultiplier
            gtEbfGasRecipeConsumptionMultipliers = new TObjectDoubleHashMap<>(10, 0.5F, 1.0D); // keep default value as
                                                                                               // 1
            // Example to make Argon recipes use half the gas amount of the primary recipe (1000L->500L, 2000L->1000L
            // etc.):
            // gtEbfGasRecipeConsumptionMultipliers.put(Materials.Argon, 1.0D / 2.0D);
            gtEbfGasRecipeConsumptionMultipliers.put(Materials.Nitrogen, 1.0D);
            gtEbfGasRecipeConsumptionMultipliers.put(Materials.Helium, 1.0D);
            gtEbfGasRecipeConsumptionMultipliers.put(Materials.Argon, 0.85D);
            gtEbfGasRecipeConsumptionMultipliers.put(Materials.Radon, 0.7D);
        }
        ArrayListMultimap<SubTag, GT_Recipe> toChange = getRecipesToChange(NOBLE_GAS, ANAEROBE_GAS);
        editRecipes(toChange, getNoGasItems(toChange));
    }

    public static void fixEnergyRequirements() {
        GT_Recipe.GT_Recipe_Map.sMappings.stream().filter(StreamUtils::filterVisualMaps)
                .forEach(gt_recipe_map -> gt_recipe_map.mRecipeList.parallelStream().forEach(gt_recipe -> {
                    for (int i = 0; i < VN.length - 1; i++) {
                        if (gt_recipe.mEUt > BW_Util.getMachineVoltageFromTier(i)
                                && gt_recipe.mEUt <= BW_Util.getTierVoltage(i)) {
                            gt_recipe.mEUt = BW_Util.getMachineVoltageFromTier(i);
                        }
                    }
                }));
    }

    private static void replaceWrongFluidOutput(Werkstoff werkstoff, GT_Recipe recipe, FluidStack wrongNamedFluid) {
        for (int i = 0; i < recipe.mFluidOutputs.length; i++) {
            if (GT_Utility.areFluidsEqual(recipe.mFluidOutputs[i], wrongNamedFluid)) {
                recipe.mFluidOutputs[i] = werkstoff.getFluidOrGas(recipe.mFluidOutputs[i].amount);
            }
        }
    }

    private static void replaceWrongFluidInput(Werkstoff werkstoff, GT_Recipe.GT_Recipe_Map map, GT_Recipe recipe,
            FluidStack wrongNamedFluid) {
        for (int i = 0; i < recipe.mFluidInputs.length; i++) {
            if (GT_Utility.areFluidsEqual(recipe.mFluidInputs[i], wrongNamedFluid)) {
                Collection<GT_Recipe> col = map.mRecipeFluidMap.get(wrongNamedFluid.getFluid().getName());
                map.mRecipeFluidMap.remove(wrongNamedFluid.getFluid().getName());
                map.mRecipeFluidMap.put(werkstoff.getFluidOrGas(1).getFluid().getName(), col);
                recipe.mFluidInputs[i] = werkstoff.getFluidOrGas(recipe.mFluidInputs[i].amount);
                map.mRecipeFluidNameMap.add(werkstoff.getFluidOrGas(1).getFluid().getName());
            }
        }
    }

    private static void addConversionRecipe(Werkstoff werkstoff, FluidStack wrongNamedFluid) {
        GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes.add(
                new BWRecipes.DynamicGTRecipe(
                        false,
                        null,
                        null,
                        null,
                        null,
                        new FluidStack[] { wrongNamedFluid },
                        new FluidStack[] { werkstoff.getFluidOrGas(1) },
                        1,
                        1,
                        0));
    }

    private static FluidStack getWrongNameFluid(Werkstoff werkstoff) {
        String name = werkstoff.getFluidOrGas(1).getFluid().getName();
        String wrongname = "molten." + name;
        return FluidRegistry.getFluidStack(wrongname, 1);
    }

    private static void enforceNobleGas(Werkstoff werkstoff) {
        FluidStack wrongNamedFluid = getWrongNameFluid(werkstoff);
        if (wrongNamedFluid != null) {
            GT_Recipe.GT_Recipe_Map.sMappings.forEach(map -> map.mRecipeList.forEach(recipe -> {
                replaceWrongFluidInput(werkstoff, map, recipe, wrongNamedFluid);
                replaceWrongFluidOutput(werkstoff, recipe, wrongNamedFluid);
            }));
            addConversionRecipe(werkstoff, wrongNamedFluid);
        }
    }

    public static void unificationRecipeEnforcer() {
        for (Werkstoff werkstoff : Werkstoff.werkstoffHashSet) {
            StaticRecipeChangeLoaders.runMaterialLinker(werkstoff);
            if (werkstoff.getGenerationFeatures().enforceUnification) {

                if (werkstoff.contains(NOBLE_GAS)) enforceNobleGas(werkstoff);

                HashSet<String> oreDictNames = new HashSet<>(werkstoff.getADDITIONAL_OREDICT());
                oreDictNames.add(werkstoff.getVarName());
                StaticRecipeChangeLoaders.runMoltenUnificationEnfocement(werkstoff);
                StaticRecipeChangeLoaders.runUnficationDeleter(werkstoff);
                for (String s : oreDictNames) for (OrePrefixes prefixes : OrePrefixes.values()) {
                    if (!werkstoff.hasItemType(prefixes)) continue;
                    String fullOreName = prefixes + s;
                    List<ItemStack> ores = OreDictionary.getOres(fullOreName, false);
                    if (ores.size() <= 1) // empty or one entry, i.e. no unification needed
                        continue;
                    for (ItemStack toReplace : ores) {
                        ItemStack replacement = werkstoff.get(prefixes);
                        if (toReplace == null || GT_Utility.areStacksEqual(toReplace, replacement)
                                || replacement == null
                                || replacement.getItem() == null)
                            continue;
                        for (GT_Recipe.GT_Recipe_Map map : GT_Recipe.GT_Recipe_Map.sMappings) {
                            nextRecipe: for (Iterator<GT_Recipe> iterator = map.mRecipeList.iterator(); iterator
                                    .hasNext();) {
                                GT_Recipe recipe = iterator.next();
                                boolean removal = map.equals(GT_Recipe_Map.sFluidExtractionRecipes)
                                        || map.equals(GT_Recipe_Map.sFluidSolidficationRecipes);
                                for (int i = 0; i < recipe.mInputs.length; i++) {
                                    if (!GT_Utility.areStacksEqual(recipe.mInputs[i], toReplace)) continue;
                                    if (removal) {
                                        iterator.remove();
                                        continue nextRecipe;
                                    }
                                    recipe.mInputs[i] = GT_Utility.copyAmount(recipe.mInputs[i].stackSize, replacement);
                                }
                                for (int i = 0; i < recipe.mOutputs.length; i++) {
                                    if (!GT_Utility.areStacksEqual(recipe.mOutputs[i], toReplace)) continue;
                                    if (removal) {
                                        iterator.remove();
                                        continue nextRecipe;
                                    }
                                    recipe.mOutputs[i] = GT_Utility
                                            .copyAmount(recipe.mOutputs[i].stackSize, replacement);
                                }
                                if (recipe.mSpecialItems instanceof ItemStack specialItemStack) {
                                    if (!GT_Utility.areStacksEqual(specialItemStack, toReplace)) continue;
                                    if (removal) {
                                        iterator.remove();
                                        continue nextRecipe;
                                    }
                                    recipe.mSpecialItems = GT_Utility
                                            .copyAmount(specialItemStack.stackSize, replacement);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void runMoltenUnificationEnfocement(Werkstoff werkstoff) {
        if (werkstoff.getGenerationFeatures().enforceUnification && werkstoff.hasItemType(OrePrefixes.cellMolten)) {
            try {
                FluidContainerRegistry.FluidContainerData data = new FluidContainerRegistry.FluidContainerData(
                        new FluidStack(Objects.requireNonNull(molten.get(werkstoff)), 144),
                        werkstoff.get(OrePrefixes.cellMolten),
                        Materials.Empty.getCells(1));
                Field f = GT_Utility.class.getDeclaredField("sFilledContainerToData");
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<GT_ItemStack, FluidContainerRegistry.FluidContainerData> sFilledContainerToData = (Map<GT_ItemStack, FluidContainerRegistry.FluidContainerData>) f
                        .get(null);
                Set<Map.Entry<GT_ItemStack, FluidContainerRegistry.FluidContainerData>> toremFilledContainerToData = new HashSet<>();
                ItemStack toReplace = null;
                for (Map.Entry<GT_ItemStack, FluidContainerRegistry.FluidContainerData> entry : sFilledContainerToData
                        .entrySet()) {
                    final String MODID = GameRegistry.findUniqueIdentifierFor(data.filledContainer.getItem()).modId;
                    if (MainMod.MOD_ID.equals(MODID) || BartWorksCrossmod.MOD_ID.equals(MODID)) continue;
                    if (entry.getValue().fluid.equals(data.fluid)
                            && !entry.getValue().filledContainer.equals(data.filledContainer)) {
                        toReplace = entry.getValue().filledContainer;
                        toremFilledContainerToData.add(entry);
                    }
                }
                sFilledContainerToData.entrySet().removeAll(toremFilledContainerToData);
                Set<GT_Recipe> toremRecipeList = new HashSet<>();
                if (toReplace != null) {
                    for (GT_Recipe.GT_Recipe_Map map : GT_Recipe.GT_Recipe_Map.sMappings) {
                        toremRecipeList.clear();
                        for (GT_Recipe recipe : map.mRecipeList) {
                            for (ItemStack mInput : recipe.mInputs) {
                                if (GT_Utility.areStacksEqual(mInput, toReplace)) {
                                    toremRecipeList.add(recipe);
                                    // recipe.mInputs[i] = data.filledContainer;
                                }
                            }
                            for (ItemStack mOutput : recipe.mOutputs) {
                                if (GT_Utility.areStacksEqual(mOutput, toReplace)) {
                                    toremRecipeList.add(recipe);
                                    // recipe.mOutputs[i] = data.filledContainer;
                                    if (map == GT_Recipe.GT_Recipe_Map.sFluidCannerRecipes
                                            && GT_Utility.areStacksEqual(mOutput, data.filledContainer)
                                            && !recipe.mFluidInputs[0].equals(data.fluid)) {
                                        toremRecipeList.add(recipe);
                                        // recipe.mOutputs[i] = data.filledContainer;
                                    }
                                }
                            }
                            if (recipe.mSpecialItems instanceof ItemStack
                                    && GT_Utility.areStacksEqual((ItemStack) recipe.mSpecialItems, toReplace)) {
                                toremRecipeList.add(recipe);
                                // recipe.mSpecialItems = data.filledContainer;
                            }
                        }
                        map.mRecipeList.removeAll(toremRecipeList);
                    }
                }
                GT_Utility.addFluidContainerData(data);
            } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    private static void runUnficationDeleter(Werkstoff werkstoff) {
        if (werkstoff.getType() == Werkstoff.Types.ELEMENT && werkstoff.getBridgeMaterial() != null
                && Element.get(werkstoff.getToolTip()) != Element._NULL) {
            werkstoff.getBridgeMaterial().mElement = Element.get(werkstoff.getToolTip());
            Element.get(werkstoff.getToolTip()).mLinkedMaterials = new ArrayList<>();
            Element.get(werkstoff.getToolTip()).mLinkedMaterials.add(werkstoff.getBridgeMaterial());
        }

        for (OrePrefixes prefixes : OrePrefixes.values()) if (werkstoff.hasItemType(prefixes)) {
            GT_OreDictUnificator.set(prefixes, werkstoff.getBridgeMaterial(), werkstoff.get(prefixes), true, true);
            for (ItemStack stack : OreDictionary.getOres(prefixes + werkstoff.getVarName())) {
                GT_OreDictUnificator.addAssociation(prefixes, werkstoff.getBridgeMaterial(), stack, false);
                GT_OreDictUnificator.getAssociation(stack).mUnificationTarget = werkstoff.get(prefixes);
            }
        }
    }

    private static void runMaterialLinker(Werkstoff werkstoff) {
        if (werkstoff.getType() == Werkstoff.Types.ELEMENT && werkstoff.getBridgeMaterial() != null
                && Element.get(werkstoff.getToolTip()) != Element._NULL) {
            werkstoff.getBridgeMaterial().mElement = Element.get(werkstoff.getToolTip());
            Element.get(werkstoff.getToolTip()).mLinkedMaterials = new ArrayList<>();
            Element.get(werkstoff.getToolTip()).mLinkedMaterials.add(werkstoff.getBridgeMaterial());
        }

        for (OrePrefixes prefixes : OrePrefixes.values())
            if (werkstoff.hasItemType(prefixes) && werkstoff.getBridgeMaterial() != null) {
                GT_OreDictUnificator.set(prefixes, werkstoff.getBridgeMaterial(), werkstoff.get(prefixes), true, true);
                for (ItemStack stack : OreDictionary.getOres(prefixes + werkstoff.getVarName())) {
                    GT_OreDictUnificator.addAssociation(prefixes, werkstoff.getBridgeMaterial(), stack, false);
                }
            }
    }

    /**
     * Constructs a list of recipes to change by scanning all EBF recipes for uses of noble gases.
     *
     * @param GasTags list of gas tags to look out for in EBF recipes
     * @return A multimap from the gas tag (noble and/or anaerobic) to all the recipes containing a gas with that tag
     */
    private static ArrayListMultimap<SubTag, GT_Recipe> getRecipesToChange(SubTag... GasTags) {
        ArrayListMultimap<SubTag, GT_Recipe> toAdd = ArrayListMultimap.create();
        for (GT_Recipe recipe : GT_Recipe.GT_Recipe_Map.sBlastRecipes.mRecipeList) {
            if (recipe.mFluidInputs != null && recipe.mFluidInputs.length > 0) {
                Materials mat = getMaterialFromInputFluid(recipe);
                if (mat != Materials._NULL) {
                    for (SubTag tag : GasTags) {
                        if (mat.contains(tag)) {
                            DebugLog.log(
                                    "Found EBF Recipe to change, Output:"
                                            + BW_Util.translateGTItemStack(recipe.mOutputs[0]));
                            toAdd.put(tag, recipe);
                        }
                    }
                }
            }
        }
        return toAdd;
    }

    /**
     * Scans EBF recipes for no-gas variants of the recipes present in base. Adds these recipes to the base multimap.
     *
     * @param base The recipe multimap to scan and modify
     * @return Set of item outputs (recipe.mOutputs[0]) of the no-gas recipes
     */
    private static HashSet<ItemStack> getNoGasItems(ArrayListMultimap<SubTag, GT_Recipe> base) {
        HashSet<ItemStack> toAdd = new HashSet<>();
        ArrayListMultimap<SubTag, GT_Recipe> repToAdd = ArrayListMultimap.create();
        for (GT_Recipe recipe : GT_Recipe.GT_Recipe_Map.sBlastRecipes.mRecipeList) {
            for (SubTag tag : base.keySet()) recipeLoop: for (GT_Recipe baseRe : base.get(tag)) {
                if (recipe.mInputs.length == baseRe.mInputs.length && recipe.mOutputs.length == baseRe.mOutputs.length)
                    for (int i = 0; i < recipe.mInputs.length; i++) {
                        ItemStack tmpInput = recipe.mInputs[i];
                        if ((recipe.mFluidInputs == null || recipe.mFluidInputs.length == 0)
                                && (whitelistForEBFNoGasRecipeDontCheckItemData.stream()
                                        .anyMatch(s -> GT_Utility.areStacksEqual(s, tmpInput))
                                        || BW_Util.checkStackAndPrefix(recipe.mInputs[i])
                                                && BW_Util.checkStackAndPrefix(baseRe.mInputs[i])
                                                && GT_OreDictUnificator
                                                        .getAssociation(recipe.mInputs[i]).mMaterial.mMaterial.equals(
                                                                GT_OreDictUnificator.getAssociation(
                                                                        baseRe.mInputs[i]).mMaterial.mMaterial)
                                                && GT_Utility.areStacksEqual(recipe.mOutputs[0], baseRe.mOutputs[0]))) {
                            toAdd.add(recipe.mOutputs[0]);
                            repToAdd.put(tag, recipe);
                            continue recipeLoop;
                        }
                    }
            }
        }
        base.putAll(repToAdd);
        return toAdd;
    }

    private static int transformEBFGasRecipeTime(int originalDuration, long originalGasProtons, long newGasProtons) {
        double protonTerm = originalGasProtons * (newGasProtons >= originalGasProtons ? 1.0D : 2.75D) - newGasProtons;
        return Math.max(1, (int) (originalDuration / 200D * Math.max(200D + protonTerm, 1D)));
    }

    private static int transformEBFGasRecipeTime(GT_Recipe recipe, Materials originalGas, Materials newGas) {
        double newEbfMul = gtEbfGasRecipeTimeMultipliers.get(newGas);
        double originalEbfMul = gtEbfGasRecipeTimeMultipliers.get(originalGas);
        if (newEbfMul < 0.0D || originalEbfMul < 0.0D) {
            return transformEBFGasRecipeTime(recipe.mDuration, originalGas.getProtons(), newGas.getProtons());
        }
        return Math.max(1, (int) (recipe.mDuration * newEbfMul / originalEbfMul));
    }

    private static int transformEBFGasRecipeTime(GT_Recipe recipe, Materials originalGas, Werkstoff newGas) {
        double newEbfMul = newGas.getStats().getEbfGasRecipeTimeMultiplier();
        double originalEbfMul = gtEbfGasRecipeTimeMultipliers.get(originalGas);
        if (newEbfMul < 0.0D || originalEbfMul < 0.0D) {
            return transformEBFGasRecipeTime(
                    recipe.mDuration,
                    originalGas.getProtons(),
                    newGas.getStats().getProtons());
        }
        return Math.max(1, (int) (recipe.mDuration * newEbfMul / originalEbfMul));
    }

    private static int transformEBFNoGasRecipeTime(GT_Recipe recipe, Materials originalGas) {
        return transformEBFGasRecipeTime(recipe.mDuration, originalGas.getProtons(), 0);
    }

    private static void editEBFMaterialRecipes(SubTag GasTag, GT_Recipe recipe, Materials originalGas,
            HashSet<GT_Recipe> toAdd) {
        for (Materials newGas : Materials.values()) {
            if (newGas.contains(GasTag)) {
                int time = transformEBFGasRecipeTime(recipe, originalGas, newGas);
                int gasAmount = Math.max(
                        1,
                        (int) Math.round(
                                recipe.mFluidInputs[0].amount * gtEbfGasRecipeConsumptionMultipliers.get(newGas)));
                if (recipe.mFluidInputs != null && recipe.mFluidInputs.length == 1
                        && recipe.mFluidInputs[0].isFluidEqual(newGas.getGas(0))) {
                    // preserve original recipe owner
                    toAdd.add(
                            new BWRecipes.DynamicGTRecipe(
                                    false,
                                    recipe.mInputs,
                                    recipe.mOutputs,
                                    recipe.mSpecialItems,
                                    recipe.mChances,
                                    new FluidStack[] { newGas.getGas(gasAmount) },
                                    recipe.mFluidOutputs,
                                    time,
                                    recipe.mEUt,
                                    recipe.mSpecialValue,
                                    recipe));
                } else {
                    // new recipe
                    toAdd.add(
                            new BWRecipes.DynamicGTRecipe(
                                    false,
                                    recipe.mInputs,
                                    recipe.mOutputs,
                                    recipe.mSpecialItems,
                                    recipe.mChances,
                                    new FluidStack[] { newGas.getGas(gasAmount) },
                                    recipe.mFluidOutputs,
                                    time,
                                    recipe.mEUt,
                                    recipe.mSpecialValue));
                }
            }
        }
    }

    private static void editEBFWerkstoffRecipes(SubTag GasTag, GT_Recipe recipe, Materials originalGas,
            HashSet<GT_Recipe> toAdd) {
        for (Werkstoff newGas : Werkstoff.werkstoffHashMap.values()) {
            if (newGas.contains(GasTag)) {
                int time = transformEBFGasRecipeTime(recipe, originalGas, newGas);
                int gasAmount = Math.max(
                        1,
                        (int) Math.round(
                                recipe.mFluidInputs[0].amount
                                        * newGas.getStats().getEbfGasRecipeConsumedAmountMultiplier()));
                if (recipe.mFluidInputs != null && recipe.mFluidInputs.length == 1
                        && recipe.mFluidInputs[0]
                                .isFluidEqual(new FluidStack(Objects.requireNonNull(fluids.get(newGas)), 0))) {
                    // preserve original recipe owner
                    toAdd.add(
                            new BWRecipes.DynamicGTRecipe(
                                    false,
                                    recipe.mInputs,
                                    recipe.mOutputs,
                                    recipe.mSpecialItems,
                                    recipe.mChances,
                                    new FluidStack[] {
                                            new FluidStack(Objects.requireNonNull(fluids.get(newGas)), gasAmount) },
                                    recipe.mFluidOutputs,
                                    time,
                                    recipe.mEUt,
                                    recipe.mSpecialValue,
                                    recipe));
                } else {
                    // new recipe
                    toAdd.add(
                            new BWRecipes.DynamicGTRecipe(
                                    false,
                                    recipe.mInputs,
                                    recipe.mOutputs,
                                    recipe.mSpecialItems,
                                    recipe.mChances,
                                    new FluidStack[] {
                                            new FluidStack(Objects.requireNonNull(fluids.get(newGas)), gasAmount) },
                                    recipe.mFluidOutputs,
                                    time,
                                    recipe.mEUt,
                                    recipe.mSpecialValue));
                }
            }
        }
    }

    private static void editEBFNoGasRecipes(GT_Recipe recipe, Materials originalGas, HashSet<GT_Recipe> toAdd,
            HashSet<ItemStack> noGas) {
        for (ItemStack is : noGas) {
            byte circuitConfiguration = 1;
            if (GT_Utility.areStacksEqual(is, recipe.mOutputs[0])) {
                ArrayList<ItemStack> inputs = new ArrayList<>(recipe.mInputs.length);
                for (ItemStack stack : recipe.mInputs)
                    if (!GT_Utility.areStacksEqual(GT_Utility.getIntegratedCircuit(11), stack)
                            && !GT_Utility.areStacksEqual(GT_Utility.getIntegratedCircuit(14), stack)
                            && !GT_Utility.areStacksEqual(GT_Utility.getIntegratedCircuit(19), stack)) {
                                if (BW_Util.checkStackAndPrefix(stack))
                                    circuitConfiguration = (byte) (OrePrefixes.dustSmall
                                            .equals(GT_OreDictUnificator.getAssociation(stack).mPrefix)
                                                    ? 4
                                                    : OrePrefixes.dustTiny.equals(
                                                            GT_OreDictUnificator.getAssociation(stack).mPrefix) ? 9
                                                                    : 1);
                                inputs.add(stack);
                            }
                inputs.add(GT_Utility.getIntegratedCircuit(circuitConfiguration));
                toAdd.add(
                        new BWRecipes.DynamicGTRecipe(
                                false,
                                inputs.toArray(new ItemStack[0]),
                                recipe.mOutputs,
                                recipe.mSpecialItems,
                                recipe.mChances,
                                null,
                                recipe.mFluidOutputs,
                                transformEBFNoGasRecipeTime(recipe, originalGas),
                                recipe.mEUt,
                                recipe.mSpecialValue,
                                recipe));
                break;
            }
        }
    }

    private static void removeDuplicateGasRecipes(HashSet<GT_Recipe> toAdd) {
        HashSet<GT_Recipe> duplicates = new HashSet<>();
        for (GT_Recipe recipe : toAdd) {
            for (GT_Recipe recipe2 : toAdd) {
                if (recipe.mEUt != recipe2.mEUt || recipe.mDuration != recipe2.mDuration
                        || recipe.mSpecialValue != recipe2.mSpecialValue
                        || recipe == recipe2
                        || recipe.mInputs.length != recipe2.mInputs.length
                        || recipe.mFluidInputs.length != recipe2.mFluidInputs.length)
                    continue;
                boolean isSame = true;
                for (int i = 0; i < recipe.mInputs.length; i++) {
                    if (!GT_Utility.areStacksEqual(recipe.mInputs[i], recipe2.mInputs[i])) isSame = false;
                }
                for (int i = 0; i < recipe.mFluidInputs.length; i++) {
                    if (!GT_Utility.areFluidsEqual(recipe.mFluidInputs[i], recipe2.mFluidInputs[i])) isSame = false;
                }
                if (isSame) duplicates.add(recipe2);
            }
        }
        toAdd.removeAll(duplicates);
    }

    private static Materials getMaterialFromInputFluid(GT_Recipe recipe) {
        String materialString = recipe.mFluidInputs[0].getFluid().getName();
        materialString = StringUtils.removeStart(materialString, "molten");
        materialString = StringUtils.removeStart(materialString, "fluid");
        materialString = StringUtils.capitalize(materialString);
        return Materials.get(materialString);
    }

    private static void editRecipes(ArrayListMultimap<SubTag, GT_Recipe> base, HashSet<ItemStack> noGas) {
        if (GT_Recipe.GT_Recipe_Map.sBlastRecipes.mRecipeFluidNameMap
                .contains(Objects.requireNonNull(fluids.get(Oganesson)).getName()))
            return;

        HashSet<GT_Recipe> toAdd = new HashSet<>();

        for (SubTag gasTag : base.keySet()) {
            for (GT_Recipe recipe : base.get(gasTag)) {
                if (recipe.mFluidInputs != null && recipe.mFluidInputs.length > 0) {
                    Materials originalGas = getMaterialFromInputFluid(recipe);
                    if (originalGas != Materials._NULL) {
                        editEBFWerkstoffRecipes(gasTag, recipe, originalGas, toAdd);
                        editEBFMaterialRecipes(gasTag, recipe, originalGas, toAdd);
                        editEBFNoGasRecipes(recipe, originalGas, toAdd, noGas);
                    }
                }
            }
            GT_Recipe.GT_Recipe_Map.sBlastRecipes.mRecipeList.removeAll(base.get(gasTag));
        }

        removeDuplicateGasRecipes(toAdd);
        toAdd.forEach(GT_Recipe.GT_Recipe_Map.sBlastRecipes::add);
    }

    public static void addElectricImplosionCompressorRecipes() {
        GT_Recipe.GT_Recipe_Map.sImplosionRecipes.mRecipeList.stream().filter(e -> e.mInputs != null).forEach(
                recipe -> BWRecipes.instance.eicMap.addRecipe(
                        true,
                        Arrays.stream(recipe.mInputs).filter(e -> !StaticRecipeChangeLoaders.checkForExplosives(e))
                                .distinct().toArray(ItemStack[]::new),
                        recipe.mOutputs,
                        null,
                        null,
                        null,
                        1,
                        (int) TierEU.RECIPE_UEV,
                        0));

        // Custom EIC recipes.
        new ElectricImplosionCompressorRecipes().run();
    }

    private static boolean checkForExplosives(ItemStack input) {
        return GT_Utility.areStacksEqual(input, new ItemStack(Blocks.tnt))
                || GT_Utility.areStacksEqual(input, GT_ModHandler.getIC2Item("industrialTnt", 1L))
                || GT_Utility.areStacksEqual(input, GT_ModHandler.getIC2Item("dynamite", 1L))
                || GT_Utility.areStacksEqual(input, ItemList.Block_Powderbarrel.get(1L));
    }
}
