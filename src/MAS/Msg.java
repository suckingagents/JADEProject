package MAS;

import java.io.Serializable;

public class Msg implements Serializable{
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
}
