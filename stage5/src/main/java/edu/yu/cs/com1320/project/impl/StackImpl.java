package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;

/**
 * @param <T>
 */
public class StackImpl < T >  implements Stack < T >
{
 private T[] stack;
 private int currentIndex;

 public StackImpl(){
    this.stack = (T[]) new Object[5];
    this.currentIndex=0;
 }
    /**
     * @param element object to add to the Stack
     */
    public void push(T element){
        if(element==null)
            return;
        //if we have reached the end of the stack array, resize.
        if(currentIndex==this.stack.length-1){
            this.stack = resize(this.stack);
        }

        this.stack[currentIndex]=element;
        currentIndex++;
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    public T pop(){
        if(currentIndex==0){
            return null;}
        else{
            T elementToBeReturned=this.stack[this.currentIndex-1];
            this.stack[currentIndex-1]=null;
            this.currentIndex=this.currentIndex-1;
            return elementToBeReturned;
        }
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
  public T peek(){
        if(this.currentIndex==0){
            return null;}
          
      return this.stack[currentIndex-1];
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
   public int size(){
        return currentIndex;
    }

      private T[] resize(T[] t) {
    T[] newArray = (T[]) new Object[t.length * 2];
    for (int i = 0; i < t.length; i++) {
      newArray[i] = t[i];
    }
    return newArray;
  }

}

