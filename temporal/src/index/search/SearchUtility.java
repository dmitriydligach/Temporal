package index.search;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.FSDirectory;

import utils.Utils;

public class SearchUtility {
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException, InvalidTokenOffsetsException {

		final int maxHits = 250;
		final String searchField = "content";
		final String indexLocation = "/home/dima/data/mimic/index/";

		String queryText = JOptionPane.showInputDialog("Enter query");
		
  	IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(indexLocation)));
  	IndexSearcher indexSearcher = new IndexSearcher(indexReader);

  	PhraseQuery phraseQuery = new PhraseQuery();
  	for(String word : queryText.split(" ")) {
  		phraseQuery.add(new Term(searchField, word));
  	}
  	phraseQuery.setSlop(0);
  	
  	TopDocs topDocs = indexSearcher.search(phraseQuery, maxHits);
  	ScoreDoc[] scoreDocs = topDocs.scoreDocs;  		

  	for(ScoreDoc scoreDoc : scoreDocs) {
  		Document document = indexSearcher.doc(scoreDoc.doc);
  		String text = document.get(searchField).toLowerCase().replace('\n', ' ');
  		String context = Utils.getContext(queryText, text, 20);
  		System.out.println(context);
  	}
  	
  	indexSearcher.close();
  	System.out.println("total hits: " + scoreDocs.length);
	}
}

