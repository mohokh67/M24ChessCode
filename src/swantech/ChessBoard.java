/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swantech;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Objects;
//import ictk.boardgame.chess.ChessBoard;
import ictk.boardgame.chess.ChessMove;
import ictk.boardgame.chess.ChessPiece;
import ictk.boardgame.chess.Square;
/**
 *
 * @author moho
 */
public class ChessBoard {
    
	private final JPanel gui = new JPanel(new BorderLayout(3, 3));
	private final JButton[][] chessBoardSquares = new JButton[8][8];  // Row, Col (but all wrong)
	private JPanel chessBoard;
	private static final String COLS = "ABCDEFGH";
	private static final int squareSize = 64;
        private static final String imageDir= "../res/";
	private static ChessEngine chessEngine;
	private static PlayColour myColour;
	private static int candidateRow = 0, candidateCol = 0;

	ChessBoard()   {
		initializeGui();

		// And init the chess engine...
		try {
			myColour = PlayColour.WHITE;
			chessEngine = new ChessEngine(chessEngine.otherColour(myColour));  // tell chess engine what colur IT is playing
		} catch (Exception e){
			System.out.println("Failed to initialse the chess engine, exiting");
			System.out.println("Error is : " + e.getMessage());
			System.exit(99);
		}

	}

	
	private void initializeGui() {

		chessBoard = new JPanel(new GridLayout(0, 9));
		chessBoard.setBorder(new LineBorder(Color.BLACK));
		gui.add(chessBoard);

		// create the chess board squares
		Insets buttonMargin = new Insets(0, 0, 0, 0);
		for (int col = 0; col < chessBoardSquares.length; col++) {
			for (int row = 0; row < chessBoardSquares[col].length; row++) {
				JButton b = new JButton();
				b.putClientProperty("row", row);
				b.putClientProperty("col", col);
				b.putClientProperty("highlighted", false);
				b.setMargin(buttonMargin);
				ImageIcon icon = new ImageIcon(new BufferedImage(squareSize,
						squareSize, BufferedImage.TYPE_INT_ARGB));
				b.setIcon(icon);
				b.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						actionSquare(e);
					}


				});
				b.setBackground(setSquareBackgroundColor(row, col));

