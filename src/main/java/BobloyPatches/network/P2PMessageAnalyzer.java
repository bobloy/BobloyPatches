package BobloyPatches.network;


import CardAugments.CardAugmentsMod;
import CardAugments.cardmods.AbstractAugment;
import spireTogether.subscribers.TiSNetworkMessageSubscriber;
import spireTogether.util.NetworkMessage;


import static BobloyPatches.network.P2PRequests.chimeraAugmentTradeRequest;

public class P2PMessageAnalyzer implements TiSNetworkMessageSubscriber {

    @Override
    public void onMessageReceive(NetworkMessage message, String messageRequest, Object messageObject, Integer senderId) {
        if(messageRequest.equals(chimeraAugmentTradeRequest)){
            Object[] dataIn = (Object[]) messageObject;

            String modID = (String) dataIn[1];
            AbstractAugment mod = CardAugmentsMod.modMap.get(modID);
        }
    }
}
