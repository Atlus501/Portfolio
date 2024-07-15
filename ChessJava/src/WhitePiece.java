class WhitePiece extends Piece{
	
	WhitePiece(String name){
		this.type = name;
	}
	
	WhitePiece(String name, String move){
	 this.type = name;
	 this.setMoved(move);
	}
}
