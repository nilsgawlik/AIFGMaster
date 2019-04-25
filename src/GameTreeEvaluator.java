import java.util.ArrayList;
import java.util.Scanner;

import lenz.htw.sawhian.Move;

class GameTreeEvaluator extends Thread {
    public static void main(String[] args) {
        GameState gameState = new GameState();
        Scanner scanner = new Scanner(System.in);
        int playerID = 0;

        while(true) {
            System.out.println(gameState.toString());
            GameTreeEvaluator gte = new GameTreeEvaluator(gameState, playerID, 8);
            gte.start();
            Move move = gte.getResult(false);
            System.out.println("Best move: " + move);

            String str = scanner.nextLine();
            if(str.equals("q")) {
                break;
            }

            if(move != null) {
                gameState.performMove(move);
            } else {
                System.out.println("pass");
            }
            System.out.println("---");

            playerID = playerAfter(playerID);
        }

        scanner.close();
    }

    // should be set to true to stop calculation
    private boolean stop;

    private GameState gameState;
    private int playerID;
    private int maxDepth;

    private Move result;

    public GameTreeEvaluator(GameState gameState, int playerID, int maxDepth) {
        this.gameState = gameState;
        this.playerID = playerID;
        this.maxDepth = maxDepth;
    }

    // wait for the thread to finish and return result
    // if stopCalculation is true terminate the thread softly and return the temporary result.
    public Move getResult(boolean stopCalculation) {
        this.stop = stopCalculation; // stop the calculation if specified
        try {
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void run() {
        result = getMoveFor(this.gameState, this.playerID, this.maxDepth);
    }

    private Move getMoveFor(GameState gameState, int playerID, int maxDepth) {
        ArrayList<Move> moves = gameState.getPossibleMoves(playerID);

        float max = -Float.MAX_VALUE;
        Move maxMove = null;

        for (Move move : moves) {
            float[] balance = evaluateSubtree(new GameState(gameState, move), playerAfter(playerID), maxDepth, 1);
            if(balance[playerID] > max){
                max = balance[playerID];
                maxMove = move;
            }
        }

        return maxMove;
    }

    private static final float[] MIN_BALANCE = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

    private float[] evaluateSubtree(GameState gameState, int playerID, int maxDepth, int depth){
        if(stop) {
            return MIN_BALANCE;
        }

        if(depth >= maxDepth) {
            return gameState.evaluate();
        } else {
            float[] maxBalance = MIN_BALANCE;
            for (Move move : gameState.getPossibleMoves(playerID)) {
                float[] balance = evaluateSubtree(new GameState(gameState, move), playerAfter(playerID), maxDepth, depth + 1);
                if(balance[playerID] > maxBalance[playerID]) {
                    maxBalance = balance;
                }
            }

            return maxBalance;
        }
    }

    private static int playerAfter(int playerID) {
        return (playerID + 1) & 0b11;
    }
}