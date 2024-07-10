package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentStoreImplTest {
    @Test
    void PlaceAlterGetAlterDeleteBinary() throws FileNotFoundException, URISyntaxException, IOException {

        //creating store variables.
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri = new URI("URI");
        DocumentStore.DocumentFormat binary = DocumentStore.DocumentFormat.BINARY;
        byte[] b = new byte[20];
        new Random().nextBytes(b);

        byte[] c = new byte[20];
        new Random().nextBytes(c);

        ByteArrayInputStream input = new ByteArrayInputStream(b);
        ByteArrayInputStream alteredInput = new ByteArrayInputStream(c);

        //ByteArrayInputStream input1=new ByteArrayInputStream(b);

        //put 1st entry in the store, should equal 0 due to not being a previous URI key
        assertEquals(0, store.put(input, uri, binary));
        //comparing the binary data of the document under uri , with the binary data
        assertTrue(Arrays.equals(b, store.get(uri).getDocumentBinaryData()));
        //altering the input, should return the previous document hashcode
        assertEquals(new DocumentImpl(uri, b).hashCode(), store.put(alteredInput, uri, binary));
        assertTrue(Arrays.equals(c, store.get(uri).getDocumentBinaryData()));
        //getting the dcoument uner uri key, should return the current document, that is ->new DocumentImpl(uri,b)
        assertEquals(new DocumentImpl(uri, c), store.get(uri));
        //altering the key again, placing under the same uri key a different document, should return the previous value hashcode-> ,DocumentImpl(uri,b).hashCode()
        assertEquals(new DocumentImpl(uri, c).hashCode(), store.put(input, uri, binary));
        //delting the KEY, should return true, becuase there is a key uri.
        assertEquals(true, store.delete(uri));
    }

    @Test
    void PlaceAlterGetAlterDeleteTXT() throws FileNotFoundException, URISyntaxException, IOException {

        //creating store variables.
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri = new URI("URI");
        DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;

        String str = "Hello!";
        String strAlter = "Goodbye!";


        StringBufferInputStream input = new StringBufferInputStream(str);
        StringBufferInputStream alteredInput = new StringBufferInputStream(strAlter);

        //ByteArrayInputStream input1=new ByteArrayInputStream(b);

        //put 1st entry in the store, should equal 0 due to not being a previous URI key
        assertEquals(0, store.put(input, uri, txt));
        //altering the input, should return the previous document hashcode
        assertEquals(new DocumentImpl(uri, str).hashCode(), store.put(alteredInput, uri, txt));
        assertEquals("Goodbye!", store.get(uri).getDocumentTxt());
        //getting the dcoument uner uri key, should return the current document, that is ->new DocumentImpl(uri,b)
        assertEquals(new DocumentImpl(uri, strAlter), store.get(uri));
        //altering the key again, placing under the same uri key a different document, should return the previous value hashcode-> ,DocumentImpl(uri,b).hashCode()
        assertEquals(new DocumentImpl(uri, strAlter).hashCode(), store.put(input, uri, txt));
        //delting the KEY, should return true, becuase there is a key uri.
        assertEquals("Hello!", store.get(uri).getDocumentTxt());
        assertEquals(true, store.delete(uri));
    }

    @Test
    void PlaceRetrieveAlterDelete() throws FileNotFoundException, URISyntaxException, IOException {
        //generate three different URI'S
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI6");

        DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;
        DocumentStore.DocumentFormat binary = DocumentStore.DocumentFormat.BINARY;

        String str1 = "String 1";
        String str2 = "String 2";
        String str3 = "String 3";
        byte[] byte1 = new byte[20];
        new Random().nextBytes(byte1);
        byte[] byte2 = new byte[20];
        new Random().nextBytes(byte2);
        byte[] byte3 = new byte[20];
        new Random().nextBytes(byte3);

        StringBufferInputStream strInput1 = new StringBufferInputStream(str1);
        StringBufferInputStream strInput2 = new StringBufferInputStream(str2);
        StringBufferInputStream strInput3 = new StringBufferInputStream(str3);
        ByteArrayInputStream binaryInput1 = new ByteArrayInputStream(byte1);
        ByteArrayInputStream binaryInput2 = new ByteArrayInputStream(byte2);
        ByteArrayInputStream binaryInput3 = new ByteArrayInputStream(byte3);

        //place all 6  documents in the store.
        assertEquals(0, store.put(strInput1, uri1, txt));
        assertEquals(0, store.put(strInput2, uri2, txt));
        assertEquals(0, store.put(strInput3, uri3, txt));
        assertEquals(0, store.put(binaryInput1, uri4, binary));
        assertEquals(0, store.put(binaryInput2, uri5, binary));
        assertEquals(0, store.put(binaryInput3, uri6, binary));
        //get all 6 documents
        assertEquals(new DocumentImpl(uri1, str1), store.get(uri1));
        assertEquals(new DocumentImpl(uri2, str2), store.get(uri2));
        assertEquals(new DocumentImpl(uri3, str3), store.get(uri3));
        assertEquals(new DocumentImpl(uri4, byte1), store.get(uri4));
        assertEquals(new DocumentImpl(uri5, byte2), store.get(uri5));
        assertEquals(new DocumentImpl(uri6, byte3), store.get(uri6));

        //alter 6 documents.
        assertEquals(new DocumentImpl(uri1, str1).hashCode(), store.put(binaryInput1, uri1, binary));
        assertEquals(new DocumentImpl(uri2, str2).hashCode(), store.put(binaryInput2, uri2, binary));
        assertEquals(new DocumentImpl(uri3, str3).hashCode(), store.put(binaryInput3, uri3, binary));
        assertEquals(new DocumentImpl(uri4, byte1).hashCode(), store.put(strInput1, uri4, txt));
        assertEquals(new DocumentImpl(uri5, byte2).hashCode(), store.put(strInput2, uri5, txt));
        assertEquals(new DocumentImpl(uri6, byte3).hashCode(), store.put(strInput3, uri6, txt));

    }

    @Test
    void putDeleteUndoTest() throws FileNotFoundException, URISyntaxException, IOException {

        //SETUP
        DocumentStoreImpl store = new DocumentStoreImpl();

        URI uri = new URI("URI");
        URI uri1 = new URI("URI1");
        DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;
        DocumentStore.DocumentFormat binary = DocumentStore.DocumentFormat.BINARY;
        byte[] b = new byte[20];
        new Random().nextBytes(b);

        ByteArrayInputStream binaryInput = new ByteArrayInputStream(b);
        StringBufferInputStream stringBuffer = new StringBufferInputStream("String");

        DocumentImpl doc = new DocumentImpl(uri, "String");
        DocumentImpl doc2 = new DocumentImpl(uri1, b);


        assertEquals(0, store.put(stringBuffer, uri, txt));
        //delete entry
        assertEquals(doc.hashCode(), store.put(null, uri, txt));
        store.undo();
        assertEquals(doc, store.get(uri));

        assertEquals(0, store.put(binaryInput, uri1, binary));
        //delete entry
        assertEquals(doc2.hashCode(), store.put(null, uri1, binary));
        store.undo();
        assertEquals(doc2, store.get(uri1));
    }


    @Test
    void putUndoTest() throws FileNotFoundException, URISyntaxException, IOException {

        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri = new URI("URI");
        URI uri1 = new URI("URI1");
        DocumentStore.DocumentFormat binary = DocumentStore.DocumentFormat.BINARY;
        DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;
        String string1 = "String1";
        String string2 = "String2";
        StringBufferInputStream stringInput1 = new StringBufferInputStream(string1);
        StringBufferInputStream stringInput2 = new StringBufferInputStream(string2);
        byte[] b = new byte[20];
        new Random().nextBytes(b);
        byte[] c = new byte[20];
        new Random().nextBytes(c);
        ByteArrayInputStream input = new ByteArrayInputStream(b);
        ByteArrayInputStream input1 = new ByteArrayInputStream(c);
        DocumentImpl doc = new DocumentImpl(uri, b);

        DocumentImpl docBinary1 = new DocumentImpl(uri1, b);
        DocumentImpl docString1 = new DocumentImpl(uri1, string1);
        DocumentImpl docString2 = new DocumentImpl(uri1, string2);


        store.put(input, uri, binary);
        store.undo();
        assertEquals(null, store.get(uri));


        store.put(input, uri, txt);
        store.undo();
        assertEquals(null, store.get(uri));


        store.put(input, uri, binary);
        store.put(input1, uri, binary);
        store.undo();
        assertEquals(doc, store.get(uri));


        store.put(stringInput1, uri1, txt);
        store.put(stringInput2, uri1, txt);
        store.undo();
        assertEquals(docString1, store.get(uri1));


        store.put(stringInput1, uri1, txt);
        store.put(stringInput2, uri1, binary);
        store.undo();
        assertEquals(docString1, store.get(uri1));


        store.put(input, uri1, binary);
        store.put(stringInput2, uri1, txt);
        store.undo();
        assertEquals(docBinary1, store.get(uri1));


    }

    @Test
    void searchByPrefixTest() throws FileNotFoundException, URISyntaxException, IOException {

        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");

        DocumentStore.DocumentFormat binary = DocumentStore.DocumentFormat.BINARY;
        DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;

        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4, that includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";

        StringBufferInputStream stringInput1 = new StringBufferInputStream(string1);
        StringBufferInputStream stringInput2 = new StringBufferInputStream(string2);
        StringBufferInputStream stringInput3 = new StringBufferInputStream(string3);
        StringBufferInputStream stringInput4 = new StringBufferInputStream(string4);
        StringBufferInputStream stringInput5 = new StringBufferInputStream(string5);
        StringBufferInputStream stringInput6 = new StringBufferInputStream(string6);
        StringBufferInputStream stringInput7 = new StringBufferInputStream(string7);
        StringBufferInputStream stringInput8 = new StringBufferInputStream(string8);


        assertEquals(0, store.put(stringInput1, uri1, txt));
        assertEquals(0, store.put(stringInput2, uri2, txt));
        assertEquals(0, store.put(stringInput3, uri3, txt));
        assertEquals(0, store.put(stringInput4, uri4, txt));
        assertEquals(0, store.put(stringInput5, uri5, txt));
        assertEquals(0, store.put(stringInput6, uri6, txt));
        assertEquals(0, store.put(stringInput7, uri7, txt));
        assertEquals(0, store.put(stringInput8, uri8, txt));


        assertEquals(new DocumentImpl(uri7, string7), store.search("the").get(0));
        assertEquals(new DocumentImpl(uri1, string1), store.search("the").get(1));
        assertEquals(new DocumentImpl(uri8, string8), store.search("the").get(2));
        assertEquals(new DocumentImpl(uri4, string4), store.search("the").get(3));
        assertEquals(new DocumentImpl(uri3, string3), store.search("the").get(4));
        assertEquals(new DocumentImpl(uri2, string2), store.search("the").get(5));
        assertEquals(new DocumentImpl(uri5, string5), store.search("the").get(6));


        List list = store.searchByPrefix("t");
        assertEquals(new DocumentImpl(uri7, string7), list.get(0));
        assertEquals(new DocumentImpl(uri1, string1), list.get(1));
        assertEquals(new DocumentImpl(uri8, string8), list.get(2));
        assertEquals(new DocumentImpl(uri4, string4), list.get(3));
        assertEquals(new DocumentImpl(uri3, string3), list.get(4));
        assertEquals(new DocumentImpl(uri2, string2), list.get(5));
        assertEquals(new DocumentImpl(uri6, string6), list.get(7));
        assertEquals(new DocumentImpl(uri5, string5), list.get(6));


        store.deleteAll("the");
        assertEquals(null, store.get(uri7));
        assertEquals(null, store.get(uri1));
        assertEquals(null, store.get(uri2));
        assertEquals(null, store.get(uri3));
        assertEquals(null, store.get(uri4));
        assertEquals(null, store.get(uri5));
        assertEquals(null, store.get(uri8));

        store.undo();
        assertEquals(new DocumentImpl(uri7, string7), store.get(uri7));
        assertEquals(new DocumentImpl(uri8, string8), store.get(uri8));
        assertEquals(new DocumentImpl(uri5, string5), store.get(uri5));
        assertEquals(new DocumentImpl(uri4, string4), store.get(uri4));
        assertEquals(new DocumentImpl(uri3, string3), store.get(uri3));
        assertEquals(new DocumentImpl(uri2, string2), store.get(uri2));
        assertEquals(new DocumentImpl(uri1, string1), store.get(uri1));

        store.deleteAll("the");
        store.undo(uri7);
        store.undo(uri7);
        assertEquals(null, store.get(uri7));
        assertTrue(!store.search("the").contains(new DocumentImpl(uri7, string7)));

        for (int i = 0; i < 100; i++) {
            store.put(new StringBufferInputStream("String" + i), new URI("URINUMBER" + i), txt);
        }


        store.undo(uri8);
        store.undo(uri5);
        store.undo(uri4);
        store.undo(uri3);
        store.undo(uri1);
        store.undo(uri2);
        assertEquals(new DocumentImpl(uri1, string1), store.get(uri1));
        store.undo(uri1);
        assertEquals(null, store.get(uri1));
        assertTrue(!store.search("the").contains(new DocumentImpl(uri1, string1)));


        assertEquals(new DocumentImpl(uri2, string2), store.get(uri2));
        store.undo(uri2);
        assertEquals(null, store.get(uri2));
        assertTrue(!store.search("the").contains(new DocumentImpl(uri2, string2)));

        assertEquals(new DocumentImpl(uri3, string3), store.get(uri3));
        store.undo(uri3);
        assertEquals(null, store.get(uri3));
        assertTrue(!store.search("the").contains(new DocumentImpl(uri3, string3)));

        assertEquals(new DocumentImpl(uri4, string4), store.get(uri4));
        store.undo(uri4);
        assertEquals(null, store.get(uri4));
        assertTrue(!store.search("the").contains(new DocumentImpl(uri4, string4)));

        assertEquals(new DocumentImpl(uri5, string5), store.get(uri5));
        store.undo(uri5);
        assertEquals(null, store.get(uri5));
        assertTrue(!store.search("the").contains(new DocumentImpl(uri5, string5)));

        assertEquals(new DocumentImpl(uri8, string8), store.get(uri8));
        store.undo(uri8);
        assertEquals(null, store.get(uri8));
        assertTrue(!store.search("the").contains(new DocumentImpl(uri8, string8)));


        URI uriForDocument1 = new URI("uriForDocument1");
        String strForDoc1 = "TOO, too toodle toooodle toothFairy tooths";
        String strForDoc2 = "TOO, tooday";
        URI uriForDocument2 = new URI("uriForDocument2");

        DocumentImpl document1 = new DocumentImpl(uriForDocument1, strForDoc1);
        DocumentImpl document2 = new DocumentImpl(uriForDocument2, strForDoc2);
        assertEquals(0, store.put(new StringBufferInputStream(strForDoc1), uriForDocument1, txt));
        assertEquals(0, store.put(new StringBufferInputStream(strForDoc2), uriForDocument2, txt));
        store.deleteAllWithPrefix("too");
        assertEquals(null, store.get(uriForDocument1));
        assertEquals(null, store.get(uriForDocument2));
        store.undo();
        assertEquals(document1, store.get(uriForDocument1));
        assertEquals(document2, store.get(uriForDocument2));

        store.deleteAllWithPrefix("tooth");
        assertEquals(null, store.get(uriForDocument1));
        assertEquals(document2, store.get(uriForDocument2));
        for (int i = 0; i < 100; i++) {
            store.put(new StringBufferInputStream("StringS" + i), new URI("URINUMBERS" + i), txt);
        }

        store.undo(uriForDocument1);
        assertEquals(document1, store.get(uriForDocument1));
        store.undo(uriForDocument2);
        assertEquals(null, store.get(uriForDocument2));


    }

    @Test
    void putUndoUriTest() throws FileNotFoundException, URISyntaxException, IOException {

        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri = new URI("URI");
        URI uri1 = new URI("URI10");

        DocumentStore.DocumentFormat binary = DocumentStore.DocumentFormat.BINARY;
        DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;

        String string1 = "String1";
        String string2 = "String2";

        StringBufferInputStream stringInput1 = new StringBufferInputStream(string1);
        StringBufferInputStream stringInput2 = new StringBufferInputStream(string2);


        byte[] c = new byte[20];
        new Random().nextBytes(c);


        ByteArrayInputStream input1 = new ByteArrayInputStream(c);


        store.put(stringInput1, uri, txt);
        store.undo(uri);
        assertEquals(null, store.get(uri));


        store.put(stringInput1, uri, txt);
        store.put(stringInput2, uri, txt);
        store.undo(uri);
        assertEquals(new DocumentImpl(uri, string1), store.get(uri));
        store.undo(uri);
        assertEquals(null, store.get(uri));
        //assertEquals(new DocumentImpl(uri,string1),store.get(uri));

        store.put(stringInput2, uri1, txt);
        store.put(stringInput1, uri1, txt);

        store.put(stringInput1, uri, txt);
        store.put(stringInput2, uri, txt);

        store.undo(uri1);
        assertEquals(new DocumentImpl(uri1, string2), store.get(uri1));
        store.undo(uri);
        assertEquals(new DocumentImpl(uri, string1), store.get(uri));
        for (int i = 0; i < 100; i++) {

            URI newURI = new URI("NEWURI" + i);

            byte[] b = new byte[20];
            new Random().nextBytes(b);

            ByteArrayInputStream input = new ByteArrayInputStream(b);

            store.put(input, newURI, binary);
            store.put(stringInput1, uri1, txt);
        }
        store.undo(uri);
        assertEquals(null, store.get(uri));


        store.put(stringInput1, uri, txt);
        store.put(null, uri, txt);
        store.undo(uri);
        assertEquals(new DocumentImpl(uri, string1), store.get(uri));

    }


    @Test
    void checkDocumentCaseSensitive() throws URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        Document doc = new DocumentImpl(new URI("URI8"), "The the tHe thE;  Tooooth ToOoOth ");
        assertEquals(doc.wordCount("the"), 1);
        assertEquals(doc.wordCount("The"), 1);
        assertEquals(doc.wordCount("thE"), 1);
        assertEquals(doc.wordCount("tHe"), 1);
        assertEquals(doc.wordCount("Tooooth"), 1);
        assertEquals(doc.wordCount("ToOoOth"), 1);

        assertEquals(0, doc.wordCount("JFAKJSBKJADS"));


        assertEquals(6, doc.getWords().size());
    }


    @Test
    void deleteAllTest() throws URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();

        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");


        DocumentStore.DocumentFormat binary = DocumentStore.DocumentFormat.BINARY;
        DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;

        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";

        StringBufferInputStream stringInput1 = new StringBufferInputStream(string1);
        StringBufferInputStream stringInput2 = new StringBufferInputStream(string2);
        StringBufferInputStream stringInput3 = new StringBufferInputStream(string3);
        StringBufferInputStream stringInput4 = new StringBufferInputStream(string4);
        StringBufferInputStream stringInput5 = new StringBufferInputStream(string5);
        StringBufferInputStream stringInput6 = new StringBufferInputStream(string6);
        StringBufferInputStream stringInput7 = new StringBufferInputStream(string7);
        StringBufferInputStream stringInput8 = new StringBufferInputStream(string8);
        StringBufferInputStream str1Input = new StringBufferInputStream(str1);
        StringBufferInputStream str2Input = new StringBufferInputStream(str2);
        StringBufferInputStream str3Input = new StringBufferInputStream(str3);

        assertEquals(0, store.put(str1Input, testForPrefix1, txt));
        assertEquals(0, store.put(str2Input, testForPrefix2, txt));
        assertEquals(0, store.put(str3Input, testForPrefix3, txt));
        assertEquals(0, store.put(stringInput1, uri1, txt));
        assertEquals(0, store.put(stringInput2, uri2, txt));
        assertEquals(0, store.put(stringInput3, uri3, txt));
        assertEquals(0, store.put(stringInput4, uri4, txt));
        assertEquals(0, store.put(stringInput5, uri5, txt));
        assertEquals(0, store.put(stringInput6, uri6, txt));
        assertEquals(0, store.put(stringInput7, uri7, txt));
        assertEquals(0, store.put(stringInput8, uri8, txt));


        store.deleteAll("Thus");
        assertEquals(null, store.get(testForPrefix1));
        store.undo();
        assertEquals(new DocumentImpl(testForPrefix1, str1), store.get(testForPrefix1));
        store.setMaxDocumentCount(10);


    }

    DocumentStoreImpl setup() throws URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");


        DocumentStore.DocumentFormat binary = DocumentStore.DocumentFormat.BINARY;
        DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;

        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";

        StringBufferInputStream stringInput1 = new StringBufferInputStream(string1);
        StringBufferInputStream stringInput2 = new StringBufferInputStream(string2);
        StringBufferInputStream stringInput3 = new StringBufferInputStream(string3);
        StringBufferInputStream stringInput4 = new StringBufferInputStream(string4);
        StringBufferInputStream stringInput5 = new StringBufferInputStream(string5);
        StringBufferInputStream stringInput6 = new StringBufferInputStream(string6);
        StringBufferInputStream stringInput7 = new StringBufferInputStream(string7);
        StringBufferInputStream stringInput8 = new StringBufferInputStream(string8);
        StringBufferInputStream str1Input = new StringBufferInputStream(str1);
        StringBufferInputStream str2Input = new StringBufferInputStream(str2);
        StringBufferInputStream str3Input = new StringBufferInputStream(str3);


        assertEquals(0, store.put(str1Input, testForPrefix1, txt));
        assertEquals(0, store.put(str2Input, testForPrefix2, txt));
        assertEquals(0, store.put(str3Input, testForPrefix3, txt));
        assertEquals(0, store.put(stringInput1, uri1, txt));
        assertEquals(0, store.put(stringInput2, uri2, txt));
        assertEquals(0, store.put(stringInput3, uri3, txt));
        assertEquals(0, store.put(stringInput4, uri4, txt));
        assertEquals(0, store.put(stringInput5, uri5, txt));
        assertEquals(0, store.put(stringInput6, uri6, txt));
        assertEquals(0, store.put(stringInput7, uri7, txt));
        assertEquals(0, store.put(stringInput8, uri8, txt));

        return store;

    }

    @Test
    void DeleteAllWithPrefixUndo() throws URISyntaxException, IOException {

        DocumentStoreImpl store = setup();

        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");


        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";

        assertEquals(8, store.deleteAllWithPrefix("String").size());
        assertEquals(null, store.get(uri6));
        assertEquals(null, store.get(uri7));
        store.undo(uri7);
        assertEquals(new DocumentImpl(uri7, string7), store.get(uri7));
        assertEquals(null, store.get(uri6));
        store.undo();
        assertEquals(new DocumentImpl(uri6, string6), store.get(uri6));
        assertEquals(new DocumentImpl(uri7, string7), store.get(uri7));
        assertEquals(new DocumentImpl(uri1, string1), store.get(uri1));
        assertEquals(new DocumentImpl(uri2, string2), store.get(uri2));
        assertEquals(new DocumentImpl(uri3, string3), store.get(uri3));
        assertEquals(new DocumentImpl(uri4, string4), store.get(uri4));
        assertEquals(new DocumentImpl(uri5, string5), store.get(uri5));
        assertEquals(new DocumentImpl(uri8, string8), store.get(uri8));

    }

    @Test
    void DeleteAllUndo() throws URISyntaxException, IOException {

        DocumentStoreImpl store = setup();

        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");


        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";

        assertEquals(1, store.deleteAllWithPrefix("String7").size());
        assertEquals(null, store.get(uri7));
        store.undo(uri7);
        assertEquals(new DocumentImpl(uri7, string7), store.get(uri7));

        store.setMaxDocumentCount(1);
        assertEquals(new DocumentImpl(uri7, string7), store.get(uri7));

    }

    @Test
    public void stage5Test() throws URISyntaxException, IOException {
        DocumentStoreImpl store = setup();
        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");


        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";


        store.setMaxDocumentCount(10);
        File fileTestForPrefix1 = new File(System.getProperty("user.dir") + "/" + testForPrefix1 + ".json");
        assertEquals(true, fileTestForPrefix1.exists());
        store.get(testForPrefix1);
        assertEquals(false, fileTestForPrefix1.exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix2 + ".json").exists());
    }

    @Test
    void setMax() throws FileNotFoundException, URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();

        URI uri1 = new URI("URIOFTHEDOCUMENTNUMBER1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");


        DocumentStore.DocumentFormat binary = DocumentStore.DocumentFormat.BINARY;
        DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;

        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";

        StringBufferInputStream stringInput1 = new StringBufferInputStream(string1);
        StringBufferInputStream stringInput2 = new StringBufferInputStream(string2);
        StringBufferInputStream stringInput3 = new StringBufferInputStream(string3);
        StringBufferInputStream stringInput4 = new StringBufferInputStream(string4);
        StringBufferInputStream stringInput5 = new StringBufferInputStream(string5);
        StringBufferInputStream stringInput6 = new StringBufferInputStream(string6);
        StringBufferInputStream stringInput7 = new StringBufferInputStream(string7);
        StringBufferInputStream stringInput8 = new StringBufferInputStream(string8);
        StringBufferInputStream str1Input = new StringBufferInputStream(str1);
        StringBufferInputStream str2Input = new StringBufferInputStream(str2);
        StringBufferInputStream str3Input = new StringBufferInputStream(str3);


        assertEquals(0, store.put(stringInput1, uri1, txt));
        assertEquals(0, store.put(stringInput2, uri2, txt));
        assertEquals(0, store.put(stringInput3, uri3, txt));
        assertEquals(0, store.put(stringInput4, uri4, txt));
        assertEquals(0, store.put(stringInput5, uri5, txt));
        assertEquals(0, store.put(stringInput6, uri6, txt));
        assertEquals(0, store.put(stringInput7, uri7, txt));
        assertEquals(0, store.put(stringInput8, uri8, txt));

        store.setMaxDocumentCount(7);

        assertEquals(true, store.search("String1").contains(new DocumentImpl(uri1, string1)));
        assertEquals(false, new File(System.getProperty("user.dir") + "/" + uri1 + ".json").exists());
        store.undo(uri1);

        assertEquals(null, store.get(uri1));

        store.undo();
        assertEquals(null, store.get(uri8));
        store.undo(uri6);
        assertEquals(null, store.get(uri6));


        assertEquals(0, store.put(stringInput1, uri1, txt));
        assertEquals(0, store.put(stringInput6, uri6, txt));
        assertEquals(0, store.put(stringInput8, uri8, txt));


        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri2 + ".json").exists());

        assertEquals(new DocumentImpl(uri2, string2).hashCode(), store.put(stringInput2, uri2, txt));

        assertEquals(true, store.search("String3").contains(new DocumentImpl(uri3, string3)));


        assertEquals(true, store.search("that").contains(new DocumentImpl(uri2, string2)));
        assertEquals(7, store.search("that").size());


        store.setMaxDocumentCount(10);
        assertEquals(0, store.put(str1Input, testForPrefix1, txt));
        assertEquals(0, store.put(str2Input, testForPrefix2, txt));
        assertEquals(0, store.put(str3Input, testForPrefix3, txt));

        assertEquals(new DocumentImpl(testForPrefix1, str1), store.searchByPrefix("Thus").get(0));
        assertEquals(new DocumentImpl(testForPrefix2, str2), store.searchByPrefix("Thus").get(1));
        assertEquals(new DocumentImpl(testForPrefix3, str3), store.searchByPrefix("Thus").get(2));
    }

    @Test
    public void stage5Testing() throws URISyntaxException, IOException {

        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");

        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";

        InputStream inputStream = new StringBufferInputStream(string5);
        DocumentStore documentStore = setup();
        int numberOfCurrentBytes = string1.getBytes().length + string2.getBytes().length + string3.getBytes().length + string4.getBytes().length + string5.getBytes().length + string6.getBytes().length + string7.getBytes().length + string8.getBytes().length + str1.getBytes().length + str2.getBytes().length + str3.getBytes().length;

        documentStore.setMaxDocumentBytes(numberOfCurrentBytes - str1.getBytes().length - 1);
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix1 + ".json").exists());
        documentStore.put(inputStream, testForPrefix1, DocumentStore.DocumentFormat.TXT);
        assertEquals(new DocumentImpl(testForPrefix1, string5), documentStore.get(testForPrefix1));
        documentStore.undo();
        assertEquals(new DocumentImpl(testForPrefix1, str1), documentStore.get(testForPrefix1));
        assertEquals(false, new File(System.getProperty("user.dir") + "/" + testForPrefix1 + ".json").exists());

    }

    @Test
    void maxBytesTest() throws URISyntaxException, IOException {
        DocumentStoreImpl store = setup();

        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");


        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";

        int numberOfCurrentBytes = string1.getBytes().length + string2.getBytes().length + string3.getBytes().length + string4.getBytes().length + string5.getBytes().length + string6.getBytes().length + string7.getBytes().length + string8.getBytes().length + str1.getBytes().length + str2.getBytes().length + str3.getBytes().length;


        store.setMaxDocumentBytes(numberOfCurrentBytes - str1.getBytes().length - 1);
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + "/" + testForPrefix1 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + "/" + testForPrefix2 + ".json").exists());
        store.setMaxDocumentCount(8);
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + "/" + testForPrefix3 + ".json").exists());
        store.delete(uri7);
        assertEquals(null, store.get(uri7));
        store.undo();
        assertEquals(new DocumentImpl(uri7, string7), store.get(uri7));
        store.setMaxDocumentBytes(numberOfCurrentBytes - str1.getBytes().length - str2.getBytes().length - str3.getBytes().length - (string1.getBytes().length + string2.getBytes().length + string3.getBytes().length));
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri1 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri2 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri3 + ".json").exists());
        assertEquals(new DocumentImpl(uri4, string4), store.get(uri4));
        store.setMaxDocumentBytes(numberOfCurrentBytes - str1.getBytes().length - str2.getBytes().length - str3.getBytes().length - (string1.getBytes().length + string2.getBytes().length + string3.getBytes().length) - 1);
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri5 + ".json").exists());
        store.deleteAllWithPrefix("JKKDKD00");
        assertEquals(null, store.get(uri6));
        StringBufferInputStream stringInput6 = new StringBufferInputStream(string6);
        DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;
        assertEquals(0, store.put(stringInput6, uri6, txt));
        StringBufferInputStream str1Input = new StringBufferInputStream(str1);
        assertEquals(new DocumentImpl(testForPrefix1, str1).hashCode(), store.put(str1Input, testForPrefix1, txt));
        store.setMaxDocumentCount(4);
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri8 + ".json").exists());
        StringBufferInputStream stringInput8 = new StringBufferInputStream(string8);
        assertEquals(new DocumentImpl(uri8, string8).hashCode(), store.put(stringInput8, uri8, txt));
        store.setMaxDocumentBytes(0);
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri6 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri7 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri8 + ".json").exists());
    }

    @Test
    public void inputTooLarge() throws URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(1);
        store.put(new StringBufferInputStream("STRING1"), new URI("URI1"), DocumentStore.DocumentFormat.TXT);
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + new URI("URI1") + ".json").exists());
    }

    @Test
    public void getWhenDocumentIsInDisk() throws URISyntaxException, IOException {
        DocumentStore store = setup();

        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");


        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";
        store.setMaxDocumentBytes(10);
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix1 + ".json").exists());
        assertEquals(new DocumentImpl(testForPrefix1, str1), store.get(testForPrefix1));
    }

    @Test
    public void CheckStage5() throws URISyntaxException, IOException {
        DocumentStore store = setup();

        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");


        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";
        store.setMaxDocumentCount(10);

        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix1 + ".json").exists());
        assertEquals(new DocumentImpl(testForPrefix1, str1), store.get(testForPrefix1));
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix2 + ".json").exists());
        assertEquals(false, new File(System.getProperty("user.dir") + "/" + testForPrefix1 + ".json").exists());

        store.setMaxDocumentCount(1);
        assertEquals(false, new File(System.getProperty("user.dir") + "/" + testForPrefix1 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix2 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix3 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri1 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri2 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri3 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri4 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri5 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri6 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri7 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri8 + ".json").exists());


    }

    @Test
    public void CheckBinaryStage5() throws URISyntaxException, IOException {
        DocumentStore store = setup();

        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URI2");
        URI uri3 = new URI("URI3");
        URI uri4 = new URI("URI4");
        URI uri5 = new URI("URI5");
        URI uri6 = new URI("URI66");
        URI uri7 = new URI("URI777");
        URI uri8 = new URI("URI888");
        URI testForPrefix1 = new URI("testForPrefix1");
        URI testForPrefix2 = new URI("testForPrefix2");
        URI testForPrefix3 = new URI("testForPrefix3");


        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String4,  includs:Th'e the The the thEH Theee The the the the the 6 times";
        String string5 = "String5, that includes tHE, The, th'e  the Theseeshj 2 times ";
        String string6 = "String6, that includes tHE, JKKDKD00 FDKKD , FDDF  FDDthGGGetjDDe DDthHD DCFFothan, string s 1 time";
        String string7 = "String7, that includes tHE, the the t'he th:e th:e the  The, th'e the the Dothan, strindg s 9 times ";
        String string8 = "String8, that includs:Th'e the The the, Striojng strign sjr the teh the the the the 7 times ";
        String str1 = "ThusDocument Thus Thus Thus";
        String str2 = "ThusDocumentCompare ThusDocumentCompare";
        String str3 = "ThusDocumentComparer";
        store.setMaxDocumentCount(10);

        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix1 + ".json").exists());
        assertEquals(new DocumentImpl(testForPrefix1, str1), store.get(testForPrefix1));
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix2 + ".json").exists());
        assertEquals(false, new File(System.getProperty("user.dir") + "/" + testForPrefix1 + ".json").exists());

        store.setMaxDocumentBytes(new DocumentImpl(testForPrefix1, str1).getDocumentTxt().getBytes().length);
        assertEquals(false, new File(System.getProperty("user.dir") + "/" + testForPrefix1 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix2 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + testForPrefix3 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri1 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri2 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri3 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri4 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri5 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri6 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri7 + ".json").exists());
        assertEquals(true, new File(System.getProperty("user.dir") + "/" + uri8 + ".json").exists());
    }

    @AfterAll
    public static void cleanUp() throws IOException {
        String userDir = System.getProperty("user.dir");
        Path path = Paths.get(userDir);
        Path parentPath = path.getParent();
        Path directory = Paths.get(parentPath.toUri());
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".json")) {
                    Files.deleteIfExists(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (dir.getFileName().toString().endsWith(".json")) {
                    Files.deleteIfExists(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });

    }

    @Test
    public void stage5Test2() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();

        URI uri1 = new URI("URI1");
        URI uri2 = new URI("URIOFDOCUMENT2");
        URI uri3 = new URI("URI333333333");

        String string1 = "String1, that includes tHE, The, th'e the the, the the theeee the eheh the she the THE is 8 times  ";
        String string2 = "String2, that includs:Th'e the The the, the hhththt ththe thee thre 3 Times";
        String string3 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";
        String string4 = "String3, that includes tHE, The, th'e the the theee the Th E the htEH THh E 5 times";


        InputStream stringBufferInputStream1 = new StringBufferInputStream(string1);
        InputStream stringBufferInputStream2 = new StringBufferInputStream(string2);
        InputStream stringBufferInputStream3 = new StringBufferInputStream(string3);
        InputStream inputStream = new StringBufferInputStream(string4);


        store.setMaxDocumentBytes(string1.getBytes().length + string2.getBytes().length + string3.getBytes().length);
        store.put(stringBufferInputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.put(stringBufferInputStream2, uri2, DocumentStore.DocumentFormat.TXT);
        store.put(stringBufferInputStream3, uri3, DocumentStore.DocumentFormat.TXT);
        store.put(inputStream, uri3, DocumentStore.DocumentFormat.TXT);
        assertEquals(false, new File(System.getProperty("user.dir") + "/" + uri1 + ".json").exists());
        assertEquals(false, new File(System.getProperty("user.dir") + "/" + uri2 + ".json").exists());
        assertEquals(false, new File(System.getProperty("user.dir") + "/" + uri3 + ".json").exists());

    }

    @Test
    public void stage5Testing1() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl(new File("/Users/dothanbar/Desktop/STAGE-5-TEST"));

        String str1 = "StringURI1";
        String str2 = "StringURI2";
        String str3 = "StringURI3";

        URI uri1 = new URI("URI-NUMBER1");
        URI uri2 = new URI("URI-NUMBER2");
        URI uri3 = new URI("URI-NUMBER3");


        store.setMaxDocumentCount(2);
        store.put(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        Document doc1 = store.get(uri1);
        Document doc2 = store.get(uri2);

        store.put(new ByteArrayInputStream(str3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

        assertEquals(true, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri1 + ".json").exists());
        assertEquals(false, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri2 + ".json").exists());
        assertEquals(false, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri3 + ".json").exists());

        store.search(str1);
        Document doc1v2 = store.get(uri1);
        int k = 0;
        if (doc1v2 == doc1) {
            k = 1;
        }
        assertEquals(0, k);

        assertEquals(true, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri2 + ".json").exists());
        assertEquals(false, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri1 + ".json").exists());
        assertEquals(false, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri3 + ".json").exists());

        store.search("StringURI2");
        Document doc2v2 = store.get(uri2);


        int z = 0;
        if (doc1v2 == doc1) {
            z = 1;
        }

        assertEquals(0, z);
        assertEquals(true, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri3 + ".json").exists());
        assertEquals(false, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri1 + ".json").exists());
        assertEquals(false, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri2 + ".json").exists());
    }


    @Test
    public void deleteViaDiskTest() throws URISyntaxException, IOException {
    DocumentStore store = new DocumentStoreImpl(new File("/Users/dothanbar/Desktop/STAGE-5-TEST"));
    store.setMaxDocumentCount(2);

        String str1 = "STRING1";
        String str2 = "String2";
        String str3 = "String3";
        URI uri1 = new URI("URI1");
        URI uri2 = new URI("uri2");
        URI uri3 = new URI("uri3");

        store.put(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        Document doc1 = store.get(uri1);
        Document doc2 = store.get(uri2);
        store.put(new ByteArrayInputStream(str3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

        assertEquals(true, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri1+ ".json").exists());
        assertEquals(false, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri2 + ".json").exists());
        assertEquals(false, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri3 + ".json").exists());

        store.delete(uri3);
        assertEquals(true, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri1+ ".json").exists());
        store.search(str1);
        assertEquals(false, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri1+ ".json").exists());
        assertEquals(false, new File("/Users/dothanbar/Desktop/STAGE-5-TEST" + "/" + uri2 + ".json").exists());

    }
}