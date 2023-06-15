package app;

import java.io.File;
import java.io.Serializable;

/**
 * This is an immutable class that holds all the information for a servent.
 *
 * @author bmilojkovic
 */
public class ServentInfo implements Serializable {

	private static final long serialVersionUID = 5304170042791281555L;
	private final String ipAddress;
	private final int listenerPort;
	
	private final int chordId;
	
	private final String workingDirectory;
	private final String storage;
	
	private int rbs;
	
	public ServentInfo(String ipAddress, int listenerPort, String workingDirectory, String storage) {
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;
		this.chordId = ChordState.chordHash(listenerPort);
	

		this.workingDirectory = workingDirectory.replace("/", File.separator);
		this.storage = storage.replace("/", File.separator);
		
		AppConfig.timestampedStandardPrint("storage " + this.storage + " working " +this.workingDirectory);
		this.rbs = -1;
	}
	
	public ServentInfo(String ipAddress, int listenerPort) {
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;
		this.chordId = ChordState.chordHash(listenerPort);
		
		this.workingDirectory = "data"+ File.separator + "working";
		this.storage = "data"+ File.separator + "storage";
		
		this.rbs = -1;
	}
	
	
	public String getStorage() {
		return storage;
	}
	
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public int getRbs() {
		return rbs;
	}
	
	public void setRbs(int rbs) {
		this.rbs = rbs;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof ServentInfo))
			return false;
			
		ServentInfo info = (ServentInfo) obj;
		return this.ipAddress.equals(info.ipAddress) && this.listenerPort == info.listenerPort;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getListenerPort() {
		return listenerPort;
	}

	public int getChordId() {
		return chordId;
	}
	
	@Override
	public String toString() {
		return "[" + chordId + "|" + ipAddress + "|" + listenerPort + "]";
	}



}
