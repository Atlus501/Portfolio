import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.PrintWriter;

class Board {

	Tile[][] board = null;
	ArrayList<Tile> whitePieces = new ArrayList<Tile>();
	ArrayList<Tile> blackPieces = new ArrayList<Tile>();
	
	int turn = 0;
	int row1 = -1;
	int col1 = -1;
	int row2 = -1;
	int col2 = -1;
	
	boolean whiteDefeat = false;
	boolean blackDefeat = false;

	int blackKingRow = -1;
	int blackKingCol = -1;
	
	int whiteKingRow = -1;
	int whiteKingCol = -1;
	
	//initializes the board
	Board(){
	  board = new Tile[8][8];
	  this.setBoard();
	}
	
	//sets up the board
	void setBoard() {
		turn = 1;
		
		for(int row = 0; row < 8; row++) {
			for(int col = 0; col < 8; col++) {
				if(row == 0) {
					if(col == 0 || col == 7)
					    board[row][col]= new Tile(new BlackPiece("Rook"), row, col);
					if(col == 1 || col == 6)
					    board[row][col]= new Tile(new BlackPiece("Knight"), row, col);
				    if(col == 2 || col == 5)
				    	board[row][col]= new Tile(new BlackPiece("Bishop"), row, col);
				    if(col == 3)
				    	board[row][col]= new Tile(new BlackPiece("Queen"), row, col);
				    if(col == 4)
				    	board[row][col]= new Tile(new BlackPiece("King"), row, col);
				    
				    blackPieces.add(board[row][col]);
				    }
				else if(row == 1) { 
					board[row][col]= new Tile(new BlackPiece("Pawn"), row, col);
					blackPieces.add(board[row][col]);
				}
				else if(row == 6) {
					board[row][col]= new Tile(new WhitePiece("Pawn"), row, col);
					whitePieces.add(board[row][col]);
				}
			    else if(row == 7) {
			    	if(col == 0 || col == 7)
			    		board[row][col]= new Tile(new WhitePiece("Rook"), row, col);
			    	if(col == 1 || col == 6)
					    board[row][col]= new Tile(new WhitePiece("Knight"), row, col);
				    if(col == 2 || col == 5)
						board[row][col]= new Tile(new WhitePiece("Bishop"), row, col);
					if(col == 3)
					    board[row][col]= new Tile(new WhitePiece("Queen"), row, col);
				    if(col == 4)
				    	board[row][col]= new Tile(new WhitePiece("King"), row, col);
				    
				    whitePieces.add(board[row][col]);
			         }
			    else
			    	board[row][col] = new Tile(row, col);
			}}
		whiteKingRow = 7;
		whiteKingCol = 4;
		
		blackKingRow = 0;
		blackKingCol = 4;
	}
	
	
	//gets all the valid moves a side could take
	String allValidMoves() {
		
		int savedR = row1;
		int savedC = col1;
		
		String result = "";
		
		if(turn%2 == 1) 
			for(Tile sample : whitePieces) {
				row1 = sample.getRow();
				col1 = sample.getCol();
				result += getValidMoves(sample.getRow(), sample.getCol());
			}
		else
			for(Tile sample : blackPieces) {
				row1 = sample.getRow();
				col1 = sample.getCol();
				result += getValidMoves(sample.getRow(), sample.getCol());
			}
		
		row1 = savedR;
		col1 = savedC;
		
		 return result;
	}
	
	//updates the arraylists that records the pieces
	void updateList(int row, int col) {
		
		if(turn %2 == 0) {
			blackPieces.remove(board[row1][col1]);
			blackPieces.add(board[row2][col2]);
			whitePieces.remove(board[row2][col2]);
			if(row != -1 && col != -1)
				whitePieces.remove(board[row][col]);
		}
		else {
			whitePieces.remove(board[row1][col1]);
			whitePieces.add(board[row2][col2]);
			blackPieces.remove(board[row2][col2]);
			if(row != -1 && col != -1)
				blackPieces.remove(board[row][col]);
		}
	}
	
