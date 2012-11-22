package MAS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

public class Frame extends JFrame {
	public JLabel statusLbl;
	private JLabel robotLbl;
	private JPanel pane;
	public HashMap<String, GuiRoom> roomMap;
	//public HashMap<String, GuiRoom> robotInRoomMap;  
	public HashMap<String, String> robotInRoomMap;
	public Frame(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 300);
		
		pane = new JPanel();
		pane.setLayout(new FlowLayout());
		pane.setBackground(Color.white);
		getContentPane().add(pane, BorderLayout.CENTER);
		
		// Cretae map of elements
		roomMap = new HashMap<String, GuiRoom>();
		robotInRoomMap = new HashMap<String, String>();
		//Status
		statusLbl = new JLabel("Status: Running");
		getContentPane().add(statusLbl, BorderLayout.SOUTH);
		
		
		setVisible(true);
	}
	
	public void updateRobotMap(){
		String robotStr, roomStr;
		GuiRoom room;
		for (Map.Entry<String, String> entry : robotInRoomMap.entrySet())
		{
			//System.out.println("ROBOTMAP: " + entry.getKey() + "/" + entry.getValue());
			roomStr = entry.getValue();
			robotStr = entry.getKey();
			room = roomMap.get(roomStr);
			if(room != null){
				room.addRobot(robotStr);
			}	
		    
		}
	}
	
	public void createNewRoomPane(String name, int dustlevel){
		GuiRoom room = new GuiRoom(name, dustlevel);
		roomMap.put(name, room);
		pane.add(room);
		pane.updateUI();
	}
	
	public void updateRoomPane(String name, int dustlevel){
		GuiRoom room = roomMap.get(name);
		if (room != null){
			room.setDustlevel(dustlevel);			
		} else {
			createNewRoomPane(name, dustlevel);
		}
//		if(robotInRoomMap.containsKey(room)) {
//			room.robotVectorList.addElement(robotInRoomMap.get(room));
//		}
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
			robotVectorList.add("Hejse");
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
		
		public void addRobot(String robot){
			if (!robotVectorList.contains(robot)){
				robotVectorList.add(robot);
				robotList.updateUI();
			}
		}
		
		public void updateRobotList(){
			//robotList.up
		}
		public void setDustlevel(int dustlevel){
			int green, red;
			this.dustlevel = dustlevel;
			dustLbl.setText(dustlevel + "");
			
			if (dustlevel < 127){ // Colour management
				red = dustlevel * 2;
				green = 255;
			}else{
				green = 255 - (dustlevel-128)*2-1;
				red = 255;
			}
			
			this.setBackground(new Color(red, green,0));
		}
		
	}
}
