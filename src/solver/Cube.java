package solver;

class Cube {
	
	Corner[] corners;
	Edge[] edges;
	
	//initialize instance of a cube
	public Cube() {
		corners = new Corner[] {new Corner("WGR"),
				  				new Corner("WBR"),
				  				new Corner("WBO"),
				  				new Corner("WGO"),
				  				new Corner("YGR"),
				  				new Corner("YBR"),
				  				new Corner("YBO"),
				  				new Corner("YGO")};
		
		edges = new Edge[] {new Edge("WG"),
							new Edge("WR"),
							new Edge("WB"),
							new Edge("WO"),
							new Edge("YG"),
							new Edge("YR"),
							new Edge("YB"),
							new Edge("YO"),
							new Edge("GR"),
							new Edge("GO"),
							new Edge("BR"),
							new Edge("BL")};
	}
	
	//returns name of edge in given position
	public Edge getEdge(String pos) {
		for(Edge e: this.edges) {
			if(e.getCurr_pos().equals(pos)){
				return e;
			}
		}
		return new Edge("");
	}
	
	public Corner getCorner(String pos) {
		for(Corner c: this.corners) {
			if(c.getCurr_pos().equals(pos)) {
				return c;
			}
		}
		return new Corner("");
	}
	
	//applies one of 18 possible turns to the cube
	public void applyTurn(String turn) throws Exception {
		switch(turn.length()) {
		case 1:
			
			
		case 2:
			if(turn.charAt(1) == '2') {
				this.applyTurn(turn.substring(0,1));
				this.applyTurn(turn.substring(0,1));
			}
			else if(turn.charAt(1) == '\'') {
				this.applyTurn(turn.substring(0,1));
				this.applyTurn(turn.substring(0,1));
				this.applyTurn(turn.substring(0,1));
			}
			
		default:
			throw new Exception("The following turn is not possible: " + turn);
		}
	}
	
	//basic idea of a right turn
	public void R() {
		//new permutations
		//->corners
		Corner UFR = getCorner("UFR");
		Corner UBR = getCorner("UBR");
		Corner DFR = getCorner("DFR");
		Corner DBR = getCorner("DBR");
		
		UFR.setCurr_pos("UBR");
		UBR.setCurr_pos("DBR");
		DBR.setCurr_pos("DFR");
		DFR.setCurr_pos("UFR");
		
		//new orientations
		UFR.setOrientation(UFR.getOrientation() +1 % 3);
		UBR.setOrientation(UBR.getOrientation() +2 % 3);
		DBR.setOrientation(DBR.getOrientation() +1 % 3);
		DFR.setOrientation(DFR.getOrientation() +2 % 3);
	}

}
