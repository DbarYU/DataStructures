package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;


import java.util.*;


public class TrieImpl <Value>  implements Trie<Value> {
    private Set setToBeReturned;
    private Value valToBeReturned;
    private static final int alphabetSize = 62;
    private Node root;

    public static class Node<Value> {
        protected List<Value> values;
        protected TrieImpl.Node[] links = new TrieImpl.Node[TrieImpl.alphabetSize];

        public Node(){
            this.values= new ArrayList<>(0);}

    }


    public TrieImpl() {
        this.root= new Node();
        this.root.values=null;
    }

    @Override
    public void put(String key, Value val) {
        if (key == null) throw new IllegalArgumentException();
        this.root = put(this.root, key, val, 0);
    }


    @Override
    public List getAllSorted(String key, Comparator comparator) {
        Node x= get(this.root,key,0);
        if(x==null)
            return Collections.emptyList();
        List<Value> newList = new ArrayList<>(x.values);
        Collections.sort(newList,Collections.reverseOrder(comparator));
       return newList;

    }

    @Override

    public List getAllWithPrefixSorted(String prefix, Comparator comparator) {
        Set<Value> set=new HashSet<>();
        Node prefixNode= get(this.root,prefix,0);
        if(prefixNode==null){
            return Collections.emptyList();
        } else{
            set.addAll(prefixNode.values);
                getAllWithPrefixSorted(prefixNode,set);
                List<Value> list=new ArrayList<>(set);
                Collections.sort(list,comparator);
            return list;
        }   }

    private  Set<Value> getAllWithPrefixSorted(Node x,Set set) {
        for(int k=0;k<62;k++){
                if(x.links[k]!=null){
                    set.addAll((x.links[k].values));
                    getAllWithPrefixSorted(x.links[k],set);
            } }
        return set;


    }
    @Override
    public Set deleteAllWithPrefix(String prefix) {
                Node x=this.root;
            for(int d=0;d<prefix.length();d++){
                int c= charToInt(prefix.charAt(d));
                x=x.links[c];
            }
            Set<Value> set=new HashSet<>();
            if(x.values.size()!=0)
               set.addAll(x.values);
            set.addAll(deleteAllWithPrefix(x));
            this.root=delete(this.root,prefix,0);
                 return set;
        }

    private  Set<Value> deleteAllWithPrefix(Node x) {
        Set set=new HashSet();
        for(int k=0;k<62;k++){
            if(x.links[k]!=null){
                set.addAll((x.links[k].values));
                x.links[k].values=Collections.emptyList();
                getAllWithPrefixSorted(x.links[k],set);
            } }
        return set;


    }

    @Override
    public Set deleteAll(String key) {
        if(key==null){
            throw new IllegalArgumentException();
        }
       this.root=delete(this.root, key, 0);
       return this.setToBeReturned;
    }


    @Override
    public Value delete(String key, Value val) {
        if(key==null)
            throw new IllegalArgumentException();
            this.root=delete1Value(this.root,key,0,val);
            return  val;
    }

    private Node put(Node x, String key, Value val, int d) {
//create a new node
        if (x == null) {
            x = new Node();
        }
//we've reached the last node in the key,
//so set the value for the key and return the node
        if (d == key.length()) {
            List<Value> arraylist = new ArrayList<>(x.values);
            arraylist.add(val);
            x.values=arraylist;
            return x;
        }
//else, proceed to the next node in the chain of nodes that
//forms the desired key

        int c =  charToInt(key.charAt(d));
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    private Node get(Node x, String key, int d)
    {
        //link was null - return null, indicating a miss
        if (x == null)
        {
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (d == key.length())
        {
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        int c =  charToInt(key.charAt(d));
        return this.get(x.links[c], key, d + 1);
    }

    private Node delete(Node x, String key, int d){
        if (x == null) {
            return null;
        }
//we're at the node to delete - set the val to null
        if (d == key.length()){
            this.setToBeReturned=Set.copyOf(x.values);
            x.values=new ArrayList<Value>(0);
        }
//continue down the trie to the target node
        else {
            int  c =charToInt(key.charAt(d));
            x.links[c] = this.delete(x.links[c], key, d + 1);
        }
        if(d==0)
            return x;
        //this node has a val – do nothing, return the node
        if (!x.values.isEmpty()||x.values==null){
            return x;
        }
//otherwise, check if subtrie rooted at x is completely empty
        for (int c = 0; c <x.links.length; c++){
            if (x.links[c] != null){
                return x; //not empty
            }
        }
//empty - set this link to null in the parent
        return null;
    }



    private Node delete1Value(Node x, String key, int d,Value val) {
        if (x == null) {
            return null;
        }
//we're at the node to delete - set the val to null
        boolean toDeleteLeafs = false;
        if (d == key.length()) {
            x.values.remove(val);
            if (x.values.isEmpty()) {
                toDeleteLeafs = true;
            }
        }
//continue down the trie to the target node
        else {
            int c = charToInt(key.charAt(d));
            x.links[c] = this.delete1Value(x.links[c], key, d + 1, val);
        }
        if (toDeleteLeafs) {
            if (d == 0)
                return x;
            //this node has a val – do nothing, return the node
            if (!x.values.isEmpty()) {
                return x;
            }
//otherwise, check if subtrie rooted at x is completely empty
            for (int c = 0; c < x.links.length; c++) {
                if (x.links[c] != null) {
                    return x; //not empty
                }
            }
//empty - set this link to null in the parent
            return null;
        }
        return x;
    }

    private int charToInt(char character) throws IllegalArgumentException{
        int c= character;
        if (c>47 &&c<58){ c= c-48;}
        else if(c>64&&c<91){c=c-55;}
        else if (c>96&&c<123) {c=c-61;}
        else{throw new IllegalArgumentException();}
        return c;
    }
    private boolean contains(Node x, String key){
        if(get(x,key,0)==null){
            return false;
        } else{return true;}
    }

}


