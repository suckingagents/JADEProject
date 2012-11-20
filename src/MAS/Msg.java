package MAS;

import java.io.Serializable;

public class Msg implements Serializable{
	static class RobotStatus implements Serializable{
		int deltaDust;
		String name;
		public RobotStatus(String name, int dDust){
			this.name = name;
			this.deltaDust = dDust;
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
}
