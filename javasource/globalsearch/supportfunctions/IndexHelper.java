package globalsearch.supportfunctions;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;

import communitycommons.ORM;
import globalsearch.proxies.Facet;

public class IndexHelper {

	public static Document createBasic(IMendixObject objectToIndex, IContext context, FacetsConfig config, List<Facet> facets) throws IOException, CoreException {
		
		Document luceneDocument = new Document();
		
		String objectName = objectToIndex.getMetaObject().getName();
		String GUID = ORM.getGUID(objectToIndex).toString();
		StringField idField = new StringField("id", GUID, Field.Store.YES);
		TextField objectNameField = new TextField("EntityName", objectName, Field.Store.YES);
		luceneDocument.add(idField);
		luceneDocument.add(objectNameField);
		
		// Get attributes.
		Map<String, ? extends IMendixObjectMember<?>> attributes = objectToIndex.getMembers(context);	
		StringBuilder contentBuilder = new StringBuilder();
		// Iterate over object members (attributes)
		for (String key : attributes.keySet()) {
			
			IMendixObjectMember<?> currentAttribute = attributes.get(key);
			
			// Store key name.
			String keyName = key;
			// Get the value of the current attribute and put it in a string.
			String value = "";
			try {
				value = currentAttribute.getValue(context).toString();
			} catch (Exception e) {
			}
			if (value.equals("") == false) {
				contentBuilder.append(value).append(" ");
				TextField keyvalue = new TextField(keyName, value, Field.Store.YES);
				TextField contentsCatchAll = new TextField("all", value, Field.Store.YES); // Catch-all contents field.
				luceneDocument.add(keyvalue);
				luceneDocument.add(contentsCatchAll);
			}
		
		}
		
		luceneDocument.add(new TextField("contents", contentBuilder.toString(), Field.Store.YES));
		
		// Create facets.
		createFacets(luceneDocument, facets, config); 
		
		return luceneDocument;
	}
	
	public static Document createAdvanced(IMendixObject objectToIndex, IContext context, FacetsConfig config, List<Facet> facets, String body) throws IOException, CoreException, ParseException {
		
		Document luceneDocument = new Document();
		
		String objectName = objectToIndex.getId().getObjectType();
		String GUID = ORM.getGUID(objectToIndex).toString();
		StringField idField = new StringField("id", GUID, Field.Store.YES);
		StringField objectNameField = new StringField("NameOfObject", objectName, Field.Store.YES);
		luceneDocument.add(idField);
		luceneDocument.add(objectNameField);
		
		JSONParser parser = new JSONParser();
		JSONObject bodytoindex = (JSONObject) parser.parse(body);
		StringBuilder contentBuilder = new StringBuilder();
		indexJsonObject(luceneDocument, bodytoindex, contentBuilder, null);
		
		luceneDocument.add(new TextField("contents", contentBuilder.toString(), Field.Store.YES));
		
		// Create facets.
		createFacets(luceneDocument, facets, config); 
		
		return luceneDocument;
	}
	
	private static void indexJsonObject(Document doc, JSONObject body, StringBuilder contentBuilder, String prefix) {
		
		// Iterate over keys
		Set<String> keys = body.keySet();
		Iterator<String> keyIterator = keys.iterator();
		while(keyIterator.hasNext()) {
			
			 String key = keyIterator.next();
			 String keyToAdd = prefix != null ? prefix + "." + key : key; 
			 Object i = body.get(key);
			 
			 if(i instanceof JSONObject) {
				 JSONObject keyObject = (JSONObject) i;
				 indexJsonObject(doc, keyObject, contentBuilder, keyToAdd);
			 }else if (i instanceof JSONArray) {
				JSONArray arrayJSON = (JSONArray) i;
			    Iterator<JSONObject> arrayIterator = arrayJSON.iterator();
			    while(arrayIterator.hasNext()) {
		    		JSONObject arrayEntryToProcess = arrayIterator.next();
		    		indexJsonObject(doc, arrayEntryToProcess, contentBuilder, keyToAdd);
		    	} 
			 }else {
				 String value = "";
				try {
					value = (String) i;
				} catch (Exception e) {
				}
				if (value.equals("") == false) {
					contentBuilder.append(value).append(" ");
					TextField keyvalue = new TextField(keyToAdd, value, Field.Store.YES);
					TextField contentsCatchAll = new TextField("all", value, Field.Store.YES); // Catch-all contents field.
					doc.add(keyvalue);
					doc.add(contentsCatchAll);
				}
			 }
			
		}
	}
	
	private static void createFacets(Document doc, List<Facet> facets, FacetsConfig config) throws CoreException{
		
		if(facets != null && !facets.isEmpty()) {
			
			for(Facet i : facets) {
				String key = (i.getKey() != null && !i.getKey().isBlank()  && !i.getKey().isEmpty()) ? i.getKey() : null;
				String value = (i.getValue() != null && !i.getValue().isBlank()  && !i.getValue().isEmpty()) ? i.getValue() : null;
				if(key != null && value != null) {
					config.setMultiValued(key, true);
					doc.add(new SortedSetDocValuesFacetField(key, value));
				}
			}

		}
	}
}