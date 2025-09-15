package BobloyPatches.network;


import CardAugments.CardAugmentsMod;
import CardAugments.cardmods.AbstractAugment;
import BobloyPatches.patches.ConspirePatches;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import conspire.actions.ReduceHolyAction;
import spireTogether.screens.trading.TradingScreen;
import spireTogether.subscribers.TiSNetworkMessageSubscriber;
import spireTogether.util.NetworkMessage;
import spireTogether.util.SpireHelp;

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

        if (messageRequest.equals(P2PRequests_Bobloy.aspirationReduceHolyAction)){
            Object[] objectArr = (Object[]) messageObject;

            String creatureId = (String) objectArr[0];
            Integer amount = (Integer) objectArr[1];

            AbstractCreature creature = SpireHelp.Gameplay.UIDToCreature(creatureId);
            if(creature != null){ // Implies you're in the same room I think
                try {
                    ConspirePatches.suppressReduceHolyNetwork = true;
                    AbstractDungeon.actionManager.addToBottom(new ReduceHolyAction(creature, amount));
                } finally {
                    ConspirePatches.suppressReduceHolyNetwork = false;
                }
            }
        }
    }
}
