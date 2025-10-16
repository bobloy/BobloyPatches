package BobloyPatches.patches.conspire;

import BobloyPatches.util.ModIDs;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.LouseDefensive;
import com.megacrit.cardcrawl.monsters.exordium.LouseNormal;
import com.megacrit.cardcrawl.random.Random;
import conspire.actions.SpawnLouseAction;
import conspire.monsters.HeadLouse;
import conspire.monsters.LouseWeak;
import conspire.powers.ReflectAttackPower;
import conspire.powers.ReflectBlockPower;
import conspire.powers.SheddingPower;
import dLib.modcompat.ModManager;
import downfall.monsters.NeowBoss;
import javassist.*;
import javassist.bytecode.DuplicateMemberException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import spireTogether.SpireTogetherMod;
import spireTogether.network.P2P.P2PManager;
import spireTogether.network.P2P.P2PMessageAnalyzer;
import spireTogether.network.P2P.P2PPlayer;
import spireTogether.network.P2P.P2PRequests;
import spireTogether.network.objects.entities.NetworkMonster;
import spireTogether.network.objects.rooms.NetworkLocation;
import spireTogether.patches.monsters.MonsterFieldPatches;
import spireTogether.util.NetworkMessage;
import spireTogether.util.SpireHelp;

import java.util.ArrayList;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.powers.MinionPower;
import com.megacrit.cardcrawl.powers.SlowPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.PhilosopherStone;

import static spireTogether.patches.SpawnedMonsterManager.monsterSpawnCount;

public class HeadLousePatches {

        //* If we're summoning a gremlin, predetermine the gremlin type in a way it is network-consistent
    @SpirePatch2(clz = HeadLouse.class, method = "makeLouse", requiredModId = ModIDs.conspire)
    public static class MakeLouseActionPatcher {
        public static Random miscRng;
        public static Random aiRng;

        public static void Prefix(int slot){
            if(SpireTogetherMod.isConnected){
                miscRng = AbstractDungeon.miscRng;
                aiRng = AbstractDungeon.aiRng;
                Long newMiscSeed = 0L;
                newMiscSeed += slot;
                Long newAiSeed = 0L;
                newAiSeed += slot;
                NetworkLocation l = SpireHelp.Gameplay.GetMapLocation(false);
                if(l != null){
                    newMiscSeed += l.x;
                    newMiscSeed += l.y;
                    newAiSeed += l.x;
                    newAiSeed += l.y;
                }
                newMiscSeed += monsterSpawnCount;
                newAiSeed += monsterSpawnCount;
                AbstractDungeon.miscRng = new Random(newMiscSeed);
                AbstractDungeon.aiRng = new Random(newAiSeed);
            }
        }

