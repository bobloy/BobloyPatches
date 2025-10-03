package BobloyPatches.patches.conspire;

import BobloyPatches.util.ModIDs;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import conspire.events.MimicChestEvent;
import spireTogether.network.P2P.P2PMessageSender;
import spireTogether.network.objects.rooms.NetworkRoom;
import spireTogether.other.RoomDataManager;

import static spireTogether.patches.network.RoomEntryPatch.AllowRoomGeneration;

public class MimicChestPatches {
    @SpirePatch2(clz = MimicChestEvent.class, method= SpirePatch.CONSTRUCTOR, requiredModId = ModIDs.conspire)
    public static class MimicChestConstructorPatch {
        @SpirePostfixPatch
        public static void ConstructorPostfix(MimicChestEvent __instance){
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.EVENT; // Undo the damage
            // TODO: Do something else to allow skipping the fight
        }

    }

    @SpirePatch2(clz = MimicChestEvent.class, method="beginFight", requiredModId = ModIDs.conspire)
    public static class MimicChestBeginFightPatch {

        @SpirePostfixPatch
        public static void BeginFightPostfix(MimicChestEvent __instance){
//            AllowRoomGeneration();
//            NetworkRoom genRoom = NetworkRoom.Generate(AbstractDungeon.getCurrRoom());
//            RoomDataManager.SetRoomCache(genRoom);
//            P2PMessageSender.Send_RoomDataChanged(genRoom);
        }
    }

}
