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
	String name;
	Msg.RoomStatus roomStatus;
	Msg.GlobalStatus globalStatus;
	boolean doBargain = false;
	protected void setup(){
		dustLevel = new Random().nextInt(Msg.roomThreshold);
		//dustLevel = ;
		dustThreshold = Msg.roomThreshold;
		maxDustLevel = 255;
		dustRatio = Msg.roomDustRatio;
		name = getAID().getLocalName();
		
		// register to DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName( getAID() ); 
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "Room" );
        sd.setName( getLocalName() );
        dfd.addServices(sd);
        
        try {  
            DFService.register(this, dfd );  
        }
        catch (FIPAException fe) { 
        	fe.printStackTrace(); 
        }
		
		addBehaviour(new dustBehaviour( this ));
		addBehaviour(new dustManagingBehaviour( this, Msg.TIME_LAPSE ));
		addBehaviour( new BargainBehavior(this));
	}
	
	class BargainBehavior extends CyclicBehaviour{
		Room room;
		int state;
		static final int ST_IDLE = 0;
		static final int ST_WAIT_INCOMING = 1;
		long q0, q1;
		static final long MAX_WAIT_TIME = Msg.TIME_LAPSE * 2;
		
		public BargainBehavior(Room room){
			this.room = room;
			state = ST_IDLE;
			q0 = System.currentTimeMillis();
			q1 = q0;
		}
		@Override
		public void action() {
			q1 = System.currentTimeMillis();
			Msg.RoomBargin bargin = null;
			ACLMessage msg = receive();
			if (msg != null){
				Object myObject = null;
				try {
					myObject = msg.getContentObject();
				} catch (UnreadableException e1) {
					System.err.println(name + " caught unreadable exception from " + msg.getSender().getLocalName());
					//e1.printStackTrace();
				}
				
				if (myObject instanceof Msg.RoomBargin) {
					bargin = (Msg.RoomBargin) myObject;
				}
			}
			
			//System.out.println(name + " STATE : " + state);
			switch (state) {
			case ST_IDLE:
					if (doBargain){
						if ( (q1 - q0) >= Msg.TIME_LAPSE * 2 ){ // Wait 10 seconds before requesting again
							// request for robot
							bargin = new Msg.RoomBargin(Msg.RoomBargin.AGENT_ROOM, Msg.RoomBargin.TYPE_REQUEST, roomStatus);
							msg = new ACLMessage(ACLMessage.INFORM);
							Msg.addAllAgentsToMsg(room, msg, "Robot");
							//System.err.println(name + " wants to bargin!");
							// send this msg
							q0 = System.currentTimeMillis();
							state = ST_WAIT_INCOMING;
						}
					}else{
						// if YES received answer no
						if (bargin != null){
							if (bargin.isAccept()){
								//System.out.println(name + "\tROOM STATE WAIT:\tACCEPTING ROBOT: " + bargin.robotStatus.name);
								msg = new ACLMessage(ACLMessage.INFORM);
								msg.addReceiver(new AID(bargin.robotStatus.name, AID.ISLOCALNAME));
								bargin.accept = false;
								bargin.type = Msg.RoomBargin.TYPE_ANSWER;
							}
						}
					}
				break;
			case ST_WAIT_INCOMING:
				//System.out.println(name + "\tROOM STATE WAIT:\t Waiting...");
				if ( (q1 - q0) >= MAX_WAIT_TIME ){
					// go back to idle
					doBargain = true;;
					state = ST_IDLE;
					//System.err.println(name + " received NO response from robots");
				}
				
				// Check for answer
				if (bargin != null){
					if (bargin.isAccept()){
						//System.out.println(name + "\tROOM STATE WAIT:\tACCEPTING ROBOT: " + bargin.robotStatus.name);
						//System.err.println(name + " received accept from " + bargin.robotStatus.name);
						msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(new AID(bargin.robotStatus.name, AID.ISLOCALNAME));
						bargin.accept = true;
						bargin.type = Msg.RoomBargin.TYPE_ANSWER;
						doBargain = false;
						q0 = System.currentTimeMillis();
						state = ST_IDLE;
					}else{
						bargin = null; // don't send
					}
				}
					
				break;
			default:
				break;
			}
			
			if (bargin != null){
				try {
					//System.err.println(name + "'s bargin has been sent!");
					msg.setContentObject(bargin);
					send(msg);
					//System.err.println(name + "\tROOM STATE IDLE:\t" + name + ": Request for robot: SENT");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			block(1000);
		}
	}
	class dustManagingBehaviour extends TickerBehaviour {
		long q0; 
		Agent agent;
		public dustManagingBehaviour(Agent a, long l){
			super(a, l);
			this.agent = a;
			q0 = System.currentTimeMillis();
		}
		
		public void onTick() {
			//System.out.println(new Date(System.currentTimeMillis()) + ": " + name + " wants cleaning!");
			long q1 = System.currentTimeMillis();
			if ((q1 - q0) > Msg.TIME_LAPSE){
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
	            msg.addReceiver(new AID("world", AID.ISLOCALNAME)); // Add GUI too
	            
	            try {
	            	roomStatus = new Msg.RoomStatus(name, dustLevel);
					msg.setContentObject(roomStatus);
				} catch (IOException e) {
					e.printStackTrace();
				}
	            send( msg );
			}
			
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
					System.err.println(name + " caught unreadable exception from " + msg.getSender().getLocalName());
					//e1.printStackTrace();
				}
				if (myObject instanceof Msg.GlobalStatus) {
					globalStatus = (Msg.GlobalStatus) myObject; 
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
			
			if (globalStatus != null){
				if(dustLevel > globalStatus.q2){
					doBargain = true;
				}else{
					doBargain = false;
				}
			}
			//System.out.println(new Date(System.currentTimeMillis()) + ": " + name + " - " + dustLevel);
			block(1000);
		}
		
	}
}
