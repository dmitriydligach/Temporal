package index.create;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class CreateIndexFromPostgre {

	public static void main(String[] args) throws IOException, SQLException {

		// connect to postgresql and get the data
		Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/MIMIC2", "dima", "dima");
		connection.setAutoCommit(false); // need this so that setFetchSize() below has an effect
		String query = "select text from mimic2v26.noteevents";
		Statement statement = connection.createStatement();
		statement.setFetchSize(1000); // fetching the entire table results in out-of-memory error
		ResultSet resultSet = statement.executeQuery(query);
		
		// set up lucene index
		Directory directory = FSDirectory.open(new File("/home/dima/data/mimic/index/"));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35, Collections.emptySet());
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_35, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		
		// now write to index
		while(resultSet.next()) {
			String text = resultSet.getString(1);
			Document document = new Document();
			document.add(new Field("content", text, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_OFFSETS));
			indexWriter.addDocument(document);
		}
		
		connection.close();
		indexWriter.close();
		System.out.println("done!");
	}
}
