package ubc.cosc322;

import java.util.ArrayList;

public class SearchTreeNode {
    private int heuristicValue;
    private Arrow arrowShot;
    private Queen queen;
    private ArrayList<SearchTreeNode> children = new ArrayList<>();
    private SuccessorHeuristicFunction successorHeuristic = new SuccessorHeuristicFunction();
    protected GameRules gameRules;

    public SearchTreeNode(GameRules board, Queen q, Arrow A) {
        gameRules = board;
        queen = q;
        arrowShot = A;
        heuristicValue = 0;
    }

    public SearchTreeNode(GameRules board) {
        gameRules = board;
    }

    public Queen getQueen() {
        return queen;
    }

    public ArrayList<SearchTreeNode> getChildren() {
        return children;
    }

    public Arrow getArrowShot() {
        return arrowShot;
    }

    public ArrayList<SearchTreeNode> setSuccessors(boolean ourMove) {
        children.clear();
        children.addAll(successorHeuristic.getSuccessors(gameRules, ourMove));
        return children;
    }

    public int getValue() {
        return heuristicValue;
    }

    public void setValue(int V) {
        heuristicValue = V;
    }
}
