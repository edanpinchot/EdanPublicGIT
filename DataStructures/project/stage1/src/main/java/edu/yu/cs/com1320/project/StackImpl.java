package edu.yu.cs.com1320.project;

public class StackImpl<T> implements Stack<T> {
    private int top;
    private T[] theStack;

    public StackImpl() {
        this.top = -1;
        this.theStack = (T[]) new Object[2];
    }

    public void push(T element) {
        if ((top) == (theStack.length - 1)) {
            T[] doubledStack = (T[]) new Object[2*theStack.length];
            for (int n = 0; n < theStack.length; n++) {
                doubledStack[n] = theStack[n];
            }
            this.theStack = doubledStack;
        }

        top++;
        theStack[top] = element;
    }

    public T pop() {
        if (top == -1) {
            return null;
        }

        T element = theStack[top];
        theStack[top] = null;
        top--;
        return element;
    }

    public T peek() {
        if (top == -1) {
            return null;
        }

        return theStack[top];
    }

    public int size() {
        return (top + 1);
    }

    protected void displayEntireStack() {
        if (top == -1) {
            System.exit(0);
        }
        System.out.println("Displaying the Stack: ");
        for (int k = 0; k < theStack.length; k++) {
            System.out.println(theStack[k] + " ");
        }
    }
}
