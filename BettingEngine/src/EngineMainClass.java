import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;


public class EngineMainClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CreateReaderProcess cp = new CreateReaderProcess();
		cp.create();
		
		Engine eng = new Engine();
		eng.spark();
	}



}
