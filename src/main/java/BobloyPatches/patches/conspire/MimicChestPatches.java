package BobloyPatches.patches.conspire;

import BobloyPatches.util.ModIDs;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import conspire.events.MimicChestEvent;
import spireTogether.network.P2P.P2PMessageSender;
import spireTogether.network.objects.rooms.NetworkRoom;
import spireTogether.other.RoomDataManager;

public class MimicChestPatches {
    @SpirePatch2(clz = MimicChestEvent.class, method="beginFight", requiredModId = ModIDs.conspire)
    public static class MimicChestBeginFightPatch {

        @SpirePostfixPatch
        public static void BeginFightPostfix(MimicChestEvent __instance){
            NetworkRoom genRoom = NetworkRoom.Generate(AbstractDungeon.getCurrRoom());
            RoomDataManager.SetRoomCache(genRoom);
            P2PMessageSender.Send_RoomDataChanged(genRoom);
        }
    }
}
