package BobloyPatches;

import basemod.BaseMod;
import basemod.interfaces.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import pansTrinkets.DefaultMod;
import pansTrinkets.helpers.TrinketHelper;

@SuppressWarnings({"unused", "WeakerAccess"})
@SpireInitializer
public class BobloyPatches implements PostInitializeSubscriber, PostCreateStartingDeckSubscriber {

    public static final String modID = "bobloypatches";

    public static String makeID(String idText) {
        return modID + ":" + idText;
    }

    public BobloyPatches() {
        BaseMod.subscribe(this);
    }

    public static String makePath(String resourcePath) {
        return modID + "Resources/" + resourcePath;
    }

    public static String makeImagePath(String resourcePath) {
        return modID + "Resources/images/" + resourcePath;
    }

    public static void initialize() {
        BobloyPatches thismod = new BobloyPatches();
    }

    @Override
    public void receivePostInitialize() {

    }

    @Override
    public void receivePostCreateStartingDeck(AbstractPlayer.PlayerClass pc, CardGroup cg){
        if (DefaultMod.enableProgressiveMaxWeight) {
            System.out.println("BobloyPatchesreceivePostCreateStartingDeck | Initial Max Weight set!");
            TrinketHelper.changeMaxWeight(0);
        }
    }
}

