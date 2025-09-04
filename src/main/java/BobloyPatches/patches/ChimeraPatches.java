package BobloyPatches.patches;


import CardAugments.patches.OnCardGeneratedPatches;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
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
}
