package cli.command.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import app.AppConfig;
import cli.CLIParser;
import cli.command.CLICommand;
import mutex.SuzukiKasamiMutex;
import servent.SimpleServentListener;

public class QuitCommand implements CLICommand{

	private CLIParser parser;
	private SimpleServentListener listener;
	private SuzukiKasamiMutex mutex;
	
	public QuitCommand(CLIParser parser, SimpleServentListener listener, SuzukiKasamiMutex mutex) {
		this.parser = parser;
		this.listener = listener;
		this.mutex = mutex;
	}
	
	@Override
	public String commandName() {
		return "quit";
	}

	@Override
	public void execute(String args) {
		
		if (AppConfig.chordState.getAllNodeInfo().size() == 0){
			mutex.lock();

			try {
				Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);

				PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
				bsWriter.write("Remove\n" + AppConfig.myServentInfo.getListenerPort() + "\n");

				bsWriter.flush();
				bsSocket.close();
			} catch (UnknownHostException e) {
				AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
			} catch (IOException e) {
				AppConfig.timestampedErrorPrint(e.getMessage() + "Errors" + Arrays.toString(e.getStackTrace()));
			}

			parser.stop();
			listener.stop();
			mutex.unlock(false);
			
		} else {
			
			mutex.lock();
			
			AppConfig.chordState.turnOffNode();
			
			AppConfig.timestampedStandardPrint("Quiting.... " + AppConfig.chordState.getAllNodeInfo().size());
			
			parser.stop();
		}			
		
	}

}
