package BobloyPatches.patches;

import BobloyPatches.util.ModIDs;
import com.blanktheevil.blockreminder.BlockPreview;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import spireTogether.network.P2P.P2PPlayer;

@SpirePatch(clz = P2PPlayer.class, method = "addBlock", requiredModId = ModIDs.spireTogether)
public class BlockReminderPatch {
    public static SpireReturn<Void> Prefix(P2PPlayer __instance, int blockAmount){
        if (!Loader.isModLoaded(ModIDs.blockReminder)){
            return SpireReturn.Continue();
        }
        if(BlockPreview.isPreview){
            return SpireReturn.Return();
        }
        return SpireReturn.Continue();
    }
}