	//see if the piece is checked
	boolean check(int row, int col, boolean white) {
		boolean check = false;
		boolean block = false;
		int orientation = -1;
		String blackList1 = "Bishop Queen";
		String blackList2 = "Rook Queen";
		
		int kingRow = row;
		int kingCol = col;
		
		if(!white) {
			orientation = 1;
		}

		//checks the area around the tile for knights
		for(double c = 0.0; c <= 3.0*Math.PI/2.0 && !check; c+= 1.0/2.0*Math.PI) {
			for(int mult = 1; mult <= 2 && !check; mult++) {
				int checkX = (int) Math.round(kingRow+2*(Math.cos(c+mult*1.0/6.0*Math.PI)));
				int checkY = (int) Math.round(kingCol+2*(Math.sin(c+mult*1.0/6.0*Math.PI)));

				if((checkX > -1 && checkX < 8 && checkY > -1 && checkY < 8) && 
					board[checkX][checkY].isOpposite(board[kingRow][kingCol].getPiece()) == 1 &&
					board[checkX][checkY].equalType("Knight")) 
					check = true;
				}
		}
		
		//checks the diagonals for pawns, queens, and bishops
		for(double c = Math.PI/4.0; c <= 7.0*Math.PI/4.0 && !check; c+= 0.5*Math.PI) {
			block = false;
			for(int i = 1; i<=7 && !check && !block; i++) {
				int checkX =  (kingRow+i* ((int)(2*Math.cos(c))));
				int checkY = (kingCol+i*((int)(2*Math.sin(c))));
				
				if(checkX > -1 && checkX < 8 && checkY > -1 && checkY < 8) {
				
					int evaluate = board[checkX][checkY].isOpposite(board[kingRow][kingCol].getPiece());
					
					String sample = board[checkX][checkY].getType();

					if(evaluate == 0) {}
					else if(evaluate == -1) {
						block = true;
					}
					else if(board[kingRow+orientation][checkY].equalType("Pawn")) {	
						check = true;
						block = true;
						}
					else if(Math.abs(kingRow - checkX) == 1 && (Math.abs(kingCol - checkY) == 1) &&
							sample.equals("King")) {
						check = true;
						block = true;
					}
					else if(blackList1.contains(sample)){
						
						check = true;
						block = true;
					}
				}
				else
					block = true;
				}}
		
		
		
		//checks the rows and coluns for queens and rooks
		for(double c = 0.0; c <= 3*Math.PI/2.0 && !check; c+= 0.5*Math.PI) {
			block = false;
			for(int i = 1; i<=7 && !block && !check; i++) {
				int checkX = (kingRow+i*(int)Math.sin(c));
				int checkY = (kingCol+i*(int)Math.cos(c));
				
				if(checkX > -1 && checkX < 8 && checkY > -1 && checkY < 8) {
				
					int evaluate = board[checkX][checkY].isOpposite(board[kingRow][kingCol].getPiece());
					
					String sample = board[checkX][checkY].getType();
					
					if(evaluate == 0) {}
					else if(evaluate == -1) {
						block = true;
					}
					else if(Math.abs(kingRow - checkX) == 1 && (Math.abs(kingCol - checkY) == 1) &&
							sample.equals("King")) {
						check = true;
						block = true;
					}
					else if(blackList2.contains(sample)) {
						check = true;
						block = true;
					}}
				else
					block = true;
			}}
		
		return check;
	}
	
	void setBoard(Tile[][] input) {this.board = input;}
	
	//loads the information of the board into a file
	//more specifically, it prints the boolean of the piece being white, if it was moved, and
	//the type of the piece
	void saveBoard() throws FileNotFoundException
	{
		File output = new File("savedGame.txt");
		PrintWriter writer = new PrintWriter(output);
		
		for(int row = 0; row < 8; row++) {
			for(int col = 0; col < 8; col++) {
				Piece p = board[row][col].getPiece();
				if(p != null) {
					writer.println((p instanceof WhitePiece) + " "
							+ p.getMoved() + " "
							+ p.getType() + " "
							+ p.getEnPassant());}
				else
					writer.println();
			}
		}
		writer.println(this.getTurn());
		writer.println(this.whiteKingRow);
		writer.println(this.whiteKingCol);
		writer.println(this.blackKingRow);
		writer.println(this.blackKingCol);
		writer.close();
	}
	
	//loads the information of the board into a file
	void loadBoard() throws FileNotFoundException
	{
		File input = new File("savedGame.txt");
		Scanner reader = new Scanner(input);
		
		int row = 0;
		int col = 0;
		
		while(reader.hasNextLine() && row <8 &&col <8) {
			
			String written = reader.nextLine();
			String[] processed = written.split(" ", 4);
			
			if(processed.length < 4) {
				board[row][col] = new Tile(row, col);
			}
			else if(processed[0].equals("true")) {
				WhitePiece p = new WhitePiece(processed[2]);
				p.setMoved(processed[1]);
				p.setEnPassant(Integer.parseInt(processed[3]));
				board[row][col].setPiece(p);;
			}
			else {
				BlackPiece p = new BlackPiece(processed[2]);
				p.setMoved(processed[1]);
				p.setEnPassant(Integer.parseInt(processed[3]));
				board[row][col].setPiece(p);;
			}
			
			col++;
			
			if(col >= 8) {
				row++;
				col = 0;
			}
		}
		if(reader.hasNextLine())
			this.setTurn(Integer.parseInt(reader.nextLine()));
		
		if(reader.hasNextLine())
			this.whiteKingRow = (Integer.parseInt(reader.nextLine()));
		
		if(reader.hasNextLine())
			this.whiteKingCol = (Integer.parseInt(reader.nextLine()));
		
		if(reader.hasNextLine())
			this.blackKingRow = (Integer.parseInt(reader.nextLine()));
		
		if(reader.hasNextLine())
			this.blackKingCol = (Integer.parseInt(reader.nextLine()));
		
		
		reader.close();
	}
	
