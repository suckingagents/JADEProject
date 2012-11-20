package MAS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mxgraph.util.png.mxPngEncodeParam.RGB;

public class Frame extends JFrame {
	public JLabel testLbl;
	private JPanel pane;
	HashMap<String, GuiRoom> map;
	public Frame(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 300);
		
//		testLbl = new JLabel("Bacon");
//		getContentPane().add(testLbl, BorderLayout.NORTH);
		
		pane = new JPanel();
		pane.setLayout(new FlowLayout());
		getContentPane().add(pane, BorderLayout.CENTER);
		
//		JPanel roomPane = new JPanel();
//		roomPane.setBackground(Color.red);
//		pane.add(roomPane);
		
		// Cretae map of elements
		map = new HashMap<String, GuiRoom>();
	//	createNewRoomPane("J�rgen", 255);
	//	updateRoomPane("Morten", 175);
		setVisible(true);
	}
	
	public void createNewRoomPane(String name, int dustlevel){
		/*
		JPanel newPane = new JPanel();
		JLabel namelbl = new JLabel(name);
		newPane.add(namelbl);
		newPane.setBackground(Color.green);
		
		map.put(name, newPane);
		*/
		GuiRoom room = new GuiRoom(name, dustlevel);
		map.put(name, room);
		pane.add(room);
		pane.updateUI();
	}
	
	public void updateRoomPane(String name, int dustlevel){
		GuiRoom room = map.get(name);
		if (room != null){
			room.setName(name);
			room.setDustlevel(dustlevel);
		} else {
			createNewRoomPane(name, dustlevel);
		}
	}
	
	class GuiRoom extends JPanel{
		private String name;
		private JLabel nameLbl;
		private JLabel dustLbl;
		private int dustlevel;
		public GuiRoom(String name, int dustlevel){
			// Set name
			this.name = name;
			nameLbl = new JLabel(name);
			dustLbl = new JLabel();
			add(nameLbl);
			add(dustLbl);
			
			// Set level
			setDustlevel(dustlevel);
	//		this.dustlevel = dustlevel;
	//		setBackground(new Color(this.dustlevel,255-this.dustlevel,0));
		}
		
		public String getName(){
			return name;
		}
		public void setName(String name){
			this.name = name;
			nameLbl.setText(this.name);
		}
		public int getDustlevel(){
			return this.dustlevel;
		}
		public void setDustlevel(int dustlevel){
			this.dustlevel = dustlevel;
			dustLbl.setText(dustlevel + "");
			this.setBackground(new Color(dustlevel, 255-dustlevel,0));
		//		this.setBackground(new Color(255-this.dustlevel, 0, 0));
		//		this.updateUI();
		//	}
		}
		
	}
}
