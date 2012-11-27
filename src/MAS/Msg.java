package MAS;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.Serializable;

public class Msg implements Serializable{
	/*
	 * Parameters
	 */
	static final int TIME_LAPSE = 100;	// Tidsenhed, der styrer hastigehden af systemet
	static final int roomAmount = 80;
	static final int robotAmount = 10;
	static final int roomThreshold = 50; // Max dustlevel før robotter må tilkaldes
	static final int robotThreshold = 50; // Min dustlevel før robotter må forlade et værelse
	static final int robotDustRatio = -6; // level, som robot støvsuger pr. time lapse
	static final int roomDustRatio = 1; // level, som room stiger med pr. tidsenhed
	static final int roomChangeCost = 20; // prisenhed, før robot må skifte værelse
	
	public static void addAllAgentsToMsg(Agent a, ACLMessage msg, String agentType){
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( agentType);
        dfd.addServices(sd);
        
        DFAgentDescription[] result = null;
		try {
			result = DFService.search(a, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
        for (int i = 0; i < result.length ; i++){
        	//System.out.println("Woolooboolo: " + result[i].getName().getLocalName() );
        	msg.addReceiver( result[i].getName() );
        }
	}
	
	
	
	static class GlobalStatus implements Serializable {
		int max, min, avg;
		public GlobalStatus(){
			
		}
	}
	static class RobotStatus implements Serializable{
		int deltaDust;
		String name;
		String room;
		public String getRoom() {
			return room;
		}
		public void setRoom(String room) {
			this.room = room;
		}
		public int getDeltaDust() {
			return deltaDust;
		}
		public void setDeltaDust(int deltaDust) {
			this.deltaDust = deltaDust;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public RobotStatus(String name, String room,int dDust){
			this.name = name;
			this.deltaDust = dDust;
			this.room = room;
		}
	}
	
	static class RoomStatus implements Serializable {
		int dustLevel;
		String name;
		public RoomStatus(String name, int dustLevel){
			this.name = name;
			this.dustLevel = dustLevel;
		}
		
	}
	
	static class RoomBargin implements Serializable {
		RoomStatus roomStatus;
		RobotStatus robotStatus;
		boolean accept;
		int sender, type;
		static final int TYPE_REQUEST = 0;
		static final int TYPE_ANSWER = 1;
		static final int AGENT_ROOM = 0;
		static final int AGENT_ROBOT = 1;
		
		public RoomBargin(int sender, int type, RoomStatus roomStatus){
			this.sender = sender;
			this.type = type;
			this.roomStatus = roomStatus;
		}

		public RoomStatus getRoomStatus() {
			return roomStatus;
		}

		public void setRoomStatus(RoomStatus roomStatus) {
			this.roomStatus = roomStatus;
		}

		public boolean isAccept() {
			return accept;
		}

		public void setAccept(boolean accept) {
			this.accept = accept;
		}

		public int getSender() {
			return sender;
		}

		public void setSender(int sender) {
			this.sender = sender;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}
	} 
}
