class Piece {
	protected String type = "";
	protected boolean moved = false;
	protected int enPassant = -1;
	
 Piece(){}
 
 //returns the type of the piece
	String getType() {
		return this.type;
	}
	
	void setType(String ty){
		this.type = ty;
	}
	
	//sets the moved variable
	void setMoved(String processed) {
		if(processed.equals("true"))
			this.moved = true;
		else
			this.moved = false;
	}
	
	//returns the move variable
	boolean getMoved() {
		return this.moved;
	}
	
	void setEnPassant(int able) {
		this.enPassant = able;
	}
	
	int getEnPassant() {
		return this.enPassant;
	}
	
}
