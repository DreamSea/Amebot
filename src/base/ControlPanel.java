package base;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ControlPanel extends JPanel implements ActionListener {
	JButton quit = new JButton("Quit");
	JTextField channelField = new JTextField();
	JButton join = new JButton("Join");
	AmeBot bot;
	
	public ControlPanel (AmeBot bot)
	{
		add(channelField);
		add(join);
		add(quit);
		channelField.setPreferredSize(new Dimension(100, 25));;
		
		quit.addActionListener(this);
		join.addActionListener(this);
		setSize(100, 100);
		this.bot = bot;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == quit)
		{
			System.out.println("quit");
		}
		else if (e.getSource() == join)
		{
			String check = channelField.getText();
			if (check.charAt(0) == '#')
			{
				bot.joinChannel(channelField.getText().toLowerCase());
			}
		}
		// TODO Auto-generated method stub
		
	}
	
	

}