	void setTurn(int t) {this.turn = t;}
	
	//returns board
	Tile[][] getBoard(){
		return board;
	}
	
	int getTurn() {
		return turn;
	}
	
	//adds the option of the move if it is within the board
	private String addOption(int row, int col){
		return row+","+col+" ";
	}
	
	//checks if the index is valid
	private boolean validIndex(int row, int col) {
		
		int test = 0;
		
		if((row1 == -1 && col1 == -1 ) && (row > -1 && row < 8 && col > -1 && col < 8)) {
			return true;
		}
		else if(row == row1 && col == col1) {
			return true;
		} 
		else if(row < 0 || row > 7 || col < 0 || col > 7) 
		{return false;}
		else if(validIndex(row1, col1))
			test = board[row][col].isOpposite(board[row1][col1].getPiece()); 
			return test > -1;
	}
	
	//generates possible valid moves of piece
	String getValidMoves(int row, int col)
	{
		String result = "";
		String compared = board[row][col].getType();
		
		//evaluate moves if the index is correct
		if(validIndex(row1, col1)||row >= 0 && row <= 7 && col >= 0 && col <= 7) {
		switch(compared){
		
		//possible moves of the pawn
			case("Pawn"):
				int orientation = 1;
			if(board[row][col].getPiece() instanceof WhitePiece)
				orientation = -1;
				
			for(int c = col-1; c <= col+1; c+=2) {
				
				//checks the diagonals of the pawn
				if(validIndex(row + orientation * 1, c)) {
					int sides = board[row+1*orientation][c].isOpposite(board[row][col].getPiece());
					
					if(sides == 1 && !getChecked(row+orientation, c)) 
						result += addOption(row+orientation, c);	
					}}
			
			//checks in front of the pawn
			if(board[row+1*orientation][col].isEmpty()) 
			{
				if(!getChecked(row+orientation, col))
				result += addOption(row+1*orientation, col);
				
				//checks two spots ahead of itself
				if(!board[row][col].getPiece().getMoved() && board[row+2*orientation][col].isEmpty()
						&& !getChecked(row + 2*orientation, col)) {
		        	result += addOption(row +2*orientation, col);
		        }}
			
			//used to check enPassant
			for(int c = col-1; c <= col+1; c+=2)
			{
				if(validIndex(row, c) && board[row][c].isOpposite(board[row][col].getPiece())==1) {
					int enPassant = board[row][c].getPiece().getEnPassant();
					Piece saved = board[row][c].getPiece();
					board[row][c].setPiece(null);
					if(this.getTurn() - enPassant == 1 && !getChecked(row+orientation, c))
						result += addOption(row+orientation, c);
					board[row][c].setPiece(saved);
			}}
				
				break;
				
			//possible moves of the knight
			case("Knight"):
				
				//uses rounded angles to calculate the possible positions of the knight
				for(double c = 0.0; c <= 3.0*Math.PI/2.0; c+= 1.0/2.0*Math.PI) {
					for(int mult = 1; mult <= 2; mult++) {
						int checkX = (int) Math.round(row+2*(Math.cos(c+mult*1.0/6.0*Math.PI)));
						int checkY = (int) Math.round(col+2*(Math.sin(c+mult*1.0/6.0*Math.PI)));

							if(validIndex(checkX, checkY) && !getChecked(checkX, checkY)) 
								result += addOption(checkX, checkY);
							}}
			
				break;	
			
			//checks the diagonals of the bishop
			case("Bishop"):
				
				//uses pi/4 + k*pi/4 angles to check the diagonals of the bishops 
				for(double c = Math.PI/4.0; c <= 7.0*Math.PI/4.0; c+= 0.5*Math.PI) {
					boolean unblock = true;
					for(int i = 1; i<=7 && unblock; i++) {
						int checkX =  (row+i* ((int)(2*Math.cos(c))));
						int checkY = (col+i*((int)(2*Math.sin(c))));
						
						if(validIndex(checkX,checkY)) {
						
							int evaluate = board[checkX][checkY].isOpposite(board[row][col].getPiece());

							if(validIndex(checkX, checkY) && !getChecked(checkX, checkY)) {
								result += addOption(checkX, checkY);
								if(evaluate == 1)
									unblock = false;
							}}
						else
							unblock = false;
						}}
			
				break;
			
			//checks the rows and column of the rook
			case("Rook"):
				
				//uses k*pi/4 angles to check around the rook
				for(double c = 0.0; c <= 3*Math.PI/2.0; c+= 0.5*Math.PI) {
					boolean unblock = true;
					for(int i = 1; i<=7 && unblock; i++) {
						int checkX = (row+i*((int)Math.sin(c)));
						int checkY = (col+i*((int)Math.cos(c)));
						
						if(validIndex(checkX,checkY)) {
						
							int evaluate = board[checkX][checkY].isOpposite(board[row][col].getPiece());

							if(validIndex(checkX, checkY) && !getChecked(checkX, checkY)) {
								result += addOption(checkX, checkY);
								if(evaluate == 1)
									unblock = false;
							}}
						else
							unblock = false;
					}}
			
				break;
			
			//use recursion to reuse the bishop and rook code
			case("Queen"):
				Piece savedQueen = board[row][col].getPiece();
				boolean isWhite = savedQueen instanceof WhitePiece;
			
				if(isWhite) 
				{
					board[row][col].setPiece(new WhitePiece("Bishop"));
					result += getValidMoves(row, col);
					board[row][col].setPiece(new WhitePiece("Rook"));
					result += getValidMoves(row,col);
				}
				else 
				{
					board[row][col].setPiece(new BlackPiece("Bishop"));
					result += getValidMoves(row, col);
					board[row][col].setPiece(new BlackPiece("Rook"));
					result += getValidMoves(row,col);
				}
				
				board[row][col].setPiece(savedQueen);
				break;
				
			//checks the space around the king piece
			case("King"):
				//checks the space around the king piece
				
				for(int r = row-1; r <= row+1; r++)
					for(int c = col-1; c<= col+1; c++) {
						if(validIndex(r, c) && (r != row || c != col) && !getChecked(r, c)) {
							result += addOption(r, c);
							}}
			
				boolean blocked = false;
			
				//checks the space around it for castling
				if(!board[row][col].getMoved()) {
					for(int c = 5; c<=7 && !blocked;c++) 
						if(c == 7 && !board[row][c].getMoved() && !getChecked(row, 6))
							result += row+","+6+" ";
						else if(!board[row][c].isEmpty())
							blocked = true;
					
					blocked = false;
					
					for(int c = 3; c >= 0 && !blocked; c--)
						if(c == 0 && !board[row][c].getMoved() && !getChecked(row, 2))
							result += row+","+2+" ";
						else if(!board[row][c].isEmpty())
							blocked = true;
				}
			
				break;
		}}
		return result;
	}
	
