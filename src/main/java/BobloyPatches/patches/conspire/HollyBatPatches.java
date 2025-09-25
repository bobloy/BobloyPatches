package BobloyPatches.patches.conspire;

import BobloyPatches.network.P2PMessageSender_Bobloy;
import BobloyPatches.util.ModIDs;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.ChangeStateAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import conspire.actions.ReduceHolyAction;
import conspire.cards.colorless.InfernalBerry;
import conspire.monsters.HollyBat;
import conspire.powers.HolyPower;
import spireTogether.SpireTogetherMod;
import spireTogether.network.P2P.P2PManager;
import spireTogether.network.P2P.P2PPlayer;
import spireTogether.network.objects.entities.NetworkPower;
import spireTogether.network.objects.items.NetworkCard;

import static spireTogether.network.P2P.P2PMessageSender.Send_MonsterPowerDecreased;
import static spireTogether.patches.network.CreatureSyncPatches.*;
import static spireTogether.util.SpireHelp.Multiplayer.Players.GetPlayers;

public class HollyBatPatches {

    @SpirePatch2(clz = HollyBat.class, method = SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
    public static class HollyBatConstructorPatch {
        @SpirePostfixPatch
        public static void HollyBatPatchPrefix(HollyBat __instance, @ByRef int[] ___holyAmt) {
            ___holyAmt[0] += ___holyAmt[0] * P2PManager.GetPlayerCountWithoutSelf();
        }
    }

    @SpirePatch2(clz = HollyBat.class, method = "countBerries", requiredModId = ModIDs.conspire)
    public static class HollyBatBerriesPatch {
        @SpirePostfixPatch
        public static int HollyBatPatchPrefix(int __result) {

            int count = 0;
            for (P2PPlayer p : GetPlayers(true, true)) {
                if (p.IsPlayerInSameRoom()) {  // AndAction - At least one player has an action remaining
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

    @SpirePatch2(clz = HollyBat.class, method = "takeTurn", requiredModId = ModIDs.conspire)
    public static class HollyBatTakeTurnPatch {
        private static final byte PELT = 1;
        private static final byte PATCHED = 7;

        @SpirePrefixPatch
        public static void HollyBatTakeTurnPatchPrefix(HollyBat __instance, @ByRef boolean[] ___donePelt, int ___holyAmt) {
            if (__instance.nextMove == PELT) {
                ___donePelt[0] = true;
                AbstractDungeon.actionManager.addToBottom(new ChangeStateAction(__instance, "PELT"));
                AbstractDungeon.actionManager.addToBottom(new WaitAction(1.2f));
                int playerCount = 1;
                for (P2PPlayer p : GetPlayers(true, true)) {
                    if (p.IsPlayerInSameRoom()) {
                        playerCount++;
                    }
                }
                int berryAmt = (int) Math.ceil((double) ___holyAmt / playerCount);
                AbstractDungeon.actionManager.addToBottom(new MakeTempCardInDrawPileAction(new InfernalBerry(), berryAmt, true, false));
                __instance.nextMove = PATCHED;
            }
        }
    }

    @SpirePatch2(clz = ReduceHolyAction.class, method = SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
    public static class ReduceHolyActionPatch {
        @SpirePostfixPatch
        public static void ReduceHolyActionPatchPostfix(ReduceHolyAction __instance, AbstractCreature target, int amount) {
            __instance.source = AbstractDungeon.player;
            if (SpireTogetherMod.isConnected) {
                if (IsMonster(target)) {
                    if (syncMonsterReducePower) {
                        if (ShouldSyncAction(__instance.source)) {
                            AbstractPower powerInstance = target.getPower(HolyPower.POWER_ID);
                            if (powerInstance != null) {
                                Send_MonsterPowerDecreased(NetworkPower.Generate(__instance.source, target, powerInstance, amount));
                                P2PMessageSender_Bobloy.sendReduceHolyAction((AbstractMonster) target);
                            }
                        }
                    }
                }
            }
        }
    }
}
