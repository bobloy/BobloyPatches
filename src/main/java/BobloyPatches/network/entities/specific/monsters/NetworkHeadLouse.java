package BobloyPatches.network.entities.specific.monsters;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import conspire.monsters.HeadLouse;
import dLib.util.Reflection;
import spireTogether.network.objects.entities.NetworkMonster;
import spireTogether.patches.monsters.MonsterFieldPatches.MonsterFieldPatcher;

import java.io.Serializable;

import static conspire.monsters.HeadLouse.MAX_LICE;


public class NetworkHeadLouse extends NetworkMonster implements Serializable {
    static final long serialVersionUID = 1L;

    public String[] lice;

    public NetworkHeadLouse(HeadLouse l){
        super(l);
        lice = new String[MAX_LICE];

        AbstractMonster[] lice = Reflection.getFieldValue("lice", l);

        for(int i = 0; i < MAX_LICE; i++){
            if(lice[i] != null) this.lice[i] = MonsterFieldPatcher.uniqueID.get(lice[i]);
            else this.lice[i] = null;
        }
    }

    @Override
    public void postMonsterPrepare(AbstractMonster standard) {
        super.postMonsterPrepare(standard);
        HeadLouse gl = (HeadLouse) standard;
        AbstractMonster[] lice = Reflection.getFieldValue("lice", gl);

        for(int i = 0; i < MAX_LICE; i++){
            if(this.lice[i] != null){
                for(int j = 0; j < AbstractDungeon.getMonsters().monsters.size(); j++){
                    AbstractMonster m = AbstractDungeon.getMonsters().monsters.get(j);
                    if(this.lice[i].equals(MonsterFieldPatcher.uniqueID.get(m))){
                        lice[i] = m;
                    }
                }
            }
            else{
                lice[i] = null;
            }
        }
        Reflection.setFieldValue("lice", gl, lice);
    }
}