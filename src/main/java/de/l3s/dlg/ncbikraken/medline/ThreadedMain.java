package de.l3s.dlg.ncbikraken.medline;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadedMain {

	public final static String statusFile = "lastDoc.log";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 3) {
			exit();
		}
		
		// check medline files directory
		String directoryName = args[0];
		File directory = new File(directoryName);
		if(!directory.exists() || directory.isFile()) {
			exit();
		}

		// check for valid poolSize
		int threadPoolSize = 0;
		try {
			threadPoolSize = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			exit();
		}
		
		// check for valid file ending
		String fileEnding = args[2];
		if(!(fileEnding.equals("zip") || fileEnding.equals("gz"))) {
			exit();
		}
		
		
		// Creating shared object
		BlockingQueue<File> sharedQueue = new LinkedBlockingQueue<File>();

		try {
			// Creating and starting Producer Thread
			Thread prodThread = new Thread(new FileProducer(sharedQueue, directory, fileEnding));
			prodThread.start();

			// Creating and starting Consumer thread
			for (int i = 0; i < threadPoolSize; i++) {
				Thread consThread = new Thread(new MedlineParser(sharedQueue));
				consThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void exit() {
		System.out.println("Please provide path to medline files, thread pool size and file ending (xml or gz).");
		System.exit(1);
	}

}
