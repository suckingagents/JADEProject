package MAS;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentDescriptor;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class Room extends Agent{
	int dustLevel;
	int dustRatio;
	int dustThreshold;
	int maxDustLevel;
	int currentAvg;
	String name;
	Msg.RoomStatus roomStatus;
	boolean doBargain = false;
	protected void setup(){
		dustLevel = new Random().nextInt(Msg.roomThreshold);
		//dustLevel = ;
		dustThreshold = Msg.roomThreshold;
		maxDustLevel = 255;
		dustRatio = Msg.roomDustRatio;
		name = getAID().getLocalName();
		
		addBehaviour(new dustBehaviour( this ));
		addBehaviour(new dustManagingBehaviour( this, Msg.TIME_LAPSE ));
		addBehaviour( new BargainBehavior(this));
	}
	
	class BargainBehavior extends CyclicBehaviour{
		Room room;
		int state;
		static final int ST_IDLE = 0;
		static final int ST_WAIT_INCOMING = 1;
		static final int ST_WAIT_TIMEOUT = 2;
		long q0;
		static final long MAX_WAIT_TIME = Msg.TIME_LAPSE * 2;
		
		public BargainBehavior(Room room){
			this.room = room;
			state = ST_IDLE;
		}
		@Override
		public void action() {
			switch (state) {
			case ST_IDLE:
					if (doBargain){
						// request for robot
						Msg.RoomBargin bargain = new Msg.RoomBargin(Msg.RoomBargin.AGENT_ROOM, Msg.RoomBargin.TYPE_REQUEST, roomStatus);
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						Msg.addAllAgentsToMsg(room, msg, "Robot");
						try {
							msg.setContentObject(bargain);
							send(msg);
							//System.err.println(name + "\tROOM STATE IDLE:\t" + name + ": Request for robot: SENT");
						} catch (IOException e) {
							e.printStackTrace();
						}

						q0 = System.currentTimeMillis();
						state = ST_WAIT_INCOMING;
					}
				break;
			case ST_WAIT_INCOMING:
				//System.out.println(name + "\tROOM STATE WAIT:\t Waiting...");
				long q1 = System.currentTimeMillis();
				q1 = q1 - q0;
				if ( q1 >= MAX_WAIT_TIME ){
					// go back to idle
					state = ST_IDLE;
					//System.err.println(name + " received NO response from robots");
				}
				
				// Check for answer
				ACLMessage msg = receive();
				if (msg != null){
					Object myObject = null;
					try {
						myObject = msg.getContentObject();
					} catch (UnreadableException e1) {
						e1.printStackTrace();
					}
					
					if (myObject instanceof Msg.RoomBargin) {
						Msg.RoomBargin bargin  = (Msg.RoomBargin) myObject; 
						//System.err.println(name + "\tROOM STATE WAIT:\t" + name + ": Robot ANSWER: " + bargin.isAccept() + " FROM " + bargin.robotStatus.name);
						if (bargin.isAccept()){
							//System.out.println(name + "\tROOM STATE WAIT:\tACCEPTING ROBOT: " + bargin.robotStatus.name);
							msg = new ACLMessage(ACLMessage.INFORM);
							msg.addReceiver(new AID(bargin.robotStatus.name, AID.ISLOCALNAME));
							bargin.accept = true;
							bargin.type = Msg.RoomBargin.TYPE_ANSWER;
							try {
								msg.setContentObject(bargin);
								send(msg);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							doBargain = false;
							q0 = System.currentTimeMillis();
							state = ST_WAIT_TIMEOUT;
						}
					}	
				}
				break;
			case ST_WAIT_TIMEOUT:
				q1 = System.currentTimeMillis();
				q1 = q1 - q0;
				if ( q1 >= Msg.TIME_LAPSE * 10 ){ // Wait 10 seconds before requesting again
					// go back to idle
					state = ST_IDLE;
				}
				break;

			default:
				break;
			}
			block(1000);
		}
	}
	class dustManagingBehaviour extends TickerBehaviour {
		Agent agent;
		public dustManagingBehaviour(Agent a, long l){
			super(a, l);
			this.agent = a;
		}
		
		public void onTick() {
			//System.out.println(new Date(System.currentTimeMillis()) + ": " + name + " wants cleaning!");
			if(dustLevel + dustRatio <= maxDustLevel) {
				dustLevel += dustRatio;
			}
			DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd  = new ServiceDescription();
            sd.setType( "Robot" );
            dfd.addServices(sd);
            
            DFAgentDescription[] result = null;
			try {
				result = DFService.search(agent, dfd);
			} catch (FIPAException e) {
				e.printStackTrace();
			}
            
            //System.out.println(result.length + " results" );
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		
			
            for (int i = 0; i < result.length ; i++){
            	//System.out.println("Woolooboolo: " + result[i].getName().getLocalName() );
            	msg.addReceiver( result[i].getName() );
            }
            msg.addReceiver(new AID("gui", AID.ISLOCALNAME)); // Add GUI too
            msg.addReceiver(new AID("stats", AID.ISLOCALNAME)); // Add Stats too
            
            try {
            	roomStatus = new Msg.RoomStatus(name, dustLevel);
				msg.setContentObject(roomStatus);
			} catch (IOException e) {
				e.printStackTrace();
			}
            send( msg );
		}
			
	}
	
	class dustBehaviour extends CyclicBehaviour {
		Agent agent;
		public dustBehaviour(Agent a){
			super(a);
			this.agent = a;
		}
		@Override
		public void action() {
			// We can only fill the room with maxDustLevel.
			
			ACLMessage msg = receive();
			if (msg != null){
				Object myObject = null;
				try {
					myObject = msg.getContentObject();
				} catch (UnreadableException e1) {
					e1.printStackTrace();
				}
				if (myObject instanceof Msg.GlobalStatus) {
					Msg.GlobalStatus tmpStatus = (Msg.GlobalStatus) myObject; 
					currentAvg = tmpStatus.avg;
				}
				if (myObject instanceof Msg.RobotStatus) {
					Msg.RobotStatus status =(Msg.RobotStatus) myObject; 
					//System.out.println(new Date(System.currentTimeMillis()) + ": " + status.name + " is removing " + status.deltaDust + " in room " + name);
					dustLevel += status.deltaDust;
				}
				if (dustLevel < 0){
					dustLevel = 0;
				}
			}
			
			if(dustLevel > dustThreshold && dustLevel > currentAvg){
				doBargain = true;
			}else{
				doBargain = false;
			}
			//System.out.println(new Date(System.currentTimeMillis()) + ": " + name + " - " + dustLevel);
			block(1000);
		}
		
	}
}
