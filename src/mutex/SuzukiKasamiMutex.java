package mutex;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import app.AppConfig;
import app.ServentInfo;
import servent.message.project.RequestMessage;
import servent.message.project.TokenMessage;
import servent.message.util.MessageUtil;

public class SuzukiKasamiMutex {

	private List<Integer> RN = new CopyOnWriteArrayList<Integer>();
	private static TokenMessage tokenMessage;
	public static AtomicBoolean hasToken = new AtomicBoolean(false);
	private AtomicBoolean isInCS = new AtomicBoolean(false);
	
	public SuzukiKasamiMutex() {
		tokenMessage = null;
		
		for (int i = 0; i < AppConfig.chordState.CHORD_SIZE; i++) {
			RN.add(0);
		}
	}
	
	public static void setToken(TokenMessage token) {
		tokenMessage = token;
		hasToken.set(true);
	}
	
	public void lock() {
		
		if (!hasToken.get()) {
			incrementRequestNumber();
			int sn = mySequenceNumber();
			

			ServentInfo receiver = AppConfig.chordState.getSuccessorTable()[0];
			if(receiver == null) {
				receiver = AppConfig.myServentInfo;
			}
			RequestMessage requestMessage = new RequestMessage(AppConfig.myServentInfo, receiver, sn);
			MessageUtil.sendMessage(requestMessage);

			AppConfig.timestampedErrorPrint("SuzukiKasamiMutex -> lock() : Node with ID " + AppConfig.myServentInfo.getChordId() + " asked for TOKEN!");
			
		}

		while(!(hasToken.get() && isInCS.compareAndSet(false, true))) {
			try {

				Thread.sleep(100);
				

				AppConfig.timestampedErrorPrint("SuzukiKasamiMutex  Node " + AppConfig.myServentInfo.getChordId() + " is waiting........");
			} catch (InterruptedException e) {
				AppConfig.timestampedErrorPrint("SuzukiKasamiMutex : " + e.getMessage() + "Error : " + Arrays.toString(e.getStackTrace()));
			}
			
		}
		
		AppConfig.timestampedErrorPrint("SuzukiKasamiMutex ID " + AppConfig.myServentInfo.getChordId() + " got token for CS........");
			
	}
	

	public void unlock(boolean b) {
		
		try {
			int i = AppConfig.myServentInfo.getChordId();

			tokenMessage.getLN().set(i, RN.get(i));

			for(ServentInfo si : AppConfig.chordState.getAllNodeInfo()) {

				int k = si.getChordId();


				if(!tokenMessage.getIds().contains(si) && (RN.get(k) == (tokenMessage.getLN().get(k) + 1))) {

					tokenMessage.getIds().add(si);

				}

			}

			if(!tokenMessage.getIds().isEmpty()) {
				ServentInfo tokenReceiver = tokenMessage.getIds().poll();

				ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(tokenReceiver.getChordId());

				TokenMessage newTokenMessage = this.getTokenMessage().createNewToken(AppConfig.myServentInfo, receiver, tokenReceiver);
				MessageUtil.sendMessage(newTokenMessage);

				hasToken.set(false);

				AppConfig.timestampedErrorPrint("SuzukiKasamiMutex unlock Node ID " + AppConfig.myServentInfo.getChordId() + " sent TOKEN to node with ID " + receiver.getChordId());
			} else if(b){

				ServentInfo receiver = AppConfig.chordState.getSuccessorTable()[0];
				TokenMessage newTokenMessage = this.getTokenMessage().createNewToken(AppConfig.myServentInfo, receiver, receiver);
				MessageUtil.sendMessage(newTokenMessage);
				AppConfig.timestampedErrorPrint("SuzukiKasamiMutex -> unlockb () : Node with ID " + AppConfig.myServentInfo.getChordId() + " sent TOKEN to node with ID " + receiver.getChordId());

				hasToken.set(false);
			}

			isInCS.set(false);

			AppConfig.timestampedErrorPrint("SuzukiKasamiMutex unlock Node  " + AppConfig.myServentInfo.getChordId() + " is leaving CS........");


		}catch (Exception e){
			AppConfig.timestampedErrorPrint("SuzukiKasamiMutex unlock(): " + e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
		}
	}

	public boolean messageIsOutdated(int i, int sn) {
		boolean ret = false;
		if (sn < RN.get(i)) {
			ret = true;
		}
		
		RN.set(i, Math.max(RN.get(i), sn));
		
		return ret;
	}


	public boolean checkOutstandingRequest(int chordId) {

		if(RN.get(chordId) == (tokenMessage.getLN().get(chordId) + 1)) {
			return true;
		}
		
		return false;
	}
	
	public void incrementRequestNumber() {
		int i = AppConfig.myServentInfo.getChordId();
		RN.set(i, RN.get(i) + 1);
	}

	public int mySequenceNumber() {
		return RN.get(AppConfig.myServentInfo.getChordId());
	}
	
	public AtomicBoolean getHasToken() {
		return hasToken;
	}
	public void setHasToken(AtomicBoolean hasToken) {
		SuzukiKasamiMutex.hasToken = hasToken;
	}
	
	public TokenMessage getTokenMessage() {
		return tokenMessage;
	}
	public void setTokenMessage(TokenMessage tokenMessage) {
		this.tokenMessage = tokenMessage;
	}
	
	public AtomicBoolean getIsInCS() {
		return isInCS;
	}
	
	public void setIsInCS(AtomicBoolean isInCS) {
		this.isInCS = isInCS;
	}
}
