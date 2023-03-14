package solver;

import java.util.HashMap;
import java.util.Map;

/*This class models an edge of a Rubik's Cube. An edge is defined by 
-its name, that is the abbreviation of the two colors on this piece
-the current position, initially this is a solved position
-the position where it needs to be to be solved
-its orientation. If an edge is oriented then it is assigned orientation 0 else 1*/

class Edge {
	
	String name;
	String sol_pos;
	String curr_pos;
	int orientation;
	
	public Edge(String name) {
		this.name = name;
		//map colors of the piece to position as solved
		Map<Character, Character> map = new HashMap<Character, Character>();
		map.put('G', 'F');
		map.put('B', 'B');
		map.put('R', 'R');
		map.put('L', 'O');
		map.put('W', 'U');
		map.put('Y', 'D');
		sol_pos = calcPos(name, map);
		curr_pos = sol_pos;
		orientation = 0;
	}
	
	public String calcPos(String name, Map<Character, Character> map) {
		String pos = "";
		for(int i = 0; i<name.length(); i++) {
			pos += map.get(name.charAt(i));
		}
		return pos;
	}
	
	public String getCurr_pos() {
		return this.curr_pos;
	}
	
	public void setCurr_pos(String new_pos) {
		this.curr_pos = new_pos;
	}
	
	public int getOrientation() {
		return this.getOrientation();
	}
	
	public void setOrientation(int new_orientation) {
		this.orientation = new_orientation;
	}

}
