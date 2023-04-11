package solver;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

/*Solver class. Constructs the GUI to enter scramble and display solution. 
Contains necessary functions to check for validity of scramble and makes calls to cube object.
Attempts to solve EO, EO-Line and the F2L part of blockbuilding optimally. Each step is done in the same way with a 
pruning algorithm that works on a tree of manageable size since the permutation class is iteratively reduced through
subgroups of the cube group*/

class Solver implements ActionListener{
	
	JFrame frame;
	JLabel label;
	JButton button;
	JTextField text;
	String scramble;
	Cube cube;
	Set<String> moves = new HashSet<>(Arrays.asList("R", "R\'", "R2",
			   "L", "L\'", "L2",
			   "U", "U\'", "U2",
			   "D", "D\'", "D2",
			   "F", "F\'", "F2",
			   "B", "B\'", "B2"));
	String solution;
	
	//construct solver
	public Solver(){
		frame = new JFrame("scramble input");
		text = new JTextField();
		text.setEditable(true);
		label = new JLabel("please input your scramble in standard WCA notation");
		button = new JButton("solve!");
		button.addActionListener(this);
		solution = "";
		frame.add(text, BorderLayout.CENTER);
		frame.add(button, BorderLayout.SOUTH);
		frame.add(label, BorderLayout.NORTH);
		frame.setSize(500,100);
		frame.setVisible(true);
	}

