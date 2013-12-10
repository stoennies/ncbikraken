package de.l3s.dlg.ncbikraken.medline;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.l3s.dlg.ncbikraken.Configuration;
import de.l3s.dlg.ncbikraken.medline.model.Abstract;
import de.l3s.dlg.ncbikraken.medline.model.AbstractText;
import de.l3s.dlg.ncbikraken.medline.model.Author;
import de.l3s.dlg.ncbikraken.medline.model.Day;
import de.l3s.dlg.ncbikraken.medline.model.ELocationID;
import de.l3s.dlg.ncbikraken.medline.model.ForeName;
import de.l3s.dlg.ncbikraken.medline.model.Journal;
import de.l3s.dlg.ncbikraken.medline.model.LastName;
import de.l3s.dlg.ncbikraken.medline.model.MedlineCitation;
import de.l3s.dlg.ncbikraken.medline.model.MeshHeading;
import de.l3s.dlg.ncbikraken.medline.model.MeshHeadingList;
import de.l3s.dlg.ncbikraken.medline.model.Month;
import de.l3s.dlg.ncbikraken.medline.model.OtherID;
import de.l3s.dlg.ncbikraken.medline.model.QualifierName;
import de.l3s.dlg.ncbikraken.medline.model.Year;
import de.l3s.dlg.ncbikraken.storage.db.Columns;
import de.l3s.dlg.ncbikraken.storage.db.DBUtil;
import eu.toennies.snippets.ExceptionUtils;
import eu.toennies.snippets.crypt.AeSimpleMD5;
import eu.toennies.snippets.db.DbPoolingDriver;
import eu.toennies.snippets.db.PoolingException;

/**
 * This class is a helper class to store a (@see PMCArticle) into the database.
 * 
 * @author toennies
 * 
 */
public final class MedlineCitationToMySql {

	private Logger log = LoggerFactory.getLogger(MedlineCitationToMySql.class);

	private Connection con;

	private PreparedStatement insertDocument, insertAuthor, insertDocToAuthor, insertJournal, insertPublishedIn,
			insertAbstractStructure, insertMeshHeading, insertError, insertMissingDocument;

	private String schema;

	/**
	 * 
	 */
	public MedlineCitationToMySql(String schemaName) {
		this.schema = schemaName;
		try {
			DbPoolingDriver.setConfig(Configuration.INSTANCE);

			this.con = DBUtil.getConnection();

			insertError = con.prepareStatement("INSERT IGNORE INTO " + schema + ".ERRORS (id,trace) VALUES (?,?)");
			insertMissingDocument = con
					.prepareStatement("INSERT IGNORE  INTO " + schema + ".MISSING_DOCUMENTS (id,path,reason) VALUES (?,?,?)");

			insertMeshHeading = con
					.prepareStatement("INSERT IGNORE INTO " + schema + ".MESH_HEADINGS (document, descriptor, qualifier, descriptor_major, qualifier_major) VALUES (?,?,?,?,?)");
			insertAbstractStructure = con
					.prepareStatement("INSERT IGNORE INTO " + schema + ".ABSTRACT_STRUCTURES (pmid,category,label,text) VALUES (?,?,?,?)");
			insertDocument = con
					.prepareStatement("INSERT IGNORE INTO " + schema + ".DOCUMENTS (pmid,pmc,doi,title,abstract) VALUES (?,?,?,?,?)");
			insertAuthor = con
					.prepareStatement("INSERT IGNORE INTO " + schema + ".AUTHORS (author_id,firstname,lastname,affiliation) VALUES (?,?,?,?)");
			insertDocToAuthor = con.prepareStatement("INSERT IGNORE INTO " + schema + ".AUTHORED_BY (author,document) VALUES (?,?)");
			insertJournal = con.prepareStatement("INSERT IGNORE INTO " + schema + ".JOURNALS (issn,title,title_abrev) VALUES (?,?,?)");
			insertPublishedIn = con
					.prepareStatement("INSERT IGNORE INTO " + schema + ".PUBLISHED_IN (document,journal,volume,issue,day,month,year) VALUES (?,?,?,?,?,?,?)");
		} catch (PoolingException e) {
			throw new RuntimeException("Could not generate DB connection: " + e.getMessage());
		} catch (SQLException e) {
			throw new RuntimeException("Could not generate DB connection: " + e.getMessage());
		}
		log.trace("dbStore object created");
	}

	/**
	 * Store the article into the database.
	 * 
	 * @param article
	 *            - the (@see PMCArticle) to store into the database
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws SQLException
	 * @throws Exception
	 */
	public void storeCitation(final MedlineCitation citation) throws NoSuchAlgorithmException,
			UnsupportedEncodingException, SQLException, NullPointerException {
		con.setAutoCommit(false);

		insertDocument(citation);
		// list of authors can be null
		if (citation.getArticle().getAuthorList() != null) {
			insertAuthors(citation);
		}
		insertJournal(citation);
		insertMeshHeading(citation);
		con.commit();
	}