        public static ExprEditor Instrument () {
            return new ExprEditor() {
                int mur = 0;
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals("com.badlogic.gdx.math.MathUtils") && 
                        m.getMethodName().equals("random")) {
                        mur++;
                        if (mur == 1 || mur == 2) {
                            m.replace("{ $_ = com.megacrit.cardcrawl.dungeons.AbstractDungeon.miscRng.random($1, $2); }");
                        }
                    }
                }
            };
        }

        public static void Postfix(int slot){
            if(SpireTogetherMod.isConnected) {
                AbstractDungeon.miscRng = miscRng;
                AbstractDungeon.aiRng = aiRng;
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
        }
    }

    @SpirePatch2(clz = SheddingPower.class, method = SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
    public static class SheddingPowerPatch {
        @SpireRawPatch
        public static void addConstructor(CtBehavior ctMethodToPatch) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();


            CtConstructor customConstructor2 = CtNewConstructor.make(
                    new CtClass[]{
                            pool.getCtClass(AbstractMonster.class.getName()),
                            CtClass.intType
                    },
                    new CtClass[0], // no exceptions
                    "{ " +
                            "this($1); " +
                            "this.amount = $2;" +
                            "}", // $1 = owner, $2 = amount
                    ctClass
            );
            ctClass.addConstructor(customConstructor2);


//            // Maybe unnecessary if `applyPowers` calls it
//            CtClass superClass = ctClass.getSuperclass().getSuperclass(); // AbstractConspirePower -> AbstractPower
//            CtMethod superMethod = superClass.getDeclaredMethod("stackPower");
//            CtMethod updateMethod = CtNewMethod.delegator(superMethod, ctClass);
//            try {
//                ctClass.addMethod(updateMethod);
//            } catch (DuplicateMemberException ignored) {
//                updateMethod = ctClass.getDeclaredMethod("stackPower");
//            }
//            updateMethod.insertAfter("{this.updateDescription();};");
        }
    }

    @SpirePatch2(clz = SheddingPower.class, method = "onAttacked", requiredModId = ModIDs.conspire)
    public static class SheddingPowerAttackedPatch {
        private static int originalAmount;

        @SpirePrefixPatch
        public static void Prefix(SheddingPower __instance) {
            // Save the original amount before the method modifies it
            originalAmount = __instance.amount;
        }

        @SpirePostfixPatch
        public static int Postfix(int __result, SheddingPower __instance) {

            // Minions have spawned
            if (__instance.amount < originalAmount) {
                // Revert the amount to its original value
                __instance.amount = originalAmount;

                // Create and enqueue the ApplyPowerAction with the calculated damage
                AbstractDungeon.actionManager.addToTop(new ApplyPowerAction(
                        __instance.owner,
                        __instance.owner,
                        new SheddingPower((AbstractMonster)__instance.owner),
                        -1
                ));
            }

            return __result;
        }
    }

    @SpirePatch2(clz = P2PMessageAnalyzer.class, method = "AnalyzeMessage", requiredModId = ModIDs.conspire, optional = true)
    public static class SheddingPowerSpawned{
        @SpirePostfixPatch
        public static void AnalyzerPostfix(NetworkMessage data){
            P2PPlayer p = P2PManager.GetPlayer(data.senderID);
            if(p != null){
                if (data.request.equals(P2PRequests.monsterSpawned)) {
                    Object[] dataIn = (Object[]) data.object;
                    NetworkLocation l = (NetworkLocation) dataIn[0];
                    NetworkMonster m = (NetworkMonster) dataIn[1];

                    if(l.IsSameAsCurrentRoomAndAction()){
                        if(SpireHelp.Gameplay.AreMonstersPresent()){
                            ArrayList<AbstractMonster> roomMonsters = AbstractDungeon.getMonsters().monsters;
                            if(!roomMonsters.isEmpty() && roomMonsters.stream().anyMatch(rm -> rm instanceof HeadLouse)){
                                for(AbstractMonster roomMonster : roomMonsters){
                                    String monsterId = MonsterFieldPatches.MonsterFieldPatcher.uniqueID.get(roomMonster);
                                    if(monsterId != null && m.uniqueID != null && monsterId.equals(m.uniqueID)){
                                        return;
                                    }
                                }

                                AbstractMonster monster = m.ToStandard();
                                if(monster != null){
                                    // Mimic SpawnLouseAction first-frame behavior on remote side
                                    float sourceX = ((float)com.megacrit.cardcrawl.core.Settings.WIDTH * 0.75f + 200.f * com.megacrit.cardcrawl.core.Settings.scale) - monster.drawX;
                                    monster.animX = sourceX;
                                    monster.init();
                                    monster.applyPowers();
                                    AbstractDungeon.getCurrRoom().monsters.monsters.add(monster);
                                    // Daily mods and Minion flag
                                    if (ModHelper.isModEnabled(com.megacrit.cardcrawl.daily.mods.Lethality.ID)) {
                                        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(monster, monster, new StrengthPower(monster, 3), 3));
                                    }
                                    if (ModHelper.isModEnabled(com.megacrit.cardcrawl.daily.mods.TimeDilation.ID)) {
                                        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(monster, monster, new SlowPower(monster, 0)));
                                    }
                                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(monster, monster, new MinionPower(monster)));
                                    // Philosopher's Stone passive
                                    if (AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(PhilosopherStone.ID)) {
                                        monster.addPower(new StrengthPower(monster, 2));
                                    }
                                    // Finish like SpawnLouseAction end-frame
                                    monster.animX = 0.0f;
                                    monster.showHealthBar();
                                }
                            }
                        }
                    }
                }
            }
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
