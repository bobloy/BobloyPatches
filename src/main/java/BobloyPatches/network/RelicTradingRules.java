package BobloyPatches.network;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import spireTogether.subscribers.TiSTradingRulesSubscriber;

public class RelicTradingRules implements TiSTradingRulesSubscriber {
    @Override
    public boolean canTradeCard(AbstractCard abstractCard) {
        return true;
    }

    @Override
    public boolean canTradeRelic(AbstractRelic abstractRelic) {
        if(abstractRelic.relicId.equals("aspiration:Nostalgia")) return false;
        if(abstractRelic.relicId.startsWith("aspiration:") && abstractRelic.relicId.endsWith("Skillbook")) return false;
        if(abstractRelic.relicId.equals("EVO:Axolotl")) return false;

        return true;
    }

    @Override
    public boolean canTradePotion(AbstractPotion abstractPotion) {
        return true;
    }

    @Override
    public boolean canTradeGold(int i) {
        return true;
    }
}
