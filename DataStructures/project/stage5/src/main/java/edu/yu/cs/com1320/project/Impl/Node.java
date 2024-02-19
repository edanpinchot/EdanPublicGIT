package edu.yu.cs.com1320.project.Impl;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;

class Node<Key, Value> {
    Key k;
    Value v;
    Node next;

    public Node(Key k, Value v) {
        this.k = k;
        this.v = v;
        this.next = null;
    }

    public void setValue(Value v) {
        this.v = v;
    }

    private Value getValue() {
        return v;
    }

    public static void main(String[] args) {
        try {
            DocumentStoreImpl dsi = new DocumentStoreImpl();

            //create and add doc1
            String str1 = "this is doc#1";
            URI uri1 = new URI("http://www.yu.edu/doc1");
            ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
            dsi.putDocument(bis, uri1);
            Thread.sleep(50);

            //create and add doc2
            String str2 = "this is doc#2";
            URI uri2 = new URI("http://www.yu.edu/doc2");
            bis = new ByteArrayInputStream(str2.getBytes());
            dsi.putDocument(bis, uri2);
            Thread.sleep(50);

            //create and add doc3
            String str3 = "this is doc#3";
            URI uri3 = new URI("http://www.yu.edu/doc3");
            bis = new ByteArrayInputStream(str3.getBytes());
            dsi.putDocument(bis, uri3);
            Thread.sleep(50);

            //create and add doc4
            String str4 = "this is doc#4";
            URI uri4 = new URI("http://www.yu.edu/doc4");
            bis = new ByteArrayInputStream(str4.getBytes());
            dsi.putDocument(bis, uri4);

//            dsi.undo(uri2);
//            dsi.undo(uri1);

            List<String> wheresthisword = dsi.search("this");
            for (String string : wheresthisword) {
                System.out.println(string);
            }

            System.out.println("hi");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
