package BobloyPatches.network;


import com.megacrit.cardcrawl.monsters.AbstractMonster;
import conspire.monsters.HollyBat;
import conspire.powers.HolyPower;
import spireTogether.network.P2P.P2PManager;
import spireTogether.network.P2P.P2PPlayer;
import spireTogether.network.objects.rooms.NetworkRoom;
import spireTogether.other.RoomDataManager;
import spireTogether.patches.monsters.MonsterFieldPatches;
import spireTogether.subscribers.TiSNetworkMessageSubscriber;
import spireTogether.util.NetworkMessage;

public class P2PMessageAnalyzer_Bobloy implements TiSNetworkMessageSubscriber {

    @Override
    public void onMessageReceive(NetworkMessage message, String messageRequest, Object messageObject, Integer senderID) {
//        if (messageRequest.equals(P2PRequests_Bobloy.chimeraAugmentTradeRequest) && TradingScreen.tradingScreen != null) {
////            Object[] dataIn = (Object[]) messageObject;
//
//            HashMap<Integer, ArrayList<String>> cardIdentifierMap = (HashMap<Integer, ArrayList<String>>) messageObject;
//
//
//            for (Integer cardPos : cardIdentifierMap.keySet()) {
//                for (String modID : cardIdentifierMap.get(cardPos)) {
//                    if (CardAugmentsMod.modMap.containsKey(modID)) {
//                        AbstractAugment a = CardAugmentsMod.modMap.get(modID);
//                        if (a != null) {
//                            addModifierButDontRunInitial(TradingScreen.tradingScreen.teammateCards.get(cardPos), a);
//                        }
//                    }
//                }
//
//            }
//
//        }

        if (messageRequest.equals(P2PRequests_Bobloy.aspirationReduceHolyAction)){
//            Object[] objectArr = (Object[]) messageObject;
            String creatureId = (String) messageObject;

            P2PPlayer sender = P2PManager.GetPlayer(senderID);

//            Integer amount = (Integer) objectArr[1];
//            AbstractCreature creature = SpireHelp.Gameplay.UIDToCreature(creatureId);

            NetworkRoom room = RoomDataManager.GetRoomCache(sender.location);
            if(room != null) {
                AbstractMonster m = MonsterFieldPatches.GetMonsterByID(creatureId);
                if (m instanceof HollyBat) { // Implies you're in the same room I think
                    int amount = m.hasPower(HolyPower.POWER_ID) ? m.getPower(HolyPower.POWER_ID).amount : 0;
                    ((HollyBat) m).onDecreaseHoly(amount);
                }
            }
        }
    }
}
