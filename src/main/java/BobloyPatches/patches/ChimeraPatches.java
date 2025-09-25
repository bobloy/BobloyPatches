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

            // Reapply card values that were lost during the initial application
            card.timesUpgraded = timesUpgraded;
            card.multiDamage = multiDamage;
            Reflection.LoadFieldValuesOnObject(card, extraData);

            DontRollChimera.dontRollChimera.set(card, true);

            card.initializeDescription();
        }

    }

    @SpirePatch2(clz = AbstractCard.class, method = SpirePatch.CLASS, requiredModId = ModIDs.spireTogether)
    public static class DontRollChimera{
        public static SpireField<Boolean> dontRollChimera = new SpireField<>(() -> false);
    }

    @SpirePatch2(clz = OnCardGeneratedPatches.ModifySpawnedMasterDeckCards.class, method = "patch", requiredModId = ModIDs.spireTogether)
    public static class ModifySpawnedMasterDeckCardsPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> patch(AbstractCard ___card) {
            if (DontRollChimera.dontRollChimera.get(___card)) {
                DontRollChimera.dontRollChimera.set(___card, false);
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

    @SpirePatch2(clz = NetworkCard.class, method = SpirePatch.CONSTRUCTOR, requiredModId = "spireTogether")
    public static class NetworkCardFields {
        @SpireRawPatch
        public static void addModifiers(CtBehavior ctBehavoir) throws CannotCompileException, NotFoundException {
            CtClass declaring = ctBehavoir.getDeclaringClass();
            // Avoid adding the field twice if other patches or game versions already defined it

            try {
                if (declaring.getField("cardModifiers") != null) {
                    return;
                }
            }catch (NotFoundException ignored) {}

            ClassPool pool = declaring.getClassPool();
            CtClass listType = pool.get("java.util.ArrayList"); // Do NOT change generic signature of JRE class
            CtField field = new CtField(listType, "cardModifiers", declaring);
            // Do not use a CtField initializer to avoid potential bad constructor signatures at runtime
            // We'll set the field explicitly in a postfix patch after construction
            declaring.addField(field);
        }

        @SpirePostfixPatch
        public static void initModifiers(NetworkCard __instance){
            ReflectionHacks.setPrivate(__instance, NetworkCard.class, "cardModifiers", new ArrayList<>());
        }
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
                    }
                }
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