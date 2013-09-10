package index.search;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class PrintDocumentsMatchingQuery {
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException {

		final int maxHits = 100;
		final String field = "content";
		final String query = "lasted minutes";
		
  	IndexReader indexReader = IndexReader.open(FSDirectory.open(new File("/home/dima/data/mimic/index/")));
  	IndexSearcher indexSearcher = new IndexSearcher(indexReader);
  	
    PhraseQuery phraseQuery = new PhraseQuery();
    for(String word : query.split(" ")) {
    	phraseQuery.add(new Term(field, word));
    }
  	phraseQuery.setSlop(2); 
  	
  	TopDocs topDocs = indexSearcher.search(phraseQuery, maxHits);
  	ScoreDoc[] scoreDocs = topDocs.scoreDocs;
  	
  	for(ScoreDoc scoreDoc : scoreDocs) {
  		Document doc = indexSearcher.doc(scoreDoc.doc);
  		String text = doc.get(field);
  		System.out.println(text);
  		System.out.println();
  	}
  	
  	indexSearcher.close();
	}
}

