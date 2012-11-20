package MAS;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentDescriptor;
import jade.core.behaviours.CyclicBehaviour;
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
	protected void setup(){
//		dustlevel = new Random().nextInt(100);
		dustLevel = 30;
		dustThreshold = 50;
		maxDustLevel = 255;
		dustRatio = 4;
		name = getAID().getLocalName();
		
		addBehaviour(new dustBehaviour( this ));
		//TODO changed tick from 5000 til 1000
//		addBehaviour(new dustManagingBehaviour( this, 5000 ));
		addBehaviour(new dustManagingBehaviour( this, 1000 ));
	}
	
	class dustManagingBehaviour extends TickerBehaviour {
		Agent agent;
		public dustManagingBehaviour(Agent a, long l){
			super(a, l);
			this.agent = a;
		}
		
		public void onTick() {
			//TODO Outcommented for GUI test purposes!
	//		if (dustlevel > threshold){
				System.out.println(new Date(System.currentTimeMillis()) + ": " + name + " wants cleaning!");
				
	//		}else if (dustlevel <= 0){
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
	            	System.out.println("Woolooboolo: " + result[i].getName().getLocalName() );
	            	msg.addReceiver( result[i].getName() );
	            }
	            msg.addReceiver(new AID("gui", AID.ISLOCALNAME)); // Add GUI too
	            
	            try {
					msg.setContentObject(new Msg.RoomStatus(name, dustLevel));
				} catch (IOException e) {
					e.printStackTrace();
				}
	            send( msg );
	//		}
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
			if(dustLevel + dustRatio < maxDustLevel) {
				dustLevel += dustRatio;
			}
			
			ACLMessage msg = receive();
			if (msg != null){
				Object myObject = null;
				try {
					myObject = msg.getContentObject();
				} catch (UnreadableException e1) {
					e1.printStackTrace();
				}
				
				if (myObject instanceof Msg.RobotStatus) {
					Msg.RobotStatus status =(Msg.RobotStatus) myObject; 
					System.out.println(status.name + " is removing " + status.deltaDust + " in room " + name);
					dustLevel += status.deltaDust;
				}
				if (dustLevel < 0){
					dustLevel = 0;
				}
			}
			System.out.println(new Date(System.currentTimeMillis()) + ": " + name + " - " + dustLevel);
			block(1000);
		}
		
	}
}
