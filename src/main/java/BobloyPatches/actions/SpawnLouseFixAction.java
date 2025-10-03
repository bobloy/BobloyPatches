package BobloyPatches.actions;

import com.megacrit.cardcrawl.monsters.AbstractMonster;
import conspire.actions.SpawnLouseAction;

public class SpawnLouseFixAction extends SpawnLouseAction {
    private AbstractMonster[] lice;
    private int slot;
    private AbstractMonster m;

    public SpawnLouseFixAction(AbstractMonster m, boolean curlUp, int slot, AbstractMonster[] slots) {
        super(m, curlUp, slot);
        this.m = m;
        this.slot = slot;
        lice = slots;
    }

    @Override
    public void update(){
        super.update();
        lice[slot] = m;
    }
}
