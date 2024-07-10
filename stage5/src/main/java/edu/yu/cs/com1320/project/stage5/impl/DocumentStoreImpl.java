package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {
    private BTree<URI, Document> bTree;
    private Trie<URI> trie;
    private StackImpl<Undoable> commandStack;
    private MinHeapImpl<MinHeapNode> minHeap;
    private HashMap<URI, MinHeapNode> minHeapHashMap;
    private HashSet<URI> documentsInDisk;
    private int keys;
    private int maxDocumentCount;
    private int maxDocumentBytes;
    private int currentBytes;

    private class MinHeapNode implements Comparable<MinHeapNode> {
        private final URI key;

        @Override
        public int compareTo(MinHeapNode o) {
            if (o == null)
                return 1;

            try {
                if (this.getNumber() > o.getNumber()) {
                    return 1;
                }
            else {
                return -1;
            } }catch(NullPointerException e){
                    return -1;}
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            MinHeapNode check = (MinHeapNode) obj;
            if (this.getKey() == check.getKey()) {
                return true;
            } else {
                return false;
            }
        }

        private Long getNumber() {
            return bTree.get(this.key).getLastUseTime();
        }

        private URI getKey() {
            return key;
        }

        public MinHeapNode(URI uri) {
            this.key = uri;
        }
    }

    public DocumentStoreImpl() {
        this.bTree = new BTreeImpl<>();
        this.bTree.setPersistenceManager(new DocumentPersistenceManager(null));
        this.trie = new TrieImpl<>();
        this.commandStack = new StackImpl<>();
        this.minHeap = new MinHeapImpl<>();
        this.keys = 0;
        this.currentBytes = 0;
        this.maxDocumentBytes = -1;
        this.maxDocumentCount = -1;
        this.minHeapHashMap = new HashMap<>();
        this.documentsInDisk = new HashSet<>();
    }
    public DocumentStoreImpl(File baseDir) {
        this.bTree = new BTreeImpl<>();
        this.bTree.setPersistenceManager(new DocumentPersistenceManager(baseDir));
        this.trie = new TrieImpl<>();
        this.commandStack = new StackImpl<>();
        this.minHeap = new MinHeapImpl<>();
        this.keys = 0;
        this.currentBytes = 0;
        this.maxDocumentBytes = -1;
        this.maxDocumentCount = -1;
        this.minHeapHashMap = new HashMap<>();
        this.documentsInDisk = new HashSet<>();
    }

    @Override
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || format == null) {
            throw new IllegalArgumentException();
        }
        //if Input is null, meaning this is a delete call.
        //need to add to element to CommandStack+remove from heap+ remove from trie.
        //plus update current keys+storeBytes.
        try {
            if (input == null) {
                Document deletedDoc = this.bTree.get(uri);
                if(delete(uri)){
                    return deletedDoc.hashCode();}
                return 0;
            } else {
                DocumentImpl document = null;
                byte[] bytes = input.readAllBytes();
                switch (format) {
                    case TXT:
                        String str = new String(bytes);
                        document = new DocumentImpl(uri, str);
                        break;
                    case BINARY:
                        document = new DocumentImpl(uri, bytes);
                        break;
                }
                int returned = put(document);
                this.keys++;
                this.currentBytes = this.currentBytes + bytes.length;
                putDocumentsToDisk();
                storeBalancer();
                input.reset();
                //Store needs to be balanced.
                //Need to add new DocumentURI to Trie+Heap+HeapMap+commandSet.
                return returned;
            }
        } catch (Exception t) {
            input.reset();
            putDocumentsToDisk();
            return 0;
        }

    }
        private int put(Document doc){
            int returned = 0;
            if (this.documentsInDisk.contains(doc.getKey()) || this.minHeapHashMap.containsKey(doc.getKey())) {
                Document temp = bTree.get(doc.getKey());
                //need to delete all the prevDocuments references in the trie.
                //remove temp from heap
                for (String word : bTree.get(temp.getKey()).getWords()) {
                    this.trie.delete(word,temp.getKey());
                }
                deleteFromHeap(temp);
                //create command
                GenericCommand command = newCommandPutExistingDocument(temp,doc);
                this.commandStack.push(command);
                if(this.documentsInDisk.contains(doc.getKey()) ) {
                    this.documentsInDisk.remove(temp.getKey());
                } else {
                    removeFromCurrentBytes(temp.getKey(),false);
                    keys--;
                }
                returned = bTree.put(doc.getKey(),doc).hashCode();
            } else  {
                GenericCommand command = commandPutNEDocument(doc);
                this.commandStack.push(command);
                bTree.put(doc.getKey(),doc);
            }
            long time = System.nanoTime();
            MinHeapNode node = new MinHeapNode(doc.getKey());
            this.minHeap.insert(node);
            this.minHeapHashMap.put(doc.getKey(),node);
            doc.setLastUseTime(time);
            for (String string : doc.getWords()) {
                trie.put(string, doc.getKey());
            }
            return returned;
        }


        @Override
        public Document get (URI uri) {
            if(uri == null){
                return  null;
            }
            long time = System.nanoTime();
            Document doc = bTree.get(uri);
            if(doc == null)
                return null;
            if (this.documentsInDisk.contains(uri)) {
                if(doc.getDocumentTxt() != null) {
                    if(this.keys == 0 || doc.getDocumentTxt().getBytes().length>this.currentBytes){
                        try {
                            this.bTree.moveToDisk(doc.getKey());
                            return doc;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }else if(doc.getDocumentBinaryData().length > this.maxDocumentBytes ||this.keys == 0){
                    try {
                        this.bTree.moveToDisk(doc.getKey());
                        return doc;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                keys++;
                addDocumentToCurrentBytes(doc);
                this.documentsInDisk.remove(uri);
                doc.setLastUseTime(time);

                MinHeapNode node = new MinHeapNode(doc.getKey());
                this.minHeapHashMap.put(uri,node);

                this.minHeap.reHeapify(node);
                try {
                    storeBalancer();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return doc;
            } else if(doc != null){
                doc.setLastUseTime(time);
                MinHeapNode node = this.minHeapHashMap.get(doc.getKey());

                this.minHeap.reHeapify(node);
                putDocumentsToDisk();
                return doc;
            }
            putDocumentsToDisk();;
            return doc;
        }

        @Override
        public boolean delete (URI uri){
            if (uri == null) {
                return false;
            }

            deleteFromHeap(bTree.get(uri));
            Document doc = bTree.put(uri,null);
            if (doc == null) {
                return false;
            }
            GenericCommand<URI> command = deleteCommand(doc);
            this.commandStack.push(command);
            //remove from heap.

            //delete from trie.
            for (String word : doc.getWords()) {
                this.trie.delete(word,uri);
            }
            //update keys and byte count.
            this.keys--;
            if(doc.getDocumentTxt() == null){
            this.currentBytes = this.currentBytes -doc.getDocumentBinaryData().length;}
            else{
                this.currentBytes = this.currentBytes - doc.getDocumentTxt().getBytes().length;
            }
            return true;
        }


    @Override
    public void undo() {
        if(commandStack.size()==0)
            throw new IllegalStateException();
        //need to check that undo doesn't exceed limits. and if it does, delete necessary documents
        Undoable command =commandStack.pop();
        command.undo();
        putDocumentsToDisk();
        try {
            storeBalancer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void undo(URI uri) throws IllegalStateException {
        if(this.commandStack.size()==0){
            throw new IllegalStateException();
        }//need to find the last action done by the given URI. need to copy all the given comm
        Undoable command;
        StackImpl <Undoable>  tempStack= new StackImpl<>();
        Boolean undo=false;
        while(commandStack.size()!=0){
            command= this.commandStack.pop();
            if(command instanceof GenericCommand){
                if(((GenericCommand<URI>) command).getTarget() == uri) {
                    undo = command.undo();break;
                } else {
                    tempStack.push(command);
                }
            }else if(command instanceof CommandSet){
                if(((CommandSet<URI>) command).containsTarget(uri)){
                    undo=((CommandSet<URI>) command).undo(uri);
                    tempStack.push(command);
                    break;
                } else { if(((CommandSet<URI>) command).size()!=0)tempStack.push(command);} }
        }
        while(tempStack.size()!=0) {
            Undoable tempCommand = tempStack.pop();
            this.commandStack.push(tempCommand);
        }
        try {
            storeBalancer();
            putDocumentsToDisk();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(undo){
            return;}
        throw new IllegalStateException();
    }

    @Override
    public List<Document> search(String keyword) {
        Comparator<URI> trieComparator = new Comparator<>() {
            String str=keyword;
            @Override public int compare(URI uri1, URI uri2) {
                try {
                int uri1Count = bTree.get(uri1).wordCount(str);
                int uri2Count = bTree.get(uri2).wordCount(str);
                    if(uri1Count - uri2Count<0){
                        return -1;
                    }
                    else if(uri1Count - uri2Count>0){
                        return  1;
                    } } catch (NullPointerException e){
                    return -1;
                }
                return 1;

            }

        };
        List<URI>list= trie.getAllSorted(keyword,trieComparator);
        List<Document>documents = new ArrayList<>();
        for(URI uri : list) {
            Document doc = this.bTree.get(uri);
            documents.add(doc);
            if(documentsInDisk.remove(uri) ){
                keys++;
                addDocumentToCurrentBytes(doc);
                MinHeapNode node = new MinHeapNode(doc.getKey());
                this.minHeapHashMap.put(doc.getKey(),node);
            }
        }
        putDocumentsToDisk();
        updateHeap(list);
        try {
            storeBalancer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return documents;
    }

    private void putDocumentsToDisk() {
        for(URI uri:this.documentsInDisk){
            try {
                this.bTree.moveToDisk(uri);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        Comparator<URI> prefixDocumentComparator = new Comparator<>() {
            String prefix=keywordPrefix;

            @Override public int compare(URI uri1, URI uri2) {
                    return prefixWithHighestWordCount(uri2,prefix)-prefixWithHighestWordCount(uri1,prefix);

            }
            private int prefixWithHighestWordCount(URI uri, String prefix){
                int max=0;
                Document doc = bTree.get(uri);
                for(String str:doc.getWords()){
                    if(str.startsWith(prefix)&&(doc.wordCount(str)>max)){
                        max=doc.wordCount(str);
                    }
                }
                return max;
            }


        };
        List<URI> list=trie.getAllWithPrefixSorted(keywordPrefix,prefixDocumentComparator);
        putDocumentsToDisk();
        updateHeap(list);
        List<Document>documents = new ArrayList<>();
        for(int i = 0; i<list.size();i++){
            URI uri = list.get(i);
            if(!this.documentsInDisk.contains(uri))
                documents.add(this.bTree.get(uri));
        }
        return documents;
    }



    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<URI>set= trie.deleteAll(keyword);
        CommandSet<URI> commandSet= new CommandSet<>();
        for(URI uri:set){
            Document doc = bTree.get(uri);
            deleteFromHeap(doc);
            //create command and add it to commandSet
            GenericCommand<URI> command= deleteCommand(doc);
            commandSet.addCommand(command);
            //remove from hashtable
            this.bTree.put(doc.getKey(),null);
            //remove from heap
            //update current limits.
            keys--;
            removeFromCurrentBytes(doc);
        }
        putDocumentsToDisk();
        this.commandStack.push(commandSet);
        set = new HashSet<>(set);
        return set;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<URI>set= trie.deleteAllWithPrefix(keywordPrefix);
        CommandSet<URI> commandSet= new CommandSet<>();
        for(URI uri:set){
            Document doc = bTree.get(uri);
            deleteFromHeap(doc);
            //create command and add it to commandSet
            GenericCommand<URI> command= deleteCommand(doc);
            commandSet.addCommand(command);
            //remove from hashtable
            this.bTree.put(doc.getKey(),null);
            //remove from heap

            //update current limits.
            keys--;
            removeFromCurrentBytes(doc);
        }
        putDocumentsToDisk();
        this.commandStack.push(commandSet);
        set = new HashSet<>(set);
        return set;}

    @Override
    public void setMaxDocumentCount(int limit) {
        this.maxDocumentCount = limit;
        try {
            storeBalancer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        this.maxDocumentBytes = limit;
        try {
            storeBalancer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void storeBalancer() throws Exception {
        while (this.keys > this.maxDocumentCount && this.maxDocumentCount != -1) {
            URI uri = this.minHeap.remove().key;
            removeFromCurrentBytes(uri,true);
            this.bTree.moveToDisk(uri);
            this.documentsInDisk.add(uri);
            keys--;
            this.minHeapHashMap.remove(uri);
        }
        if (this.maxDocumentBytes < this.currentBytes && this.maxDocumentBytes != -1) {
            maxDocumentBytesReached();
        }
    }

    private void deleteDocumentFromTrie(URI uri){
        if (!this.documentsInDisk.contains(uri)) {
            for (String word : bTree.get(uri).getWords()) {
                this.trie.delete(word,uri);
            }
        }
    }
    private void removeFromCurrentBytes(URI uri,boolean toPutInDisk) {
        if (!this.documentsInDisk.contains(uri) || this.minHeapHashMap.containsKey(uri)) {
            int bytes;
            Document tempDoc = bTree.get(uri);
            if (tempDoc.getDocumentTxt() == null) {
                bytes = tempDoc.getDocumentBinaryData().length;
            } else {
                bytes = tempDoc.getDocumentTxt().getBytes().length;
            }
            this.currentBytes = this.currentBytes - bytes;
            if(toPutInDisk)
            this.documentsInDisk.add(uri);
        }
    }
    private void removeFromCurrentBytes(Document doc) {
            int bytes;
            if (doc.getDocumentTxt() == null) {
                bytes = doc.getDocumentBinaryData().length;
            } else {
                bytes = doc.getDocumentTxt().getBytes().length;
            }
            this.currentBytes = this.currentBytes - bytes;
    }
    private void maxDocumentBytesReached() throws Exception {
        while(this.currentBytes>this.maxDocumentBytes){
            URI uri=this.minHeap.remove().key;
            removeFromCurrentBytes(uri,true);
            keys--;
            this.bTree.moveToDisk(uri);
            this.minHeapHashMap.remove(uri);
        }
    }
    private void deleteFromHeap(Document doc) {
        if(this.minHeapHashMap.containsKey(doc.getKey())) {
            MinHeapNode minHeapNode = this.minHeapHashMap.get(doc.getKey());
            doc.setLastUseTime(1);
            this.minHeap.reHeapify(minHeapNode);
            this.minHeap.remove();
            this.minHeapHashMap.remove(doc.getKey());
        }
    }
    private GenericCommand commandPutNEDocument(Document document) {
        GenericCommand<URI> command = new GenericCommand<>(document.getKey(), (URI uriCommand) -> {
            if(documentsInDisk.contains(document)){
            document.setLastUseTime(1);
            this.minHeap.reHeapify(this.minHeapHashMap.get(document.getKey()));
            this.minHeap.remove();}
            if (this.bTree.put(document.getKey(), null).equals(document)) {
                //delete all references in the trie.
                for (String word : document.getWords()) {
                    this.trie.delete(word,document.getKey());
                }

                if(!this.documentsInDisk.contains(document.getKey())){
                    keys--;
                    removeFromCurrentBytes(document);
                    this.minHeapHashMap.remove(document.getKey());
                }

                this.documentsInDisk.remove(document.getKey());
                return true;
            } else {
                return false;
            }

        });
        return command;
    }
    private GenericCommand newCommandPutExistingDocument(Document prevDocument,Document currentDocument) {
        GenericCommand<URI> command = new GenericCommand<>(currentDocument.getKey(), (URI uriCommand) -> {
            deleteFromHeap(currentDocument);
            if (this.bTree.put(uriCommand, prevDocument).equals(currentDocument)) {
                //remove document from trie
                deleteDocumentFromTrie(currentDocument.getKey());
                //put new document in the trie.
                for (String str : prevDocument.getWords()) {
                    trie.put(str, prevDocument.getKey());
                }
                //insert new document in heap.
                MinHeapNode node = new MinHeapNode(prevDocument.getKey());
                this.minHeap.insert(node);
                this.minHeapHashMap.put(uriCommand,node);
                //remove old document from current byte count
                //add new document to current byte count
                int bytes;
                if (currentDocument.getDocumentTxt() == null) {
                    bytes = currentDocument.getDocumentBinaryData().length;
                } else {
                    bytes = currentDocument.getDocumentTxt().getBytes().length;
                }
                this.currentBytes = this.currentBytes - bytes;
                addDocumentToCurrentBytes(prevDocument);
                try {
                    storeBalancer();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return true;
            } else {
                return false;
            }

        });
        return command;
    }
    private void addDocumentToCurrentBytes(Document doc){
        int bytes;
        if (doc.getDocumentTxt()==null){
            bytes=doc.getDocumentBinaryData().length;
        } else{
            bytes=doc.getDocumentTxt().getBytes().length;
        }
        this.currentBytes=this.currentBytes+bytes;
    }
    private GenericCommand<URI> deleteCommand(Document document){
        GenericCommand<URI> command= new GenericCommand<>(document.getKey(),(URI uriCommand)-> {
            if(this.bTree.put(uriCommand,document)==null){
                for(String str:document.getWords()){
                    this.trie.put(str,document.getKey());}
                long nanoTime = System.nanoTime();
                document.setLastUseTime(nanoTime);
                MinHeapNode node = new MinHeapNode(document.getKey());
                this.minHeap.insert(node);
                this.minHeapHashMap.put(document.getKey(),node);

                this.keys++;
                addDocumentToCurrentBytes(document);
                return true;}
            else{
                return false;
            }

        });
        return command;
    }
    private void updateHeap(List<URI> list){
        for(URI uri: list){
            if(this.minHeapHashMap.containsKey(uri)){
            MinHeapNode node = this.minHeapHashMap.get(uri);
            this.minHeap.reHeapify(node);
        } }
    }
    private void updateHeap(URI uri){
        if(!this.documentsInDisk.contains(uri)){
            MinHeapNode node = this.minHeapHashMap.get(uri);
            this.minHeap.reHeapify(node);
        }
    }
}
