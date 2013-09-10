package index.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class EventSearch {
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException {
	
		String eventFile = "/home/dima/thyme/event-context/events.txt";
		Set<String> events = readEvents(eventFile);
		Set<String> notFoundEvents = verifyPresenseInIndex(events);
		System.out.println("total unique events: " + events.size());
		System.out.println("could not find in index: " + notFoundEvents.size());
		System.out.println(notFoundEvents);
	}
	
	public static Set<String> verifyPresenseInIndex(Set<String> events) throws CorruptIndexException, IOException, ParseException {

		final int maxHits = 1;
		final String field = "content";

		Set<String> notFoundEvents = new HashSet<String>();
		
		IndexReader indexReader = IndexReader.open(FSDirectory.open(new File("/home/dima/data/mimic/index/")));
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
  	Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_35);
  	QueryParser queryParser = new QueryParser(Version.LUCENE_35, field, standardAnalyzer);
  	
		for(String event : events) {
	  	String escaped = QueryParser.escape(event);
	  	Query query = queryParser.parse(escaped);
	  	
	  	TopDocs topDocs = indexSearcher.search(query, maxHits);
	  	ScoreDoc[] scoreDocs = topDocs.scoreDocs;

	  	if(scoreDocs.length < 1) {
	  		notFoundEvents.add(event);
	  	}
		}
		
		indexSearcher.close();
		return notFoundEvents;
	}
	
	public static Set<String> readEvents(String path) throws FileNotFoundException {
		
		Set<String> events = new HashSet<String>();
		Scanner scanner = new Scanner(new File(path));
		
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] elements = line.split("\\|");
			if(elements.length == 2 && elements[0].length() > 0) {
				events.add(elements[0]);
			}
		}
		
		return events;
	}
}

