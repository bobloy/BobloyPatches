package BobloyPatches.network;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import spireTogether.subscribers.TiSTradingRulesSubscriber;

public class RelicTradingRules implements TiSTradingRulesSubscriber {
    @Override
    public boolean canTradeCard(AbstractCard abstractCard) {
        return false;
    }

    @Override
    public boolean canTradeRelic(AbstractRelic abstractRelic) {
        if(abstractRelic.relicId.equals("aspiration:Nostalgia")) return false;

        return false;
    }

    @Override
    public boolean canTradePotion(AbstractPotion abstractPotion) {
        return false;
    }

    @Override
    public boolean canTradeGold(int i) {
        return false;
    }
}
