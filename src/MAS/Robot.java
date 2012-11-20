package MAS;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

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
	int dustRemoveRatio;
	protected void setup(){
		room = null;
		name = getAID().getLocalName();
		dustRemoveRatio = -9;
		
		Object[] args = getArguments();
        String s;
        if (args != null) {
            room = (String) args[0];
            System.out.println(name + " in room " + room);
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
        
        addBehaviour( new cleainingBehaviour( this, 1000));
        addBehaviour(new ListenBehaviour( this ));
	}
	
	class ListenBehaviour extends CyclicBehaviour {
		Agent agent;
		public ListenBehaviour(Agent a){
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
					Msg.RoomStatus roomStatus =(Msg.RoomStatus) myObject; 
					System.out.println(new Date(System.currentTimeMillis()) + ": " + roomStatus.name + " has dustlevel " + roomStatus.dustLevel + " ("+name+")");
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
			int dDust = dustRemoveRatio;
			Msg.RobotStatus myStatus = new Msg.RobotStatus(name, dDust);
			try {
				msg.setContentObject(myStatus);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			send( msg );
		}
		
	}
}
