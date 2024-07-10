package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File baseDir;
    public DocumentPersistenceManager(File baseDir){
        if(baseDir == null ){
            this.baseDir = new File(new String(System.getProperty("user.dir")));
        } else{
            this.baseDir = baseDir;
        }
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        if(uri == null ){
            throw new IllegalArgumentException();
        }
        String path = uri.getSchemeSpecificPart().replaceAll("[^A-Za-z0-9\\-\\/._]", "")+".json";

        File file = new File(baseDir.toString() +"/"+path);
        file.getParentFile().mkdirs();

        boolean isTXTDocument = val.getDocumentTxt() != null ? true : false;

        GsonBuilder gsonBuilder = new GsonBuilder();

         JsonSerializer<Document> jsonSerializer = (document, type, jsonSerializationContext) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("URI",document.getKey().toString());
            if (isTXTDocument){
                jsonObject.addProperty("TEXT", document.getDocumentTxt());
                Gson gson = new Gson();
                jsonObject.addProperty("WordMap",gson.toJson(document.getWordMap()));
            }else{
                String base64Encoded = DatatypeConverter.printBase64Binary(document.getDocumentBinaryData());
                jsonObject.addProperty("Binary[]",base64Encoded);
            }
            return jsonObject;
         };

        Gson customGson= gsonBuilder.registerTypeAdapter(DocumentImpl.class,jsonSerializer).create();

        String json = customGson.toJson(val);
        FileWriter writer = new FileWriter(file);
        try {
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            writer.close();

        }
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        if(uri == null)
            throw new IllegalArgumentException();

        String path = uri.getSchemeSpecificPart().replaceAll("[^A-Za-z0-9\\-\\/._]", "")+".json";

        Path fileName = Path.of(baseDir.toString() +"/"+path);
        String str = Files.readString(fileName);

        JsonDeserializer<Document> deserializer = (jsonElement, type, jsonDeserializationContext) -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            URI uri1;
            try {
                uri1 = new URI(jsonObject.get("URI").getAsString());
            } catch (URISyntaxException e) {
                return null;
            }
            boolean isBinary = jsonObject.has("Binary[]");
            if(isBinary){
                byte[] bytes = DatatypeConverter.parseBase64Binary(jsonObject.get("Binary[]").getAsString());
                return new DocumentImpl(uri1,bytes);
            } else {
                String txt = jsonObject.get("TEXT").getAsString();
                Map<String, Integer> map;
                String jsonString = jsonObject.get("WordMap").getAsString();

                Gson gson = new Gson();
                Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
                map = gson.fromJson(jsonString, mapType);
                return new DocumentImpl(uri1, txt, map);
            }
        };
        Gson gsonBuilder = new GsonBuilder().registerTypeAdapter(DocumentImpl.class,deserializer).create();
        return gsonBuilder.fromJson(str,DocumentImpl.class);
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        String path = uri.getSchemeSpecificPart().replaceAll("[^A-Za-z0-9\\-\\/._]", "")+".json";
        File f = new File(baseDir.toString() +"/"+ path);
        return f.delete();
    }

}
