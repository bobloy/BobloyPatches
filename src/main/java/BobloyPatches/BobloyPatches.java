package BobloyPatches;

import BobloyPatches.network.P2PMessageAnalyzer_Bobloy;
import BobloyPatches.network.RelicTradingRules;
import basemod.BaseMod;
import basemod.interfaces.*;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pansTrinkets.DefaultMod;
import pansTrinkets.helpers.TrinketHelper;
import spireTogether.SpireTogetherMod;
import spireTogether.modcompat.downfall.subscribers.DownfallTradingRules;
import spireTogether.subscribers.TiSSubscribers;

@SuppressWarnings({"unused", "WeakerAccess"})
@SpireInitializer
public class BobloyPatches implements PostInitializeSubscriber, PostCreateStartingDeckSubscriber {
    public static final Logger logger = LogManager.getLogger(BobloyPatches.class.getName());

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
        BobloyPatches bobloyPatches = new BobloyPatches();
    }

    @Override
    public void receivePostInitialize() {
        TiSSubscribers.subscribe(new P2PMessageAnalyzer_Bobloy());
        SpireTogetherMod.subscribe(new RelicTradingRules());
        logger.info("========================= BobloyPatches Initialized. =========================");
    }

    @Override
    public void receivePostCreateStartingDeck(AbstractPlayer.PlayerClass pc, CardGroup cg){
        if (Loader.isModLoaded("PansTrinkets") && DefaultMod.enableProgressiveMaxWeight) {
            System.out.println("BobloyPatchesreceivePostCreateStartingDeck | Initial Max Weight set!");
            TrinketHelper.changeMaxWeight(0);
        }
    }

}

