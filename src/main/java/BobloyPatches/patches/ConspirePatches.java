package BobloyPatches.patches;

import BobloyPatches.network.P2PMessageSender_Bobloy;
import BobloyPatches.util.ModIDs;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import conspire.actions.ReduceHolyAction;
import conspire.cards.colorless.InfernalBerry;
import conspire.monsters.HollyBat;
import spireTogether.network.P2P.P2PManager;
import spireTogether.network.P2P.P2PPlayer;
import spireTogether.network.objects.items.NetworkCard;

import static spireTogether.util.SpireHelp.Multiplayer.Players.GetPlayers;

public class ConspirePatches {

    // Guard flag to prevent infinite network loops when applying ReduceHolyAction from a received message
    public static boolean suppressReduceHolyNetwork = false;

    @SpirePatch2(clz = HollyBat.class, method=SpirePatch.CLASS, requiredModId = ModIDs.conspire)
    public static class OriginalHolyAmt{
        public static SpireField<Integer> origHolyAmt = new SpireField<>(() -> 0);
    }

    @SpirePatch2(clz = HollyBat.class, method="usePreBattleAction", requiredModId = ModIDs.conspire)
    public static class HollyBatPatch {
        @SpirePrefixPatch
        public static void HollyBatPatchPrefix(HollyBat __instance, int ___holyAmt) {
            OriginalHolyAmt.origHolyAmt.set(__instance, ___holyAmt);
            ___holyAmt += ___holyAmt * P2PManager.GetPlayerCountWithoutSelf();
        }

        @SpirePostfixPatch
        public static void HollyBatPatchPostfix(HollyBat __instance, int ___holyAmt) {
            ___holyAmt = OriginalHolyAmt.origHolyAmt.get(__instance);
        }
    }

    @SpirePatch2(clz = HollyBat.class, method="countBerries", requiredModId = ModIDs.conspire)
    public static class HollyBatBerriesPatch {
        @SpirePostfixPatch
        public static int HollyBatPatchPrefix(int __result) {

            int count = 0;
            for(P2PPlayer p : GetPlayers(true, true)){
                if(p.IsPlayerInSameRoom()){
                    for (NetworkCard c : p.handPile) {
                        if (c.ToStandard() instanceof InfernalBerry) ++count;
                    }
                    for (NetworkCard c : p.drawPile) {
                        if (c.ToStandard() instanceof InfernalBerry) ++count;
                    }
                    for (NetworkCard c : p.discardPile) {
                        if (c.ToStandard() instanceof InfernalBerry) ++count;
                    }
                }
            }

            return __result + count;
        }
    }

    @SpirePatch2(clz = ReduceHolyAction.class, method=SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
    public static class ReduceHolyActionPatch {
        @SpirePostfixPatch
        public static void ReduceHolyActionPatchPostfix(ReduceHolyAction __instance, AbstractCreature target, int amount) {
            if (!suppressReduceHolyNetwork) {
                P2PMessageSender_Bobloy.sendReduceHolyAction(target, amount);
            }
        }
    }

}
