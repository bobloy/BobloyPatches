package BobloyPatches.network;


import CardAugments.CardAugmentsMod;
import CardAugments.cardmods.AbstractAugment;
import spireTogether.screens.trading.TradingScreen;
import spireTogether.subscribers.TiSNetworkMessageSubscriber;
import spireTogether.util.NetworkMessage;

import java.util.ArrayList;
import java.util.HashMap;

import static BobloyPatches.patches.ChimeraPatches.addModifierButDontRunInitial;

public class P2PMessageAnalyzer_Bobloy implements TiSNetworkMessageSubscriber {

    @Override
    public void onMessageReceive(NetworkMessage message, String messageRequest, Object messageObject, Integer senderId) {
        if (messageRequest.equals(P2PRequests_Bobloy.chimeraAugmentTradeRequest) && TradingScreen.tradingScreen != null) {
//            Object[] dataIn = (Object[]) messageObject;

            HashMap<Integer, ArrayList<String>> cardIdentifierMap = (HashMap<Integer, ArrayList<String>>) messageObject;


            for (Integer cardPos : cardIdentifierMap.keySet()) {
                for (String modID : cardIdentifierMap.get(cardPos)) {
                    if (CardAugmentsMod.modMap.containsKey(modID)) {
                        AbstractAugment a = CardAugmentsMod.modMap.get(modID);
                        if (a != null) {
                            addModifierButDontRunInitial(TradingScreen.tradingScreen.teammateCards.get(cardPos), a);
                        }
                    }
                }

            }

        }
    }
}
