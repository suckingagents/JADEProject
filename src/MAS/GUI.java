package MAS;

import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class GUI extends GuiAgent {
	transient protected Frame gui; 
	protected void setup(){
		gui = new Frame();
		addBehaviour( new ListenBehaviour(this));
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
