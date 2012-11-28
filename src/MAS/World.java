package MAS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.Map.Entry;

import MAS.Msg.RobotStatus;
import MAS.Msg.RoomStatus;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class World extends GuiAgent {
	/*
	 * -gui room1:MAS.Room;room2:MAS.Room;room3:MAS.Room;room4:MAS.Room;room5:MAS.Room;room6:MAS.Room;room7:MAS.Room;robot1:MAS.Robot("room1");robot2:MAS.Robot("room1");robot3:MAS.Robot("room2");gui:MAS.GUI;
	 */
	transient protected Frame gui;
	
	private final String filename = "D:/agent/out.txt";
	long t0;
	HashMap<String, Integer> map;
	HashMap<String, String> robotMap;
	HashMap<String, Integer> roomRobotMap;
	
	protected void setup(){
		
		t0 = System.currentTimeMillis();
		map = new HashMap<String, Integer>();
		robotMap = new HashMap<String, String>();
		roomRobotMap = new HashMap<String, Integer>();
		addBehaviour( new ListenBehaviour( this ));
		addBehaviour( new StatisticsBehviour(this, Msg.TIME_LAPSE));
		
		File deletefile = new File(filename);
		deletefile.delete();
		
		gui = new Frame();
		addBehaviour( new ListenBehaviour(this));
		AgentContainer c = getContainerController();
		// Add rooms
		ArrayList<String> rooms = new ArrayList<String>();
		for(int i = 0; i < Msg.roomAmount; i++){
			rooms.add("room"+i);
		}
		try {
			for (int i = 0; i < rooms.size(); i++) {
				c.createNewAgent( rooms.get(i), "MAS.Room", null ).start();
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		// add robots
		ArrayList<String> robots = new ArrayList<String>();
		for(int i = 0; i < Msg.robotAmount; i++){
			robots.add("robot"+i);
		}
		
		Object [] args = new Object[1];
        args[0] = "room";
		try {
			for (int i = 0; i < robots.size(); i++) {
				int roomrand = new Random().nextInt(Msg.roomAmount);
				args[0] = "room"+roomrand;
				c.createNewAgent( robots.get(i), "MAS.Robot", args).start();;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	class StatisticsBehviour extends TickerBehaviour{
		long q1;
		Agent agent;
		public StatisticsBehviour(Agent a, long period) {
			super(a, period);
			q1 = System.currentTimeMillis();
			agent = a;
		}

		@Override
		protected void onTick() {
			ArrayList<Integer> list = new ArrayList<Integer>();
			Vector<Integer> printlist = new Vector<Integer>();
			String room, robot;
			int value, roomSum = 0, roomAvg = 0, roomN = 0, inactiveRobots = 0, robotN = 0;
			roomN = map.size();
			robotN = robotMap.size();
			int total = 255*roomN;
			
			printlist.add((int) (System.currentTimeMillis() - t0));
			printlist.add(robotN);
			
			// Calculate room statistics
			for (Entry<String, Integer> entry : map.entrySet()){
				room = entry.getKey();
				value = entry.getValue();
				roomSum += value;
				printlist.add(value);
				list.add(value);
			}
			
			Collections.sort(list);
			int median = list.get(list.size()/2);
			int quart1 =list.get(list.size()/4);
			int quart3 =list.get((list.size()*3)/4);
			
			// Calculate robot statistics
			for (Entry<String, String> entry : robotMap.entrySet()){
				robot = entry.getKey();
				room = entry.getValue();
				if (room != null){
					if (map.get(room) != null){
						value = map.get(room);
						if (value < 5){
							inactiveRobots++;
						}
					}
				}
			}
			if (roomN > 0){
				roomAvg = roomSum / roomN;
			}

			printlist.add(inactiveRobots);
			// All values are up to date
			// Send a message
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			Msg.addAllAgentsToMsg(agent, msg, "Robot");
			Msg.addAllAgentsToMsg(agent, msg, "Room");
			Msg.GlobalStatus status = new Msg.GlobalStatus();
			status.avg = roomAvg;
			status.q0 = quart1;
			status.q1 = median;
			status.q2 = quart3;
			try {
				msg.setContentObject(status);
				send(msg);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Print
			if (System.currentTimeMillis() - q1 > 1000){
				q1 = System.currentTimeMillis();
				System.out.println(new Date(System.currentTimeMillis()) + ": STATISTICS:\tAvg:\tq1:\tq2:\tq3:\tTotal:\tRooms:\tRobots:\tInactive:");	
				System.out.println(new Date(System.currentTimeMillis()) + ": STATISTICS:\t" + roomAvg + "\t" + quart1 + "\t" + median + "\t" + quart3 + "\t" + roomSum + " / " + total + "\t" + roomN + "\t" + robotN + "\t" + inactiveRobots);
			}
			// save to file
			if (roomN >= Msg.roomAmount && robotN >= Msg.robotAmount ){
				try{
					  // Create file 
					FileWriter fstream = new FileWriter(filename,true);
					  BufferedWriter out = new BufferedWriter(fstream);
					  for (Integer i : printlist){
						  out.write(i + ",");  
					  }
					  out.write("\n");
					  //Close the output stream
					  out.close();
				}catch (Exception e){//Catch exception if any
					  System.err.println("Error: " + e.getMessage());
				}	
			}
		}
		
	}
	
	
	
	class ListenBehaviour extends CyclicBehaviour {
		World aGui;
		public ListenBehaviour(World gui){
			super(gui);
			this.aGui = gui;
		} 
		
		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg != null){
				Object myObject = null;
				try {
					myObject = msg.getContentObject();
				} catch (UnreadableException e1) {
					System.err.println("Gui caught unreadable exception from " + msg.getSender().getLocalName());
					//e1.printStackTrace();
				}
				
				if (myObject instanceof Msg.RoomStatus) {
					Msg.RoomStatus roomStatus = (Msg.RoomStatus) myObject; 
					aGui.gui.updateRoomPane(roomStatus.name, roomStatus.dustLevel);
					map.put(roomStatus.name, roomStatus.dustLevel);
				} else if (myObject instanceof Msg.RobotStatus) {
					Msg.RobotStatus robotStatus = (Msg.RobotStatus) myObject;
					aGui.gui.robotInRoomMap.put(((Msg.RobotStatus) myObject).getName(), ((Msg.RobotStatus) myObject).getRoom());
					aGui.gui.updateRobotMap();
					robotMap.put(robotStatus.name, robotStatus.room);
					//System.out.println("ROBOTMAP: " + aGui.gui.toString());
				}
			}
			block(1000);
		}
		
	}
	
	
	@Override
	protected void onGuiEvent(GuiEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
