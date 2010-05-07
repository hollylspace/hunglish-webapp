package hu.mokk.hunglish.domain;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.test.RooIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.lucene.HungarianLuceneTester;
import hu.mokk.hunglish.lucene.analysis.StemmerAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
@Configurable
@RooIntegrationTest(entity = Bisen.class)
public class BisenIntegrationTest {

	@Test
	public void testMarkerMethod() {
	}

	@Autowired
	private BisenDataOnDemand dod;

	@Test
	public void testHash() {
		hu.mokk.hunglish.domain.Bisen.updateHashCodes();
	}

	@Test
	public void testFindBisen2() {
		hu.mokk.hunglish.domain.Bisen bisen = hu.mokk.hunglish.domain.Bisen
				.findBisen(new Long(23324));
		System.out.println(bisen);
		System.out.println("countDuplicates:" + bisen.countDuplicates());
	}

	String indexDir = "C:\\temp\\hunglishindex";

	@Test
	public void testSearch() throws CorruptIndexException, IOException, IllegalAccessException, InstantiationException, ParseException, net.sf.jhunlang.jmorph.parser.ParseException {
		IndexSearcher isearcher = new IndexSearcher(new SimpleFSDirectory(
				new File(indexDir)), true); // read-only=true
		
		Analyzer anal = getAnalyzer();
		
		HungarianLuceneTester.query(Bisen.huSentenceFieldName, "katonai", anal, isearcher);
		HungarianLuceneTester.query(Bisen.huSentenceFieldName, "fiam", anal, isearcher);
		HungarianLuceneTester.query(Bisen.huSentenceFieldName, "gyors", anal, isearcher);
		HungarianLuceneTester.query(Bisen.huSentenceFieldName, "megy", anal, isearcher);
		
		HungarianLuceneTester.query(Bisen.enSentenceFieldName, "many", anal, isearcher);
		
		HungarianLuceneTester.query(Bisen.enSentenceFieldName, "general", anal, isearcher);
		
		
		// hu.mokk.hunglish.domain.Bisen bisen =
		// hu.mokk.hunglish.domain.Bisen.findBisen(new Long(23324));
		// System.out.println(bisen);
		// System.out.println("countDuplicates:"+bisen.countDuplicates());
		//query()
	}


