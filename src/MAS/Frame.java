package MAS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

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

		for (Entry<String, GuiRoom> entry : roomMap.entrySet()){
			roomStr = entry.getKey();
			room = entry.getValue();
			//room.robotVectorList.clear();
			room.robotAmountLbl.setText("0");
		}
		
		for (Map.Entry<String, String> entry : robotInRoomMap.entrySet())
		{
			//System.out.println("ROBOTMAP: " + entry.getKey() + "/" + entry.getValue());
			roomStr = entry.getValue();
			robotStr = entry.getKey();
			room = roomMap.get(roomStr);
			if(room != null){
				room.addRobot(robotStr);
				int amount = Integer.parseInt(room.robotAmountLbl.getText());
				amount++;
				room.robotAmountLbl.setText(""+amount);
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
		public JLabel robotAmountLbl;
		private int dustlevel;
		//Vector<String> robotVectorList;
		//JList robotList;
		public GuiRoom(String name, int dustlevel){
			// Set name
			//this.robotVectorList = new Vector<String>();
			//this.robotList = new JList(robotVectorList);
			//robotList.setFixedCellWidth(50);
			//robotVectorList.add("Hejse");
			this.setLayout(new BorderLayout());
			this.setBorder(new EmptyBorder(10, 10, 10, 10) );
			this.name = name;
			this.nameLbl = new JLabel(name);
			this.dustLbl = new JLabel();
			robotAmountLbl = new JLabel("0");
			this.add(nameLbl, BorderLayout.NORTH);
			this.add(robotAmountLbl, BorderLayout.CENTER);
			//this.add(robotList);
			this.add(dustLbl, BorderLayout.SOUTH);
			this.setMinimumSize(new Dimension(200, 50));
			
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
			/*
			if (!robotVectorList.contains(robot)){
				robotVectorList.add(robot);
				//robotList.updateUI();
			}
			*/
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
			
			if (red >= 255 ){
				red = 255;
			}else if (red <= 0){
				red = 0;
			}
			
			if (green >= 255 ){
				green = 255;
			}else if (green <= 0){
				green = 0;
			}
			this.setBackground(new Color(red, green,0));
			Color c = this.getBackground();
			c = new Color(255-c.getRed(),255-c.getGreen(),255-c.getBlue());
			nameLbl.setForeground(c);
			dustLbl.setForeground(c);
			robotAmountLbl.setForeground(c);
			
			//nameLbl.setForeground(255,255,255);
		}
		
	}
}
