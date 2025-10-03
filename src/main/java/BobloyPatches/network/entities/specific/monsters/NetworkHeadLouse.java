package BobloyPatches.network.entities.specific.monsters;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.LouseDefensive;
import com.megacrit.cardcrawl.monsters.exordium.LouseNormal;
import conspire.monsters.HeadLouse;
import conspire.monsters.LouseWeak;
import dLib.util.Reflection;
import spireTogether.network.objects.entities.NetworkMonster;
import spireTogether.patches.monsters.MonsterFieldPatches.MonsterFieldPatcher;

import java.io.Serializable;

import static conspire.monsters.HeadLouse.MAX_LICE;


public class NetworkHeadLouse extends NetworkMonster implements Serializable {
    static final long serialVersionUID = 1L;

//    public String[] lice;

    public NetworkHeadLouse(HeadLouse l){
        super(l);
//        lice = new String[MAX_LICE];
//
//        AbstractMonster[] lice = ReflectionHacks.getPrivate(l, HeadLouse.class, "lice");
//
//        for(int i = 0; i < MAX_LICE; i++){
//            if(lice[i] != null) this.lice[i] = MonsterFieldPatcher.uniqueID.get(lice[i]);
//            else this.lice[i] = null;
//        }
    }

    @Override
    public void postMonsterPrepare(AbstractMonster standard) {
        super.postMonsterPrepare(standard);
        HeadLouse gl = (HeadLouse) standard;

        AbstractMonster[] lice = ReflectionHacks.getPrivate(gl, HeadLouse.class, "lice");
        int i = 0;

//        for(int i = 0; i < MAX_LICE; i++){
//            if(this.lice[i] != null){
                for(int j = 0; j < AbstractDungeon.getMonsters().monsters.size(); j++){
                    AbstractMonster m = AbstractDungeon.getMonsters().monsters.get(j);
//                    if(this.lice[i].equals(MonsterFieldPatcher.uniqueID.get(m))){
                    if (m instanceof LouseNormal || m instanceof LouseWeak || m instanceof LouseDefensive) {
                        lice[i] = m;
                        i++;
                    }
//                    }
                }
//            }
//            else{
//                lice[i] = null;
//            }
//        }
        ReflectionHacks.setPrivate(gl, HeadLouse.class, "lice", lice);
    }
}