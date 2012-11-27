package MAS;

import java.util.ArrayList;

import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class GUI extends GuiAgent {
	/*
	 * -gui room1:MAS.Room;room2:MAS.Room;room3:MAS.Room;room4:MAS.Room;room5:MAS.Room;room6:MAS.Room;room7:MAS.Room;robot1:MAS.Robot("room1");robot2:MAS.Robot("room1");robot3:MAS.Robot("room2");gui:MAS.GUI;
	 */
	transient protected Frame gui; 
	protected void setup(){
		gui = new Frame();
		addBehaviour( new ListenBehaviour(this));
		AgentContainer c = getContainerController();
		try {
			c.createNewAgent( "stats", "MAS.Statistics", null).start();;
		} catch (StaleProxyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Add rooms
		ArrayList<String> rooms = new ArrayList<String>();
		for(int i = 0; i < 80; i++){
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
		Object [] args = new Object[1];
        args[0] = "room1";
		ArrayList<String> robots = new ArrayList<String>();
		for(int i = 0; i < 10; i++){
			robots.add("robotN"+i);
		}
		try {
			for (int i = 0; i < robots.size(); i++) {
				c.createNewAgent( robots.get(i), "MAS.Robot", args).start();;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	class ListenBehaviour extends CyclicBehaviour {
		GUI aGui;
		public ListenBehaviour(GUI gui){
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
					e1.printStackTrace();
				}
				
				if (myObject instanceof Msg.RoomStatus) {
					Msg.RoomStatus roomStatus = (Msg.RoomStatus) myObject; 
					aGui.gui.updateRoomPane(roomStatus.name, roomStatus.dustLevel);
				} else if (myObject instanceof Msg.RobotStatus) {
					Msg.RobotStatus robotStatus = (Msg.RobotStatus) myObject;
					aGui.gui.robotInRoomMap.put(((Msg.RobotStatus) myObject).getName(), ((Msg.RobotStatus) myObject).getRoom());
					aGui.gui.updateRobotMap();
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