	private void insertMeshHeading(MedlineCitation citation) throws SQLException {

		// document, descriptor, qualifier, descriptor
		// major, qualifier major
		final Integer pmid = Integer.parseInt(citation.getPMID().getvalue());
		MeshHeadingList headings = citation.getMeshHeadingList();
		if (headings == null) {
			return;
		}
		for (MeshHeading mesh : citation.getMeshHeadingList().getMeshHeading()) {
			final String descriptor = mesh.getDescriptorName().getvalue();
			if (mesh.getQualifierName().size() == 0) {
				insertMeshHeading.setInt(Columns.ONE.ordinal(), pmid);
				insertMeshHeading.setString(Columns.TWO.ordinal(), descriptor);
				insertMeshHeading.setNull(Columns.THREE.ordinal(), Types.VARCHAR);
				insertMeshHeading.setString(Columns.FOUR.ordinal(), mesh.getDescriptorName().getMajorTopicYN());
				insertMeshHeading.setNull(Columns.FIVE.ordinal(), Types.CHAR);
				insertMeshHeading.addBatch();
			} else {
				for (QualifierName qualifier : mesh.getQualifierName()) {
					insertMeshHeading.setInt(Columns.ONE.ordinal(), pmid);
					insertMeshHeading.setString(Columns.TWO.ordinal(), descriptor);
					insertMeshHeading.setString(Columns.THREE.ordinal(), qualifier.getvalue());
					insertMeshHeading.setString(Columns.FOUR.ordinal(), mesh.getDescriptorName().getMajorTopicYN());
					insertMeshHeading.setString(Columns.FIVE.ordinal(), qualifier.getMajorTopicYN());
					insertMeshHeading.addBatch();
				}

			}
		}
		insertMeshHeading.executeBatch();
	}

