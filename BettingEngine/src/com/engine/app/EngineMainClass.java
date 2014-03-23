package com.engine.app;
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
		// TODO Auto-generated method stub

		/*String[] cmd = {
		        "python",
		        "D:\\Work\\yowsup\\BettingEngine\\yowsup-cli",
		        "-c",
		        "config.example",
		        "-s",
		        "13026901224",
		        "FromBettingEngine"
		    };
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while(reader.readLine() != null){
				System.out.println(reader.readLine());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		CreateProcess cp = new CreateProcess();
		cp.start();
		
		Engine eng = new Engine();
		eng.spark();
	}



}
