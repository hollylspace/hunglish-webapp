package hu.mokk.hunglish.lucene;


import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.SimpleFSDirectory;

public class SpellIndexBuilder {

	private static Log logger = LogFactory.getLog(SpellIndexBuilder.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("ejjo");
		validateArgs(args);		
		rebuildSpellIndex(args[0], args[1], args[2]);
	}

	private static String USAGE = "usage: java SpellIndexBuilder <fieldname> <index directory> <spell index directory>";
	private static void validateArgs(String[] args){
		if ((args == null) || (args.length != 3) || (!directoryExists(args[1]))
				|| (!directoryExists(args[2]))){
			System.err.println(USAGE);
			throw new RuntimeException(USAGE);
		}
	}
	
	public static void rebuildSpellIndex(String fieldName, String indexDir, String spellIndexDirectory){
		reCreateDir(spellIndexDirectory);
		logger.info("SPELL start rebuildSpellIndex");
		try {
			IndexReader indexReader = null;
			try {
				SimpleFSDirectory indexFSDir = new SimpleFSDirectory(new File(indexDir));
				SimpleFSDirectory spellFSDir = new SimpleFSDirectory(new File(spellIndexDirectory));
				boolean readOnly = true;
				indexReader = IndexReader.open(indexFSDir, readOnly);
				logger.debug("SPELL start to build NGRAM index for did you mean");
				Dictionary dictionary = new LuceneDictionary(indexReader, fieldName);
				SpellChecker spellChecker = new SpellChecker(spellFSDir);
				spellChecker.indexDictionary(dictionary);
				logger.info("SPELL NGRAM index built");
			} finally {
		        if (indexReader != null) {
		            indexReader.close();
		        }
			}
		} catch (Exception e) {
			throw new RuntimeException("Error, Cannot create SPELL index:", e);
		}	        
	}
	
	public static void reCreateDir(String theDir) {
		File dir = null;
		try {
			dir = new File(theDir);
			FileUtils.deleteQuietly(dir);
			FileUtils.forceMkdir(dir);
		} catch (IOException e) {
			throw new RuntimeException("Cannot recreate directory:" + theDir, e);
		}
	}

	public static boolean directoryExists(String directoryName)
	{
	  File theDir = new File(directoryName);
	  return theDir.isDirectory();
	}
	
}
