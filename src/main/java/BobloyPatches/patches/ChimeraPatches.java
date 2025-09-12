package BobloyPatches.patches;


import BobloyPatches.BobloyPatches;
import CardAugments.CardAugmentsMod;
import CardAugments.cardmods.AbstractAugment;
import CardAugments.patches.OnCardGeneratedPatches;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spireTogether.network.objects.items.NetworkCard;
import spireTogether.screens.trading.TradingScreen;

import java.util.ArrayList;
import java.util.Collections;

public class ChimeraPatches {
    static boolean isTrading = false;

    public static void addModifierButDontRunInitial(AbstractCard card, AbstractCardModifier mod) {
        if (mod.shouldApply(card)) {
            CardModifierManager.modifiers(card).add(mod);
            Collections.sort(CardModifierManager.modifiers(card));
//            mod.onInitialApplication(card);
            CardModifierManager.onCardModified(card);
            card.initializeDescription();
        }

    }

    @SpirePatch2(clz = OnCardGeneratedPatches.ModifySpawnedCardsPatch.class, method = "patch", requiredModId="CardAugments")
    public static class ModifySpawnedCardsPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> patch() {
            if(isTrading){
                isTrading = false;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch2(clz = TradingScreen.class, method = "Trade", requiredModId="spireTogether")
    public static class TradePatch {
        @SpirePrefixPatch
        public static void patch() {
            if(!Loader.isModLoaded("CardAugments")){
                return;
            }
            isTrading = true;

        }
    }


    @SpirePatch2(clz = NetworkCard.class, method = SpirePatch.CLASS, requiredModId = "spireTogether")
    public static class NetworkCardFields {
        public static SpireField<ArrayList<String>> cardModifiers = new SpireField<>(ArrayList::new);
    }


    @SpirePatch2(clz = NetworkCard.class, method = "Generate", paramtypez = {AbstractCard.class, AbstractMonster.class}, requiredModId = "spireTogether")
    public static class GeneratePatch {
        @SpirePostfixPatch
        public static NetworkCard patch(AbstractCard c, NetworkCard __result) {

            if (__result == null) {
                return null;
            }

            if(Loader.isModLoaded("CardAugments")){
                ArrayList<String> modifierIDs = new ArrayList<>();
                for (AbstractCardModifier m : CardModifierManager.modifiers(c)) {
                    if (m instanceof AbstractAugment) {
                        modifierIDs.add(m.identifier(c));
//                        __result.cardModifiers.add(m.identifier(c));
                    }
                }
                NetworkCardFields.cardModifiers.set(__result, modifierIDs);
            }

            return __result;
        }
    }


    // TODO: Use the SpireField on NetworkCard to reconstruct the list of modifiers on the AbstractCard.
    @SpirePatch2(clz = NetworkCard.class, method = "ToStandard", paramtypez = {}, requiredModId = "spireTogether")
    public static class ToStandardPatch {
        @SpirePostfixPatch
        public static AbstractCard patch(NetworkCard __instance, AbstractCard __result) {

            // Guard against null card reconstruction to avoid NPEs during modifier application
            if (__result == null) {
                return null;
            }

            if(Loader.isModLoaded("CardAugments")){
                ArrayList<String> modifierIDs = NetworkCardFields.cardModifiers.get(__instance);
                if (modifierIDs == null) {
                    return __result;
                }
                for (String modID : modifierIDs){
                    if (CardAugmentsMod.modMap.containsKey(modID)) {
                        AbstractAugment a = CardAugmentsMod.modMap.get(modID);
                        if (a != null && a.canApplyTo(__result)) {
                            addModifierButDontRunInitial(__result, a);
                        }
                    }
                }
            }

            return __result;
        }
    }

////        public static ExprEditor Instrument () {
////            return new ExprEditor() {
////                int ficp = 0;
////                @Override
////                public void edit(MethodCall m) throws CannotCompileException {
////                    try {
////                        if(m.getMethodName().equals("findIdealCenterPosition")) {
////                            if(ficp++ ==0){
////                                m.replace("{" +
////                                        "$_ = $proceed($$);" +
////                                        "$1.initializeDescription();" +
////                                        "}");
////                            }
////                        }
////                    } catch (Exception e) {
////                        System.out.println("Failed to patch chimera trading");
////                    }
////                }
////            };
////        }


}


//GsonBuilder builder = new GsonBuilder();
//if (CardModifierPatches.modifierAdapter == null) {
//    CardModifierPatches.initializeAdapterFactory();
//}
//builder.registerTypeAdapterFactory(CardModifierPatches.modifierAdapter);
//Gson gson = builder.create();
//ModSaves.ArrayListOfJsonElement cardModifierSaves = ModSaves.cardModifierSaves.get(CardCrawlGame.saveFile);
//i = 0;
//if (cardModifierSaves != null) {
//    for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
//        ArrayList<AbstractCardModifier> cardModifiers = new ArrayList<>();
//
//        JsonElement loaded = i >= cardModifierSaves.size() ? null : cardModifierSaves.get(i);
//        if (loaded != null && loaded.isJsonArray()) {
//            JsonArray array = loaded.getAsJsonArray();
//
//            for (JsonElement element : array) {
//                AbstractCardModifier cardModifier = null;
//                try {
//                    cardModifier = gson.fromJson(element, new TypeToken<AbstractCardModifier>() {
//                    }.getType());
//                } catch (Exception e) {
//                    System.out.println("Unable to load cardmod: " + element);
//                    cardModifiers.add(getErrorMod());
//                }
//                if (cardModifier != null) {
//                    cardModifiers.add(cardModifier);
//                }
//            }
//        }
//        CardModifierManager.removeAllModifiers(card, true);
//        for (AbstractCardModifier mod : cardModifiers) {
//            CardModifierManager.addModifier(card, mod.makeCopy());
//        }
//        i++;