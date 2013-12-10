package de.l3s.dlg.ncbikraken.medline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.l3s.dlg.ncbikraken.Configuration;
import de.l3s.dlg.ncbikraken.medline.model.MedlineCitation;
import eu.toennies.snippets.ComputingTime;
import eu.toennies.snippets.MemoryUsage;
import eu.toennies.snippets.crypt.AeSimpleMD5;

public class MedlineParser implements Runnable {

	private static Logger log = LoggerFactory.getLogger(MedlineParser.class);
	private final BlockingQueue<File> sharedQueue;

	public MedlineParser(BlockingQueue<File> sharedQueue) {
		this.sharedQueue = sharedQueue;
	}

	@Override
	public void run() {
		MemoryUsage memory = new MemoryUsage();
		long startTime = System.currentTimeMillis();

		FileWriter statusWriter = null;
		while (!this.sharedQueue.isEmpty()) {
			try {
				File file = this.sharedQueue.take();
				importFile(file);
				try {
					statusWriter = new FileWriter(ThreadedMain.statusFile, false);
					statusWriter.write(file.getAbsolutePath());
					statusWriter.flush();
				} catch (IOException e) {
					log.warn("Could not store status information: " + e.getMessage());
				} finally {
					try {
						if (statusWriter != null) {
							statusWriter.close();
						}
					} catch (IOException e) {
						log.error(e.getMessage());
					}
				}
			} catch (InterruptedException e1) {
				log.warn(e1.getLocalizedMessage());
			}

		}

		log.info("Memory used: " + memory.getRoundedMB());
		log.info("Elapsed time: " + ComputingTime.getInHours(startTime));
	}

	private static void importFile(File file) {
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		MedlineCitationToMySql storage = new MedlineCitationToMySql(Configuration.INSTANCE.getMedlineDbTableName());
		try {
			InputStream in;
			if(file.getAbsolutePath().endsWith("gz")) {
				in = new GZIPInputStream(new FileInputStream(file));
			} else if(file.getAbsolutePath().endsWith("zip")) {
					in = new ZipInputStream(new FileInputStream(file));
			} else {
				in = new FileInputStream(file);
			}
			XMLEventReader xmler = xmlif.createXMLEventReader(in);
			EventFilter filter = new EventFilter() {
				public boolean accept(XMLEvent event) {
					return event.isStartElement();
				}
			};
			XMLEventReader xmlfer = xmlif.createFilteredReader(xmler, filter);

			// Jump to the first element in the document, the enclosing
			// DescriptorRecordSet
			@SuppressWarnings("unused")
			StartElement startElement = (StartElement) xmlfer.nextEvent();

			// Parse into typed objects
			JAXBContext ctx = JAXBContext.newInstance("de.l3s.dlg.ncbikraken.medline.model");
			Unmarshaller um = ctx.createUnmarshaller();

			while (xmlfer.peek() != null) {
				Object o = um.unmarshal(xmler);
				if (o instanceof MedlineCitation) {
					MedlineCitation citation = (MedlineCitation) o;
					
					try {
						storage.storeCitation(citation);
						log.info("Stored in db: " + citation.getArticle().getArticleTitle());
					} catch (Exception e) {
						storage.rollback();
						log.warn("Could not store citation: " + citation.getArticle().getArticleTitle() + " --> "
								+ e.getMessage());

						try {
							String errorId = AeSimpleMD5.MD5(e.getMessage());
							storage.logError(errorId, e, Integer.parseInt(citation.getPMID().getvalue()),
									file.getAbsolutePath());
						} catch (NoSuchAlgorithmException e1) {
							e1.printStackTrace();
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
					}

				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			log.warn("Could not handle file: " + file.getAbsolutePath());
		} catch (IOException e) {
			log.warn("Could not close filestream: " + file.getAbsolutePath());
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			log.warn("Could not parse or unmarshall xml file: " + file.getAbsolutePath());
		} finally {
			storage.close();
		}

	}

}