	public static Analyzer getAnalyzer() throws IOException,
			IllegalAccessException, InstantiationException, ParseException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		HungarianLuceneTester lemmatizerProvider = new HungarianLuceneTester();
		lemmatizerProvider.initLemmatizer();
		Analyzer analyzer = new StemmerAnalyzer(Version.LUCENE_CURRENT,
				lemmatizerProvider.getLemmatizerMap());
		;
		return analyzer;
	}

	private IndexWriter getIndexWriter() throws CorruptIndexException, LockObtainFailedException, IOException, IllegalAccessException, InstantiationException, ParseException, net.sf.jhunlang.jmorph.parser.ParseException{
		int mergeFactor = 100;
		int maxBufferedDocs = 1000;
		IndexWriter indexWriter = new IndexWriter(new SimpleFSDirectory(
				new File(indexDir)), getAnalyzer(), true,
				IndexWriter.MaxFieldLength.UNLIMITED);
		indexWriter.setMergeFactor(mergeFactor);
		indexWriter.setMaxBufferedDocs(maxBufferedDocs);
		return indexWriter;
	}
	
	@Test
	public void testIndexDoc() throws IOException, IllegalAccessException,
			InstantiationException, ParseException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		IndexWriter indexWriter = getIndexWriter();
		hu.mokk.hunglish.domain.Bisen.indexDoc(indexWriter, new Long(2));
		indexWriter.close();
	}

	@Test
	public void testIndexSen() throws IOException, IllegalAccessException,
			InstantiationException, ParseException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		IndexWriter indexWriter = getIndexWriter();
		hu.mokk.hunglish.domain.Bisen.indexSen(indexWriter, new Long(8));
		indexWriter.close();
	}
	
	
	@Test
	public void testIndexAll() throws IOException, IllegalAccessException,
			InstantiationException, ParseException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		IndexWriter indexWriter = getIndexWriter();
		hu.mokk.hunglish.domain.Bisen.indexAll(indexWriter);
		indexWriter.close();
	}
	
	
	@Test
	public void testCountBisens() {
		org.junit.Assert.assertNotNull(
				"Data on demand for 'Bisen' failed to initialize correctly",
				dod.getRandomBisen());
		long count = hu.mokk.hunglish.domain.Bisen.countBisens();
		System.out.println(new Long(count));
		org.junit.Assert
				.assertTrue(
						"Counter for 'Bisen' incorrectly reported there were no entries",
						count > 0);
	}

	@Test
	public void testFindBisen() {
		org.junit.Assert.assertNotNull(
				"Data on demand for 'Bisen' failed to initialize correctly",
				dod.getRandomBisen());
		java.lang.Long id = dod.getRandomBisen().getId();
		org.junit.Assert.assertNotNull(
				"Data on demand for 'Bisen' failed to provide an identifier",
				id);
		hu.mokk.hunglish.domain.Bisen obj = hu.mokk.hunglish.domain.Bisen
				.findBisen(id);
		org.junit.Assert.assertNotNull(
				"Find method for 'Bisen' illegally returned null for id '" + id
						+ "'", obj);
		org.junit.Assert.assertEquals(
				"Find method for 'Bisen' returned the incorrect identifier",
				id, obj.getId());
	}

	@Test
	public void testFindAllBisens() {
		// org.junit.Assert.assertNotNull("Data on demand for 'Bisen' failed to initialize correctly",
		// dod.getRandomBisen());
		// long count = hu.mokk.hunglish.domain.Bisen.countBisens();
		// org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Bisen', as there are "
		// + count +
		// " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test",
		// count < 250);
		// java.util.List<hu.mokk.hunglish.domain.Bisen> result =
		// hu.mokk.hunglish.domain.Bisen.findAllBisens();
		assert (true);
		// org.junit.Assert.assertNotNull("Find all method for 'Bisen' illegally returned null",
		// result);
		// org.junit.Assert.assertTrue("Find all method for 'Bisen' failed to return any data",
		// result.size() > 0);
	}

	@Test
	public void testFindBisenEntries() {
		org.junit.Assert.assertNotNull(
				"Data on demand for 'Bisen' failed to initialize correctly",
				dod.getRandomBisen());
		long count = hu.mokk.hunglish.domain.Bisen.countBisens();
		if (count > 20)
			count = 20;
		java.util.List<hu.mokk.hunglish.domain.Bisen> result = hu.mokk.hunglish.domain.Bisen
				.findBisenEntries(0, (int) count);
		org.junit.Assert.assertNotNull(
				"Find entries method for 'Bisen' illegally returned null",
				result);
		org.junit.Assert
				.assertEquals(
						"Find entries method for 'Bisen' returned an incorrect number of entries",
						count, result.size());
	}

	@Test
	@Transactional
	public void testFlush() {
		org.junit.Assert.assertNotNull(
				"Data on demand for 'Bisen' failed to initialize correctly",
				dod.getRandomBisen());
		java.lang.Long id = dod.getRandomBisen().getId();
		org.junit.Assert.assertNotNull(
				"Data on demand for 'Bisen' failed to provide an identifier",
				id);
		hu.mokk.hunglish.domain.Bisen obj = hu.mokk.hunglish.domain.Bisen
				.findBisen(id);
		org.junit.Assert.assertNotNull(
				"Find method for 'Bisen' illegally returned null for id '" + id
						+ "'", obj);
		boolean modified = dod.modifyBisen(obj);
		java.lang.Integer currentVersion = obj.getVersion();
		obj.flush();
		// org.junit.Assert.assertTrue("Version for 'Bisen' failed to increment on flush directive",
		// obj.getVersion() > currentVersion || !modified);
	}

	@Test
	@Transactional
	public void testMerge() {
		org.junit.Assert.assertNotNull(
				"Data on demand for 'Bisen' failed to initialize correctly",
				dod.getRandomBisen());
		java.lang.Long id = new Long(2);// dod.getRandomBisen().getId();
		// org.junit.Assert.assertNotNull("Data on demand for 'Bisen' failed to provide an identifier",
		// id);
		hu.mokk.hunglish.domain.Bisen obj = hu.mokk.hunglish.domain.Bisen
				.findBisen(id);
		org.junit.Assert.assertNotNull(
				"Find method for 'Bisen' illegally returned null for id '" + id
						+ "'", obj);
		// boolean modified = dod.modifyBisen(obj);
		obj.setHuSentenceHash(new Long(666));
		java.lang.Integer currentVersion = obj.getVersion();
		obj.merge();
		obj.flush();
		// org.junit.Assert.assertTrue("Version for 'Bisen' failed to increment on merge and flush directive",
		// obj.getVersion() > currentVersion || !modified);
	}

	@Test
	@Transactional
	public void testPersist() {
		org.junit.Assert.assertNotNull(
				"Data on demand for 'Bisen' failed to initialize correctly",
				dod.getRandomBisen());
		hu.mokk.hunglish.domain.Bisen obj = dod
				.getNewTransientBisen(Integer.MAX_VALUE);
		org.junit.Assert
				.assertNotNull(
						"Data on demand for 'Bisen' failed to provide a new transient entity",
						obj);
		org.junit.Assert.assertNull("Expected 'Bisen' identifier to be null",
				obj.getId());
		obj.persist();
		obj.flush();
		org.junit.Assert
				.assertNotNull(
						"Expected 'Bisen' identifier to no longer be null", obj
								.getId());
	}

	@Test
	@Transactional
	public void testRemove() {
		org.junit.Assert.assertNotNull(
				"Data on demand for 'Bisen' failed to initialize correctly",
				dod.getRandomBisen());
		java.lang.Long id = dod.getRandomBisen().getId();
		org.junit.Assert.assertNotNull(
				"Data on demand for 'Bisen' failed to provide an identifier",
				id);
		hu.mokk.hunglish.domain.Bisen obj = hu.mokk.hunglish.domain.Bisen
				.findBisen(id);
		org.junit.Assert.assertNotNull(
				"Find method for 'Bisen' illegally returned null for id '" + id
						+ "'", obj);
		obj.remove();
		org.junit.Assert.assertNull(
				"Failed to remove 'Bisen' with identifier '" + id + "'",
				hu.mokk.hunglish.domain.Bisen.findBisen(id));
	}
}
