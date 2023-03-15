package solver;

class Cube {
	
	Corner[] corners;
	Edge[] edges;
	Corner WGR, WBR, WBO, WGO, YGR, YBR, YBO, YGO;
	Edge WG, WR, WB, WO, YG, YR, YB, YO, GR, GO, BR, BO;
	//initialize instance of a cube
	public Cube() {
		
		WGR = new Corner("WGR");
		WBR = new Corner("WBR");
		WBO = new Corner("WBO");
		WGO = new Corner("WGO");
		YGR = new Corner("YGR");
		YBR = new Corner("YBR");
		YBO = new Corner("YBO");
		YGO = new Corner("YGO");
		corners = new Corner[] {WGR, WBR, WBO, WGO, YGR, YBR, YBO, YGO};
		
		WG = new Edge("WG");
		WR = new Edge("WR");
		WB = new Edge("WB");
		WO = new Edge("WO");
		YG = new Edge("YG");
		YR = new Edge("YR");
		YB = new Edge("YB");
		YO = new Edge("YO");
		GR = new Edge("GR");
		GO = new Edge("GO");
		BR = new Edge("BR");
		BO = new Edge("BO");
		edges = new Edge[] {WG, WR, WB, WO, YG, YR, YB, YO, GR, GO, BR, BO};
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
	
	public void scramble(String[] scramble) {
		for(String s: scramble) {
			try {
				this.applyTurn(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	//applies one of 18 possible turns to the cube
	public void applyTurn(String turn) throws Exception {
		switch(turn.length()) {
		case 1:
			//handles corners
			Corner[] face_corners = this.getFaceCorners(turn.substring(0,1));
			
			//permute corners
			String buf = face_corners[0].getCurr_pos();
			for(int i = 0; i < 3; i++) {
				face_corners[i].setCurr_pos(face_corners[i+1].getCurr_pos());
			}
			face_corners[3].setCurr_pos(buf);
			
			//new orientation easy because of array structure
			if(turn.equals("U") || turn.equals("D")) {
				//skip
			}
			else {
				for(int i = 0; i<4; i++) {
					if(i % 2 == 0) {
						face_corners[i].setOrientation((face_corners[i].getOrientation() + 1) % 3);
					}
					else {
						face_corners[i].setOrientation((face_corners[i].getOrientation() + 2) % 3);
					}
				}
			}
			
			//handles edges
			Edge[] face_edges = this.getFaceEdges(turn.substring(0,1));
			
			//permute edges
			buf = face_edges[0].getCurr_pos();
			for(int i = 0; i < 3; i++) {
				face_edges[i].setCurr_pos(face_edges[i+1].getCurr_pos());
			}
			face_edges[3].setCurr_pos(buf);
			
			//new orientation only for F and B moves
			if(turn.equals("F") || turn.equals("B")) {
				for(int i = 0; i < 4; i++) {
					face_edges[i].setOrientation((face_edges[i].getOrientation() + 1) % 2);
				}
			}
			break;
			
		case 2:
			if(turn.charAt(1) == '2') {
				this.applyTurn(turn.substring(0,1));
				this.applyTurn(turn.substring(0,1));
				break;
			}
			else if(turn.charAt(1) == '\'') {
				this.applyTurn(turn.substring(0,1));
				this.applyTurn(turn.substring(0,1));
				this.applyTurn(turn.substring(0,1));
				break;
			}
			
		default:
			throw new Exception("The following turn is not possible: " + turn);
		}
	}
	
	//this function returns the corners of the given face in the order that they will be cyclicly exchanged. 
		//Except for U and D, all turns do a clockwise twist on the first and third corner and an anti-clockwise twist on 
		//the second and fourth. This is important for the applyTurn function.
		public Corner[] getFaceCorners(String face) {
			Corner[] side;
			switch(face) {
			case "R":
				side = new Corner[] {this.getCorner("UFR"),this.getCorner("UBR"),this.getCorner("DBR"),this.getCorner("DFR")};
				break;
			case "L":
				side = new Corner[] {this.getCorner("UBL"),this.getCorner("UFL"),this.getCorner("DFL"),this.getCorner("DBL")};
				break;
			case "U":
				side = new Corner[] {this.getCorner("UFR"),this.getCorner("UFL"),this.getCorner("UBL"),this.getCorner("UBR")};
				break;
			case "D":
				side = new Corner[] {this.getCorner("DFR"),this.getCorner("DBR"),this.getCorner("DBL"),this.getCorner("DFL")};
				break;
			case "F":
				side = new Corner[] {this.getCorner("DFR"),this.getCorner("DFL"),this.getCorner("UFL"),this.getCorner("UFR")};
				break;
			case "B":
				side = new Corner[] {this.getCorner("UBR"),this.getCorner("UBL"),this.getCorner("DBL"),this.getCorner("DBR")};
				break;
			default:
				throw new Error("The following turn is not possible: " + face);
			}
			return side;
		}
		
		public Edge[] getFaceEdges(String face) {
			Edge[] side;
			switch(face) {
			case "R":
				side = new Edge[] {this.getEdge("UR"),this.getEdge("BR"),this.getEdge("DR"),this.getEdge("FR")};
				break;
			case "L":
				side = new Edge[] {this.getEdge("UL"),this.getEdge("FL"),this.getEdge("DL"),this.getEdge("BL")};
				break;
			case "U":
				side = new Edge[] {this.getEdge("UR"),this.getEdge("UF"),this.getEdge("UL"),this.getEdge("UB")};
				break;
			case "D":
				side = new Edge[] {this.getEdge("DR"),this.getEdge("DB"),this.getEdge("DL"),this.getEdge("DF")};
				break;
			case "F":
				side = new Edge[] {this.getEdge("UF"),this.getEdge("FR"),this.getEdge("DF"),this.getEdge("FL")};
				break;
			case "B":
				side = new Edge[] {this.getEdge("UB"),this.getEdge("BL"),this.getEdge("DB"),this.getEdge("BR")};
				break;
			default:
				throw new Error("The following turn is not possible: " + face);
			}
			return side;
		}
	
}
