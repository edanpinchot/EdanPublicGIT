package edu.yu.cs.com1320.project.Impl;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class DocumentStoreImpl implements DocumentStore {
    private CompressionFormat defaultCompressionFormat;
    private HashTableImpl<URI, DocumentImpl> store;
    private StackImpl<Command> commandStack;
    private TrieImpl<DocumentImpl> wordTrie;
    private MinHeapImpl<DocumentImpl> daHeap;
    private int bytes;
    private int maxDocumentCount;
    private int maxDocumentBytes;

    public DocumentStoreImpl() {
        this.store = new HashTableImpl(2);
        this.commandStack = new StackImpl<>();
        this.wordTrie = new TrieImpl<>();
        this.daHeap = new MinHeapImpl<>(new DocumentImpl[2]);
        this.bytes = 0;
        this.maxDocumentCount = Integer.MAX_VALUE;
        this.maxDocumentBytes = Integer.MAX_VALUE;
    }

    public void setMaxDocumentCount(int limit) {
        if (limit == 0) {
            return;
        }
        while (store.getSize() > limit) {                      //if set exceeds max, delete document completely
            maxedOutDelete(daHeap.removeMin().getKey());
        }
        this.maxDocumentCount = limit;
    }

    public void setMaxDocumentBytes(int limit) {
        while (bytes > limit) {                      //if set exceeds max, delete document completely
            maxedOutDelete(daHeap.removeMin().getKey());
        }
        this.maxDocumentBytes = limit;
    }

    public List<String> search(String keyword) {
        List<DocumentImpl> docsWithWord;
        String string = null;
        //try to "getAllSorted" of the documents in which this word appears
        try {
            docsWithWord = wordTrie.getAllSorted(keyword);
        }
        //if that throws an error, it means that the word is not in the trie, or that it has no value, so return an empty linkedlist
        catch (Exception e) {
            return new LinkedList<>();
        }
        List<String> docsToString = new LinkedList<>();

        for (DocumentImpl doc : docsWithWord) {
            //update each doc's lastUseTime
            doc.setLastUseTime(System.currentTimeMillis());
            daHeap.reHeapify(doc);
            //decompress each of the documents to get the uncompressed Strings that we want to return hwew
            string = decompressionSwitch(doc.getCompressionFormat(), doc.getDocument());
            //add each uncompressed doc's string to the List that we'll return
            docsToString.add(string);
        }
        return docsToString;
    }

    public List<byte[]> searchCompressed(String keyword) {
        List<DocumentImpl> docsWithWord;
        try {
            docsWithWord = wordTrie.getAllSorted(keyword);
        }
        catch (Exception e) {
            return new LinkedList<>();
        }
        List<byte[]> docsToString = new LinkedList<>();
        for (DocumentImpl doc : docsWithWord) {
            //update doc's lastUseTime
            doc.setLastUseTime(System.currentTimeMillis());
            daHeap.reHeapify(doc);
            docsToString.add(doc.getDocument());
        }
        return docsToString;
    }

    public void setDefaultCompressionFormat(CompressionFormat format) {
        defaultCompressionFormat = format;
    }

    public CompressionFormat getDefaultCompressionFormat() {
        return CompressionFormat.ZIP;
    }


    protected byte[] zipCompress(String str) {
        byte[] compressed = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(out);
            ZipArchiveEntry zip = new ZipArchiveEntry("testdata/test1.xml");
            zip.setSize(str.length());
            zipOut.putArchiveEntry(zip);
            zipOut.write(in.readAllBytes());

            zipOut.closeArchiveEntry();
            zipOut.finish();
            zipOut.close();
            out.close();
            compressed = out.toByteArray();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return compressed;
    }

    protected String zipDecompress(byte[] compressedFile) {
        String decompressedDoc = null;
        try {
            ByteArrayInputStream in1  = new ByteArrayInputStream(compressedFile);
            BufferedInputStream in = new BufferedInputStream(in1);
            ZipArchiveEntry ent = new ZipArchiveEntry("a");
            ZipArchiveInputStream zipIn = new ZipArchiveInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while((ent = zipIn.getNextZipEntry()) != null) {
                out.write(zipIn.readAllBytes());
            }
            decompressedDoc = out.toString();
            zipIn.close();
            in.close();
            out.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return decompressedDoc;
    }

    protected byte[] jarCompress(String str) {
        byte[] compressed = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JarArchiveOutputStream jarOut = new JarArchiveOutputStream(out);
            JarArchiveEntry jar = new JarArchiveEntry("testdata/test1.xml");
            jar.setSize(str.length());
            jarOut.putArchiveEntry(jar);
            jarOut.write(in.readAllBytes());

            jarOut.closeArchiveEntry();
            jarOut.finish();
            jarOut.close();
            out.close();
            compressed = out.toByteArray();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return compressed;
    }

    protected String jarDecompress(byte[] compressedFile) {
        String decompressedDoc = null;
        try {
            ByteArrayInputStream in1  = new ByteArrayInputStream(compressedFile);
            BufferedInputStream in = new BufferedInputStream(in1);
            JarArchiveEntry ent = new JarArchiveEntry("a");
            JarArchiveInputStream jarIn = new JarArchiveInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while((ent = jarIn.getNextJarEntry()) != null){
                out.write(jarIn.readAllBytes());
            }
            decompressedDoc = out.toString();
            jarIn.close();
            in.close();
            out.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return decompressedDoc;
    }

    protected byte[] sevenzCompress(String str) {
        byte[] bytes = str.getBytes();
        SeekableInMemoryByteChannel compressed = new SeekableInMemoryByteChannel(bytes.length);
        try {
            File file = new File("a");
            FileUtils.writeByteArrayToFile(file, bytes);

            SevenZOutputFile out = new SevenZOutputFile(compressed);
            SevenZArchiveEntry ent = out.createArchiveEntry(file, "b");
            out.putArchiveEntry(ent);
            FileInputStream in = new FileInputStream(file);
            byte[] b = new byte[1024];
            int count = 0;
            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
            in.close();
            out.closeArchiveEntry();
            out.close();
            file.delete();
        }

        catch (Exception e){
            e.printStackTrace();
        }

        return compressed.array();
    }

    protected String sevenzDecompress(byte[] compressedFile) {
        byte[] decompressedDoc = null;
        try {
            SeekableInMemoryByteChannel inMemoryByteChannel = new SeekableInMemoryByteChannel(compressedFile);
            SevenZFile file = new SevenZFile(inMemoryByteChannel);
            SevenZArchiveEntry ent = file.getNextEntry();
            int size = (int) ent.getSize();
            decompressedDoc = new byte[size];
            file.read(decompressedDoc, 0, decompressedDoc.length);
            file.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return new String(decompressedDoc);
    }

    protected byte[] gzipCompress(String str) {
        byte[] compressed = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out);
            gzOut.write(str.getBytes());
            gzOut.close();
            compressed = out.toByteArray();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return compressed;
    }

    protected String gzipDecompress(byte[] compressedFile) {
        String decompressedDoc = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(compressedFile);
            GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int n = 0;
            while (-1 != (n = gzIn.read())) {
                out.write(n);
            }
            decompressedDoc = out.toString();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return decompressedDoc;
    }

    protected byte[] bzip2Compress(String str) {
        byte[] compressed = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out);
            bzOut.write(str.getBytes());
            bzOut.close();
            compressed = out.toByteArray();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return compressed;
    }

    protected String bzip2Decompress(byte[] compressedFile) {
        String decompressedDoc = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(compressedFile);
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int n = 0;
            while (-1 != (n = bzIn.read())) {
                out.write(n);
            }
            decompressedDoc = out.toString();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return decompressedDoc;
    }


    public int putDocument(InputStream input, URI uri) {
        //if default format wasn't set, then use other 'put' with ZIP compression format
        if (defaultCompressionFormat == null) {
            return putDocument(input, uri, CompressionFormat.ZIP);
        }
        else {
            String docString = null;
            DocumentImpl previousDoc = store.get(uri);
            long previousUseTime = 0;                                                                                   //keep track of previous doc's use time in case of undo
            try { docString = IOUtils.toString(input); } catch (IOException e) { }
            String altDocString = docString;
            HashMap<String, Integer> docWords = wordsToHashMap(docString);                                              //at this point, scan through 'docString' and add words to the doc's hashtable
            byte[] compressedFile = compressionSwitch(defaultCompressionFormat, docString);                             //compression

            DocumentImpl doc = new DocumentImpl(compressedFile, docString.hashCode(), uri, defaultCompressionFormat, docWords);

            while ((store.getSize()+1 > maxDocumentCount) || (bytes+compressedFile.length > maxDocumentBytes)) {       //if we've exceeded max, delete document completely
                maxedOutDelete(daHeap.removeMin().getKey());
            }
            if (previousDoc != null) {                                                                                  //if this is replacing a doc, then we have to remove that previous doc's words from trie, bytes, and remove from heap
                previousUseTime = previousDoc.getLastUseTime();
                allTheDeletion(previousDoc.getDocument().length, previousDoc, previousDoc.getKey());
            }
            int bytesLength = compressedFile.length;
            long previousUseTime2 = previousUseTime;
            long useTime = System.currentTimeMillis();
            allThePuts(bytesLength, doc, useTime, uri, altDocString, wordTrie);

            Function<URI, Boolean> undo = undoLambda(previousDoc, doc, bytesLength, uri, previousUseTime2);
            Function<URI, Boolean> redo = redoLambda(previousDoc, doc, bytesLength, uri, altDocString);

            Command command = new Command(uri, undo, redo);
            commandStack.push(command);
            return docString.hashCode();
        }
    }

    public int putDocument(InputStream input, URI uri, CompressionFormat format) {
        String docString = null;
        DocumentImpl previousDoc = store.get(uri);
        long previousUseTime = 0;                                                                                       //save previous doc's use time, so that when undoing and putting back the previousdoc, we can set back its usetime
        try { docString = IOUtils.toString(input); } catch (IOException e) { }                                          //convert input stream into a string
        String altDocString = docString;
        HashMap<String, Integer> docWords = wordsToHashMap(docString);                                                  //counts words in string, creates a hashmap with each word and its frequency
        byte[] compressedFile = compressionSwitch(format, docString);                                                   //compress the string

        DocumentImpl doc = new DocumentImpl(compressedFile, docString.hashCode(), uri, format, docWords);

        while ((store.getSize()+1 > maxDocumentCount) || (bytes+compressedFile.length > maxDocumentBytes)) {           //if we've exceeded max, delete document completely
            maxedOutDelete(daHeap.removeMin().getKey());
        }
        if (previousDoc != null) {                                                                                      //if this is replacing a doc, then we have to remove that previous doc's words from trie, bytes, and remove from heap
            previousUseTime = previousDoc.getLastUseTime();
            allTheDeletion(previousDoc.getDocument().length, previousDoc, previousDoc.getKey());
        }
        int bytesLength = compressedFile.length;
        long previousUseTime2 = previousUseTime;
        long useTime = System.currentTimeMillis();
        allThePuts(bytesLength, doc, useTime, uri, altDocString, wordTrie);

        Function<URI, Boolean> undo = undoLambda(previousDoc, doc, bytesLength, uri, previousUseTime2);
        Function<URI, Boolean> redo = redoLambda(previousDoc, doc, bytesLength, uri, altDocString);

        Command command = new Command(uri, undo, redo);
        commandStack.push(command);

        return doc.hashCode();
    }

    public String getDocument(URI uri) {
        String decompressedDoc = null;
        if (store.get(uri) == null) {
            return null;
        }

        else {
            //decompress doc
            decompressedDoc = decompressionSwitch(store.get(uri).getCompressionFormat(), store.get(uri).getDocument());
            //update doc's lastUseTime and reheapify
            store.get(uri).setLastUseTime(System.currentTimeMillis());
            daHeap.reHeapify(store.get(uri));
            return decompressedDoc;
        }
    }

    public byte[] getCompressedDocument(URI uri) {
        //update doc's lastUseTime and reheapify
        store.get(uri).setLastUseTime(System.currentTimeMillis());
        daHeap.reHeapify(store.get(uri));
        return store.get(uri).getDocument();
    }

    public boolean deleteDocument(URI uri) {
        if (store.get(uri) == null) {
            return false;
        }
        else {
            DocumentImpl docHolder = store.get(uri);
            long putTime = docHolder.getLastUseTime();                                                                  //save time from the "put" so that we can reinstate it in case of 'undo'
            allTheDeletion(docHolder.getDocument().length, docHolder, uri);
            store.remove(uri);                                                                                          //remove doc from store

            Function<URI,Boolean> undo = (URI uri1) -> {
                while ((store.getSize()+1 > maxDocumentCount) || (bytes+docHolder.getDocument().length > maxDocumentBytes)) {          //if we've exceeded max, delete document completely
                    maxedOutDelete(daHeap.removeMin().getKey());
                }
                bytes += docHolder.getDocument().length;
                docHolder.setLastUseTime(putTime);
                daHeap.insert(docHolder);
                daHeap.reHeapify(docHolder);
                store.put(uri,docHolder);                                                                               //first put the document back in the store
                for (String string : docHolder.getDocWords().keySet()) {                                                //then put the documents words back into the trie
                    wordTrie.put(string, store.get(uri));
                }
                return (store.put(uri,docHolder) != null);                                                              //i think
            };
            Function<URI,Boolean> redo = (URI uri2) -> {
                allTheDeletion(docHolder.getDocument().length, docHolder, uri);
                return store.remove(uri);                                                                               //re-remove doc from store
            };

            Command command = new Command(uri, undo, redo);
            commandStack.push(command);
            return true;
        }
    }

    public boolean undo() throws IllegalStateException {
        if (commandStack.peek() == null) {
            return false;
        }
        return commandStack.pop().undo();
    }

    public boolean undo(URI uri) throws IllegalStateException {
        StackImpl<Command> commandHolder = new StackImpl<Command>();
        if (store.get(uri) == null) {
            return false;
        }
        //if stack gets to size zero, it will return false
        while (commandStack.size() != 0) {
                //if we reach the command we want, undo it, then push any commands that had been popped off back onto the stack
                if (commandStack.peek().getUri() == uri) {
//                    //update doc's lastUseTime, then reheapify with its new time
                    commandStack.peek().undo();
                    while (commandHolder.size() != 0) {
                        commandStack.push(commandHolder.pop());
                        commandStack.peek().redo();
                    }
                    return true;
                }
                //if this is not the command we want, pop it off but save it in array so we can push it back on after we undo the specified command
                else {
                    commandStack.peek().undo();
                    commandHolder.push(commandStack.pop());
                }
        }

        return false;
    }


    private HashMap<String, Integer> wordsToHashMap(String docString) {
        docString = docString.toLowerCase();                                //makes the whole string lowercase before breaking it down, so that the search is case sensitive
        HashMap<String, Integer> docWords = new HashMap<>();
        int frequency;

        //first break down given string into an array of each word in it
        String[] words = docString.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^\\w]", "");
        }
        //next look at each word in the string, count how many times it appears, then add it and its frequency to the map
        for (int k = 0; k < words.length; k++) {
            frequency = 1;
            //only count it if it is not already in the map, aka if it wasn't already counted
            while (!docWords.containsKey(words[k])) {
                for (int j = (k + 1); j < words.length; j++) {
                    if (words[k].equals(words[j])) {
                        frequency++;
                    }
                }
                docWords.put(words[k], frequency);
                //I think at this point, also 'put' the (word, doc) into the TrieImpl
//                wordTrie.put(words[k], );
            }
        }
        return docWords;
    }

    private void wordsToTrie(String docString, DocumentImpl doc, TrieImpl<DocumentImpl> wordTrie) {
        docString = docString.toLowerCase();

        //first break down given string into an array of each word in it
        String[] words = docString.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^\\w]", "");
        }
        for (int k = 0; k < words.length; k++) {
            wordTrie.put(words[k], doc);
        }
    }

    private void wordsOutOfTrie(URI uri) {
        //what document object does this uri point to
        DocumentImpl doc = store.get(uri);
        //iterate through the document's words, deleting each one from the trie
        for (String string : doc.getDocWords().keySet()) {
            wordTrie.delete(string, doc);
        }
    }

    private void allThePuts(int bytesLength, DocumentImpl doc, long useTime, URI uri, String altDocString, TrieImpl wordTrie) {
        bytes += bytesLength;                                                               //add this doc's bytes to total store bytes
        doc.setLastUseTime(useTime);
        daHeap.insert(doc);
        daHeap.reHeapify(doc);
        store.put(uri, doc);                                                                //put in hashtable
        wordsToTrie(altDocString, doc, wordTrie);
    }

    private void allTheDeletion(int bytesLength, DocumentImpl doc, URI uri) {
        bytes -= bytesLength;
        heapDelete(doc);
        wordsOutOfTrie(uri);
    }

    private boolean addBackDoc(int bytesLength, DocumentImpl doc, long useTime, URI uri) {
        bytes += bytesLength;
        doc.setLastUseTime(useTime);
        daHeap.insert(doc);
        daHeap.reHeapify(doc);
        for (String string : doc.getDocWords().keySet()) {
            wordTrie.put(string, doc);
        }
        return (store.put(uri, doc) != null);
    }

    private Function<URI, Boolean> undoLambda(DocumentImpl previousDoc, DocumentImpl doc, int bytesLength, URI uri, long useTime) {
        Function<URI, Boolean> undo = (URI uri1) -> {
            //if this "put" just put a new value to an already existing key, then put previous doc and its words back into store and trie
            if (previousDoc != null) {
                allTheDeletion(bytesLength, doc, uri);                                                                  //delete everything from new doc
                return addBackDoc(previousDoc.getDocument().length, previousDoc, useTime, uri);
            }                                                                                                           //add back previous doc
            //otherwise it put a brand new doc in, so undo removes the doc and its words frmo the trie
            else {
                allTheDeletion(bytesLength, doc, uri);
                return store.remove(uri);
            }
        };
        return undo;
    }

    private Function<URI, Boolean> redoLambda(DocumentImpl previousDoc, DocumentImpl doc, int bytesLength, URI uri, String altDocString) {
        Function<URI, Boolean> redo = (URI uri2) -> {
            if (previousDoc != null) {
                allTheDeletion(previousDoc.getDocument().length, previousDoc, previousDoc.getKey());
            }
            allThePuts(bytesLength, doc, System.currentTimeMillis(), uri, altDocString, wordTrie);
            return true;
        };
        return redo;
    }

    private void heapDelete(DocumentImpl doc) {
        doc.setLastUseTime(0);
        daHeap.reHeapify(doc);
        daHeap.removeMin();
    }

    private void maxedOutDelete(URI uri) {
        DocumentImpl doc = store.get(uri);
        bytes -= doc.getDocument().length;
        wordsOutOfTrie(uri);
        store.remove(uri);

        //look throguh commands in stack, popping off any one who's uri matches this uri...aka completely remove command's related to this doc
        StackImpl<Command> commandHolder = new StackImpl<Command>();
        while (commandStack.size() != 0) {
            commandHolder.push(commandStack.pop());
        }
        while (commandHolder.size() != 0) {
            if (commandHolder.peek().getUri() == uri) {
                commandHolder.pop();
            }
            else {
                commandStack.push(commandHolder.pop());
            }
        }
    }

    private byte[] compressionSwitch(CompressionFormat format, String docString) {
        byte[] compressedFile = null;
        switch (format) {
            case ZIP:
                compressedFile = zipCompress(docString);
                break;
            case JAR:
                compressedFile = jarCompress(docString);
                break;
            case SEVENZ:
                compressedFile = sevenzCompress(docString);
                break;
            case GZIP:
                compressedFile = gzipCompress(docString);
                break;
            case BZIP2:
                compressedFile = bzip2Compress(docString);
                break;
        }
        return compressedFile;
    }

    private String decompressionSwitch(CompressionFormat format, byte[] compressedDoc) {
        String decompressedDoc = null;
        switch (format) {
            case ZIP:
                decompressedDoc = zipDecompress(compressedDoc);
                break;
            case JAR:
                decompressedDoc = jarDecompress(compressedDoc);
                break;
            case SEVENZ:
                decompressedDoc = sevenzDecompress(compressedDoc);
                break;
            case GZIP:
                decompressedDoc = gzipDecompress(compressedDoc);
                break;
            case BZIP2:
                decompressedDoc = bzip2Decompress(compressedDoc);
                break;
        }
        return decompressedDoc;
    }
}

//        Function<URI, Boolean> undo = (URI uri1) -> {
//            //if this "put" just put a new value to an already existing key, then put previous doc and its words back into store and trie
//            if (previousDoc != null) {
//                allTheDeletion(bytesLength, doc, uri);                                                                  //delete everything from new doc
//                return addBackDoc(previousDoc.getDocument().length, previousDoc, previousUseTime2, uri);
//            }              //add back previous doc
//            //otherwise it put a brand new doc in, so undo removes the doc and its words frmo the trie
//            else {
//                allTheDeletion(bytesLength,doc,uri);
//                return store.remove(uri);
//            }
//        };

//        Function<URI, Boolean> redo = (URI uri2) -> {
//            if (previousDoc != null) {
//                allTheDeletion(previousDoc.getDocument().length, previousDoc, previousDoc.getKey()); }
//            allThePuts(bytesLength, doc, useTime, uri2, altDocString, wordTrie);
//            return true; };