	//see if the move will see if the king is going to be checked
	boolean getChecked(int row, int col) throws ArrayIndexOutOfBoundsException
	{
		if(row >= 0 && row <= 7 && col >= 0 && col <= 7
				&& row1 >= 0 && row1 <= 7 && col1 >= 0 && col1 <= 7) {
			boolean result = false;
		
			Piece savedPiece = board[row][col].getPiece();
			
			int kingRow = whiteKingRow;
			int kingCol = whiteKingCol;
		
			if(turn %2 == 0) {
				kingRow = blackKingRow;
				kingCol = blackKingCol;
			}
			
			board[row][col].setPiece(board[row1][col1].getPiece());
			board[row1][col1].setPiece(null);
			
			if(board[row][col].equalType("King")) {
					kingRow = row;
					kingCol = col;
				}
			
			result = check(kingRow, kingCol, turn %2 ==1); 
		
			board[row1][col1].setPiece(board[row][col].getPiece());
			board[row][col].setPiece(savedPiece);
			
			return result;
		}
		else return false;
	}
	
	//moves the piece if the requirements are met
	String move(int inrow, int incol) {
		
		if(row1 == -1 && col1 == -1 && (validIndex(inrow, incol)))
		{
		//checks if the move is valid by seeing if it is compatible with the turn counter	
			if((board[inrow][incol].isWhite() && turn % 2 ==1) ||
					(board[inrow][incol].isBlack() && turn %2 == 0)) {
				
				row1= inrow;
				col1 = incol;

				return getValidMoves(row1, col1);
			}
		}
		else if(validIndex(inrow, incol)){
			row2 = inrow;
			col2 = incol;

		//gets the list of valid moves
		String validOptions = getValidMoves(row1, col1);
		
		//what to do it it is a valid move
		if(isValid(validOptions)) {
			
			int orientation = 1;
			
			if(this.getTurn() %2 == 1)
				orientation = -1;
			
			//if the piece is a pawn, hasn't been moved, and moved two spaces,
			//it can be enPassanted
			if(board[row1][col1].equalType("Pawn") && 
					!board[row1][col1].getPiece().getMoved() && 
					Math.abs(row1 - row2) == 2) {
				board[row1][col1].getPiece().setEnPassant(this.getTurn());
			}
			
			//corrects the rook's movement in castling
			if(!board[row1][col1].getMoved() && 
					board[row1][col1].equalType("King") 
					&& Math.abs(col2-col1)>1) {
				if(col2 == 2) {
					board[row1][3].setPiece(board[row1][0].getPiece());
					board[row1][0].setPiece(null);
				}
				else if(col2 == 6) {
					board[row1][5].setPiece(board[row1][7].getPiece());
					board[row1][7].setPiece(null);
				}
			}
			
			//sets the pieces
			board[row1][col1].getPiece().setMoved("true");
			board[row2][col2].addPiece(board[row1][col1].getPiece(), turn);
			board[row1][col1].setPiece(null);
			
			Tile enPassantMoved = board[row2][col2];
			Tile enPassantRemoved = null;
			
			if(row2-orientation > -1 && row2-orientation < 8 && col2 > -1 && col2 < 8) 
				enPassantRemoved = board[row2-orientation][col2];
			
			//corrects the movement of the en Passant
			if(enPassantRemoved != null && 
					enPassantRemoved.isOpposite(enPassantMoved.getPiece()) == 1 &&
					enPassantMoved.equalType("Pawn") && 
					enPassantRemoved.equalType("Pawn")) {
				enPassantRemoved.setPiece(null);
				updateList(row2-orientation, col2);
			}
			
			//if the piece was a king, saves the king's coordinates
			if(board[row2][col2].equalType("King"))
				if(turn%2==0) {
					blackKingRow = row2;
					blackKingCol = col2;
					
					
				}
				else {
					whiteKingRow = row2;
					whiteKingCol = col2;
				}
			
			updateList(-1,-1);
			
			turn++;
		}
		//if the second tile is valid, then it erases the first coord
		row1 = -1;
		col1 = -1;
		row2 = -1;
		col2 = -1;
		return "";
	}
		//activated in situations where the user needs to select another
		//piece of their same color
		else if((turn % 2 == 1 && board[inrow][incol].isWhite())
				|| (turn % 2 == 0 && board[inrow][incol].isBlack())){
			col1 = incol;
			row1 = inrow;
			return getValidMoves(row1, col1);
		}	
		return checkVictory();
		}
	//what happens if one side has ran out of moves
		
