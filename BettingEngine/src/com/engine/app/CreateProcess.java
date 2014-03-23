package com.engine.app;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class CreateProcess {

	void start(){
		ProcessBuilder processBuilder = new ProcessBuilder();
		
		String[] sendCommand = { "cmd.exe", "D:\\Work\\yowsup\\whatsapp\\whatsapp.bat" , "13026901224-1394919313"};
		
		
		//processBuilder.directory(new File("D:\\Work\\yowsup\\whatsapp"));
		//processBuilder.environment().put("PYTHONPATH", "D:\\Work\\yowsup\\whatsapp");
		//processBuilder.command(sendCommand);
		// System.out.println(processBuilder.environment());
		//Process process;
		try {
			//process = processBuilder.start();
			//Process process = new ProcessBuilder("D:\\Work\\yowsup\\whatsapp\\whatsapp.bat", "13026901224-1394919313").start();
			Process process = new ProcessBuilder("D:\\Work\\yowsup\\whatsapp\\whatsapp.bat", "13026901224-1394919313").start();
			process.waitFor();
			InputStream stream = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String str = null;
			while ((str = br.readLine()) != null) {
			    System.out.println(str);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
}
