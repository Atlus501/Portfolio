import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;

import javax.swing.JLayeredPane;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;

import javax.swing.border.LineBorder;
import javax.swing.JButton;

public class ChessGui extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */


	/**
	 * Create the frame.
	 */

	//adds the necessary components
	static JPanel mainBoard = null;
	static JLayeredPane promoteBoard = null;
	static JLabel turn = null;
	static JLayeredPane mainDisplayBoard = null;
	static JLayeredPane displayValid = null;
	static JLayeredPane victoryScreen = null;
	static JLabel victoryDisplay = null;
	static Board newGame = null;
	static int savedRow = -1;
	static int savedCol = -1;
	static boolean continueGame = true;
	
	//the default contructor of the object
	public ChessGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 698, 776);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//saves the game if it hasn't ended yet
		 addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosing(WindowEvent e) {
	                // Call your custom method here
	                if(continueGame == true)
						try {
							newGame.saveBoard();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

	                // Dispose the frame
	                dispose();

	                // Optionally, exit the program
	                System.exit(0);
	            }
	        });

		setContentPane(contentPane);
		mainBoard = contentPane; 
		contentPane.setLayout(null);
		
		//the panel that is going ot show the victory
		JLayeredPane layeredPane_1 = new JLayeredPane();
		layeredPane_1.setBounds(200, 301, 299, 98);
		victoryScreen = layeredPane_1;
		victoryScreen.setVisible(false);
		victoryScreen.setLayout(null);
		victoryScreen.setBorder(new LineBorder(new Color(0, 250, 250), 3));
		victoryScreen.setBackground(new Color(0,0,0));
		victoryScreen.setOpaque(true);
		mainBoard.add(layeredPane_1);
		
		JLabel victory = new JLabel("", SwingConstants.CENTER);
		victory.setBounds(10,10,279,77);
		victory.setForeground(new Color(0, 200, 200));
		victoryDisplay = victory;
		victoryDisplay.setFont(new Font("Arial", Font.PLAIN, 40));
		victoryScreen.add(victoryDisplay);
		
		//the following is board for the promotion
		JLayeredPane panel = new JLayeredPane();
		panel.setBounds(200, 301, 299, 98);
		promoteBoard = panel;
		panel.setLayout(null);
		mainBoard.add(panel);
		panel.setVisible(false);
		promoteBoard.setBackground(new Color(150, 0, 250));
		promoteBoard.setOpaque(true);
		
		JButton btnNewButton = new JButton("Knight");
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnNewButton.setBounds(37, 52, 99, 35);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Tile[][] board = newGame.getBoard();
				board[savedRow][savedCol].getPiece().setType("Knight");
				mainBoard.remove(promoteBoard);
				continueGame = true;
				fillBoard();
				showValid("");
				mainBoard.revalidate(); // Revalidate the panel
				mainBoard.repaint();
			}
		});
		panel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Queen");
		btnNewButton_1.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainBoard.remove(promoteBoard);
				Tile[][] board = newGame.getBoard();
				board[savedRow][savedCol].getPiece().setType("Queen");
				mainBoard.remove(promoteBoard);
				continueGame = true;
				fillBoard();
				showValid("");
				mainBoard.revalidate(); // Revalidate the panel
				mainBoard.repaint();
			}
		});
		btnNewButton_1.setBounds(175, 52, 99, 35);
		panel.add(btnNewButton_1);
		
		JLabel lblNewLabel_1 = new JLabel("Promote Piece To:", SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblNewLabel_1.setBounds(37, 11, 247, 27);
		panel.add(lblNewLabel_1);
		
		//the layer that I'm going to put the chess pieces
		JLayeredPane layeredPanePieces = new JLayeredPane();
		layeredPanePieces.setBounds(74+537*1/9+2, 93+523*1/9+2, 
				537*8/9-3, 523*8/9-3);
		contentPane.add(layeredPanePieces);
		layeredPanePieces.setLayout(new GridLayout(8, 8));
		displayValid = layeredPanePieces;
		
		//just the chess label
		JLabel lblNewLabel = new JLabel("Chess");
		lblNewLabel.setBackground(new Color(0, 255, 0));
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 35));
		lblNewLabel.setBounds(283, 11, 151, 58);
		contentPane.add(lblNewLabel);
		
		//the layer that I'm going to set up the board
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		layeredPane.setBounds(74, 93, 537, 523);
		contentPane.add(layeredPane);
		layeredPane.setLayout(new GridLayout(9, 9));
		mainDisplayBoard = layeredPane;
		
		//the label that shows the turns
		JLabel turner = new JLabel("", SwingConstants.CENTER);
		turner.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		turner.setBounds(474, 30, 135, 33);
		turner.setOpaque(true);
		contentPane.add(turner);
		turn = turner;
		
		//button that loads the previous game
		JButton loadGame = new JButton("Load Previous Game");
		loadGame.setFont(new Font("Tahoma", Font.PLAIN, 25));
		loadGame.setBounds(200, 637, 304, 33);
		loadGame.setFocusable(false);
		loadGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					newGame.loadBoard();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				fillBoard();
				showValid("");
				victoryScreen.setVisible(false);
				promoteBoard.setVisible(false);
				mainBoard.revalidate(); 
				mainBoard.repaint();
				}});
		mainBoard.add(loadGame);
		
		//the new game
		Board game = new Board();
		newGame = game;
		
		//the interactive board is filled
		fillBoard();
		restartGame();
		
		//the method that adds in all the pieces from the data of the board	
	}
	
	static void checkPromotion() {
		Tile[][] game = newGame.getBoard();
		
		for(int row = 0; row <= 7; row += 7)
			for(int col = 0; col <= 7; col++) {
				
				Tile tested = game[row][col];
				
				if((row == 0 && !tested.isEmpty() && tested.isWhite() && tested.getPiece().getType() == "Pawn")
					|| (row == 7 && !tested.isEmpty() && tested.isBlack() && tested.getPiece().getType() == "Pawn")) {
					savedRow = row;
					savedCol = col;
					promoteBoard.setVisible(true);
					continueGame = false;
				}}}
	
	//method that restarts the game 
	static void restartGame() {
		
		//the button that is going to be used to reset the game
		JButton restart = new JButton("New Game");
		restart.setFont(new Font("Tahoma", Font.PLAIN, 20));
		restart.setBounds(59, 30, 160, 33);
		restart.setFocusable(false);
		
		restart.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			continueGame = true;
			newGame.setBoard();
			fillBoard();
			showValid("");
			victoryScreen.setVisible(false);
			promoteBoard.setVisible(false);
			mainBoard.revalidate(); 
			mainBoard.repaint();
			}});
		
		mainBoard.add(restart);	
		
		
	}
	
	//if the tile is valid, there would be a green O label that shows which
	//tiles are valid
	static void showValid(String sample) {
		
		displayValid.removeAll();
		
		for(int row = 0; row <= 7; row++)
			for(int col = 0; col <= 7; col++) {
				
				JLabel valid = new JLabel("", SwingConstants.CENTER);
				valid.setForeground(new Color(0,250,0));
				valid.setFont(new Font("Arial", Font.PLAIN, 30));
				
				String coord = row+","+col;
				
				if(sample.contains(coord))
					valid.setText("O");
				
				displayValid.add(valid);
			}}
	
	//fills in the checkerboard pattern of the board
	static void fillBoard() 
	{
		mainDisplayBoard.removeAll();
		
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				
				Tile[][] internalBoard = newGame.getBoard();
				
				//updates the turner
				turn.setFont(new Font("Tahoma", Font.PLAIN, 20));
				turn.setText("Turn: "+newGame.getTurn());
				
				if(newGame.getTurn() % 2 == 1) {
					turn.setForeground(new Color(0,0,0));
					turn.setBackground(new Color(250, 150, 0));
				} else {
					turn.setBackground(new Color(0,0,0));
					turn.setForeground(new Color(250, 0, 250));
				}
				
				//creates the labels that mark the edges of the board
				JLabel added = new JLabel("", SwingConstants.CENTER);
				added.setFont(new Font("Arial", Font.PLAIN, 30));
				added.setOpaque(true);
				
				//creates the checkerboard pattern
				if((row + col)%2 == 0) {
					added.setBackground(new Color(0,0,0));
					added.setForeground(new Color(255,255,255));
				}
				else {
					added.setBackground(new Color(255, 255,255));
					added.setForeground(new Color(0,0,0));
				}
				
				//creates the letters label at the top
				if(row == 0) {
					switch(col) {
					case 1:
						 added.setText("A");
				         break;
					case 2:
						added.setText("B");
						break;
					case 3:
						added.setText("C");
						break;
					case 4:
						added.setText("D");
						break;
					case 5:
						added.setText("E");
						break;
					case 6: 
						added.setText("F");
						break;
					case 7:
						added.setText("G");
						break;
					case 8:
						added.setText("H");
					}
					mainDisplayBoard.add(added);
				}
				//creates the numbers label on the left
				else if(col == 0 && row != 0) {
					added.setText(""+row);
					mainDisplayBoard.add(added);
					;
				}else {
					JButton addedButton = new JButton();
					addedButton.setRolloverEnabled(false);
					addedButton.setFont(new Font("Arial", Font.PLAIN, 18));
					addedButton.setEnabled(true);
					addedButton.setFocusable(false);
					
					Tile sample = internalBoard[row-1][col-1];
						
					if(sample.getPiece()!=null)
						addedButton.setText(sample.getPiece().getType().substring(0,2));
					
					//sets the background of the button 
					if((row + col)%2 == 0) 
						addedButton.setBackground(new Color(0,0,0));
					else 
						addedButton.setBackground(new Color(255, 255,255));
					
					//sets the final row and col for the method
					final int savedR = row-1;
					final int savedC = col-1;
					
					//sets the text color based on the color of the piece
					if(sample.getPiece() != null) 
						if(sample.isBlack()) 
							addedButton.setForeground(new Color(250, 0, 250));
						else
							addedButton.setForeground(new Color(250, 150, 0));
					
					//adds set move command to all of the buttons
					addedButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							
							if(continueGame) {
							String sample = newGame.move(savedR, savedC);
							showValid(sample);
							
							fillBoard();
							checkPromotion();
							
							
							String victory = newGame.checkVictory();
							if(victory.equals("White Victory") || 
									victory.equals("Stalemate") || 
									victory.equals("Black Victory")) 
							{
								continueGame = false;
								victoryScreen.setVisible(true);
								
								if(victory.equals("White Victory"))
									victoryDisplay.setForeground(new Color(250, 150, 0));
								else if(victory.equals("Black Victory"))
									victoryDisplay.setForeground(new Color(250, 0, 250));
								else
									victoryDisplay.setForeground(new Color(0, 200, 200));
								
								victoryDisplay.setText(victory+"!");
								fillBoard();
							}
							
							
							mainBoard.revalidate(); // Revalidate the panel
							mainBoard.repaint();

							}
						}});
					mainDisplayBoard.add(addedButton);	
				}}}}	
}