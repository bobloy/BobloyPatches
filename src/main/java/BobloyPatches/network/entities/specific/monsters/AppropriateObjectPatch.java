package BobloyPatches.network.entities.specific.monsters;

import BobloyPatches.util.ModIDs;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import conspire.monsters.HeadLouse;
import conspire.monsters.OrnateMirror;
import spireTogether.network.objects.entities.NetworkMonster;

public class AppropriateObjectPatch {
    @SpirePatch2(clz = NetworkMonster.class, method = "GetAppropriateObject", requiredModId = ModIDs.spireTogether)
    public static class GetAppropriateObjectPatch {
        @SpirePostfixPatch
        public static NetworkMonster GetAppropriateObjectPatchPostfix(AbstractMonster m, NetworkMonster __result) {
            if (m instanceof HeadLouse) {
                return new NetworkHeadLouse((HeadLouse) m);
            }
            if (m instanceof OrnateMirror){
                return new NetworkOrnateMirror((OrnateMirror) m);
            }
            return __result;
        }
    }
}
