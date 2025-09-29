package BobloyPatches.patches;

import BobloyPatches.util.DownfallMapPath;
import BobloyPatches.util.ModIDs;
import com.derekjass.sts.weightedpaths.WeightedPaths;
import com.derekjass.sts.weightedpaths.paths.MapPath;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import downfall.patches.EvilModeCharacterSelect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

// Patch the external mod class: com.derekjass.sts.weightedpaths.paths.MapPath.addRoomToPath(MapEdge)
public class WeightedPathsPatches {
    private static final Logger logger = LogManager.getLogger(WeightedPathsPatches.class.getName());

    @SpirePatch2(clz = WeightedPaths.class, method = "regeneratePaths", requiredModId = ModIDs.weightedPaths)
    public static class RegeneratePathsDownfallPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> prefix(List<MapPath> ___paths) {
            if (Loader.isModLoaded(ModIDs.downfall) && EvilModeCharacterSelect.evilMode) {
                ___paths = DownfallMapPath.generateDownAll();
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch2(clz = MapPath.class, method = "addRoomToPath", requiredModId = ModIDs.weightedPaths)
    public static class SafeAddRoomToPath {
        @SpirePrefixPatch
        public static SpireReturn<Void> prefix(MapPath __instance, MapEdge edge) {
            // Only intervene for alternate map mods (e.g., Downfall)
            if (!isAlternateMapActive()) {
                return SpireReturn.Continue();
            }
            try {
                ArrayList<ArrayList<MapRoomNode>> map = CardCrawlGame.dungeon.getMap();
                if (map == null) {
                    debug("Skipping edge: map is null");
                    return SpireReturn.Return();
                }

                int y = edge.dstY;
                int x = edge.dstX;

                if (y < 0 || y >= map.size()) {
                    debug("Skipping edge: dstY out of bounds. dstY=" + y + " rows=" + map.size());
                    return SpireReturn.Return();
                }

                List<MapRoomNode> row = map.get(y);
                if (row == null) {
                    debug("Skipping edge: row is null at y=" + y);
                    return SpireReturn.Return();
                }

                if (x < 0 || x >= row.size()) {
                    debug("Skipping edge: dstX out of bounds. dstX=" + x + " rowSize=" + row.size() + " at y=" + y);
                    return SpireReturn.Return();
                }

                MapRoomNode room = row.get(x);
                if (room == null) {
                    debug("Skipping edge: null room at (" + x + "," + y + ")");
                    return SpireReturn.Return();
                }

                // Perform the same side effect as original: add the room to the path
                __instance.add(room);

                // Skip original to avoid risky direct indexing on non-rectangular maps
                return SpireReturn.Return();
            } catch (Exception e) {
                logger.warn("WeightedPaths safe add failed; skipping edge. Reason: " + e.getMessage(), e);
                return SpireReturn.Return();
            }
        }

        private static boolean isAlternateMapActive() {
            // Downfall mod id commonly used is "downfall". Fallbacks can be added if needed.
            if (Loader.isModLoaded(ModIDs.downfall)) return true;
            return Loader.isModLoaded("ActLikeIt");
            // Add more alternate map mod ids here if necessary
        }

        private static void debug(String msg) {
            logger.debug("[WeightedPaths AltMap Guard] " + msg);
        }
    }
}
