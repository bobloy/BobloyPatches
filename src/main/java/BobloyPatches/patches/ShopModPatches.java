package BobloyPatches.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

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


//    @SpirePatch2(clz = AbstractDungeon.class, method = "render")
//    public static class AbstractDungeonRenderPatch {
//        public static ExprEditor Instrument () {
//            return new ExprEditor() {
//                int tpr = 0;
//                int rbs = 0;
//                @Override
//                public void edit(MethodCall m) throws CannotCompileException {
//                    try {
//                        if (m.getClassName().equals("com.megacrit.cardcrawl.ui.panels.TopPanel") && m.getMethodName().equals("render")) {
//                            if (tpr++ == 0) {
//                                m.replace("{}");
//                            }
//                        }
//                        if (m.getClassName().equals("com.megacrit.cardcrawl.core.overlayMenu") && m.getMethodName().equals("renderBlackScreen")) {
//                            if (rbs++ == 0) {
//                                m.replace("{" +
//                                        "$_ = $proceed($$);" +
//                                        "if (com.megacrit.cardcrawl.dungeons.AbstractDungeon.screen != com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen.UNLOCK) {" +
//                                        "    if (com.megacrit.cardcrawl.dungeons.AbstractDungeon.topPanel != null) { com.megacrit.cardcrawl.dungeons.AbstractDungeon.topPanel.render($1); }" +
//                                        "}" +
//                                        "}");
//                            }
//                        }
//                    }
//                    catch(Exception e){return;}
//                }
//            };
//        }
//    }

//    @SpirePatch2(clz = PotionPopUp.class, method = "render")
//    public static class PotionPopUpRenderPatch {
//        public static ExprEditor Instrument () {
//            return new ExprEditor() {
//                int rfcw = 0;
//                @Override
//                public void edit(MethodCall m) throws CannotCompileException {
//                    if (m.getMethodName().equals("queuePowerTips")) {
//                        if (rfcw++ == 0) {
//                            m.replace("{}");
//                        }
//                    }
//                }
//            };
//        }
//    }
}
