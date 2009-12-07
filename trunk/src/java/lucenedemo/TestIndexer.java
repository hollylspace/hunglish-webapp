package lucenedemo;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class TestIndexer {

	public static void main(String[] args) throws IOException, ParseException {
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		Directory directory = FSDirectory.open(new File("/tmp/testindex"));

		IndexWriter iwriter = new IndexWriter(directory, analyzer, true,
				new IndexWriter.MaxFieldLength(25000));
		Document doc = new Document();
		String text = "This is the text to be indexed.";
		doc.add(new Field("fieldname", text, Field.Store.YES,
				Field.Index.ANALYZED));
		iwriter.addDocument(doc);
		iwriter.close();
		
		// Now search the index:
	    IndexSearcher isearcher = new IndexSearcher(directory, true); // read-only=true
	    // Parse a simple query that searches for "text":
	    QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "fieldname", analyzer);
	    Query query = parser.parse("text");
	    ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
	    System.out.println("hits.length"+hits.length);
	    // Iterate through the results:
	    for (int i = 0; i < hits.length; i++) {
	      Document hitDoc = isearcher.doc(hits[i].doc);
	      System.out.println("hitDoc"+hitDoc.get("fieldname"));
	    }
	    isearcher.close();
	    directory.close();		
	}

}
