package BobloyPatches.patches;


import BobloyPatches.util.ModIDs;
import CardAugments.CardAugmentsMod;
import CardAugments.cardmods.AbstractAugment;
import CardAugments.patches.OnCardGeneratedPatches;
import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.*;
import spireTogether.network.P2P.P2PCallbacks;
import spireTogether.network.P2P.P2PMessageSender;
import spireTogether.network.objects.items.NetworkCard;
import spireTogether.screens.trading.TradingScreen;
import spireTogether.util.Reflection;
import spireTogether.util.SerializablePair;

import java.util.ArrayList;
import java.util.Collections;

import static BobloyPatches.network.P2PMessageSender_Bobloy.sendChimeraTrade;

public class ChimeraPatches {
    static int isTrading = 0;

    public static void addModifierButDontRunInitial(AbstractCard card, AbstractCardModifier mod, ArrayList<SerializablePair<String, Object>> extraData) {
        if (mod.shouldApply(card)) {

            int timesUpgraded = card.timesUpgraded;
            int[] multiDamage = card.multiDamage;

            CardModifierManager.modifiers(card).add(mod);
            Collections.sort(CardModifierManager.modifiers(card));
            mod.onInitialApplication(card);
            CardModifierManager.onCardModified(card);

            card.timesUpgraded = timesUpgraded;
            card.multiDamage = multiDamage;
            Reflection.LoadFieldValuesOnObject(card, extraData);

            card.initializeDescription();
        }

    }


    @SpirePatch2(clz = OnCardGeneratedPatches.ModifySpawnedMasterDeckCards.class, method = "patch", requiredModId = ModIDs.spireTogether)
    public static class ModifySpawnedMasterDeckCardsPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> patch(AbstractCard ___card) {
            if (!CardModifierManager.modifiers(___card).isEmpty()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch2(clz = OnCardGeneratedPatches.CreatedCards.class, method = "roll", requiredModId = ModIDs.spireTogether)
    public static class CreatedCardsPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> patch(Object[] __args) {
            if (__args[0] instanceof AbstractCard) {
                AbstractCard card = (AbstractCard) __args[0];
                return ModifySpawnedMasterDeckCardsPatch.patch(card);
            }
            return SpireReturn.Continue();
        }
    }


    // Specifically targeted no-chimera triggers on trade.
//    @SpirePatch2(clz = CardAugmentsMod.class, method = "rollCardAugment", paramtypez = {AbstractCard.class, int.class}, requiredModId = ModIDs.cardAugments)
//    public static class ModifySpawnedCardsPatch {
//        @SpirePrefixPatch
//        public static SpireReturn<Void> patch() {
//            if (isTrading > 0) {
//                isTrading--;
//                return SpireReturn.Return(null);
//            }
//            return SpireReturn.Continue();
//        }
//    }
//
//    @SpirePatch2(clz = P2PCallbacks.class, method = "OnTradeToModifyReceivingCards", requiredModId = ModIDs.spireTogether)
//    public static class TradePatch {
//        @SpirePostfixPatch
//        public static ArrayList<AbstractCard> patch(ArrayList<AbstractCard> __result) {
//            if (!Loader.isModLoaded("CardAugments") || __result.isEmpty()) {
//                return __result;
//            }
//            isTrading = __result.size();
//            return __result;
//        }
//    }

    // NEW METHOD: ONLY WORKS FOR TRADING
//    @SpirePatch2(clz = P2PMessageSender.class, method = "Send_TradingChangedCards", requiredModId = ModIDs.spireTogether)
//    public static class Send_TradingChangedCardsPatch {
//        @SpirePostfixPatch
//        public static void patch(Integer playerID) {
//            if (!Loader.isModLoaded("CardAugments")) {
//                return;
//            }
//            sendChimeraTrade(TradingScreen.tradingScreen.playerCards, playerID);
//        }
//    }

    // OLD METHOD: WORKS FOR ALL CARDS INSTEAD OF JUST TRADING, RISKY??
    @SpirePatch2(clz = NetworkCard.class, method = SpirePatch.CONSTRUCTOR, requiredModId = "spireTogether")
    public static class NetworkCardFields {
        @SpireRawPatch
        public static void addModifiers(CtBehavior ctBehavoir) throws CannotCompileException, NotFoundException {
//            CtClass runData = ctBehavoir.getDeclaringClass().getClassPool().get("spireTogether.network.objects.runData");

//            String fieldSource = "public java.util.ArrayList<java.lang.String> cardModifiers = new java.util.ArrayList<>();";

//            CtField field =  CtField.make(fieldSource, ctBehavoir.getDeclaringClass());
            CtClass ctClass = ClassPool.getDefault().get("java.util.ArrayList");

            ctClass.setGenericSignature("Ljava/util/ArrayList<Ljava/lang/String;>;");

            CtField field = new CtField(ctClass, "cardModifiers", ctBehavoir.getDeclaringClass());

            ctBehavoir.getDeclaringClass().addField(field);
        }

//        public static SpireField<ArrayList<String>> cardModifiers = new SpireField<>(ArrayList::new);
    }


    @SpirePatch2(clz = NetworkCard.class, method = "Generate", paramtypez = {AbstractCard.class, AbstractMonster.class}, requiredModId = "spireTogether")
    public static class GeneratePatch {
        @SpirePostfixPatch
        public static NetworkCard patch(AbstractCard c, NetworkCard __result) {

            if (__result == null) {
                return null;
            }

            if (Loader.isModLoaded("CardAugments")) {
                ArrayList<String> modifierIDs = new ArrayList<>();
                for (AbstractCardModifier m : CardModifierManager.modifiers(c)) {
                    if (m instanceof AbstractAugment) {
                        modifierIDs.add(m.identifier(c));
//                        __result.cardModifiers.add(m.identifier(c));
                    }
                }
//                NetworkCardFields.cardModifiers.set(__result, modifierIDs);
//                __result.cardModifiers = modifierIDs;
                ReflectionHacks.setPrivate(__result, NetworkCard.class, "cardModifiers", modifierIDs);
            }

            return __result;
        }
    }


    @SpirePatch2(clz = NetworkCard.class, method = "ToStandard", paramtypez = {}, requiredModId = "spireTogether")
    public static class ToStandardPatch {
        @SpirePostfixPatch
        public static AbstractCard patch(NetworkCard __instance, AbstractCard __result) {

            // Guard against null card reconstruction to avoid NPEs during modifier application
            if (__result == null) {
                return null;
            }

            if (Loader.isModLoaded("CardAugments")) {
//                ArrayList<String> modifierIDs = NetworkCardFields.cardModifiers.get(__instance);
                ArrayList<String> modifierIDs = ReflectionHacks.getPrivate(__instance, NetworkCard.class, "cardModifiers");
                if (modifierIDs == null) {
                    return __result;
                }
                for (String modID : modifierIDs) {
                    if (CardAugmentsMod.modMap.containsKey(modID)) {
                        AbstractAugment a = CardAugmentsMod.modMap.get(modID);
                        if (a != null) {  // Removed: && a.canApplyTo(__result)
                            addModifierButDontRunInitial(__result, a, __instance.extraData);
                        }
                    }
                }
            }

            return __result;
        }
    }
}