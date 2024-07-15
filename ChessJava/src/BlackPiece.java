class BlackPiece extends Piece{

	BlackPiece(String name){
		this.type = name;
	}
	
	BlackPiece(String name, String move){
		 this.type = name;
		 this.setMoved(move);
		}
	
}
