package BobloyPatches.util;

import com.derekjass.sts.weightedpaths.paths.MapPath;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.TheEnding;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DownfallMapPath extends MapPath {
    /**
     * Generate all possible paths when the map is flipped (Downfall/Evil mode).
     * Behaves like MapPath.generateAll, but treats the end of the map (top rows)
     * as the starting location and progresses toward y == 0.
     */
    public static List<MapPath> generateDownAll() {
        List<MapPath> paths = new ArrayList<>();
        // Do not generate in Act 4 (matches MapPath.generateAll behavior)
        if (CardCrawlGame.dungeon instanceof TheEnding) {
            return paths;
        }

        if (!AbstractDungeon.firstRoomChosen) {
            // At the beginning of an act in Downfall, the player starts at the last pre-boss row.
            // In vanilla this is row 0; for Downfall we use MAP_HEIGHT - 2 (pre-boss).
            paths = generateDownStarterPaths();
        } else if (AbstractDungeon.getCurrMapNode() == null) {
            return new ArrayList<>();
        } else if (AbstractDungeon.getCurrMapNode().y > 0) {
            // Generate from current room, traversing toward y == 0 using the (reversed) edges.
            MapRoomNode curr = AbstractDungeon.getCurrMapNode();
            if (!curr.hasEdges()) {
                return new ArrayList<>();
            }
            for (MapEdge edge : curr.getEdges()) {
                MapPath path = new MapPath();
                if (safelyAddEdgeDestination(path, edge)) {
                    paths.add(path);
                }
            }
        } else {
            // Floor not eligible (already at y == 0)
            return paths;
        }

        generateRemainingReverse(paths);
        return paths;
    }

    private static boolean safelyAddEdgeDestination(MapPath path, MapEdge edge) {
        try {
            List<ArrayList<MapRoomNode>> map = CardCrawlGame.dungeon.getMap();
            int y = edge.dstY;
            int x = edge.dstX;
            if (map == null || y < 0 || y >= map.size()) return false;
            ArrayList<MapRoomNode> row = map.get(y);
            if (row == null || x < 0 || x >= row.size()) return false;
            MapRoomNode room = row.get(x);
            if (room == null) return false;
            path.add(room);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static List<MapPath> generateDownStarterPaths() {
        List<MapPath> paths = new LinkedList<>();
        int startRow = AbstractDungeon.MAP_HEIGHT - 2; // Pre-boss row becomes the starting row in Downfall
        List<MapRoomNode> firstFloor = CardCrawlGame.dungeon.getMap().get(startRow);
        for (MapRoomNode room : firstFloor) {
            if (!room.hasEdges()) {
                continue;
            }
            MapPath path = new MapPath();
            path.add(room);
            paths.add(path);
        }
        return paths;
    }

    private static void generateRemainingReverse(List<MapPath> paths) {
        List<MapPath> newPaths = new LinkedList<>();
        Iterator<MapPath> iter = paths.iterator();
        while (iter.hasNext()) {
            MapPath path = iter.next();
            MapRoomNode lastRoom = path.peekLast();
            if (lastRoom == null) {
                iter.remove();
                continue;
            } else if (lastRoom.y == 0) {
                // Reached the beginning row
                continue;
            } else if (!lastRoom.hasEdges()) {
                // No further connections; drop this path
                iter.remove();
                continue;
            }
            // Branch like MapPath.generateRemaining, but continue toward decreasing y
            for (int i = 1; i < lastRoom.getEdges().size(); i++) {
                MapPath newPath = (MapPath) path.clone();
                if (safelyAddEdgeDestination(newPath, lastRoom.getEdges().get(i))) {
                    newPaths.add(newPath);
                }
            }
            safelyAddEdgeDestination(path, lastRoom.getEdges().get(0));
        }
        paths.addAll(newPaths);
        // Continue until every path reaches y == 0
        if (paths.stream().anyMatch(p -> {
            MapRoomNode tail = p.peekLast();
            return tail != null && tail.y > 0;
        })) {
            generateRemainingReverse(paths);
        }
    }
}
