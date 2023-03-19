package solver;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

/*Solver class. Constructs the GUI to enter scramble and display solution. 
Contains necessary functions to check for validity of scramble and makes calls to cube object.*/

class Solver implements ActionListener{
	
	JFrame frame;
	JLabel label;
	JButton button;
	JTextField text;
	String scramble;
	Cube cube;
	Cube tmp_cube;
	Set<String> moves = new HashSet<>(Arrays.asList("R", "R\'", "R2",
			   "L", "L\'", "L2",
			   "U", "U\'", "U2",
			   "D", "D\'", "D2",
			   "F", "F\'", "F2",
			   "B", "B\'", "B2"));
	String solution;
	
	//construct solver
	public Solver(){
		cube = new Cube();
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
		this.cube.scramble(scramble_array);
		//temporary cube that can be used to solve each step and generated from scratch
		tmp_cube = new Cube();
		this.tmp_cube.scramble(scramble_array);
				
		//######################## solve eo ##############################################################
		//max_depth = 7: every eo can be solved in at most 7 turns
		//searches eo for tmp_cube, once the best is found, it is applied to cube
		//first, eo is a suboptimal eo, that will be greedily improved
		String[] eo = EO("", " ", " ", 0, 7).split("\\s+");
		//search for optimal eo
		String tmp = "";
		while(true && eo.length > 1) {
			tmp_cube = new Cube();
			tmp_cube.scramble(scramble_array);
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
		cube.scramble(eo);
		solution += "\t //EO <br/>";		
		//##############################################################################################
		//in this stage, only double turns of front and back faces are needed
		moves.remove("F");
		moves.remove("F\'");
		moves.remove("B");
		moves.remove("B\'");
		
		//################################# EOLine #####################################################
		//solving EOLine in fewest possible moves
		//generate new temporary cube, apply scramble and eo
		this.tmp_cube = new Cube();
		tmp_cube.scramble(scramble_array);
		tmp_cube.scramble(eo);
		//EOLine with max_depth 5 because the edges can be solved in at most 5 turns
		String[] EOLine = EOLine("", " ", " ", 0, 5).split("\\s+");
		//greedily search for better solutions
		while(true && EOLine.length > 1) {
			tmp_cube = new Cube();
			tmp_cube.scramble(scramble_array);
			tmp_cube.scramble(eo);
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
		//cube.scramble(EOLine);
		solution += "\t //EOLine <br/>";
		//##############################################################################################
		
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
		for(Edge e: this.tmp_cube.edges) {
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
		if(this.tmp_cube.getEdge("UF").getOrientation() == 1 &&
		   this.tmp_cube.getEdge("FR").getOrientation() == 1 &&
		   this.tmp_cube.getEdge("DF").getOrientation() == 1 &&
		   this.tmp_cube.getEdge("FL").getOrientation() == 1) {
			this.tmp_cube.applyTurn("F");
			String inter_res =  EO(turns + "F ", "F", previous_move, num_turns+1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.tmp_cube.UndoTurn("F");
		}
		//all edges on back face are wrong, turn B, recursive step
		else if(this.tmp_cube.getEdge("UB").getOrientation() == 1 &&
				this.tmp_cube.getEdge("BR").getOrientation() == 1 &&
				this.tmp_cube.getEdge("DB").getOrientation() == 1 &&
				this.tmp_cube.getEdge("BL").getOrientation() == 1) {
			this.tmp_cube.applyTurn("B");
			String inter_res = EO(turns + "B ", "B", previous_move,num_turns+1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.tmp_cube.UndoTurn("B");
		}
		//loop through allowed turns
		for(String move: moves) {
			//if move is modification of previous one, or turns opposite side from previous one but the same as the one before, skip 
			if((move.charAt(0) == previous_move.charAt(0)) || (move.charAt(0) == prevprev.charAt(0) && isOpposite(move.charAt(0), previous_move.charAt(0)))) {
				continue;
			}
			//turning F when all edges on F are correct is unnecessary
			if(move.charAt(0) == 'F' && this.tmp_cube.getEdge("UF").getOrientation() == 0 &&
									 	this.tmp_cube.getEdge("FR").getOrientation() == 0 &&
									 	this.tmp_cube.getEdge("DF").getOrientation() == 0 &&
									 	this.tmp_cube.getEdge("FL").getOrientation() == 0) {
				continue;
			}
			//turning B when all edges on B are correct is unnecessary
			if(move.charAt(0) == 'B' && this.tmp_cube.getEdge("UB").getOrientation() == 0 &&
				 						this.tmp_cube.getEdge("BR").getOrientation() == 0 &&
				 						this.tmp_cube.getEdge("DB").getOrientation() == 0 &&
				 						this.tmp_cube.getEdge("BL").getOrientation() == 0) {
				continue;
			}
			this.tmp_cube.applyTurn(move);
			String inter_res =  EO(turns + move + " ", move, previous_move, num_turns +1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.tmp_cube.UndoTurn(move);
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
		String blue_pos = this.tmp_cube.YB.getCurr_pos();
		String green_pos = this.tmp_cube.YG.getCurr_pos();
		
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
			this.tmp_cube.applyTurn(move);
			String inter_res = EOLine(EOLine + move + " ", move, previous_move, num_turns+1, max_depth);
			if(!inter_res.equals("fail")) {
				return inter_res;
			}
			this.tmp_cube.UndoTurn(move);
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
}
