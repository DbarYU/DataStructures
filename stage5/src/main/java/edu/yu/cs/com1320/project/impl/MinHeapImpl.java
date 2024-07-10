package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    public MinHeapImpl() {
        this.elements= (E[]) new Comparable[5];

    }

    @Override
    public void reHeapify(E element) {
        int index= getArrayIndex(element);
        if(index==-1)
            return;

        //if the element is the root node, then it can only downheap,
        //if it is greater than one of its two children.
        if(index==1&&this.count>=3){
            if(isGreater(index,2)||isGreater(index,3)){
                downHeap(index);
                return;
            }
            else{ return;}
            //if there are less than 3 elements in the heap, meaning there is only a parent and a
            // child, swap if child is smaller than root.
        } else if(index==1&&this.count==2){
            if(isGreater(index,2)){
                this.swap(index,2);
                return;
        } return; }else if(index==1&&this.count==1) {return;} else{


        boolean isLeafNode = Math.floor(this.count/2) < index ? true: false;
        //if element is leafnode, can only upheap. Need to compare with parent who is smaller
        //if parent is bigger, upheap, if parent is smaller return.
            if(isLeafNode){
                if(isGreater(index/2,index)){
                    upHeap(index);
                return;}
                //if element is not a leafnode, then can either upheap if parent is bigger,
                // or downheap if one if children is smaller.
            }else {
                if (isGreater(index / 2, index)) {
                    upHeap(index);
                    return;
                }
                //if parent node only has left leaf, compare with
                else if ((index * 2) + 1 > this.count) {
                    if (isGreater(index, 2 * index)) {
                        downHeap(index);
                        return;
                    }
                } else if (isGreater(index, index * 2) || isGreater(index, 2 * index + 1)) {
                    downHeap(index);
                    return;
                }
            }

        }


    }

    @Override
    protected int getArrayIndex(E element) {
        return elementSearch(element);
    }

    @Override
    protected void doubleArraySize() {
        E[] tempArray=(E[]) new Comparable[this.count*2];
        for(int i=1;i<=this.count;i++){
            tempArray[i]=this.elements[i];
        }
        this.elements=tempArray;
    }
    private int elementSearch(E element){

         for(int i=1;i<=this.count;i++){
             if(this.elements[i]==null){
                 return -1;}
            if(element.equals(this.elements[i])){
              return i;}
          }
         return -1;
    }

}
