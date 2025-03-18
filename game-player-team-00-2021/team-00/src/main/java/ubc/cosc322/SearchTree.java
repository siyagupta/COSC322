package ubc.cosc322;

import java.util.ArrayList;

public class SearchTree {
    private SearchTreeNode root;
    private minDisHeur minDistH = new minDisHeur();
    private int depth;
    private int numOfMoves;
    public int evaluation;
    private ArrayList<SearchTreeNode> frontier = new ArrayList<>();

    public SearchTree(SearchTreeNode node) {
        this.root = node;
        this.depth = 0;
        this.evaluation = 0;
    }

    private int AlphaBeta(SearchTreeNode N, int D, int alpha, int beta, boolean maxPlayer) {
        if (D == 0 || N.getChildren().isEmpty()) {
            evaluation++;
            minDistH.calculate(N.gameRules);
            N.setValue(minDistH.ownedByUs - minDistH.ownedByThem);
            return N.getValue();
        }

        if (maxPlayer) {
            int V = Integer.MIN_VALUE;
            for (SearchTreeNode S : N.getChildren()) {
                V = Math.max(V, AlphaBeta(S, D - 1, alpha, beta, false));
                alpha = Math.max(alpha, V);
                if (beta <= alpha) break;
            }
            N.setValue(V);
            return V;
        } else {
            int V = Integer.MAX_VALUE;
            for (SearchTreeNode S : N.getChildren()) {
                V = Math.min(V, AlphaBeta(S, D - 1, alpha, beta, true));
                beta = Math.min(beta, V);
                if (beta <= alpha) break;
            }
            N.setValue(V);
            return V;
        }
    }

    public void expandFrontier() {
        ArrayList<SearchTreeNode> newFrontier = new ArrayList<>();
        boolean isOurTurn = (depth % 2 == 0);
        for (SearchTreeNode S : frontier) {
            newFrontier.addAll(S.setSuccessors(isOurTurn));
        }
        frontier.clear();
        for (SearchTreeNode S : newFrontier) {
            frontier.add(new SearchTreeNode(S.gameRules.deepCopy()));
        }
        depth++;
    }

    public void performAlphaBeta() {
        evaluation = 0;
        AlphaBeta(root, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
    }

    public SearchTreeNode makeMove() {
        expandFrontier();
        performAlphaBeta();
        SearchTreeNode bestMove = getMoveAfterAlphaBeta();
        if (bestMove != null) {
            makeMoveOnRoot(bestMove.getQueen(), bestMove.getArrowShot());
        }
        return bestMove;
    }

    private SearchTreeNode getMoveAfterAlphaBeta() {
        int max = Integer.MIN_VALUE;
        SearchTreeNode best = null;
        for (SearchTreeNode S : root.getChildren()) {
            if (S.getValue() > max) {
                max = S.getValue();
                best = S;
            }
        }
        return best;
    }

    public void makeMoveOnRoot(Queen qCurrentPos, Arrow a) {
        numOfMoves++;
        root.gameRules.addArrow(a);
        for (Queen Q : (qCurrentPos.isOpponent ? root.gameRules.enemy : root.gameRules.friend)) {
            if (Q.row == qCurrentPos.previousRow && Q.col == qCurrentPos.previousCol) {
                Q.moveQueen(qCurrentPos.row, qCurrentPos.col);
                break;
            }
        }
        root.gameRules.updateAfterMove();
    }
}
