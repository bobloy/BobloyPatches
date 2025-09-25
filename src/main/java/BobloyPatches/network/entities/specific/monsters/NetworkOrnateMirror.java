package BobloyPatches.network.entities.specific.monsters;

import com.megacrit.cardcrawl.monsters.AbstractMonster;
import conspire.monsters.OrnateMirror;
import spireTogether.network.objects.entities.NetworkMonster;

import java.io.Serializable;

public class NetworkOrnateMirror extends NetworkMonster implements Serializable {
    protected NetworkOrnateMirror(OrnateMirror m) {
        super(m);
    }
}
