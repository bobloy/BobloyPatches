package BobloyPatches.patches;

import java.lang.System;

import BobloyPatches.BobloyPatches;
import BobloyPatches.util.ModIDs;
import basemod.abstracts.CustomSavable;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pansTrinkets.DefaultMod;
import pansTrinkets.helpers.TrinketHelper;

// TODO figure out     "pansTrinkets" dependency in ModTheSpire.json

public class TrinketHelperPatches implements CustomSavable<Float> {
    public static final Logger logger = LogManager.getLogger(TrinketHelperPatches.class.getName());

    public static int modWeight =  0;
    public static Float modWeightF =  0f;


    public static void changeModWeight(int amount) {
        modWeight = amount + modWeight;
        modWeightF = (float)amount + modWeightF;
    }

    public static void changeModWeight(float amount) {
        modWeightF = amount + modWeightF;
        modWeight = modWeightF.intValue();
    }

    public static int getModWeight() {
        return modWeight;
    }
    @Override
    public Float onSave() {
        return 0f;
    }

    @Override
    public void onLoad(Float saved) {

    }

    @SpirePatch(
            clz = TrinketHelper.class,
            method = "changeMaxWeight",
            paramtypez = {int.class},
            requiredModId = ModIDs.pansTrinkets
    )
    public static class TrinketHelperPatchesChangeMaxWeightPatch {
        @SpirePostfixPatch
        public static void changeMaxWeightPatch(int change) {
            logger.info("TrinketHelperPatchesChangeMaxWeightPatch | Max Weight changed!");
            changeModWeight(change);
            if (DefaultMod.enableProgressiveMaxWeight) {
                TrinketHelper.maxWeightF = (float) (AbstractDungeon.player.masterDeck.size() / 3);
                TrinketHelper.maxWeight = TrinketHelper.maxWeightF.intValue() + getModWeight();
            }
        }
    }

    @SpirePatch(
            clz = TrinketHelper.class,
            method = "changeMaxWeight",
            paramtypez = {float.class},
            requiredModId = ModIDs.pansTrinkets
    )
    public static class TrinketHelperPatchesChangeMaxWeightPatchF {
        @SpirePostfixPatch
        public static void changeMaxWeightPatch(float change) {
            logger.info("TrinketHelperPatchesChangeMaxWeightPatchF | Max Weight changed!");
            // changeModWeight(change);
            // NOTE: Do not change mod weight for float changes. The only float change is the 0.5 increment from adding cards

            if (DefaultMod.enableProgressiveMaxWeight) {
                TrinketHelper.maxWeightF = (float) (AbstractDungeon.player.masterDeck.size() / 3);
                TrinketHelper.maxWeight = TrinketHelper.maxWeightF.intValue() + getModWeight();
            }
        }
    }

    @SpirePatch(
            clz = CardGroup.class,
            method = "removeCard",
            paramtypez = {AbstractCard.class},
            requiredModId = ModIDs.pansTrinkets
    )
    public static class OnRemoveCardFromMasterDeckPatch {
        public static void Postfix(CardGroup __instance, AbstractCard c) {
            if (!Loader.isModLoaded(ModIDs.pansTrinkets)){
                return;
            }
            if (__instance.type == CardGroup.CardGroupType.MASTER_DECK) {
                TrinketHelper.changeMaxWeight(0);
            }
        }
    }

}


