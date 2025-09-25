package BobloyPatches.patches;

import BobloyPatches.util.ModIDs;
import basemod.BaseMod;
import basemod.ModLabeledButton;
import basemod.ModPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import evolution.Axolotl;
import evolution.Evolution;
import javassist.*;
import javassist.bytecode.DuplicateMemberException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

import static java.lang.Boolean.TRUE;

public class EvolutionPatches {
    private static TreeMap<String, int[]> evolution = new TreeMap<>();

    private static void resetEvolution() {
        TreeMap<String, int[]> blankEvolution = new TreeMap<>();
        String sEvolution = (new Gson()).toJson(blankEvolution);
        Gdx.files.local("preferences/evolution").writeString(sEvolution, false, String.valueOf(StandardCharsets.UTF_8));
    }

    @SpirePatch2(clz = BaseMod.class, method="registerModBadge", requiredModId = ModIDs.evolution)
    public static class EvolutionRegisterModBadgePatch {
        @SpirePrefixPatch
        public static void prefixPatch(Texture t, String name, String author, String desc, ModPanel settingsPanel) {
            if(name.equals("Evolution")){
                ModPanel configPanel = new ModPanel();
                ModLabeledButton resetProgress = new ModLabeledButton("Reset ALL Evolution Progress",400.0F, 400.0F,
                        Settings.CREAM_COLOR, Color.WHITE, FontHelper.charDescFont, configPanel,(button) -> {
                    resetEvolution();
                });

                settingsPanel.addUIElement(resetProgress);
            }
        }
    }

    // Sets sell price for Axolotls to -1 so it can't be sold with shopmod
    @SpirePatch2(clz = Axolotl.class, method = SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.evolution)
    public static class AddPriceOverride {
        @SpireRawPatch
        public static void addMethod(CtBehavior ctMethodToPatch) throws NotFoundException, CannotCompileException {
            CtClass ctNestClass = ctMethodToPatch.getDeclaringClass();
            CtClass superClass = ctNestClass.getSuperclass().getSuperclass(); // CustomRelic -> AbstractRelic
            CtMethod superMethod = superClass.getDeclaredMethod("getPrice");
            CtMethod updateMethod = CtNewMethod.delegator(superMethod, ctNestClass);
            try{
                ctNestClass.addMethod(updateMethod);
            } catch (DuplicateMemberException ignored) {
                updateMethod = ctNestClass.getDeclaredMethod("getPrice");
            }
            updateMethod.insertAfter("{return -1;};");
        }
    }
}
