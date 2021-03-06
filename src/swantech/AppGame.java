package swantech;

import ictk.boardgame.GameInfo;
import ictk.boardgame.chess.ChessGameInfo;

import javax.swing.*;

/**
 * This is the main application class.
 * This class handles each game.
 * It provides a game's instances such as the board <br>
 * instance and the chess engine instance.
 * @author Ifetayo
 */
public class AppGame {

    private static GameBoard gameBoard;
    //int gameStatus;
    private Profile blackPlayer;
    private Profile whitePlayer;
    private static ChessEngine chessEngine;
    private static PlayColour myColour;
    private static AppGame appGame;

    /**
     * Return the instance of the running game
     * @return AppGame reference object
     */
    public static AppGame GameInstance(){ //TODO check for null, not sure why I should
        return appGame;
    }

    /**
     * Return the reference to the running Game Board instance
     * @return GameBoard reference object
     */
    public GameBoard GetGameBoardInstance(){ //TODO check for null
        return gameBoard;
    }

    /**
     * Return a reference to the chess engine
     * @return ChessEngine
     */
    public ChessEngine getChessEngineInstance(){

        if (chessEngine == null){
            StartChessEngine();
        }
        return chessEngine;
    }

    /**
     * Start the Chess engine
     *
     */
    private void StartChessEngine(/*PlayColour engineColour*/){

        try {
            myColour = PlayColour.WHITE;
            chessEngine = new swantech.ChessEngine(chessEngine.otherColour(myColour));  // tell chess engine what colour it is playing
        } catch (Exception e){
            System.err.println("Failed to initialse the chess engine, exiting");
            System.err.println("Error is : " + e.getMessage());
            System.exit(99);
        }
    }

    /**
     * Show the game display <br>
     * The application does nothing for now <br>
     *
     */
    public void ShowGameDisplay(){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gameBoard = new GameBoard();
            }
        });
    }

    /**
     * Main method
     * @param args
     */
    public static void main(String[] args){
        appGame = new AppGame();
        appGame.ShowGameDisplay(); //show the display of the game
    }
}
