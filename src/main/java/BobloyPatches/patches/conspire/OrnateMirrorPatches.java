package BobloyPatches.patches.conspire;

import BobloyPatches.util.ModIDs;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import conspire.helpers.AscensionHelper;
import conspire.powers.ReflectAttackPower;
import conspire.powers.ReflectBlockPower;
import javassist.*;
import javassist.bytecode.DuplicateMemberException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

public class OrnateMirrorPatches {
//    @SpirePatch2(clz = ReflectAttackPower.class, method = SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
//    @SpirePatch2(clz = ReflectBlockPower.class, method = SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
//    public static class saveAmountPatch {
//        @SpirePostfixPatch
//        public static void saveAmountPatchPostfix(AbstractPower __instance, AbstractMonster owner, float fraction) {
//            __instance.amount = (int) (fraction * 100);
//        }
//    }

    public static float calculateReflectAmount() {
        try {
            return AscensionHelper.harder(AbstractMonster.EnemyType.BOSS) ? 0.5f : 0.4f;
        } catch (Exception e) {
            return 0.4f; // Default fallback
        }
    }

    @SpirePatch2(clz = ReflectBlockPower.class, method = SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
    @SpirePatch2(clz = ReflectAttackPower.class, method = SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
    public static class ReflectAttackBlockPowerPatch {
        @SpireRawPatch
        public static void addConstructor(CtBehavior ctMethodToPatch) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();

//            CtConstructor customConstructor = CtNewConstructor.make(
//                    new CtClass[]{
//                            pool.getCtClass(AbstractMonster.class.getName())
//                    },
//                    new CtClass[0],
//                    "{ this($1, " + OrnateMirrorPatches.class.getName() + ".calculateReflectAmount()); }",
//                    ctClass
//            );
//
//            ctClass.addConstructor(customConstructor);

            CtConstructor customConstructor2 = CtNewConstructor.make(
                    new CtClass[]{
                            pool.getCtClass(AbstractMonster.class.getName()),
                            CtClass.intType
                    },
                    new CtClass[0], // no exceptions
                    "{ " +
                            "this($1, " + OrnateMirrorPatches.class.getName() + ".calculateReflectAmount()); " +
                            "this.amount = $2;" +
                            "}", // $1 = owner, $2 = amount
                    ctClass
            );
            ctClass.addConstructor(customConstructor2);

            CtConstructor customConstructor3 = CtNewConstructor.make(
                    new CtClass[]{
                            pool.getCtClass(AbstractCreature.class.getName()),
                            CtClass.intType
                    },
                    new CtClass[0], // no exceptions
                    "{ " +
                            "this($1, " + OrnateMirrorPatches.class.getName() + ".calculateReflectAmount()); " +
                            "this.amount = $2;" +
                            "}", // $1 = owner, $2 = amount
                    ctClass
            );
            ctClass.addConstructor(customConstructor3);

            // Maybe unnecessary if `applyPowers` calls it
            CtClass superClass = ctClass.getSuperclass().getSuperclass(); // AbstractConspirePower -> AbstractPower
            CtMethod superMethod = superClass.getDeclaredMethod("stackPower");
            CtMethod updateMethod = CtNewMethod.delegator(superMethod, ctClass);
            try {
                ctClass.addMethod(updateMethod);
            } catch (DuplicateMemberException ignored) {
                updateMethod = ctClass.getDeclaredMethod("stackPower");
            }
            updateMethod.insertAfter("{this.updateDescription();};");
        }
    }

    @SpirePatch2(clz = ReflectAttackPower.class, method = "onAttacked", requiredModId = ModIDs.conspire)
    @SpirePatch2(clz = ReflectBlockPower.class, method = "onCreatureGainedBlock", requiredModId = ModIDs.conspire)
    public static class ReflectAttackUseApplyPowerPatch {
        @SpireInstrumentPatch
        public static ExprEditor Instrument () {
            return new ExprEditor() {
                @Override
                public void edit(FieldAccess f) throws CannotCompileException {
                    if (f.getFieldName().equals("amount")){
                        String className = f.getClassName();
                        String aps = ReflectAttackPower.class.getName();
                        String bps = ReflectBlockPower.class.getName();
                        String apa = ApplyPowerAction.class.getName();
                        if (className.equals(aps)) {
                            f.replace("{this.addToBot(" +
                                    "new " + apa + "($0.owner, $0.owner, new " + aps + "($0.owner, ((Integer)$_).intValue()), ((Integer)$_).intValue()));}");
                        } else if (className.equals(bps)) {
                            f.replace("{this.addToBot(" +
                                    "new " + apa + "($0.owner, $0.owner, new " + bps + "($0.owner, ((Integer)$_).intValue()), ((Integer)$_).intValue()));}");
                        }
                    }
                }
            };
        }
    }



}
