package edu.yu.cs.com1320.project.stage5.impl;
import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    private URI uri;
    private String str;
    private byte[] byteAr;
    private Map<String,Integer> wordCounts;
    private  transient long time;

    public DocumentImpl(URI uri, String txt){
        if(uri==null||txt==null||txt.length()<1){
            throw new IllegalArgumentException();
        }
        this.uri=uri;
        this.str=txt;
        this.time=System.nanoTime();
        this.byteAr=null;
        this.wordCounts=new HashMap<>();
        getWordCount();
    }
    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCountMap){
        this.uri=uri;
        this.str=text;
        this.byteAr=null;
        this.wordCounts = new HashMap<>();
        if(wordCountMap==null){
        getWordCount();}else{
            this.wordCounts = wordCountMap;
        }


    }

    private void getWordCount() {
        String[] words = this.str.replaceAll("[^a-zA-Z0-9 ]", "").split(" ");
        for(int i =0;i<words.length;i++)
            this.wordCounts.merge(words[i], 1, Integer::sum);
        wordCounts.remove("");
    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if(uri==null||binaryData==null||binaryData.length==0){
            throw new IllegalArgumentException();
        }
        this.uri=uri;
        this.str=null;
        this.byteAr=binaryData;
        this.time=System.nanoTime();
        this.wordCounts= Collections.emptyMap();
    }
    /**
     * @return content of text document
     */
    public String getDocumentTxt(){
        return this.str;
    }

    /**
     * @return content of binary data document
     */
    public byte[] getDocumentBinaryData(){
        return this.byteAr;

    }

    /**
     * @return URI which uniquely identifies this document
     */
    public URI getKey(){
        return this.uri;
    }

    @Override
    public int  wordCount(String word) {
        if (this.wordCounts.get(word)==null){
            return 0;}else{
            return this.wordCounts.get(word);}
    }

    @Override
    public Set<String> getWords() {
        return this.wordCounts.keySet();
    }

    @Override
    public long getLastUseTime() {
        return this.time;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.time = timeInNanoseconds;
    }

    @Override
    public Map<String, Integer> getWordMap() {
        return this.wordCounts;
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        this.wordCounts=wordMap;
    }

    @Override
    public int hashCode(){
        return Utils.calculateHashCode(this.uri,this.str,this.getDocumentBinaryData());}


    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj==null){
            return false;
        }
        if(getClass()!=obj.getClass()){
            return false;
        }

        DocumentImpl check= (DocumentImpl)obj;


        if(this.hashCode()==check.hashCode()){
            return true;
        } else{
            return false;
        }

    }


    @Override
    public int compareTo(Document o) {
        int toBereturned ;
        if(o==null)
            toBereturned=1;

        if(this.time>o.getLastUseTime())
            toBereturned=1;
        else{
            toBereturned =-1;
        }
        return toBereturned;

    }
}
