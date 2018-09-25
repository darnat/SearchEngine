package cecs429.documents;

import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.*;

/**
 * Represents a document that is saved as a json file in the local file system.
 */
public class JsonFileDocument implements FileDocument {
	private int mDocumentId;
	private Path mFilePath;
	private String mTitle;
	
	/**
	 * Constructs a JsonFileDocument with the given document ID representing the file at the given
	 * absolute file path.
	 */
	public JsonFileDocument(int id, Path absoluteFilePath) {
		mDocumentId = id;
		mFilePath = absoluteFilePath;
	}
	
	@Override
	public Path getFilePath() {
		return mFilePath;
	}
	
	@Override
	public int getId() {
		return mDocumentId;
	}
	
	@Override
	public Reader getContent() {
		return new StringReader(getJsonValue("body"));
	}
	
	@Override
	public String getTitle() {
		if (mTitle == null) {
			mTitle = getJsonValue("title");
		}
		return mTitle;
	}

	private String getJsonValue(String field) {
		String value = null;
		try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader(mFilePath.toFile())))) {
			reader.beginObject();
			while (reader.hasNext()) {
				if (reader.nextName().equals(field)) {
					value = reader.nextString();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException ex) {
			System.out.println(ex);
		}

		return value;
	}
	
	public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
		return new JsonFileDocument(documentId, absolutePath);
	}
}
