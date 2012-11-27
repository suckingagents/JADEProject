package MAS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import MAS.Frame.GuiRoom;
import MAS.Msg.RobotStatus;
import MAS.Msg.RoomStatus;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Statistics extends Agent {
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
	}
	
	class StatisticsBehviour extends TickerBehaviour{

		public StatisticsBehviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			Vector<Integer> printlist = new Vector<Integer>();
			String room, robot;
			int value, roomSum = 0, roomAvg = 0, roomN = 0, inactiveRobots = 0, robotN = 0;
			roomN = map.size();
			robotN = robotMap.size();
			int total = 255*roomN;
			
			printlist.add((int) (System.currentTimeMillis() - t0));
			printlist.add(robotN);
			printlist.add(inactiveRobots);
			
			// Calculate room statistics
			for (Entry<String, Integer> entry : map.entrySet()){
				room = entry.getKey();
				value = entry.getValue();
				roomSum += value;
				printlist.add(value);
			}
			// Calculate robot statistics
			for (Entry<String, String> entry : robotMap.entrySet()){
				robot = entry.getKey();
				room = entry.getValue();
				value = map.get(room);
				if (value < 5){
					inactiveRobots++;
				}
			}
			if (roomN > 0){
				roomAvg = roomSum / roomN;
			}


			System.out.println(new Date(System.currentTimeMillis()) + ": STATISTICS:\t Avg: " + roomAvg + "\tTotal: " + roomSum + " / " + total + "\t Rooms: " + roomN + "\t Robots: " + robotN + "\tInactive: " + inactiveRobots);
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
	class ListenBehaviour extends CyclicBehaviour{
		public ListenBehaviour(Agent a){
			super(a);
		}
		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg != null){
				Object myObject = null;
				try {
					myObject = msg.getContentObject();
				} catch (UnreadableException e1) {
					e1.printStackTrace();
				}
				
				if (myObject instanceof Msg.RoomStatus) {
					RoomStatus roomStatus = (Msg.RoomStatus) myObject; 
					map.put(roomStatus.name, roomStatus.dustLevel);
				}else if (myObject instanceof Msg.RobotStatus) {
					RobotStatus robotStatus = (Msg.RobotStatus) myObject; 
					robotMap.put(robotStatus.name, robotStatus.room);
				}
			}
			block(1000);
		}
		
	}
}
