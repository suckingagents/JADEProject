package MAS;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import MAS.Msg.RoomStatus;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Robot extends Agent{
	String name;
	String room;
	Msg.RoomStatus roomStatus;
	Msg.RobotStatus robotStatus;
	static final int roomChangeCost = 20; 
	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	int dustRemoveRatio;
	protected void setup(){
		room = null;
		name = getAID().getLocalName();
		dustRemoveRatio = -6;
		
		Object[] args = getArguments();
        String s;
        if (args != null) {
            room = (String) args[0];
            //System.out.println(new Date(System.currentTimeMillis()) + ": " + name + " in room " + room);
        }
        
        // register to DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName( getAID() ); 
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "Robot" );
        sd.setName( getLocalName() );
        dfd.addServices(sd);
        
        try {  
            DFService.register(this, dfd );  
        }
        catch (FIPAException fe) { 
        	fe.printStackTrace(); 
        }
        
        addBehaviour( new cleainingBehaviour( this, Msg.TIME_LAPSE));
        addBehaviour(new ListenBehaviour( this ));    
	}
	
	class ListenBehaviour extends CyclicBehaviour {
		Agent agent;
		static final int ST_IDLE = 0;
		static final int ST_WAIT_FOR_ACCEPT = 1;
		int state;
		long q0;
		int MAX_WAIT_TIME = Msg.TIME_LAPSE * 2;
		public ListenBehaviour(Agent a){
			state = ST_IDLE;
			this.agent = a;
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
					RoomStatus tmpRoomStatus = (Msg.RoomStatus) myObject; 
					if (room.equals(tmpRoomStatus.name)){
						roomStatus = tmpRoomStatus;	
					}
					  
					//System.out.println(new Date(System.currentTimeMillis()) + ": " + roomStatus.name + " has dustlevel " + roomStatus.dustLevel + " ("+name+")");
				}	
				
				if (myObject instanceof Msg.RoomBargin){
					Msg.RoomBargin bargain = (Msg.RoomBargin) myObject;
					//System.out.println(name + " received bargin: " + bargain.type);
					switch (state) {
					case ST_IDLE:
							//if(bargain.type == Msg.RoomBargin.TYPE_REQUEST && !roomStatus.name.equals(bargain.roomStatus.name)) {
						if(bargain.type == Msg.RoomBargin.TYPE_REQUEST) {
								bargain.sender = Msg.RoomBargin.AGENT_ROBOT;
								bargain.type = Msg.RoomBargin.TYPE_ANSWER;
								bargain.robotStatus = robotStatus; 
								msg = new ACLMessage(ACLMessage.INFORM);
								msg.addReceiver(new AID(bargain.getRoomStatus().name, AID.ISLOCALNAME));
								// Calc
								//System.out.println(name + "\tROBOT STATE IDLE:\t"+ bargain.getRoomStatus().name + " Requests robot");
								//System.out.println(name + "\tROBOT STATE IDLE:\tMy dust demand: " + (roomStatus.dustLevel + roomChangeCost) + " VS. " + bargain.getRoomStatus().dustLevel);
								if ((roomStatus.dustLevel + roomChangeCost) < bargain.getRoomStatus().dustLevel){
									// send yes
									bargain.accept = true;
									try {
										msg.setContentObject(bargain);
										send(msg);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}			
									q0 = System.currentTimeMillis();
									state = ST_WAIT_FOR_ACCEPT;
								}else{
									// send no
									bargain.accept = false;
									try {
										msg.setContentObject(bargain);
										send(msg);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									state = ST_IDLE;
								}
								//System.out.println(name + "\t ROBOT STATE IDLE: sent msg: " +bargain.accept + " to " + bargain.getRoomStatus().name );
							}
						break;
					case ST_WAIT_FOR_ACCEPT:
						//System.out.println(name + "\tROBOT STATE WAIT:\t Received msg: " + bargain.type);
						// timer > rooms timeout
						long q1 = System.currentTimeMillis();
						q1 = q1 - q0;
						if ( q1 >= MAX_WAIT_TIME ){
							// go back to idle
							state = ST_IDLE;
							//System.err.println(name + " didn't receive accept from room");
						}
						if(bargain.type == Msg.RoomBargin.TYPE_ANSWER) {
							//System.out.println(name + "\tROBOT STATE WAIT:\t received answer from room");
							if (bargain.isAccept()){
								//System.out.println("ROBOT STATE WAIT:\t asnwer was yes");
								// change room
								room = bargain.roomStatus.name;
								//System.err.println(name + " changes room to " + room);
								state = ST_IDLE;
							}
						}
						break;

					default:
						break;
					}
				}

			}
			block(1000);
		}
		
	}
	
	class cleainingBehaviour extends TickerBehaviour {
		Agent agent;
		public cleainingBehaviour(Agent a, long l){
			super(a, l);
			this.agent = a;
		}
		
		@Override
		protected void onTick() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver( new AID( room , AID.ISLOCALNAME));
			msg.addReceiver(new AID( "gui", AID.ISLOCALNAME ));
			msg.addReceiver(new AID( "stats", AID.ISLOCALNAME ));
			int dDust = dustRemoveRatio;
			robotStatus = new Msg.RobotStatus(name, room, dDust);
			try {
				msg.setContentObject(robotStatus);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			send( msg );
		}
		
	}
}
