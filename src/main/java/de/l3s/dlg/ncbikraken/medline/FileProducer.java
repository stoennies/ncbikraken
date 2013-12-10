package de.l3s.dlg.ncbikraken.medline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.toennies.snippets.io.FileLister;

public class FileProducer implements Runnable {

	private static Logger log = LoggerFactory.getLogger(FileProducer.class);
	private final BlockingQueue<File> sharedQueue;
	private final List<File> files;

	public FileProducer(BlockingQueue<File> sharedQueue, File directory, String fileEnding) throws IOException {
		this.sharedQueue = sharedQueue;
		files = FileLister.getSortedWindowsExplorerList(directory, new String[] { fileEnding }, false);
	}

	@Override
	public void run() {
		try {
			ListIterator<File> it = checkForRestart();
			while (it.hasNext()) {
				File file = it.next();
				this.sharedQueue.put(file);
				log.debug("Putting file to queue: " + file.getAbsolutePath());

			}
		} catch (IOException e1) {
			log.error(e1.getLocalizedMessage());
		} catch (InterruptedException e) {
			log.warn(e.getLocalizedMessage());
		}

	}

	private ListIterator<File> checkForRestart() throws IOException {
		ListIterator<File> it;
		// check for restart
		File statusLog = new File(ThreadedMain.statusFile);
		if (statusLog.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(statusLog));
			String lastFile = reader.readLine();
			log.info("lockFile exists. Starting with: " + lastFile);
			reader.close();
			
			log.info(lastFile);

			it = files.listIterator(files.lastIndexOf(new File(lastFile)));
		} else {
			it = files.listIterator();
		}
		return it;
	}

}
