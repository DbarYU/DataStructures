package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BTreeImplTest {

    @Test
    public void wrongBTreeImpleTest(){
            BTreeImpl<Integer, String> st = new BTreeImpl<>();
            assertEquals(null,st.put(1, "one"));
            assertEquals(null,st.put(2, "two"));
            assertEquals(null,st.put(3, "three"));
            assertEquals(null,st.put(4, "four"));
            assertEquals(null,st.put(5, "five"));
            assertEquals(null,st.put(6, "six"));
            assertEquals(null,st.put(7, "seven"));
            assertEquals(null,st.put(8, "eight"));
            assertEquals(null,st.put(9, "nine"));

            assertEquals("one",st.get(1));
            assertEquals("two",st.get(2));
            assertEquals("three",st.get(3));
            assertEquals("four",st.get(4));
            assertEquals("five",st.get(5));
            assertEquals("six",st.get(6));
            assertEquals("seven",st.get(7));
            assertEquals("eight",st.get(8));
            assertEquals("nine",st.get(9));

            assertEquals("one",st.put(1, "Changed one"));
            assertEquals("two",st.put(2, "Changed two"));
            assertEquals("three",st.put(3, "Changed three"));
            assertEquals("four",st.put(4, "Changed four"));
            assertEquals("five",st.put(5, "Changed five"));
            assertEquals("six",st.put(6, "Changed six"));
            assertEquals("seven",st.put(7, "Changed seven"));
            assertEquals("eight",st.put(8, "Changed eight"));
            assertEquals("nine",st.put(9, "Changed nine"));

            assertEquals("Changed one",st.get(1));
            assertEquals("Changed two",st.get(2));
            assertEquals("Changed three",st.get(3));
            assertEquals("Changed four",st.get(4));
            assertEquals("Changed five",st.get(5));
            assertEquals("Changed six",st.get(6));
            assertEquals("Changed seven",st.get(7));
            assertEquals("Changed eight",st.get(8));
            assertEquals("Changed nine",st.get(9));
        }


    @Test
    public void bTreeTest() throws Exception {
        BTreeImpl<URI, Document> tree = new BTreeImpl<>();

        URI uri1 = new URI("http://DocumentStoreJava/Documents/Doc1");
        URI uri2 = new URI("http://DocumentStoreJava/Documents/Doc2");
        URI uri3 = new URI("http://DocumentStoreJava/Documents/Doc3");
        URI uri4 = new URI("mailto:java-net@www.example.com");


        String str1 = "First Document in the DocumentStore, it it is is stored in http://DocumentStoreJava/Documents/Doc1";
        String str2 = "Second Document in the DocumentStore, it is stored in http://DocumentStoreJava/Documents/Doc2";
        String string4 = "THIS IS - URI uri4 = new URI(mailto:java-net@www.example.com)";

        Random rd = new Random();
        byte[] byteAr= new byte[20];
        rd.nextBytes(byteAr);

        Document doc1 = new DocumentImpl(uri1,str1);
        Document doc2 = new DocumentImpl(uri2,str2);
        Document doc3 = new DocumentImpl(uri3,byteAr);
        Document doc4 = new DocumentImpl(uri4,string4);

        assertEquals(null,tree.put(uri1, doc1));
        assertEquals(null,tree.put(uri2, doc2));
        assertEquals(null,tree.put(uri3, doc3));
        assertEquals(null,tree.put(uri4, doc4));

        assertEquals(doc1,tree.get(uri1));
        assertEquals(doc2,tree.get(uri2));
        assertEquals(doc3,tree.get(uri3));
        assertEquals(doc4,tree.get(uri4));
        DocumentPersistenceManager doc = new DocumentPersistenceManager(new File(System.getProperty("user.dir")+"/Users/dothanbar/Desktop/ComputerScience/YU/BTree.Testing"));

        tree.setPersistenceManager(doc);
        tree.moveToDisk(uri1);
        assertEquals(doc1,tree.get(uri1));

    }
}