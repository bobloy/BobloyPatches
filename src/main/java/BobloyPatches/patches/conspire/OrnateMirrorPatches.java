package BobloyPatches.patches.conspire;

import BobloyPatches.util.ModIDs;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
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

//            CtConstructor customConstructor3 = CtNewConstructor.make(
//                    new CtClass[]{
//                            pool.getCtClass(AbstractCreature.class.getName()),
//                            CtClass.intType
//                    },
//                    new CtClass[0], // no exceptions
//                    "{ " +
//                            "this($1, " + OrnateMirrorPatches.class.getName() + ".calculateReflectAmount()); " +
//                            "this.amount = $2;" +
//                            "}", // $1 = owner, $2 = amount
//                    ctClass
//            );
//            ctClass.addConstructor(customConstructor3);

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
    public static class ReflectAttackPowerPatch {
        private static int originalAmount;

        @SpirePrefixPatch
        public static void Prefix(ReflectAttackPower __instance) {
            // Save the original amount before the method modifies it
            originalAmount = __instance.amount;
        }

        @SpirePostfixPatch
        public static int Postfix(int __result, ReflectAttackPower __instance, float ___fraction) {
            // Calculate the damage that was added (same calculation as in the original method)
            int damage = __instance.amount - originalAmount;

            if (damage > 0) {
                // Revert the amount to its original value
                __instance.amount = originalAmount;

                // Create and enqueue the ApplyPowerAction with the calculated damage
                ReflectAttackPower newPower = new ReflectAttackPower((AbstractMonster)__instance.owner, ___fraction);
                newPower.amount = damage;
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(
                        __instance.owner,
                        __instance.owner,
                        newPower
                ));
            }

            return __result;
        }
    }

    @SpirePatch2(clz = ReflectBlockPower.class, method = "onCreatureGainedBlock", requiredModId = ModIDs.conspire)
    public static class ReflectBlockPowerPatch {
        private static int originalAmount;

        @SpirePrefixPatch
        public static void Prefix(ReflectBlockPower __instance, AbstractCreature target, float blockAmt) {
            // Save the original amount before the method modifies it
            originalAmount = __instance.amount;
        }

        @SpirePostfixPatch
        public static void Postfix(ReflectBlockPower __instance, AbstractCreature target, float blockAmt, float ___fraction) {
            // Calculate the block that was added (same calculation as in the original method)
            int block = __instance.amount - originalAmount;

            if (block > 0) {
                // Revert the amount to its original value
                __instance.amount = originalAmount;

                // Create and enqueue the ApplyPowerAction with the calculated block
                ReflectBlockPower newPower = new ReflectBlockPower((AbstractMonster)__instance.owner, ___fraction);
                newPower.amount = block;
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(
                        __instance.owner,
                        __instance.owner,
                        newPower
                ));
            }
        }
    }



}
