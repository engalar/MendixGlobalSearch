package globalsearch.supportfunctions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;

public class DirectoryCreator {
	
	private static Map<String, IndexWriter> writers = new HashMap<String, IndexWriter>();
	
	public static FSDirectory create() throws IOException {
		
		FSDirectory indexDir = FSDirectory.open(Paths.get(getDirectory()));
		return indexDir;
	}
	
	public static String getDirectory() {
		String configFilePath = new File(System.getProperty("java.io.tmpdir")).getParent();
		String fileSep = System.getProperty("file.separator");
		String directory = configFilePath + fileSep  + "searchIndex";
		return directory;
	}
	
	public static String getFacetDirectory() {
		String configFilePath = new File(System.getProperty("java.io.tmpdir")).getParent();
		String fileSep = System.getProperty("file.separator");
		String directory = configFilePath + fileSep  + "facets";
		return directory;
	}
	
	public static IndexWriter createWriter(String directory) throws IOException {
		
		ILogNode LOG = Core.getLogger("GlobalSearch:directorycreator");
		if (writers.containsKey(directory)) {
			LOG.info("Trying to reuse existing writer");
			IndexWriter writerExisting = writers.get(directory);
			if (writerExisting.isOpen()) {
				LOG.info("Writer is still open, reusing existing one.");
				return writerExisting;
			}else {
				LOG.info("Writer is closed, creating new one.");
				writerExisting.close();
				FSDirectory indexDir = FSDirectory.open(Paths.get(directory));
				IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
				IndexWriter newIndexWriter = new IndexWriter(indexDir, config);
				writers.put(directory, newIndexWriter);
				return newIndexWriter;
			}
		}else {
			LOG.info("Creating new writer");
			FSDirectory indexDir = FSDirectory.open(Paths.get(directory));
			IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
			IndexWriter writer = new IndexWriter(indexDir, config);
			writers.put(directory,writer);
			return writer; 
		}
		
	}
	
	public static TaxonomyWriter createFacetWriter(String directory) throws IOException {
		
		FSDirectory facetDir = FSDirectory.open(Paths.get(directory));
		TaxonomyWriter taxwriter = new DirectoryTaxonomyWriter(facetDir, OpenMode.CREATE);
		return taxwriter;
	}

}
