package edu.yu.cs.com1320.project.Impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentIO;
import edu.yu.cs.com1320.project.DocumentStore;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;

public class DocumentIOImpl extends DocumentIO {

    public DocumentIOImpl(File baseDir) {
        this.baseDir = baseDir;
    }

    public DocumentIOImpl() {
    }

    public class DocSerializer implements JsonSerializer<DocumentImpl> {
        @Override
        public JsonElement serialize(DocumentImpl doc, Type typeOfT, JsonSerializationContext context) {
            JsonObject object = new JsonObject();

            //convert each of the properties to Strings and then add them to the 'object' which will be returned
            JsonPrimitive bytePrimitive = new JsonPrimitive(Base64.encodeBase64String(doc.getDocument()));   // Base64.getEncoder().encodeToString(doc.getDocument()));
            String stringArray = bytePrimitive.getAsString();
            object.addProperty("byteArray", stringArray);
            String compressionFormat = doc.getCompressionFormat().toString();
            object.addProperty("compressionFormat", compressionFormat);
            String uri = doc.getKey().toString();
            object.addProperty("uri", uri);
            String hashCode = Integer.toString(doc.getDocumentHashCode());
            object.addProperty("hashCode", hashCode);
            String wordMap = doc.getWordMap().toString();
            object.addProperty("wordMap", wordMap);

            // we create the json object for the doc and send it back to the Gson serializer
            return object;
        }
    }

    public File serialize(Document doc) {
        Gson gson = new GsonBuilder().registerTypeAdapter(DocumentImpl.class, new DocSerializer()).setPrettyPrinting().create();
        Type docType = new TypeToken<DocumentImpl>(){}.getType();
        String json = gson.toJson(doc, docType);
        //String json = gson.toJson(doc);
        File file2 = null;

        try {
            String path1 = doc.getKey().getAuthority(); //+something for rest of string except last /
            File file = new File(baseDir.getAbsolutePath() + File.pathSeparator + path1);
            Files.createDirectories(file.toPath());
            file2 = new File(file.getAbsolutePath() + doc.getKey().getPath() + ".json");
            if (!file2.exists()) {
                Files.createFile(file2.toPath());
            }
            FileWriter writer = new FileWriter(file2);
            writer.write(json);
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return file2;
    }

    public class DocDeserializer implements JsonDeserializer<DocumentImpl> {
        @Override
        public DocumentImpl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String bytes = json.getAsJsonObject().get("byteArray").getAsString();
            //byte[] byteArray = Base64.getDecoder().decode(bytes.getAsString());
            byte[] byteArray = Base64.decodeBase64(bytes);

            int hashCode = json.getAsJsonObject().get("hashCode").getAsInt();

            String stringURI = json.getAsJsonObject().get("uri").getAsString();
                URI uri = URI.create(stringURI);

            String stringCompressionFormat = json.getAsJsonObject().get("compressionFormat").getAsString();
                DocumentStore.CompressionFormat compressionFormat = DocumentStore.CompressionFormat.valueOf(stringCompressionFormat);

            String stringWordMap = json.getAsJsonObject().get("wordMap").getAsString();
                HashMap<String, Integer> wordMap = new HashMap<>();
                //turn = signs into spaces
                String s = stringWordMap.replaceAll("=", " ");
                //this will split up big string into individual strings based on spaces
                //i.e. '{data 4, k 1, ki 1}' becomes '{data' '4,' 'k' '1,' 'ki' '1}'
                String[] words = s.split("\\s+");
                //get rid of symbols and whatnot
                for (int i = 0; i < words.length; i++) {
                   words[i] = words[i].replaceAll("[^\\w]", "");
                }
                //put each word and int into the map
                for (int j = 0; j < words.length; j+=2) {
                    wordMap.put(words[j], Integer.parseInt(words[j+1]));
                }

            DocumentImpl doc = new DocumentImpl(byteArray, hashCode, uri, compressionFormat, wordMap);
            return doc;
        }
    }

    public Document deserialize(URI uri) {
        Gson gson = new GsonBuilder().registerTypeAdapter(DocumentImpl.class, new DocDeserializer()).setPrettyPrinting().create();
        Type docType = new TypeToken<DocumentImpl>(){}.getType();
        DocumentImpl doc = null;

        try {
            String path = baseDir.getAbsolutePath() + File.pathSeparator + uri.getAuthority() + uri.getPath() + ".json";
            doc = gson.fromJson(new FileReader(path), docType);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }
}