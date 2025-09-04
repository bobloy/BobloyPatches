package BobloyPatches.patches;

import com.blanktheevil.blockreminder.BlockPreview;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import spireTogether.network.P2P.P2PPlayer;


//@SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)  // "<ctor>" ?
//public final class CryogeneticsPatch {
//    public static void Raw(final CtBehavior ctBehavior) {
//        System.out.println("\nCryogeneticsPatch | Start Patch");
//
//        // Guard Together-in-Spire multiplayer addBlock during preview
//        try {
//            ClassPool pool = ctBehavior.getDeclaringClass().getClassPool();
//            CtClass p2pClass = pool.get("spireTogether.network.P2P.P2PPlayer");
//            CtMethod addBlock = p2pClass.getDeclaredMethod("addBlock");
//            System.out.println("\t|\t- Guarding P2PPlayer.addBlock during preview");
//            addBlock.insertBefore("{if (" + BlockPreview.class.getName() + ".isPreview) { return; }}");
//            p2pClass.toClass();
//            System.out.println("\t|\tSuccess...\n\t|");
//        } catch (javassist.NotFoundException ignore) {
//            System.out.println("\t|\t- P2PPlayer.addBlock not found (multiplayer mod may be absent).");
//        } catch (javassist.CannotCompileException e) {
//            System.out.println("\t|\t- P2PPlayer.addBlock failed to patch.");
//        }
//
//    }
//}

@SpirePatch(clz = P2PPlayer.class, method = "addBlock", requiredModId = "spireTogether")
public class CryogeneticsPatch{
    public static SpireReturn<Void> Prefix(P2PPlayer __instance, int blockAmount){
        if (!Loader.isModLoaded("block-reminder")){
            return SpireReturn.Continue();
        }
        if(BlockPreview.isPreview){
            return SpireReturn.Return();
        }
        return SpireReturn.Continue();
    };
}