				chessBoardSquares[row][col] = b;
			}
		}


		resetPieces();  // Place the pieces in the start position

		// fill the chess board
		chessBoard.add(new JLabel(""));
		// fill the top row
		for (int ii = 0; ii < 8; ii++) {
			chessBoard.add(new JLabel(COLS.substring(ii, ii + 1),
					SwingConstants.CENTER));
		}
		// fill the black non-pawn piece row
		for (int ii = 0; ii < 8; ii++) {
			for (int jj = 0; jj < 8; jj++) {
				switch (jj) {
				case 0:
					chessBoard.add(new JLabel("" + (ii + 1), SwingConstants.CENTER));
				default:
					chessBoard.add(chessBoardSquares[jj][ii]);
				}
			}
		}
               /*
                for (int ii = 7; ii >= 0; ii--) {
			chessBoard.add(new JLabel(COLS.substring(ii, ii + 1), SwingConstants.CENTER));
		}
                
                */
                
		JFrame f = new JFrame("Chess Board");
		f.add(this.getGui());
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setLocationByPlatform(true);

		// ensures the frame is the minimum size it needs to be
		// in order display the components within it
		f.pack();
		// ensures the minimum size is enforced.
		f.setMinimumSize(f.getSize());
		f.setVisible(true);

	}


	// Just shorthand to avoid the typecast on every use.
	// NOTE chessEngine (and ictk library) work on rank & file = [1..8]
	// but this class works on [0..7],
	// AND rank in the wrong order! Should be 1 at Bottom (White Queen rank)
	// so these methods do the conversion
	// ONLY use these methods to convert to/from Square

	private Square intSquare(int f, int r)
	{
		assert f >= 0 && f < 8 && r >= 0 && r < 8;

		r = 8-r;
		f++;
		Square s = chessEngine.getChessSquare(r, f);

		return s;
	}
	private int squareToRow(Square s)
	{
		return s.getFile()-1;
	}
	private int squareToCol(Square s)
	{
		return 8 - s.getRank();
	}


	/**
	 * This is the big one.
	 * Called when user clicks ANY square
	 * @param actionEvent e - get the jButton pressed from this
	 */
	private void actionSquare(ActionEvent e) {

		Object source = e.getSource();
		if (source instanceof JButton) {
			JButton b = (JButton)source;
			System.out.println("Whose move : " + chessEngine.whoseMove());
			// Now have the button that was pressed
			// get the details of the square
			int row = (Integer)b.getClientProperty("row");
			int col = (Integer)b.getClientProperty("col");
			boolean isHighlighted = (Boolean)b.getClientProperty("highlighted");

			// OK lets play chess...
			// Its always users turn if we get here
			if (isHighlighted){
				// If it is highlighted, that's because I have already marked it as a legal move square,
				// So we can make a move!
				tryBoardMove(row, col);

			} else {
				// Else its an unhighlighted square, is it another candidate?
				// first  unhighlight anything (its left over from last attempt)

				showMoves(row, col);
			}
		} // else its not a button, I dom't care what else it may be
	}

	/**
	 * User has clicked any square. If it their piece and can be moved,  \n
	 * Record the place, highlight legal move squares
	 * @param row
	 * @param col
	 */
	private void showMoves(int row, int col) {
		unhighlightAll();
		System.out.println("Button Clicked row : " + Integer.toString(row) + ", col : " + Integer.toString(col));

		// What are the legal moves?
		ArrayList<Square> squares = chessEngine.getLegalMoves(intSquare(row, col));
		if (squares == null) {
            System.out.println("Nothing on this square");
            flashSquare(row, col);
        } else {
            // OK, there are legal moves from here, record this as a candidate and highlight the legal moves.
            candidateCol = col;
            candidateRow = row;
            for (int i = 0; i < squares.size(); i++) {
                highlight(squareToRow(squares.get(i)), squareToCol(squares.get(i)), true);
            }
        }
	}

	/**
	 * User has already selected a possible move piece, and has now selected one of the highligted legal move squares
	 * SO make the move, then play opposing move.
	 * @param row  TOI square
	 * @param col
	 * (globals candidateRow, candidateCol have the FROM details)
	 */
	private void tryBoardMove(int row, int col) {
		Square fromSquare = intSquare(candidateRow, candidateCol);
		Square toSquare = intSquare(row, col);
		ChessEngineErrors ce = chessEngine.makeMyMove(fromSquare, toSquare);

		if (ce != ChessEngineErrors.OK){
            System.out.println("Chess engine ERROR : " + ce.name());
        } else {
            // Move OK, update board
            makeBoardMove(fromSquare, toSquare);

			MakeOtherPlayerMove();

        }
	}

	/**
	 * Player has just made their (legal) move -
	 * this method does the 'Other Player' move
	 */
	private void MakeOtherPlayerMove() {
		ChessMove to = chessEngine.engineMove();
		makeBoardMove(to.getOrigin(), to.getDestination());
	}

	private JButton buttonFromSquare(Square s)
	{
		Square t = s;
		return chessBoardSquares[s.getFile()-1][8-s.getRank()];
	}
	private void makeBoardMove(Square fromSquare, Square toSquare) {
		JButton fromButon = buttonFromSquare(fromSquare);
		JButton toButon = buttonFromSquare(toSquare);
		String piece = (String)fromButon.getClientProperty("piece");
		RemovePiece(squareToRow(fromSquare), squareToCol(fromSquare));
		setPiece(squareToRow(toSquare), squareToCol(toSquare), piece);
		unhighlightAll();
	}

	private void flashSquare(int row, int col) {

		highlight(row, col, true);
		chessBoard.repaint();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		highlight(row, col, false);
	}


	/**
	 * Simple utility to set the background colour based on row and col.
	 * As a function because its needed to unhighlight a square
	 * @param row 0..7
	 * @param col 0..7
	 * @return A colour (Gray or White)
	 */
	private Color setSquareBackgroundColor (int row, int col) {
		assert row >= 0 && row < 8 && col >= 0 && col < 8;

		if ((row % 2 == 1 && col % 2 == 1)
				|| (row % 2 == 0 && col % 2 == 0)) {
			return Color.WHITE;
		} else {
			return Color.GRAY;
		}
	}

	/**
	 * Highlights or unhighlights a given square
	 * @param row, 0 - 7
	 * @param col, 0 - 7
	 * @param highlight
	 */
	public void highlight(int row, int col, boolean highlight) {
		assert row >= 0 && row < 8 && col >= 0 && col < 8;

		JButton b = this.chessBoardSquares[row][col];
		if (highlight) {
			b.setBackground(Color.ORANGE);
			b.putClientProperty("highlighted", true);
		}
		else {
			b.setBackground(setSquareBackgroundColor(row, col));
			b.putClientProperty("highlighted", false);
		}

	}

	/**
	 * Shorthand utility, remove all highlighting
	 */
	private void unhighlightAll() {
		for (int r = 0; r < 8; r++)
			for (int c = 0; c < 8; c++)
				highlight(r, c, false);
		candidateRow = candidateCol = 0;
	}

	public void RemovePiece(int row, int col) {

		assert row >= 0 && row < 8 && col >= 0 && col < 8;

		JButton b = chessBoardSquares[row][col];
		ImageIcon icon = new ImageIcon(new BufferedImage(squareSize,
				squareSize, BufferedImage.TYPE_INT_ARGB));
		b.setIcon(icon);
		b.setBackground(setSquareBackgroundColor(row, col));
		b.putClientProperty("piece", "");

	}

	public void resetPieces() {
		for (int i = 0; i < 8; i++) {
			setPiece(i, 6, "wp");
			setPiece(i, 1, "bp");
		}

		setPiece(7, 7, "wr");
		setPiece(6, 7, "wn");
		setPiece(5, 7, "wb");
		setPiece(4, 7, "wk");
		setPiece(3, 7, "wq");
		setPiece(2, 7, "wb");
		setPiece(1, 7, "wk");
		setPiece(0, 7, "wr");

		setPiece(0, 0, "br");
		setPiece(1, 0, "bn");
		setPiece(2, 0, "bb");
		setPiece(3, 0, "bq");
		setPiece(4, 0, "bk");
		setPiece(5, 0, "bb");
		setPiece(6, 0, "bn");
		setPiece(7, 0, "br");
	}

	public void setPiece(int row, int col, String pieceName) {
		assert row >= 0 && row < 8 && col >= 0 && col < 8;

		JButton b = chessBoardSquares[row][col];
		ImageIcon icon;
		BufferedImage piece = null;

		String fileName = imageDir + pieceName + ".png";
                try {
                    piece = ImageIO.read(this.getClass().getResource(fileName));
                } catch (IOException ex) {
                //Logger.getLogger(ChessBoard.class.getName()).log(Level.SEVERE, null, ex);
                }

		icon = new ImageIcon(piece);
		b.setIcon(icon);
		b.setBackground(setSquareBackgroundColor(row, col));
		b.putClientProperty("piece", pieceName);
	}
        
        private  JComponent getChessBoard() {
		return chessBoard;
	}

	private JComponent getGui() {
		return gui;
	}

}