package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentPersistenceManagerTest {

    @Test
    void serialize() throws URISyntaxException, IOException {
        URI uri1 = new URI("http://DocumentStoreJava/Documents/Doc1");
        URI uri2 = new URI("http://www.yu.edu/documents/doc1");
        URI uri3 = new URI("http://DocumentStoreJava/Documents/Doc3");
        URI uri4 = new URI("mailto:java-net@www.example.com");


        String str1 = "First Document in the DocumentStore, it it is is stored in http://DocumentStoreJava/Documents/Doc1";
        String str2 = "Second Document in the DocumentStore, it is stored in http://www.yu.edu/documents/doc1";
        String string4 = "THIS IS - URI uri4 = new URI(mailto:java-net@www.example.com)";

        Random rd = new Random();
        byte[] byteAr = new byte[20];
        rd.nextBytes(byteAr);

        Document doc1 = new DocumentImpl(uri1, str1);
        Document doc2 = new DocumentImpl(uri2, str2);
        Document doc3 = new DocumentImpl(uri3, byteAr);
        Document doc4 = new DocumentImpl(uri4, string4);


        DocumentPersistenceManager doc = new DocumentPersistenceManager(new File(System.getProperty("user.dir")+"/Users/dothanbar/Desktop/ComputerScience/YU/CS.STAGE.5.TEST"));

        doc.serialize(uri1, doc1);
        doc.serialize(uri2, doc2);
        doc.serialize(uri3, doc3);
        doc.serialize(uri4, doc4);

        Document document = doc.deserialize(uri1);
        Document document1 = doc.deserialize(uri2);
        Document docBinary = doc.deserialize(uri3);
        Document document4 = doc.deserialize(uri4);

        assertEquals(uri1, document.getKey());
        assertEquals(str1, document.getDocumentTxt());

        assertEquals(doc3, docBinary);
        assertTrue(Arrays.equals(byteAr, doc3.getDocumentBinaryData()));

        assertEquals(doc4, document4);

        assertEquals(doc2, document1);
        assertEquals(str2, document1.getDocumentTxt());
        assertEquals(doc2.getWords(), document1.getWords());
        assertEquals(doc2.wordCount("the"), document1.wordCount("the"));
        doc.delete(uri1);


    }
}
