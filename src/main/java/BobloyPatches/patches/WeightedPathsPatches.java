package BobloyPatches.patches;

import BobloyPatches.util.DownfallMapPath;
import BobloyPatches.util.ModIDs;
import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.derekjass.sts.weightedpaths.WeightedPaths;
import com.derekjass.sts.weightedpaths.patches.WeightRenderPatches;
import com.derekjass.sts.weightedpaths.paths.MapPath;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.map.Legend;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import downfall.patches.EvilModeCharacterSelect;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import mintySpire.patches.map.MiniMapDisplay;
import com.megacrit.cardcrawl.map.MapRoomNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class WeightedPathsPatches {
    private static final Logger logger = LogManager.getLogger(WeightedPathsPatches.class.getName());

//    @SpirePatch2(clz = WeightedPaths.class, method = "regeneratePaths", requiredModId = ModIDs.weightedPaths)
//    public static class RegeneratePathsDownfallPatch {
//        @SpirePrefixPatch
//        public static SpireReturn<Void> prefix(List<MapPath> ___paths) {
//            if (Loader.isModLoaded(ModIDs.downfall) && EvilModeCharacterSelect.evilMode) {
//                ___paths = DownfallMapPath.generateDownAll();
//                return SpireReturn.Return();
//            }
//            return SpireReturn.Continue();
//        }
//    }

    @SpirePatch(
            clz = Legend.class,
            method = "render"
    )
    public static class End {
        public static SpireReturn<Void> Prefix(Legend __instance, SpriteBatch sb) {
            if (MiniMapDisplay.renderingMiniMap) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    // Old Method
//    @SpirePatch(
//            optional = true,
//            clz = WeightRenderPatches.PostMapRoomNodeRenderPatch.class,
//            method = "onMapRoomNodeRender"
//    )
//    public static class FixWeightedPathsScrolling {
//        static float saveOffsetY;
//
//        @SpirePrefixPatch
//        public static void adjustWeightPosition(MapRoomNode room, SpriteBatch sb) {
//            if (MiniMapDisplay.renderingMiniMap) {
//                // Adjust the hitbox Y position to account for the minimap's different offsetY
//                saveOffsetY = ReflectionHacks.getPrivateStatic(MiniMapDisplay.class, "saveOffsetY");
//                room.hb.cY = room.hb.cY - saveOffsetY + DungeonMapScreen.offsetY;
//            }
//        }
//
//        @SpirePostfixPatch
//        public static void restoreWeightPosition(MapRoomNode room, SpriteBatch sb) {
//            if (MiniMapDisplay.renderingMiniMap) {
//                // Restore the original hitbox Y position
//                room.hb.cY = room.hb.cY + saveOffsetY - DungeonMapScreen.offsetY;
//            }
//        }
//    }

    @SpirePatch(
            optional = true,
            cls = "com.derekjass.sts.weightedpaths.patches.WeightRenderPatches",
            method = "drawNodeValue"
    )
    public static class FixWeightedPathsScrolling {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(FieldAccess f) throws CannotCompileException {
                    // Intercept reads of room.hb.cY
                    if (f.isReader() && f.getFieldName().equals("cY")) {
                        f.replace(String.format("$_ = %s.adjustYForMinimap($proceed($$));", 
                            FixWeightedPathsScrolling.class.getName()));
                    }
                }
            };
        }

        public static float adjustYForMinimap(float originalY) {
            if (MiniMapDisplay.renderingMiniMap) {
                float saveOffsetY = ReflectionHacks.getPrivateStatic(MiniMapDisplay.class, "saveOffsetY");
                return originalY - saveOffsetY + DungeonMapScreen.offsetY;
            }
            return originalY;
        }
    }


    @SpirePatch2(clz = MapPath.class, method = "generateAll", requiredModId = ModIDs.weightedPaths)
    public static class GenerateAllDownfallPatch {
        @SpirePrefixPatch
        public static SpireReturn<List<MapPath>> prefix() {
            if (Loader.isModLoaded(ModIDs.downfall) && EvilModeCharacterSelect.evilMode) {
                List<MapPath> paths = DownfallMapPath.generateDownAll();
                return SpireReturn.Return(paths);
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
            if (Loader.isModLoaded(ModIDs.actLikeIt)) return true;

            return false;
            // Add more alternate map mod ids here if necessary
        }

        private static void debug(String msg) {
            logger.debug("[WeightedPaths AltMap Guard] " + msg);
        }
    }
}
