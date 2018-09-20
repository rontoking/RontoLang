package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.IntArray;
import com.rontoking.rontolang.interpreter.objects.RontoPoint;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Function;

public  class Pathfinding {
    private final int width, height;
    private final BinaryHeap<PathNode> open;
    private final PathNode[] nodes;
    int runID;
    private final IntArray path = new IntArray();
    private int targetX, targetY;

    public static Pathfinding pathfinding;
    public static IntArray intArray;
    public static RontoPoint[] ret;

    public Pathfinding(int width, int height) {
        this.width = width;
        this.height = height;
        open = new BinaryHeap(width * 4, false);
        nodes = new PathNode[width * height];
    }

    /**
     * Returns x,y pairs that are the path from the target to the start.
     */
    public static RontoPoint[] path(RontoPoint pos, RontoPoint goal, int width, int height, final Function canPath, final Interpreter interpreter, final Class ownerClass, final Block instanceBlock){
        pathfinding = new Pathfinding(width, height){
            protected boolean isValid (int x, int y){
                return (Boolean)Executor.executeBlock(canPath.code, interpreter, ownerClass, canPath, new Reference[]{new Reference(x), new Reference(y)}, instanceBlock).value;
            }
        };
        intArray = pathfinding.getPath((int)pos.floatX(), (int)pos.floatY(), (int)goal.floatX(), (int)goal.floatY());
        ret = new RontoPoint[intArray.size / 2];
        for (int i = 0; i < intArray.size; i += 2)
            ret[ret.length - 1 - i / 2] = new RontoPoint( intArray.get(i), intArray.get(i + 1), interpreter);

        return ret;
    }

    public IntArray getPath(int startX, int startY, int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;

        path.clear();
        open.clear();

        runID++;
        if (runID < 0) runID = 1;

        int index = startY * width + startX;
        PathNode root = nodes[index];
        if (root == null) {
            root = new PathNode(0);
            root.x = startX;
            root.y = startY;
            nodes[index] = root;
        }
        root.parent = null;
        root.pathCost = 0;
        open.add(root, 0);

        int lastColumn = width - 1, lastRow = height - 1;
        while (open.size > 0) {
            PathNode node = open.pop();
            if (node.x == targetX && node.y == targetY) {
                while (node != root) {
                    path.add(node.x);
                    path.add(node.y);
                    node = node.parent;
                }
                break;
            }
            node.closedID = runID;
            int x = node.x;
            int y = node.y;
            if (x < lastColumn) {
                addNode(node, x + 1, y, 10);
                if (y < lastRow) addNode(node, x + 1, y + 1, 14); // Diagonals cost more, roughly equivalent to sqrt(2).
                if (y > 0) addNode(node, x + 1, y - 1, 14);
            }
            if (x > 0) {
                addNode(node, x - 1, y, 10);
                if (y < lastRow) addNode(node, x - 1, y + 1, 14);
                if (y > 0) addNode(node, x - 1, y - 1, 14);
            }
            if (y < lastRow) addNode(node, x, y + 1, 10);
            if (y > 0) addNode(node, x, y - 1, 10);
        }
        return path;
    }

    private void addNode(PathNode parent, int x, int y, int cost) {
        if(parent == null) {
            if (!isValid(x, y)) return;
        }
        else {
            if (!isValid(x, y)) return;
        }

        int pathCost = parent.pathCost + cost;
        float score = pathCost + Math.abs(x - targetX) + Math.abs(y - targetY);

        int index = y * width + x;
        PathNode node = nodes[index];
        if (node != null && node.runID == runID) { // Node already encountered for this run.
            if (node.closedID != runID && pathCost < node.pathCost) { // Node isn't closed and new cost is lower.
                // Update the existing node.
                open.setValue(node, score);
                node.parent = parent;
                node.pathCost = pathCost;
            }
        } else {
            // Use node from the cache or create a new one.
            if (node == null) {
                node = new PathNode(0);
                node.x = x;
                node.y = y;
                nodes[index] = node;
            }
            open.add(node, score);
            node.runID = runID;
            node.parent = parent;
            node.pathCost = pathCost;
        }
    }

    protected boolean isValid(int x, int y) {
        return true;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    static private class PathNode extends BinaryHeap.Node {
        int runID, closedID, x, y, pathCost;
        PathNode parent;

        public PathNode(float value) {
            super(value);
        }
    }
}
