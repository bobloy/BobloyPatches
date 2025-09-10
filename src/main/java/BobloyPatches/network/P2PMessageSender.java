package BobloyPatches.network;

import CardAugments.cardmods.AbstractAugment;
import spireTogether.network.P2P.P2PManager;
import spireTogether.network.objects.items.NetworkCard;

import java.util.ArrayList;

import static BobloyPatches.network.P2PRequests.chimeraAugmentTradeRequest;

public class P2PMessageSender {
    public static void cardAugmentTradeRequest(NetworkCard card, ArrayList<AbstractAugment> cardModifiers){
        if (cardModifiers.isEmpty()) return;

        for (AbstractAugment a : cardModifiers) {
            P2PManager.SendData(chimeraAugmentTradeRequest, card.uniqueID, a);
        }
    }
}
