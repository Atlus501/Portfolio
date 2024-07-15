class Tile {

	private BlackPiece black = null;
	private WhitePiece white = null;
	private int row = 0;
	private int col = 0;
	
	//sets an empty tile
	Tile(int r, int c){
		row = r;
		col = c;
	}
	
	//sets a tile with a blackpiece in it
	Tile(BlackPiece bl, int r, int c){
		black = bl;
		row = r;
		col = c;
	}
	
	//sets a tile with a whitepiece in it
	Tile(WhitePiece wh, int r, int c){
		white = wh;
		row = r;
		col = c;
	}
	
	int getRow() {return this.row;}
	int getCol() {return this.col;}
	
	//if necessary, it sets the piece of that Tile
	void setPiece(Piece p) {
		if(p instanceof WhitePiece) {
			white = (WhitePiece) p;
			black = null;
			}
		else if(p instanceof BlackPiece) {
			black = (BlackPiece) p;
			white = null;
		}
		else if(p == null) {
			white = null;
			black = null;
		}
	}
	
	boolean getMoved() {
		return this.getPiece().getMoved();
	}
	
	boolean equalType(String input) {
		return this.getType().equals(input);
	}
	
	//evaluates the pieces of the tile based on the existing pieces and turn
	//if the operation is successful, it returns true; and, the opposite is true
	boolean addPiece(Piece p, int turn) {
		if(turn % 2 == 1 && this.white == null && p instanceof WhitePiece) {
			this.black = null;
			this.white = (WhitePiece) p;
			return true;
		}
		else if(turn % 2 == 0 && this.black == null && p instanceof BlackPiece) {
			this.white = null;
			this.black = (BlackPiece) p;
			return true;
		}
		return false;
	}
	
	//returns if tile is empty
	boolean isEmpty() {
		return white==null && black == null;
	}
	
	//returns if there is opposite colored piece on the tile
	int isOpposite(Piece p) {
		if(this.isEmpty() || p == null)
			return 0;
		else if(p instanceof WhitePiece)
			if(white != null)
			    return -1;
			else
				return 1;
		else
			if(black != null)
				return -1;
			else
				return 1;
	}
	
	//gets the type of the Tile's piece
	String getType() {
		if(this.isEmpty()) return "";
		else
			return this.getPiece().getType();
	}
	
	//returns the piece that is in the Tile
	Piece getPiece() {
		if(this.white != null)
			return this.white;
		else if(this.black != null)
			return this.black;
		else return null;
	}
	
	//returns if the piece is white
	boolean isWhite() {
		return white != null;
	}
	
	//returns if the piece is black
	boolean isBlack() {
		return black != null;
	}
	
}
