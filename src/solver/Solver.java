package solver;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	
	public Solver(){
		cube = new Cube();
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
	
	public void begin_solve() throws Exception {
		while(scramble == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(checkScramble(scramble)) {
			this.label.setText("calculating...");
			this.button.setEnabled(false);
		}
		else {
			this.label.setText("invalid scramble");
		}
		
		String[] scramble_array = scramble.split("\\s+");
		//String[] scramble_array = new String[] {"D"};
		this.cube.scramble(scramble_array);
		
		//this.cube.applyTurn("R2");
		for(Corner c: this.cube.corners) {
			System.out.println(c.toString());
		}
		for(Edge e: this.cube.edges) {
			System.out.println(e.toString());
		}
		return;
	}
	
	public boolean checkScramble(String s) {
		Set<Character> valid_first = Set.of('R', 'L', 'U', 'D', 'F', 'B');
		Set<Character> valid_modifier = Set.of('2', '\'');
		s = s.replaceAll("\\s", "");
		int i = 0;
		while(i<s.length()-1) {
			if(valid_first.contains(s.charAt(i))) {
				if(valid_modifier.contains(s.charAt(i+1))) {
					i += 2;
					continue;
				}
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
	
	public static void main(String args[]) {
		new Solver();
	}
	
}
