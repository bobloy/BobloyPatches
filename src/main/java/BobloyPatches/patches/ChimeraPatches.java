package BobloyPatches.patches;


import CardAugments.patches.OnCardGeneratedPatches;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import spireTogether.network.objects.items.NetworkCard;
import spireTogether.screens.trading.TradingScreen;

public class ChimeraPatches {
    static boolean isTrading = false;

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
//            if(!Loader.isModLoaded("CardAugments")){
//                return;
//            }
            isTrading = true;
        }
    }

//    @SpirePatch2(clz = NetworkCard.class, method = "ToStandard", paramtypez = {}, requiredModId = "spireTogether")
//    public static class ToStandardPatch {
//        @SpirePostfixPatch
//        public static AbstractCard patch(AbstractCard __instance) {
//            __instance.initializeDescription();
//            return __instance;
//        }
//
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
//    }


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