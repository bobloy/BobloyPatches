package BobloyPatches.patches;

import BobloyPatches.util.ModIDs;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import com.evacipated.cardcrawl.mod.widepotions.extensions.PotionExtensions;
import shopmod.relics.MerchantsRug;

public class ShopModPatches {

    @SpirePatch2(clz = PotionPopUp.class, method = "update")
    public static class PotionPopUpUpdatePatch {
        public static ExprEditor Instrument () {
            return new ExprEditor() {
                int qpt = 0;
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("queuePowerTips")) {
                        if (qpt++ == 0) {
                            m.replace("{}");
                        }
                    }
                }
            };
        }
    }
    
    @SpirePatch2(clz = AbstractCard.class, method = "renderCardTip")
    public static class AbstractCardRenderCardTipPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> patch(AbstractCard __instance) {
            if (!AbstractDungeon.topPanel.potionUi.isHidden) {
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }
    
    @SpirePatch2(cls = "shopmod.relics.MerchantsRug", method = "potionSalePrice", requiredModId = ModIDs.shopMod)
    public static class WidePotionSalePricePostfix {
        @SpirePostfixPatch
        public static int patch(int __result, int slot, com.megacrit.cardcrawl.potions.AbstractPotion potion) {
            if (__result > 0 && Loader.isModLoaded(ModIDs.widePotions) && PotionExtensions.isWide(potion)) {
                return __result * 2;
            }
            return __result;
        }
    }
}
