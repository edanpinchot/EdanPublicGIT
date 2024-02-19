package edu.yu.cs.com1320.project.Impl;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
    //private HashTableImpl<URI, DocumentImpl> store;
    private BTreeImpl<URI, DocumentImpl> daTree;
    private StackImpl<Command> commandStack;
    private TrieImpl<URI> wordTrie;
    private MinHeapImpl<URI> daHeap;
    private int bytes;
    private int maxDocumentCount;
    private int maxDocumentBytes;
    private DocumentIOImpl docIO;
    private List<URI> stuffOnDisk;

    public DocumentStoreImpl() {
        //this.store = new HashTableImpl(2);
        this.daTree = new BTreeImpl<>(new File(System.getProperty("user.dir")));                          /////////////
        this.commandStack = new StackImpl<>();
        this.wordTrie = new TrieImpl<>();
        this.daHeap = new MinHeapImpl<>(new URI[2]);
        this.bytes = 0;
        this.maxDocumentCount = Integer.MAX_VALUE;
        this.maxDocumentBytes = Integer.MAX_VALUE;
        this.docIO = new DocumentIOImpl();
        this.stuffOnDisk = new LinkedList<>();
    }

    //constructor that takes File baseDir and passes it to the constructor of DocumentIO
    public DocumentStoreImpl(File baseDir) {
        this.daTree = new BTreeImpl<>(baseDir);
        this.commandStack = new StackImpl<>();
        this.wordTrie = new TrieImpl<>();
        this.daHeap = new MinHeapImpl<>(new URI[2]);
        this.bytes = 0;
        this.maxDocumentCount = Integer.MAX_VALUE;
        this.maxDocumentBytes = Integer.MAX_VALUE;
        this.docIO = new DocumentIOImpl();
        this.stuffOnDisk = new LinkedList<>();
    }

    public void setMaxDocumentCount(int limit) {
        if (limit == 0) {
            return;
        }
        while (daTree.size() > limit) {             //if set exceeds max, remove doc from Heap, and move it to disk in tree
            URI uri = daHeap.removeMin();
            maxedOutDelete(uri);
            wordTrie.docOutOfTrie(uri);
            daHeap.docOutOfHeap(uri);
        }
        this.maxDocumentCount = limit;
    }

    public void setMaxDocumentBytes(int limit) {
        while (bytes > limit) {                      //if set exceeds max, remove doc from Heap, and move it to disk in tree
            URI uri = daHeap.removeMin();
            maxedOutDelete(uri);
            wordTrie.docOutOfTrie(uri);
            daHeap.docOutOfHeap(uri);
        }
        this.maxDocumentBytes = limit;
    }

    public List<String> search(String keyword) {
        List<URI> docsWithWord;
        String string;
        //try to "getAllSorted" of the documents in which this word appears
        try {
            docsWithWord = wordTrie.getAllSorted(keyword);
        }
        //if that throws an error, it means that the word is not in the trie, or that it has no value, so return an empty linkedlist
        catch (Exception e) {
            return new LinkedList<>();
        }
        List<String> docsToString = new LinkedList<>();

        for (URI uri : docsWithWord) {
            //update each doc's lastUseTime
            DocumentImpl doc = daTree.get(uri);
            doc.setLastUseTime(System.currentTimeMillis());
            daHeap.reHeapify(uri);
            //decompress each of the documents to get the uncompressed Strings that we want to return hwew
            string = decompressionSwitch(doc.getCompressionFormat(), doc.getDocument());
            //add each uncompressed doc's string to the List that we'll return
            docsToString.add(string);
        }
        return docsToString;
    }

    public List<byte[]> searchCompressed(String keyword) {
        List<URI> docsWithWord;
        try {
            docsWithWord = wordTrie.getAllSorted(keyword);
        }
        catch (Exception e) {
            return new LinkedList<>();
        }
        List<byte[]> docsToString = new LinkedList<>();
        for (URI uri : docsWithWord) {
            //update doc's lastUseTime
            DocumentImpl doc = daTree.get(uri);
            doc.setLastUseTime(System.currentTimeMillis());
            daHeap.reHeapify(uri);
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
            DocumentImpl previousDoc = daTree.get(uri);
            long previousUseTime = 0;                                                                                   //keep track of previous doc's use time in case of undo
            try { docString = IOUtils.toString(input); } catch (IOException e) {e.printStackTrace();}
            String altDocString = docString;
            List<URI> maxedOutDocs = new LinkedList<>();
            HashMap<String, Integer> docWords = wordsToHashMap(docString);                                              //at this point, scan through 'docString' and add words to the doc's hashtable
            byte[] compressedFile = compressionSwitch(defaultCompressionFormat, docString);                             //compression

            DocumentImpl doc = new DocumentImpl(compressedFile, docString.hashCode(), uri, defaultCompressionFormat, docWords);

            while ((daTree.size()+1 > maxDocumentCount) || (bytes+compressedFile.length > maxDocumentBytes)) {          //if we will exceed the max with this put, move least recent document to disk
                URI uriToDelete = daHeap.removeMin();
                //adding it to maxedOutDocs serves as a marker of things that this "put" in specific memory managed onto disk,
                // so that when we do an undo, we can properly add those back.
                // Seems like Judah said we dont need to account for that though
                maxedOutDocs.add(uriToDelete);
                maxedOutDelete(uriToDelete);
                wordTrie.docOutOfTrie(uriToDelete);
                daHeap.docOutOfHeap(uriToDelete);
            }
            if (previousDoc != null) {                                                                                  //if this is replacing a doc, then we have to remove that previous doc's words from trie, bytes, and remove from heap
                previousUseTime = previousDoc.getLastUseTime();
                allTheDeletion(previousDoc.getDocument().length, previousDoc, previousDoc.getKey());
            }
            int bytesLength = compressedFile.length;
            long previousUseTime2 = previousUseTime;
            long useTime = System.currentTimeMillis();
            allThePuts(bytesLength, doc, useTime, uri, altDocString, wordTrie);

            Function<URI, Boolean> undo = undoLambda(previousDoc, doc, bytesLength, uri, previousUseTime2, maxedOutDocs);
            Function<URI, Boolean> redo = redoLambda(previousDoc, doc, bytesLength, uri, altDocString);

            Command command = new Command(uri, undo, redo);
            commandStack.push(command);
            return docString.hashCode();
        }
    }

    public int putDocument(InputStream input, URI uri, CompressionFormat format) {
        String docString = null;
        DocumentImpl previousDoc = daTree.get(uri);
        long previousUseTime = 0;                                                                                       //save previous doc's use time, so that when undoing and putting back the previousdoc, we can set back its usetime
        try { docString = IOUtils.toString(input); } catch (IOException e) {e.printStackTrace();}                                          //convert input stream into a string
        String altDocString = docString;
        List<URI> maxedOutDocs = new LinkedList<>();
        HashMap<String, Integer> docWords = wordsToHashMap(docString);                                                  //counts words in string, creates a hashmap with each word and its frequency
        byte[] compressedFile = compressionSwitch(format, docString);                                                   //compress the string

        DocumentImpl doc = new DocumentImpl(compressedFile, docString.hashCode(), uri, format, docWords);

        while ((daTree.size()+1 > maxDocumentCount) || (bytes+compressedFile.length > maxDocumentBytes)) {           //if we've exceeded max, delete document completely
            URI uriToDelete = daHeap.removeMin();
            maxedOutDocs.add(uriToDelete);
            maxedOutDelete(uriToDelete);
            wordTrie.docOutOfTrie(uriToDelete);
            daHeap.docOutOfHeap(uriToDelete);
        }
        if (previousDoc != null) {                                                                                      //if this is replacing a doc, then we have to remove that previous doc's words from trie, bytes, and remove from heap
            previousUseTime = previousDoc.getLastUseTime();
            allTheDeletion(previousDoc.getDocument().length, previousDoc, previousDoc.getKey());
        }
        int bytesLength = compressedFile.length;
        long previousUseTime2 = previousUseTime;
        long useTime = System.currentTimeMillis();
        allThePuts(bytesLength, doc, useTime, uri, altDocString, wordTrie);

        Function<URI, Boolean> undo = undoLambda(previousDoc, doc, bytesLength, uri, previousUseTime2, maxedOutDocs);
        Function<URI, Boolean> redo = redoLambda(previousDoc, doc, bytesLength, uri, altDocString);

        Command command = new Command(uri, undo, redo);
        commandStack.push(command);

        return doc.hashCode();
    }

    public String getDocument(URI uri) {
        DocumentImpl doc = daTree.get(uri);
        if (doc == null) {
            return null;
        }
        doc.setLastUseTime(System.currentTimeMillis());
        //if this was a "get" of a document written to disk, so put it back in the Heap, and then deal with the
        //possibility that this would increase the size of the BTree and may have pushed it over the limit
        if (stuffOnDisk.contains(uri)) {
            wordTrie.docIntoTrie(uri, doc);
            daHeap.docIntoHeap(uri, doc);
            daHeap.insert(uri);
            bytes+=doc.getDocument().length;
            daHeap.reHeapify(uri);
            stuffOnDisk.remove(uri);
            while ((daTree.size() > maxDocumentCount) || (bytes > maxDocumentBytes)) {           //if we've exceeded max, delete document completely
                URI uriToDelete = daHeap.removeMin();
                wordTrie.docOutOfTrie(uriToDelete);
                daHeap.docOutOfHeap(uriToDelete);
                maxedOutDelete(uriToDelete);
            }
        }
        daHeap.reHeapify(uri);
        //decompress doc
        String decompressedDoc = decompressionSwitch(doc.getCompressionFormat(), doc.getDocument());
        return decompressedDoc;
    }

    public byte[] getCompressedDocument(URI uri) {
        //update doc's lastUseTime and reheapify
        DocumentImpl doc = daTree.get(uri);
        if (doc == null) {
            return null;
        }
        doc.setLastUseTime(System.currentTimeMillis());

        if (stuffOnDisk.contains(uri)) {
            wordTrie.docIntoTrie(uri, doc);
            daHeap.docIntoHeap(uri, doc);
            daHeap.insert(uri);
            bytes+=doc.getDocument().length;
            daHeap.reHeapify(uri);
            stuffOnDisk.remove(uri);
            while ((daTree.size() > maxDocumentCount) || (bytes > maxDocumentBytes)) {           //if we've exceeded max, delete document completely
                URI uriToDelete = daHeap.removeMin();
                wordTrie.docOutOfTrie(uriToDelete);
                daHeap.docOutOfHeap(uriToDelete);
                maxedOutDelete(uriToDelete);
            }
        }

        daHeap.reHeapify(uri);
        return doc.getDocument();
    }

    public boolean deleteDocument(URI uri) {
        if (daTree.get(uri) == null) {
            return false;
        }
        else {
            DocumentImpl docHolder = daTree.get(uri);
            long putTime = docHolder.getLastUseTime();                                                                  //save time from the "put" so that we can reinstate it in case of 'undo'
            allTheDeletion(docHolder.getDocument().length, docHolder, uri);
            daTree.delete(uri);                                                                                          //remove doc from store

            Function<URI,Boolean> undo = (URI uri1) -> {
                while ((daTree.size()+1 > maxDocumentCount) || (bytes+docHolder.getDocument().length > maxDocumentBytes)) {          //if we've exceeded max, delete document completely
                    URI uriToDelete = daHeap.removeMin();
                    wordTrie.docOutOfTrie(uriToDelete);
                    daHeap.docOutOfHeap(uriToDelete);
                    maxedOutDelete(uriToDelete);
                }
                bytes += docHolder.getDocument().length;
                docHolder.setLastUseTime(putTime);
                wordTrie.docIntoTrie(uri, docHolder);
                daHeap.docIntoHeap(uri, docHolder);
                daHeap.insert(docHolder.getKey());
                daHeap.reHeapify(docHolder.getKey());
                daTree.put(uri,docHolder);                                                                               //first put the document back in the store
                for (String string : docHolder.getWordMap().keySet()) {                                                //then put the documents words back into the trie
                    wordTrie.put(string, uri);
                }
                return (daTree.put(uri,docHolder) != null);                                                              //i think
            };
            Function<URI,Boolean> redo = (URI uri2) -> {
                allTheDeletion(docHolder.getDocument().length, docHolder, uri);
                daTree.delete(uri);                                                                               //re-remove doc from store
                return true;
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
        if (daTree.get(uri) == null) {
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

    private void wordsToTrie(String docString, DocumentImpl doc, TrieImpl<URI> wordTrie) {
        docString = docString.toLowerCase();

        //first break down given string into an array of each word in it
        String[] words = docString.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^\\w]", "");
        }
        for (int k = 0; k < words.length; k++) {
            wordTrie.put(words[k], doc.getKey());
        }
    }

    private void wordsOutOfTrie(URI uri) {
        //what document object does this uri point to
        DocumentImpl doc = daTree.get(uri);
        //iterate through the document's words, deleting each one from the trie
        for (String string : doc.getWordMap().keySet()) {
            wordTrie.delete(string, uri);
        }
    }

    private void allThePuts(int bytesLength, DocumentImpl doc, long useTime, URI uri, String altDocString, TrieImpl wordTrie) {
        bytes += bytesLength;                                                               //add this doc's bytes to total store bytes
        doc.setLastUseTime(useTime);
        this.wordTrie.docIntoTrie(uri, doc);
        daHeap.docIntoHeap(uri, doc);
        daHeap.insert(uri);
        daHeap.reHeapify(uri);
        daTree.put(uri, doc);                                                                //put in hashtable
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
        wordTrie.docIntoTrie(uri, doc);
        daHeap.docIntoHeap(uri, doc);
        daHeap.insert(uri);
        daHeap.reHeapify(uri);
        for (String string : doc.getWordMap().keySet()) {
            wordTrie.put(string, uri);
        }
        return (daTree.put(uri, doc) != null);
    }

    private Function<URI, Boolean> undoLambda(DocumentImpl previousDoc, DocumentImpl doc, int bytesLength, URI uri, long useTime, List<URI> maxedOutDocs) {
        Function<URI, Boolean> undo = (URI uri1) -> {
            //if nothing was memory managed:
            if (maxedOutDocs.isEmpty()) {
                //case 1: undoing a put in which nothing was memory managed, but the new document replaced a previous document
                if (previousDoc != null) {
                    allTheDeletion(bytesLength, doc, uri);                                                                  //delete everything from new doc
                    return addBackDoc(previousDoc.getDocument().length, previousDoc, useTime, uri);
                }                                                                                                           //add back previous doc
                //case 2: undoing a put in which nothing was memory managed, and new document was stam new, so just need to delete new doc
                else {
                    allTheDeletion(bytesLength, doc, uri);
                    daTree.delete(uri);
                    return true;
                }
            }
            //in cases of memory management:
            else {
                //case 3: undoing a put in which both (1) things were memory managed, so now have to be deserialized and put back,
                //and (2) the doc replaced a previous doc, so it has to be swapped back
                if (previousDoc != null) {
                    allTheDeletion(bytesLength, doc, uri);                                                                  //delete everything from new doc
                    for (URI you : maxedOutDocs) {
                        daTree.put(you, (DocumentImpl) docIO.deserialize(you));
                        daHeap.insert(you);
                    }
                    return addBackDoc(previousDoc.getDocument().length, previousDoc, useTime, uri);
                }                                                                                                           //add back previous doc
                //case 4: undoing a put in which things were memory managed, so the new doc needs to be deleted and those need to be added back
                else {
                    allTheDeletion(bytesLength, doc, uri);
                    daTree.delete(uri);
                    for (URI you : maxedOutDocs) {
                        daTree.put(you, (DocumentImpl) docIO.deserialize(you));
                        wordTrie.docIntoTrie(you, daTree.get(you));
                        daHeap.docIntoHeap(you, daTree.get(you));
                        daHeap.insert(you);
                    }
                    return true;
                }
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
        wordTrie.docIntoTrie(doc.getKey(), doc);
        daHeap.docIntoHeap(doc.getKey(), doc);
        daHeap.reHeapify(doc.getKey());
        URI uriToDelete = daHeap.removeMin();
        wordTrie.docOutOfTrie(uriToDelete);
        daHeap.docOutOfHeap(uriToDelete);
    }

    private void maxedOutDelete(URI uri) {
        DocumentImpl doc = daTree.get(uri);
        bytes -= doc.getDocument().length;
        //wordsOutOfTrie(uri);
        //store.remove(uri);
        try {                               //i think
            //add it to uris that are now on disk
            stuffOnDisk.add(uri);
            //serialize it and move it onto disk
            daTree.moveToDisk(uri);
        }
        catch (Exception e){
            e.printStackTrace();
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