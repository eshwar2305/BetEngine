import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class CreateReaderProcess implements LogFileTailerListener{
	private LogFileTailer m_tailer;
	Process p;
	String[] m_cmd = {
	        "python",
	        "Reader_Yowsup\\yowsup-cli",
	       // "-d",
	        "-c",
	        "Reader_Yowsup\\config.example",
	        "-i",
	        "13026901224-1394919313" // Terminator
	       // "13026901224", //Send msg to Eshu number
	        //"13026901224-1333685017", -- BG family
	        //"919686811944-1394712848", //- BAri talk
	    };
	
	@Override
	public void newLogFileLine(String newLine) {
		System.out.println(newLine);
		if(newLine.contains("Disconnected")){
	    	try {
	    		if(p == null){
	    			System.out.println("Creating Process for First Time");
	    			p = Runtime.getRuntime().exec(m_cmd);
	    		}else{
	    			System.out.println("Destroying Process...");
	    			p.destroy();
	    			System.out.println("Creating Duplicate Process..");
	    			p = Runtime.getRuntime().exec(m_cmd);
	    		}
	    		
	    		/*BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    		while(reader.readLine() != null){
	    			System.out.println("Reader - " + reader.readLine());
	    		}*/	
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
		}
	}

	public void create() {
		newLogFileLine("Disconnected");
	    m_tailer = new LogFileTailer( new File( "Reader_Yowsup\\Examples\\disconnected_out.txt" ), 1000, false );
	    m_tailer.addLogFileTailerListener( this );
	    m_tailer.start();
	}
}