	String checkVictory() {
	String sample = allValidMoves();
		
	if(sample.equals("")) {
		
		if(turn %2 == 1) {
			if(check(whiteKingRow,whiteKingCol, true)) { //
				turn--;
				return"Black Victory";}
			else {
				turn--;
				return "Stalemate";}
		}
		else {
			if(check(blackKingRow, blackKingCol, false)) { //
				turn--;
				return "White Victory";}
			else {
				turn--;
				return "Stalemate";}
	}} //if only the king piece or a king and a bishop/knight are left, that counts
	//as a stalemate
	else if(blackPieces.size() < 3 && whitePieces.size() < 3) {
		
		String blackType = "";
		String whiteType = "";
		
		if(blackPieces.size() == 2 && whitePieces.size() == 2) {
			for(int index = 0; index < 2; index++) {
				
				blackType += blackPieces.get(index).getType();
				whiteType += whitePieces.get(index).getType();
				
		}
			if(blackType.contains("Bishop Knight") && whiteType.contains("Bishop Knight"))
				return "Stalemate";
		}
		else
			return "Stalemate";
	}
	
	return "";
	}
		
	//checks if the tile is valid for moving
	private boolean isValid(String input) {
		String tested = row2+","+col2;
		return input.contains(tested);
	}
	
	//prints the state of the board in the console
	//only used for debugging and testing
	void printBoard() {
		for(int row = 0; row < 8; row++) {
			for(int col = 0; col < 8; col++) {
				if(board[row][col].getPiece() != null)
				System.out.print(board[row][col].getType()+" ");
				else
					System.out.print("[ ]");
			}
		System.out.println("/");
		}
		System.out.println();
	}
}

