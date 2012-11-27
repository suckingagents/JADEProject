package MAS;

import java.io.Serializable;

public class Msg implements Serializable{
	static final int TIME_LAPSE = 100;
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
