package BobloyPatches.network;

import BobloyPatches.util.ModIDs;
import CardAugments.cardmods.AbstractAugment;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import spireTogether.network.P2P.P2PManager;
import spireTogether.patches.monsters.MonsterFieldPatches;
import spireTogether.util.NetworkMessage;

import java.util.ArrayList;
import java.util.HashMap;


public class P2PMessageSender_Bobloy {
    public static void sendChimeraTrade(ArrayList<AbstractCard> cards, Integer playerID) {
        if (cards.isEmpty() || !Loader.isModLoaded(ModIDs.cardAugments)) return;

        HashMap<Integer, ArrayList<String>> cardIdentifierMap = new HashMap<>();


        ArrayList<String> modIdentifiers;

        for (int i = 0; i < cards.size(); i++) {
            AbstractCard c = cards.get(i);
            modIdentifiers = new ArrayList<>();
            for (AbstractCardModifier m : CardModifierManager.modifiers(c)) {
                if (m instanceof AbstractAugment) {
                    modIdentifiers.add(m.identifier(c));
                    //                        __result.cardModifiers.add(m.identifier(c));
                }
            }
            cardIdentifierMap.put(i, modIdentifiers);
        }


        P2PManager.SendData(new NetworkMessage(P2PRequests_Bobloy.chimeraAugmentTradeRequest, cardIdentifierMap), playerID);

    }

    public static void sendReduceHolyAction(AbstractMonster target){
        String id = MonsterFieldPatches.GetMonsterID(target);

        P2PManager.SendData(P2PRequests_Bobloy.aspirationReduceHolyAction, id);
    }
}