	@Override
	//handles input using text-field and button
	public void actionPerformed(ActionEvent e) {
		scramble = null;
		if(e.getSource() == this.button) {
			scramble = this.text.getText();
		}	
		
		try {
			solve();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	//start solving process
	public void solve() throws Exception {
		while(scramble == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//######################## check if scramble is feasible #########################################
		if(checkScramble(scramble)) {
			this.label.setText("calculating...");
			this.button.setEnabled(false);
		}
		else {
			this.label.setText("invalid scramble");
		}//###############################################################################################
		
		
		//split scramble in string array
		String[] scramble_array = scramble.split("\\s+");
		//temporary cube that can be used to solve each step and generated from scratch
		cube = new Cube();
		cube.scramble(scramble_array);
				
		//######################## solve eo ##############################################################
		//max_depth = 7: every eo can be solved in at most 7 turns
		//searches eo for tmp_cube, once the best is found, it is applied to cube
		//first, eo is a suboptimal eo, that will be greedily improved
		String[] eo = EO("", " ", " ", 0, 7).split("\\s+");
		//search for optimal eo
		String tmp = "";
		while(true && eo.length > 1) {
			cube = new Cube();
			cube.scramble(scramble_array);
			tmp = EO("", " ", " ", 0, eo.length-1);
			if(tmp.equals("fail")) {
				break;
			}
			eo = tmp.split("\\s+");
		}
		for(String s: eo) {
			solution += s + " ";
		}
		//apply eo to cube
		cube.scramble(eo);;
		solution += "\t //EO <br/>";		
		//##############################################################################################
		//in this stage, only double turns of front and back faces are needed
		moves.remove("F");
		moves.remove("F\'");
		moves.remove("B");
		moves.remove("B\'");
		
		//################################# EOLine #####################################################
		//solving EOLine in fewest possible moves
		//EOLine with max_depth 5 because the edges can be solved in at most 5 turns
		String[] EOLine = EOLine("", " ", " ", 0, 5).split("\\s+");
		//greedily search for better solutions
		while(EOLine.length > 1) {
			cube = new Cube();
			cube.scramble(scramble_array);
			cube.scramble(eo);
			tmp = EOLine("", " ", " ", 0, EOLine.length-1);
			if(tmp.equals("fail")) {
				break;
			}
			EOLine = tmp.split("\\s+");
		}
		for(String s: EOLine) {
			solution += s + " ";
		}
		//apply EOLine to cube
		cube.scramble(EOLine);
		solution += "\t //EOLine <br/>";
		//##############################################################################################
		
		//the following moves are not needed anymore
		moves.remove("F2");
		moves.remove("B2");
		moves.remove("D");
		moves.remove("D2");
		moves.remove("D\'");
		
		//############################# Block-Building #################################################
		/*Idea for left block:
		 * since all edges are oriented, the number of F2L cases could be reduced to 12 if done in a smart way
		 * thus, first solve the orange-yellow edge piece (at that point cross-1) and make sure neither edge nor corner
		 * of relevant pair are in the right layer. Then, the pair can mathematically be solved with only 
		 * left and up face turns. Any pair can be solved in 12 moves at most, which gives a maximum search depth of 2*12^3
		 * or roughly 3500 possible paths to search for the optimal solution*/
		String left_block = "";
		
		//step 1: solve YO
		String step1 = Yo("", " ", " ", 0 ,3);
		String[] Yo_array = step1.split("\\s+");
		//greedily improve step 1:
		while(Yo_array.length > 1) {
			cube = new Cube();
			cube.scramble(scramble_array);
			cube.scramble(eo);
			cube.scramble(EOLine);
			tmp = Yo("", " ", " ", 0, Yo_array.length - 1);
			if(tmp.equals("fail")) {
				break;
			}
			step1 = tmp;
			Yo_array = tmp.split("\\s+");
		}
		left_block += step1;
		cube.scramble(Yo_array);

		//step 2: move edge and corner away from right layer, can be done in at most 2 turns
		String step2 = step2_4(2);
		left_block += step2;
		
		//step 3: solve back left F2L pair, only left and up turn needed, at most 12 turns
		String step3 = step3_5(3, "", " ", 0, 12);
		String[] step3_array = step3.split("\\s+");
		while(step3_array.length > 1) {
			cube = new Cube();
			cube.scramble(scramble_array);
			cube.scramble(eo);
			cube.scramble(EOLine);
			cube.scramble(left_block.split("\\s+"));
			tmp = step3_5(3, "", " ", 0, step3_array.length-1);
			if(tmp.equals("fail")) {
				break;
			}
			step3 = tmp;
			step3_array = step3.split("\\s+");
		}
		left_block += step3;
		cube.scramble(step3_array);

		//step 4: move edge and corner away from right layer, can be done in at most 2 turns
		String step4 = step2_4(4);
		left_block += step4;
		
		//step 5: solve FL F2L pair in at most 12 turns
		String step5 = step3_5(5, "", " ", 0, 12);
		String[] step5_array = step5.split("\\s+");
		while(step5_array.length > 1) {
			cube = new Cube();
			cube.scramble(scramble_array);
			cube.scramble(eo);
			cube.scramble(EOLine);
			cube.scramble(left_block.split("\\s+"));
			tmp = step3_5(5, "", " ", 0, step5_array.length-1);
			if(tmp.equals("fail")) {
				break;
			}
			step5 = tmp;
			step5_array = step5.split("\\s+");
		}
		cube.scramble(step5_array);
		left_block += step5;
		
		//this might include consecutive turns of same face, remove with Simplify
		left_block = Simplify(left_block);
		solution += left_block + "\t //Left 2x2x3 <br/>";
		
		moves.remove("L");
		moves.remove("L2");
		moves.remove("L\'");
		//################# Corner Permutation #################################
		//step 1: permute D layer corners to check U layer permutation
		String CP = "";
		String CP1 = CP1("", " ", 0, 5);
		String[] CP1array = CP1.split("\\s+");
		
		while(CP1array.length > 1) {
			cube = new Cube();
			cube.scramble(scramble_array);
			cube.scramble(eo);
			cube.scramble(EOLine);
			cube.scramble(left_block.split("\\s+"));
			tmp = CP1("", " ", 0, CP1array.length-1);
			if(tmp.equals("fail")) {
				break;
			}
			CP1 = tmp;
			CP1array = CP1.split("\\s+");
		}
		
		cube.scramble(CP1array);
		CP += CP1;
		System.out.println(CP);
		
		//step 2: solve U layer permutation. If adjacent swap, use A perm, for diagonal swap find optimal solution:
		//R U' R' U' F2 D' B L B' D F2 (11)
		//U R B2 L2 D L D' L B2 R' (10)
		displaySolution();
		return;
	}
	
	//check if scramble is feasible
	public boolean checkScramble(String s) {
		//sets with valid turns
		Set<Character> valid_first = Set.of('R', 'L', 'U', 'D', 'F', 'B');
		Set<Character> valid_modifier = Set.of('2', '\'');
		s = s.replaceAll("\\s", "");
		int i = 0;
		//needs to check if turn is one or two characters long (e.g. R or R')
		while(i<s.length()-1) {
			if(valid_first.contains(s.charAt(i))) {
				//only one character for turn
				if(valid_modifier.contains(s.charAt(i+1))) {
					i += 2;
					continue;
				}
				//two characters
				else if(valid_first.contains(s.charAt(i+1))) {
					i += 1;
					continue;
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}
		return true;
	}
	
	public String EO(String turns, String previous_move, String prevprev, int num_turns, int max_depth) {
		//construct list of wrong edges
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for(Edge e: this.cube.edges) {
			if(e.getOrientation() == 1) {
				edges.add(e);
			}
		}
		//no wrong edges, then eo solved, return
		if(edges.isEmpty()) {
			return turns;
		}
		//already used 7 turns but not done, return fail
		if(num_turns == max_depth) {
			return "fail";
		}
		//all edges on front face are wrong, turn F, recursive step
		if(this.cube.getEdge("UF").getOrientation() == 1 &&
		   this.cube.getEdge("FR").getOrientation() == 1 &&
		   this.cube.getEdge("DF").getOrientation() == 1 &&
		   this.cube.getEdge("FL").getOrientation() == 1) {
			this.cube.applyTurn("F");
			String inter_res =  EO(turns + "F ", "F", previous_move, num_turns+1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.cube.UndoTurn("F");
		}
		//all edges on back face are wrong, turn B, recursive step
		else if(this.cube.getEdge("UB").getOrientation() == 1 &&
				this.cube.getEdge("BR").getOrientation() == 1 &&
				this.cube.getEdge("DB").getOrientation() == 1 &&
				this.cube.getEdge("BL").getOrientation() == 1) {
			this.cube.applyTurn("B");
			String inter_res = EO(turns + "B ", "B", previous_move,num_turns+1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.cube.UndoTurn("B");
		}
		//loop through allowed turns
		for(String move: moves) {
			//if move is modification of previous one, or turns opposite side from previous one but the same as the one before, skip 
			if((move.charAt(0) == previous_move.charAt(0)) || (move.charAt(0) == prevprev.charAt(0) && isOpposite(move.charAt(0), previous_move.charAt(0)))) {
				continue;
			}
			//turning F when all edges on F are correct is unnecessary
			if(move.charAt(0) == 'F' && this.cube.getEdge("UF").getOrientation() == 0 &&
									 	this.cube.getEdge("FR").getOrientation() == 0 &&
									 	this.cube.getEdge("DF").getOrientation() == 0 &&
									 	this.cube.getEdge("FL").getOrientation() == 0) {
				continue;
			}
			//turning B when all edges on B are correct is unnecessary
			if(move.charAt(0) == 'B' && this.cube.getEdge("UB").getOrientation() == 0 &&
				 						this.cube.getEdge("BR").getOrientation() == 0 &&
				 						this.cube.getEdge("DB").getOrientation() == 0 &&
				 						this.cube.getEdge("BL").getOrientation() == 0) {
				continue;
			}
			this.cube.applyTurn(move);
			String inter_res =  EO(turns + move + " ", move, previous_move, num_turns +1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.cube.UndoTurn(move);
		}
		return "fail";
	}
	
	public boolean isOpposite(char mv1, char mv2) {
		if(mv2 == ' ') {
			return true;
		}
		switch(mv1) {
		case 'R':
			return mv2 == 'L';
		case 'L':
			return mv2 == 'R';
		case 'U':
			return mv2 == 'D';
		case 'D':
			return mv2 == 'U';
		case 'F':
			return mv2 == 'B';
		case 'B':
			return mv2 == 'F';
		default:
			return true;
		}
	}
	
	public String EOLine(String EOLine, String previous_move, String prevprev, int num_turns, int max_depth) {
		//the two edges that need to be permuted
		String blue_pos = this.cube.YB.getCurr_pos();
		String green_pos = this.cube.YG.getCurr_pos();
		
		if(blue_pos.equals("DB") && green_pos.equals("DF")) {
			return EOLine;
		}
		
		if(num_turns == max_depth) {
			return "fail";
		}
		
		for(String move: moves) {
			//if move is modification of previous one, or turns opposite side from previous one but the same as the one before, skip 
			if((move.charAt(0) == previous_move.charAt(0)) || (move.charAt(0) == prevprev.charAt(0) && isOpposite(move.charAt(0), previous_move.charAt(0)))) {
				continue;
			}
			//if move doesn't affect any of the two important pieces, skip
			if(blue_pos.indexOf(move.charAt(0)) == -1 && green_pos.indexOf(move.charAt(0)) == -1) {
				continue;
			}
			this.cube.applyTurn(move);
			String inter_res = EOLine(EOLine + move + " ", move, previous_move, num_turns+1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.cube.UndoTurn(move);
		}
		return "fail";
	}
	
	//solves yellow-orange edge piece before starting with left F2L pairs
	public String Yo(String Yo, String previous_move, String prevprev, int num_turns, int max_depth) {
		String pos = cube.YO.getCurr_pos();
		if(pos.equals("DL")) {
			return Yo;
		}
		if(num_turns == max_depth) {
			return "fail";
		}
		
		for(String move: moves) {
			//if move is modification of previous one, or turns opposite side from previous one but the same as the one before, skip 
			if((move.charAt(0) == previous_move.charAt(0)) || (move.charAt(0) == prevprev.charAt(0) && isOpposite(move.charAt(0), previous_move.charAt(0)))) {
				continue;
			}
			
			//if move does not affect edge, skip
			if(pos.indexOf(move.charAt(0)) == -1) {
				continue;
			}
			
			this.cube.applyTurn(move);
			String inter_res = Yo(Yo + move + " ", move, previous_move, num_turns+1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.cube.UndoTurn(move);
			
		}
		return "fail";
	}
	
	//move important F2L pieces away from R layer, then only L and U turns needed (at most 2 turns)
	public String step2_4(int step) {
		String stepp = "";
		for(int i = 0; i<4; i++) {
			String c_pos = "";
			String e_pos = "";
			if(step == 2) {
				c_pos = cube.YBO.getCurr_pos();
				e_pos = cube.BO.getCurr_pos();
			}
			else {
				c_pos = cube.YGO.getCurr_pos();
				e_pos = cube.GO.getCurr_pos();
			}
			if((c_pos.indexOf("R") == -1 || c_pos.equals("UFR") || c_pos.equals("UBR")) && (e_pos.indexOf("R") == -1 || e_pos.equals("UR"))) {
				break;
			}
			cube.applyTurn("R");
			stepp += "R ";
		}
		//add a blank space to solution and simplify (e.g. R R to R2)
		switch(stepp.length()) {
			case 2:
				return stepp;
			case 4:
				return "R2 ";
			case 6:
				return "R\' ";
			case 8:
				cube.applyTurn("U2");
				return "U2 " + step2_4(4) + " ";
		}
		return stepp;
	}
	
	//solves BL and FL F2L pairs, needs at most 12 turns
	public String step3_5(int step, String stepp, String previous_move, int num_turns, int max_depth) {
		String c_pos = "";
		String e_pos = "";
		int c_or = 0;
		//only L and U turns needed
		String[] pos_moves = new String[] {"L", "L2", "L\'", "U", "U2", "U\'"};

		//BL pair
		if(step == 3) {
			c_pos = cube.YBO.getCurr_pos();
			e_pos = cube.BO.getCurr_pos(); 
			c_or = cube.YBO.getOrientation();
			if(c_pos.equals("DBL") && e_pos.equals("BL") && c_or == 0 && cube.YO.getCurr_pos().equals("DL")) {
				return stepp;
			}
		}
		//FL pair
		else {
			c_pos = cube.YGO.getCurr_pos();
			e_pos = cube.GO.getCurr_pos();
			c_or = cube.YGO.getOrientation();
			if(c_pos.equals("DFL") && e_pos.equals("FL") && c_or == 0 && cube.YO.getCurr_pos().equals("DL")
			   && cube.YBO.getCurr_pos().equals("DBL") && cube.BO.getCurr_pos().equals("BL") && cube.YBO.getOrientation() == 0) {
				return stepp;
			}
		}
		
		if(num_turns == max_depth) {
			return "fail";
		}
		
		for(String move: pos_moves) {
			if(move.charAt(0) == previous_move.charAt(0)){
				continue;
			}
		
			this.cube.applyTurn(move);
			String inter_res = step3_5(step, stepp + move + " ", move, num_turns+1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.cube.UndoTurn(move);
		}
		return "fail";
	}
	
	public String CP1(String stepp, String previous_move, int num_turns, int max_depth) {
		if(cube.YGR.getCurr_pos().equals("DFR") && cube.YBR.getCurr_pos().equals("DBR")) {
			return stepp;
		}
		if(num_turns == max_depth) {
			return "fail";
		}
		
		for(String move: moves) {
			if(move.charAt(0) == previous_move.charAt(0)){
				continue;
			}
		
			this.cube.applyTurn(move);
			String inter_res = CP1(stepp + move + " ", move, num_turns+1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.cube.UndoTurn(move);
		}
		return "fail";
	}
	
	public static void main(String args[]) {
		new Solver();
	}	
	
	//show solution in separate window
	public void displaySolution() {
		String display = "<html>Scramble: " + this.scramble + "<br/>Solution:<br/>" + this.solution + "</html>";
		JFrame frame2 = new JFrame("Solution");
		JLabel label2 = new JLabel(display);
		frame2.add(label2, BorderLayout.NORTH);
		frame2.setSize(500, 500);
		frame2.setVisible(true);
		this.frame.setVisible(false);	
	}
	
	//simplify given algorithm by removing redundancies (e.g. R R2 -> R')
	public String Simplify(String s) {
		boolean flag = true;
		//possibly loop multiple times (e.g. R R R -> R2 R -> R')
		while(flag) {
			String[] arr = s.split("\\s+");
			flag = false;
			for(int i = 0; i<arr.length-1; i++) {
				if(arr[i].charAt(0) == arr[i+1].charAt(0)) {
					flag = true;
					String temp = Combine(arr[i],arr[i+1]) + " ";
					if(temp.endsWith("4 ")) {
						arr[i] = "";
					}
					else {
						arr[i] = temp;
					}
					arr[i+1] = "";
					i++;
				}
			}
			s = "";
			for(String a: arr) {
				if(!a.isEmpty()) {
					s += a + " ";
				}
			}
		}
		return s;
	}
	
	//combine the two given consecutive turns of same face
	public String Combine(String mv1, String mv2) {
		if(mv1.length() == 1) {
			mv1 += " ";
		}
		if(mv2.length() == 1) {
			mv2 += " ";
		}
		Map<String, String> combo = new HashMap<String, String>();
		combo.put(" \'", "4");
		combo.put(" 2", "\'");
		combo.put("2\'", " ");
		combo.put("  ", "2");
		combo.put("22", "4");
		combo.put("\'\'", "2");
		combo.put(" \' ", "4");
		combo.put("2 ", "\'");
		combo.put("\'2", " ");
		return mv1.substring(0,1) + combo.get(mv1.substring(1,2) + mv2.substring(1,2));
	}
}
