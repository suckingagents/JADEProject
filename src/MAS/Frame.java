package MAS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.mxgraph.util.png.mxPngEncodeParam.RGB;

public class Frame extends JFrame {
	public JLabel testLbl;
	private JPanel pane;
	public HashMap<String, GuiRoom> roomMap;
	public HashMap<String, GuiRoom> robotInRoomMap;  
	public Frame(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 300);
		
		pane = new JPanel();
		pane.setLayout(new FlowLayout());
		getContentPane().add(pane, BorderLayout.CENTER);
		
		// Cretae map of elements
		roomMap = new HashMap<String, GuiRoom>();
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
		roomMap.put(name, room);
		pane.add(room);
		pane.updateUI();
	}
	
	public void updateRoomPane(String name, int dustlevel){
		GuiRoom room = roomMap.get(name);
		if (room != null){
		//	room.setName(name);
			room.setDustlevel(dustlevel);			
		} else {
			createNewRoomPane(name, dustlevel);
		}
		if(robotInRoomMap.containsKey(room)) {
//			room.robotVectorList.addElement(robotInRoomMap.get(room));
		}
	}
	
	class GuiRoom extends JPanel{
		private String name;
		private JLabel nameLbl;
		private JLabel dustLbl;
		private int dustlevel;
		Vector<String> robotVectorList;
		JList robotList;
		public GuiRoom(String name, int dustlevel){
			// Set name
			this.robotVectorList = new Vector<String>();
			this.robotList = new JList(robotVectorList);
			this.name = name;
			this.nameLbl = new JLabel(name);
			this.dustLbl = new JLabel();
			this.add(nameLbl);
			this.add(robotList);
			this.add(dustLbl);
			
			// Set level
			this.setDustlevel(dustlevel);
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
			
		//		this.updateUI();
		//	}
		}
		
	}
}
