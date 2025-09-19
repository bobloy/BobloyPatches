package BobloyPatches.patches;

import BobloyPatches.network.P2PMessageSender_Bobloy;
import BobloyPatches.network.entities.specific.monsters.NetworkHeadLouse;
import BobloyPatches.util.ModIDs;
import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.ChangeStateAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.unique.SummonGremlinAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.monsters.exordium.LouseDefensive;
import com.megacrit.cardcrawl.monsters.exordium.LouseNormal;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.random.Random;
import conspire.Conspire;
import conspire.actions.ReduceHolyAction;
import conspire.cards.colorless.InfernalBerry;
import conspire.monsters.HeadLouse;
import conspire.monsters.HollyBat;
import conspire.monsters.LouseWeak;
import conspire.powers.HolyPower;
import dLib.util.Reflection;
import spireTogether.SpireTogetherMod;
import spireTogether.network.P2P.P2PManager;
import spireTogether.network.P2P.P2PPlayer;
import spireTogether.network.objects.entities.NetworkMonster;
import spireTogether.network.objects.entities.NetworkPower;
import spireTogether.network.objects.items.NetworkCard;
import spireTogether.network.objects.rooms.NetworkLocation;
import spireTogether.util.SpireHelp;

import static spireTogether.network.P2P.P2PMessageSender.Send_MonsterPowerDecreased;
import static spireTogether.patches.SpawnedMonsterManager.monsterSpawnCount;
import static spireTogether.patches.network.CreatureSyncPatches.*;
import static spireTogether.util.SpireHelp.Multiplayer.Players.GetPlayers;

public class ConspirePatches {

//    @SpirePatch2(clz = HollyBat.class, method=SpirePatch.CLASS, requiredModId = ModIDs.conspire)
//    public static class OriginalHolyAmt{
//        public static SpireField<Integer> origHolyAmt = new SpireField<>(() -> 0);
//    }

    public static AbstractMonster getLouse(int slot) {
        float x, y;
        switch (slot) {
            case 0:
                x = -65.f;
                y = -35.f;
                break;
            case 2:
                x = -370.f;
                y = -30.f;
                break;
            case 4:
                x = -670.f;
                y = -35.f;
                break;
            case 1:
                x = -230.f;
                y = 53.f;
                break;
            case 3:
                x = -535.f;
                y = 54.f;
                break;
            default:
                x = -330.f;
                y = 0;
                break;
        }
        x += MathUtils.random(-5.0f, 5.0f);
        y += MathUtils.random(-5.0f, 5.0f);

        switch (AbstractDungeon.miscRng.random(2)) {
            case 0:
                return new LouseNormal(x, y);
            case 1:
                return new LouseWeak(x, y);
            default:
                return new LouseDefensive(x, y); // max 1 defensive louse, to prevent huge weak stacks
        }
    }

    @SpirePatch2(clz = HollyBat.class, method = SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
    public static class HollyBatPatch {
        @SpirePostfixPatch
        public static void HollyBatPatchPrefix(HollyBat __instance, @ByRef int[] ___holyAmt) {
            ___holyAmt[0] += ___holyAmt[0] * P2PManager.GetPlayerCountWithoutSelf();
        }

//        @SpirePostfixPatch
//        public static void HollyBatPatchPostfix(HollyBat __instance, int ___holyAmt) {
//            ___holyAmt = OriginalHolyAmt.origHolyAmt.get(__instance);
//        }
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

    @SpirePatch2(clz = NetworkMonster.class, method = "GetAppropriateObject", requiredModId = ModIDs.spireTogether)
    public static class GetAppropriateObjectPatch {
        @SpirePostfixPatch
        public static NetworkMonster GetAppropriateObjectPatchPostfix(AbstractMonster m, NetworkMonster __result) {
            if (m instanceof HeadLouse) {
                return new NetworkHeadLouse((HeadLouse) m);
            }
            return __result;
        }
    }

    //* If we're summoning a gremlin, predetermine the gremlin type in a way it is network-consistent
    @SpirePatch2(clz = HeadLouse.class, method = "makeLouse")
    public static class GetRandomGremlinActionPatcher{
        public static Random miscRng;

        public static void Prefix(int slot){
            if(SpireTogetherMod.isConnected){
                miscRng = AbstractDungeon.miscRng;
                Long newSeed = 0L;
                newSeed += slot;
                NetworkLocation l = SpireHelp.Gameplay.GetMapLocation(false);
                if(l != null){
                    newSeed += l.x;
                    newSeed += l.y;
                }
                newSeed += monsterSpawnCount;
                AbstractDungeon.miscRng = new Random(newSeed);
            }
        }

        public static void Postfix(int slot){
            if(SpireTogetherMod.isConnected) {
                AbstractDungeon.miscRng = miscRng;
            }
        }
    }

    @SpirePatch2(clz = HeadLouse.class, method = "usePreBattleAction", requiredModId = ModIDs.conspire)
    public static class HeadLouseUsePreBattleActionPatch {
        @SpireInsertPatch(rloc = 3, localvars = {"toSummon"})
        public static void HeadLouseUsePreBattleActionPatchInsert(HeadLouse __instance, @ByRef int[] toSummon) {
//            toSummon[0] = 0;
            for (AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters) {
                if (m instanceof LouseNormal || m instanceof LouseWeak || m instanceof LouseDefensive) {
                    --toSummon[0];
                }
            }
//            AbstractMonster[] lice = Reflection.getFieldValue("lice", __instance);
//            lice[0] = AbstractDungeon.getMonsters().monsters.get(0);
//            lice[1] = AbstractDungeon.getMonsters().monsters.get(1);
//            lice[2] = AbstractDungeon.getMonsters().monsters.get(2);
//            Reflection.setFieldValue("lice", __instance, lice);
        }
    }

//    @SpirePatch2(clz = Conspire.class, method = "receiveEditMonsters")
//    public static class ReceiveEditMonstersPatch {
//        @SpirePostfixPatch
//        public static void EditMonstersPostfix() {
//            HeadLouse m = new HeadLouse();
//            BaseMod.addMonster(HeadLouse.ENCOUNTER_NAME, HeadLouse.ENCOUNTER_NAME, () -> new MonsterGroup(
//                    new AbstractMonster[]{
//                            ReflectionHacks.privateMethod(HeadLouse.class, "makeLouse").invoke(m, 0),
//                            ReflectionHacks.privateMethod(HeadLouse.class, "makeLouse").invoke(m, 1),
//                            ReflectionHacks.privateMethod(HeadLouse.class, "makeLouse").invoke(m, 2),
//                            m
//                    }));
//        }
//    }

//    @SpirePatch2(clz = HeadLouse.class, method=SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
//    public static class HeadLouseConstructorPatch {
//        @SpireRawPatch
//        public static void rawpatch(CtBehavior ctMethodToPatch) throws CannotCompileException, NotFoundException {
//            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
//            CtField ctField = ctClass.getField("lice");
//            ctField.setModifiers(Modifier.PUBLIC);
//        }
//    }

}
