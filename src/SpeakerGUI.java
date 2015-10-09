import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * The interface for the phoneme parser.
 * @author Sean Mullan
 * @see Runner.java
 * @see Voice.java
 * @see PhonemeDict.java
 */
public class SpeakerGUI extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8098148137461733073L;
	Runner runner;

	/**
	 * launches the GUI
	 * @param args
	 */
	public static void main(String[] args) {
		new SpeakerGUI();
	}

	private JButton startButton;
	private JTextArea phrase;
	private JTextArea output;

	/**
	 * Creates and initializes the graphical interface for the phoneme parser
	 */
	public SpeakerGUI(){
		ClickListener actions = new ClickListener();


		//sets up the SpeakerGUI frame
		this.setSize(700,350);
		this.setTitle("Phoneme Generator");
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Sets the location based on the screen
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension d = tk.getScreenSize();
		int x = d.width/4;
		int y = d.height/4;
		this.setLocation(x,y);

		//jpanel code
		JPanel panel = new JPanel();
		panel.setLayout(null);
		Insets insets = panel.getInsets();

		//StartButton
		startButton = new JButton("Loading Dictionary");
		startButton.setEnabled(false);
		Dimension size = startButton.getPreferredSize();
		startButton.setBounds(insets.right+350-size.width/2,280,size.width, size.height);
		startButton.addActionListener(actions);
		panel.add(startButton);
		
		//Box to type in phrase
		phrase = new JTextArea(20, 4);
		size = phrase.getPreferredSize();
		phrase.setBounds(insets.left+20,20,insets.left+650, 120);
		phrase.setText("Type Text In Here");
		phrase.setEnabled(true);
		panel.add(phrase);
		phrase.setLineWrap(true);
		phrase.setWrapStyleWord(true);
		phrase.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				if(phrase.getText().equals("Type Text In Here")){phrase.setText(null);}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(phrase.getText().equals("")){phrase.setText("Type Text In Here");}
			}
		});

		//Box to display phonemes
		output = new JTextArea(20,4);
		size = output.getPreferredSize();
		output.setBounds(insets.left+20,160,insets.left+650, 120);
		output.setText("Phonemes show up here");
		output.setEnabled(true);
		output.setLineWrap(true);
		output.setWrapStyleWord(true);
		panel.add(output);


		this.add(panel);
		this.setVisible(true);
		startButton.requestFocus();
		//initializes dictionary etc.
		runner = new Runner();
		startButton.setEnabled(true);
		startButton.setText("Text => Phonemes");
	}

	/**
	 * internal class to handle button events
	 * @author Sean Mullan
	 *
	 */
	private class ClickListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if (e.getSource() == startButton){

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						output.setText(Runner.parse(phrase.getText()));
					}
				});
			}
		}
	}
}