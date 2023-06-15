package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import mutex.SuzukiKasamiMutex;
import servent.message.NewNodeMessage;
import servent.message.project.TokenMessage;
import servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {

	private int getSomeServentPort() {
		int bsPort = AppConfig.BOOTSTRAP_PORT;
		
		int retVal = -2;
		
		try {
			Socket bsSocket = new Socket("localhost", bsPort);
			
			//9 : Formatiramo hail poruku kojom kazemo BS da hocemo da se prikljucimo
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Hail\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();
			
			Scanner bsScanner = new Scanner(bsSocket.getInputStream());
			retVal = bsScanner.nextInt();
			
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	@Override
	public void run() {
		
		//9 : pita bootstrap za neki ID u sistemu, i onda njemu hocemo da posaljemo poruku new node
		int someServentPort = getSomeServentPort();
		
		if (someServentPort == -2) {
			
			AppConfig.timestampedErrorPrint("Error in contacting bootstrap. Exiting...");
			System.exit(0);
			
		}
		
		if (someServentPort == -1) { //bootstrap gave us -1 -> we are first
			AppConfig.timestampedStandardPrint("First node in Chord system.");
			
			TokenMessage tokenMessage = new TokenMessage(AppConfig.myServentInfo, AppConfig.myServentInfo, AppConfig.myServentInfo);
		
			SuzukiKasamiMutex.setToken(tokenMessage);
			
			AppConfig.myServentInfo.setRbs(AppConfig.myServentInfo.getChordId());
			

		} else { //bootstrap gave us something else - let that node tell our successor that we are here
			
			//TODO : BootStrap treba da vrati i IP!
			String ipAddress = "localhost";
			ServentInfo dummy = new ServentInfo(ipAddress, someServentPort);
			
			NewNodeMessage nnm = new NewNodeMessage(AppConfig.myServentInfo, dummy, dummy.getChordId());
			MessageUtil.sendMessage(nnm);

			AppConfig.myServentInfo.setRbs(dummy.getChordId());
			
		}
	}

}
