package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.File;
import java.io.IOException;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
    private static final int MAX = 6;
    private int height;
    private BTreeImpl.Node root;
    private int numberOfEntries;
    private PersistenceManager documentPersistenceManager;
    private boolean isWrittenToDisk = false;

    private static class Node {
        private int entryCount; //number of non-null entries
        private Entry[] entries = new Entry[BTreeImpl.MAX];//child links

        public Node(int k) {
            this.entryCount = k;
        }
    }

    private static class Entry {
        private Comparable key;
        private Object val;
        private Node child;

        public Entry(Comparable key, Object val, BTreeImpl.Node child) {
            this.key = key;
            this.val = val;
            this.child = child;
        }

    }

    public BTreeImpl() {
        this.root = new Node(0);
        this.height = 0;
        this.numberOfEntries = 0;
    }

    @Override
    public Value get(Key k) {
        {
            if (k == null) {
                throw new IllegalArgumentException("argument to get() is null");
            }
            Entry entry = this.get(this.root, k, this.height);
            if (entry == null) {
                return null;
            } else if (entry.val != null) {
                Value val = null;
                if (entry.val instanceof File) {
                    try {
                        val = (Value) documentPersistenceManager.deserialize(entry.key);
                        entry.val = val;
                        documentPersistenceManager.delete(entry.key);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    val = (Value) entry.val;
                }
                return val;
            }
            return null;
        }
    }


    private Entry get(Node node, Key key, int height) {
        Entry[] entries = node.entries;
        //current node is external (i.e. height == 0)
        if (height == 0) {
            for (int j = 0; j < node.entryCount; j++) {
                if (isEqual(key, entries[j].key)) {
                    //found desired key. Return its value
                    return entries[j];
                }
            }
            //didn't find the key
            return null;
        }

        //current node is internal (height > 0)
        else {
            for (int j = 0; j < node.entryCount; j++) {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be in the subtree below the current entry),
                //then recurse into the current entry’s child
                if (j + 1 == node.entryCount || less(key, entries[j + 1].key)) {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            //didn't find the key
            return null;
        }
    }

    @Override
    public Value put(Key k, Value v) {
        //if the key already exists in the b-tree, simply replace the value
        Entry alreadyThere = this.get(this.root, k, this.height);
        if (alreadyThere != null) {
            Value val;
            //if the Value of the leaf Entry  is null, that means the document has been serialized
            if(alreadyThere.val instanceof File){
                try {
                    val = (Value)documentPersistenceManager.deserialize(alreadyThere.key);
                    documentPersistenceManager.delete(alreadyThere.key);
                    alreadyThere.val = v;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else  {
                val = (Value) alreadyThere.val;
                alreadyThere.val = v;
            }
            return val;
        }
        Node newNode = this.put(this.root, k, v, this.height);
        this.numberOfEntries++;
        if (newNode == null) {//no split of root, we’re done
            return null;
        }
//private put method only returns non-null if root.entryCount == Btree.MAX
//(see if-else on previous slide.) Private code will have copied the upper M/2
//entries over. We now set the old root to be new root's first entry, and
//set the node returned from private method to be new root's second entry
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
//a split at the root always increases the tree height by 1
        this.height++;
        return null;
    }

    private Node put(Node currentNode, Key key, Value val, int height) {
        int j;
        Entry newEntry = new Entry(key, val, null);
        if (height == 0) {
            for (j = 0; j < currentNode.entryCount; j++) {
                if (less(key, currentNode.entries[j].key)) {
                    break;
                }
            }
        } else {
            for (j = 0; j < currentNode.entryCount; j++) {
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key)) {
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null) {
                        return null;
                    }
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        for (int i = currentNode.entryCount; i > j; i--) {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < this.MAX) {
            return null;
        } else {
            return this.split(currentNode);
        }
    }

    @Override
    public void moveToDisk(Key k) throws Exception {
        Entry entry = get(this.root,k,this.height);
        if(entry.val instanceof File ||entry.val == null){
            return;
        }
        this.documentPersistenceManager.serialize(entry.key,entry.val);
        entry.val = new File(k.toString());
    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {
        this.documentPersistenceManager= pm;
    }

    private boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean isEqual(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }

    private Node split(Node node) {
        Node newNode = new Node(this.MAX / 2);
//copy top half of currentNode into newNode
        for (int j = 0; j < this.MAX / 2; j++) {
            newNode.entries[j] = node.entries[this.MAX / 2 + j];
//set references in top half of currentNode to null to avoid memory leaks
            node.entries[this.MAX / 2 + j] = null;
        }
//divide currentNode.entryCount by 2
        node.entryCount = this.MAX / 2;
        return newNode;
    }
}