	private void insertJournal(MedlineCitation citation) throws SQLException {
		final Integer pmid = Integer.parseInt(citation.getPMID().getvalue());
		final Journal journal = citation.getArticle().getJournal();
		String issn;
		if (journal.getISSN() != null) {
			issn = journal.getISSN().getvalue();
		} else {
			issn = citation.getMedlineJournalInfo().getNlmUniqueID();
		}
		final String title = journal.getTitle();
		final String abrev = journal.getISOAbbreviation();
		final String volume = journal.getJournalIssue().getVolume();
		final String issue = journal.getJournalIssue().getIssue();

		// issn,title,title_abrev
		insertJournal.setString(Columns.ONE.ordinal(), issn);
		insertJournal.setString(Columns.TWO.ordinal(), title);
		insertJournal.setString(Columns.THREE.ordinal(), abrev);
		insertJournal.execute();

		// document,journal,volume,issue,day,month,year
		insertPublishedIn.setInt(Columns.ONE.ordinal(), pmid);
		insertPublishedIn.setString(Columns.TWO.ordinal(), issn);
		insertPublishedIn.setString(Columns.THREE.ordinal(), volume);
		insertPublishedIn.setString(Columns.FOUR.ordinal(), issue);

		// first init with null
		insertPublishedIn.setNull(Columns.FIVE.ordinal(), Types.INTEGER);
		insertPublishedIn.setNull(Columns.SIX.ordinal(), Types.INTEGER);
		insertPublishedIn.setNull(Columns.SEVEN.ordinal(), Types.INTEGER);
		// if avaialable than reset to value
		for (Object o : journal.getJournalIssue().getPubDate().getYearOrMonthOrDayOrSeasonOrMedlineDate()) {
			if (o instanceof Year) {
				Year year = (Year) o;
				insertPublishedIn.setInt(Columns.SEVEN.ordinal(), Integer.parseInt(year.getvalue()));
			} else if (o instanceof Month) {
				Month month = (Month) o;
				if (month.getvalue().matches("\\D{3}")) {
					try {
						Date date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(month.getvalue());
						insertPublishedIn.setInt(Columns.SIX.ordinal(),
								Integer.parseInt(new SimpleDateFormat("MM", Locale.ENGLISH).format(date)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else if (month.getvalue().matches("\\D{4,}")) {
					try {
						Date date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(month.getvalue());
						insertPublishedIn.setInt(Columns.SIX.ordinal(),
								Integer.parseInt(new SimpleDateFormat("MM", Locale.ENGLISH).format(date)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else {
					insertPublishedIn.setInt(Columns.SIX.ordinal(), Integer.parseInt(month.getvalue()));
				}
			} else if (o instanceof Day) {
				Day day = (Day) o;
				insertPublishedIn.setInt(Columns.FIVE.ordinal(), Integer.parseInt(day.getvalue()));
			}
		}
		insertPublishedIn.execute();
	}

	private void insertAuthors(MedlineCitation citation) throws SQLException, NoSuchAlgorithmException,
			UnsupportedEncodingException {
		for (Author author : citation.getArticle().getAuthorList().getAuthor()) {
			String foreName = "";
			String lastName = "";
			for (Object o : author.getLastNameOrForeNameOrInitialsOrSuffixOrCollectiveName()) {

				if (o instanceof ForeName) {
					ForeName fName = (ForeName) o;
					foreName = fName.getvalue();
				} else if (o instanceof LastName) {
					LastName lname = (LastName) o;
					lastName = lname.getvalue();
				}
			}

			final String authorId = AeSimpleMD5.MD5(foreName + " " + lastName);
			// author_id,firstname,lastname,affiliation
			insertAuthor.setString(Columns.ONE.ordinal(), authorId);
			insertAuthor.setString(Columns.TWO.ordinal(), foreName);
			insertAuthor.setString(Columns.THREE.ordinal(), lastName);
			insertAuthor.setString(Columns.FOUR.ordinal(), citation.getArticle().getAffiliation());
			insertAuthor.execute();

			// author,document
			insertDocToAuthor.setString(Columns.ONE.ordinal(), authorId);
			insertDocToAuthor.setInt(Columns.TWO.ordinal(), Integer.parseInt(citation.getPMID().getvalue()));
			insertDocToAuthor.addBatch();

		}
		// insertAuthor.executeBatch();
		insertDocToAuthor.executeBatch();
	}

	private void insertDocument(MedlineCitation citation) throws NumberFormatException, SQLException {
		// pmid,pmc,doi,title,abstract
		insertDocument.setInt(Columns.ONE.ordinal(), Integer.parseInt(citation.getPMID().getvalue()));

		String pmcid = null;
		for (OtherID id : citation.getOtherID()) {
			if (id.getSource().equals("NLM") && id.getvalue().startsWith("PMC")) {
				pmcid = id.getvalue().substring(3);
			}
		}
		if (pmcid != null) {
			insertDocument.setInt(Columns.TWO.ordinal(), Integer.parseInt(pmcid));
		} else {
			insertDocument.setNull(Columns.TWO.ordinal(), Types.INTEGER);
		}

		String doi = null;
		for (Object test : citation.getArticle().getPaginationOrELocationID()) {
			if (test instanceof ELocationID) {
				ELocationID id = (ELocationID) test;
				if (id.getEIdType().equals("doi")) {
					doi = id.getvalue();
				}
			}
		}
		if (doi != null) {
			insertDocument.setString(Columns.THREE.ordinal(), doi);
		} else {
			insertDocument.setNull(Columns.THREE.ordinal(), Types.VARCHAR);
		}

		insertDocument.setString(Columns.FOUR.ordinal(), citation.getArticle().getArticleTitle());

		// Abstract as fulltext into documents table and structured into
		// AbstractAtructure table
		Abstract abstrakt = citation.getArticle().getAbstract();
		if (abstrakt == null) {
			insertDocument.setNull(Columns.FIVE.ordinal(), Types.VARCHAR);
		} else {
			StringBuffer abstractFullText = new StringBuffer();
			for (AbstractText text : citation.getArticle().getAbstract().getAbstractText()) {
				abstractFullText.append(text.getvalue());

				// pmid,category,label,text
				final String category = text.getNlmCategory();
				final String label = text.getLabel();

				if (category != null) {
					insertAbstractStructure.setInt(Columns.ONE.ordinal(),
							Integer.parseInt(citation.getPMID().getvalue()));
					insertAbstractStructure.setString(Columns.TWO.ordinal(), text.getNlmCategory());
					if (label != null) {
						insertAbstractStructure.setString(Columns.THREE.ordinal(), text.getLabel());
					} else {
						insertAbstractStructure.setNull(Columns.THREE.ordinal(), Types.VARCHAR);
					}
					insertAbstractStructure.setString(Columns.FOUR.ordinal(), text.getvalue());
					insertAbstractStructure.execute();
				}
			}
			insertDocument.setString(Columns.FIVE.ordinal(), abstractFullText.toString());
		}
		insertDocument.execute();
	}

	public void close() {
		DBUtil.closeSilent(insertAuthor);
		DBUtil.closeSilent(insertDocToAuthor);
		DBUtil.closeSilent(insertDocument);
		DBUtil.closeSilent(insertJournal);
		DBUtil.closeSilent(insertPublishedIn);
		DBUtil.closeSilent(insertMeshHeading);
		DBUtil.closeSilent(insertError);
		DBUtil.closeSilent(insertMissingDocument);
		DBUtil.closeSilent(con);
	}

	public void rollback() {
		try {
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void logError(String errorId, Exception e, int documentId, String documentPath) {

		try {
			System.out.println(errorId.length());
			insertError.setString(Columns.ONE.ordinal(), errorId);
			insertError.setString(Columns.TWO.ordinal(), ExceptionUtils.getStackTrace(e));
			insertError.execute();

			insertMissingDocument.setInt(Columns.ONE.ordinal(), documentId);
			insertMissingDocument.setString(Columns.TWO.ordinal(), documentPath);
			insertMissingDocument.setString(Columns.THREE.ordinal(), errorId);
			insertMissingDocument.execute();
			con.commit();
		} catch (SQLException e2) {
			System.out.println("THIS SHOULD NOT HAPPEN!" + e2.getLocalizedMessage());
			close();
			System.exit(1);
		}

	}

}
