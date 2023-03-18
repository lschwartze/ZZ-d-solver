package solver;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

/*Solver class. Constructs the GUI to enter scramble and display solution. 
Contains necessary functions to check for validity of scramble and makes calls to cube object.*/

class Solver implements ActionListener{
	
	JLabel label;
	JButton button;
	JTextField text;
	String scramble;
	Cube cube;
	Cube tmp_cube;
	String[] moves = new String[] {"R", "R\'", "R2",
			   "L", "L\'", "L2",
			   "U", "U\'", "U2",
			   "D", "D\'", "D2",
			   "F", "F\'", "F2",
			   "B", "B\'", "B2"};
	
	//construct solver
	public Solver(){
		cube = new Cube();
		tmp_cube = new Cube();
		JFrame frame = new JFrame("scramble input");
		text = new JTextField();
		text.setEditable(true);
		label = new JLabel("please input your scramble in standard WCA notation");
		button = new JButton("solve!");
		button.addActionListener(this);
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
			begin_solve();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	//start solving process
	public void begin_solve() throws Exception {
		while(scramble == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//check if scramble is feasible
		if(checkScramble(scramble)) {
			this.label.setText("calculating...");
			this.button.setEnabled(false);
		}
		else {
			this.label.setText("invalid scramble");
		}
		
		//split scramble in string array
		String[] scramble_array = scramble.split("\\s+");
		this.cube.scramble(scramble_array);
		this.tmp_cube.scramble(scramble_array);
				
		//max_depth = 7: every eo can be solved in at most 7 turns
		String[] eo = EOLine("", " ", " ", 0, 7).split("\\s+");
		for(String s: eo) {
			System.out.print(s);
		}
		System.out.println("\n");
		String tmp = "";
		while(true) {
			tmp_cube = new Cube();
			tmp_cube.scramble(scramble_array);
			tmp = EOLine("", " ", " ", 0, eo.length-1);
			if(tmp.equals("fail")) {
				break;
			}
			eo = tmp.split("\\s+");
		}
		for(String s: eo) {
			System.out.print(s);
		}
		//System.out.println(Simplify(eo));
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
	
	//ToDo: search for shorter eo, don't stop if one is found 
	public String EOLine(String turns, String previous_move, String prevprev, int num_turns, int max_depth) {
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
			String inter_res =  EOLine(turns + "F ", "F", previous_move, num_turns+1, max_depth);
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
			String inter_res = EOLine(turns + "B ", "B", previous_move,num_turns+1, max_depth);
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
			String inter_res =  EOLine(turns + move + " ", move, previous_move, num_turns +1, max_depth);
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
	
	public static void main(String args[]) {
		new Solver();
	}	
	
	
	/*These functions are not used currently but may prove helpful later*/
	
	public String Simplify(String str) {
		String[] s = str.split("\\s+");
		int i = 0;
		boolean flag = false;
		while(i<s.length-2) {
			char si = s[i].charAt(0);
			char si1 = s[i+1].charAt(0);
			char si2 = s[i+2].charAt(0);
			System.out.println("si: " + si + " si1: " + si1);
			System.out.println(isOpposite(si,si1));
			System.out.println("si: " + si + " si2: " + si2);
			System.out.println(si==si2);
			if((si == si2 && isOpposite(si, si1)) || (si == ' ' && isOpposite(si,si1))) {
				s[i] = Combine(s[i], s[i+2]);
				s[i+2] = " ";
				flag = true;
			}
			i+=1;
		}
		String res = "";
		for(int j = 0; j<s.length; j++) {
			if(s[j].endsWith("4")) {
				continue;
			}
			res += s[j] + " ";
		}
		//System.out.println(res);
		if(flag) {
			//res = Simplify(res);
		}
		return res;
	}
	
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
