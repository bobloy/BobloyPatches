package BobloyPatches.patches.conspire;

import BobloyPatches.util.ModIDs;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.LouseDefensive;
import com.megacrit.cardcrawl.monsters.exordium.LouseNormal;
import com.megacrit.cardcrawl.random.Random;
import conspire.monsters.HeadLouse;
import conspire.monsters.LouseWeak;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import spireTogether.SpireTogetherMod;
import spireTogether.network.objects.rooms.NetworkLocation;
import spireTogether.util.SpireHelp;

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
                    if (m.getMethodName().equals("MathUtils.random")) {
                        if (mur++ == 0 || mur == 1) {
                            m.replace("{com.megacrit.cardcrawl.dungeons.AbstractDungeon.miscRng.random($0, $1);}");
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
