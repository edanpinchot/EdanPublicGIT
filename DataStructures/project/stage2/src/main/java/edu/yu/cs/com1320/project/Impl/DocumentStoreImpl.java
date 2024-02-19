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
import org.apache.commons.io.IOUtils;

public class DocumentStoreImpl implements DocumentStore {
    private CompressionFormat defaultCompressionFormat;
    private HashTableImpl<URI, DocumentImpl> store;
    private StackImpl<Command> commandStack;
    private TrieImpl<DocumentImpl> wordTrie;

    public DocumentStoreImpl() {
        this.store = new HashTableImpl(2);
        this.commandStack = new StackImpl<Command>();
        this.wordTrie = new TrieImpl<DocumentImpl>();
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
            //decompress each of the documents to get the uncompressed Strings that we want to return hwew
            switch (doc.getCompressionFormat()) {
                case ZIP:
                    string = zipUncompress(doc.getDocument());
                    break;
                case JAR:
                    string = jarUncompress(doc.getDocument());
                    break;
                case SEVENZ:
                    string = sevenzUncompress(doc.getDocument());
                    break;
                case GZIP:
                    string = gzipUncompress(doc.getDocument());
                    break;
                case BZIP2:
                    string = bzip2Uncompress(doc.getDocument());
                    break;
            }
            //add each uncompressed doc's string to the List that we'll return
            docsToString.add(string);
//            docsToString.add(doc.getKey().toString();
//            docsToString.add(doc.toString());
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

    protected String zipUncompress(byte[] compressedFile) {
        String uncompressedDoc = null;
        try {
            ByteArrayInputStream in1  = new ByteArrayInputStream(compressedFile);
            BufferedInputStream in = new BufferedInputStream(in1);
            ZipArchiveEntry ent = new ZipArchiveEntry("a");
            ZipArchiveInputStream zipIn = new ZipArchiveInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while((ent = zipIn.getNextZipEntry()) != null) {
                out.write(zipIn.readAllBytes());
            }
            uncompressedDoc = out.toString();
            zipIn.close();
            in.close();
            out.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return uncompressedDoc;
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

    protected String jarUncompress(byte[] compressedFile) {
        String uncompressedDoc = null;
        try {
            ByteArrayInputStream in1  = new ByteArrayInputStream(compressedFile);
            BufferedInputStream in = new BufferedInputStream(in1);
            JarArchiveEntry ent = new JarArchiveEntry("a");
            JarArchiveInputStream jarIn = new JarArchiveInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while((ent = jarIn.getNextJarEntry()) != null){
                out.write(jarIn.readAllBytes());
            }
            uncompressedDoc = out.toString();
            jarIn.close();
            in.close();
            out.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return uncompressedDoc;
    }

    protected byte[] sevenzCompress(String str) {
        byte[] compressed = null;
//        try {
//            ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
//            SeekableInMemoryByteChannel out = new SeekableInMemoryByteChannel();
//            SevenZOutputFile sevenzOut = new SevenZOutputFile(out);
//            SevenZArchiveEntry sevenZ = sevenzOut.createArchiveEntry(new File("a"), "a");
//            //SevenZArchiveEntry jar = new SevenZArchiveEntry();
//            sevenZ.setSize(str.length());
//            sevenzOut.putArchiveEntry(sevenZ);
//            in.write(in.readAllBytes());
//
//            jarOut.closeArchiveEntry();
//            jarOut.finish();
//            jarOut.close();
//            out.close();
//            compressed = out.array();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }

        return compressed;
    }

    protected String sevenzUncompress(byte[] compressedFile) {
        String uncompressedDoc = null;
//        try {
//            SeekableInMemoryByteChannel in1  = new SeekableInMemoryByteChannel(compressedFile);
//            //BufferedInputStream in = new BufferedInputStream(in1);
//            SevenZOutputFile jarOut = new SevenZOutputFile(in1);
//            SevenZArchiveEntry ent = jarOut.createArchiveEntry(new File("a"), "a");
//            SeekableInMemoryByteChannel pp = new SeekableInMemoryByteChannel(in1);
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            while((ent = jarIn.getNextEntry()) != null){
//                out.write(jarIn.readAllBytes());
//            }
//            uncompressedDoc = out.toString();
//            jarIn.close();
//            //in.close();
//            out.close();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }

        return uncompressedDoc;
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

    protected String gzipUncompress(byte[] compressedFile) {
        String uncompressedDoc = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(compressedFile);
            GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int n = 0;
            while (-1 != (n = gzIn.read())) {
                out.write(n);
            }
            uncompressedDoc = out.toString();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return uncompressedDoc;
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

    protected String bzip2Uncompress(byte[] compressedFile) {
        String uncompressedDoc = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(compressedFile);
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int n = 0;
            while (-1 != (n = bzIn.read())) {
                out.write(n);
            }
            uncompressedDoc = out.toString();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return uncompressedDoc;
    }


    public int putDocument(InputStream input, URI uri) {
        byte[] compressedFile = null;
        String docString = null;
        DocumentImpl previousDoc = store.get(uri);
        HashMap<String, Integer> docWords;

        try {
            docString = IOUtils.toString(input);
            String altDocString = docString;
        }
        catch (IOException e) {
        }

        String altDocString = docString;
        //at this point, scan through 'docString' and add words to the doc's hashtable
        docWords = wordsToHashMap(docString);

        if (defaultCompressionFormat != null) {
            switch (defaultCompressionFormat) {
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
            DocumentImpl doc = new DocumentImpl(compressedFile, docString.hashCode(), uri, defaultCompressionFormat, docWords);
            if (store.get(uri).getDocumentHashCode() == docString.hashCode()) {
                return docString.hashCode();
            }
            store.put(uri, doc);

            //put in trie
            wordsToTrie(altDocString, doc, wordTrie);

            Function<URI,Boolean> undo = (URI uri1) -> {
                //if this "put" just put a new value to an already existing key, then put previous doc and its words back into store and trie
                if (previousDoc != null) {
                    for (String string : previousDoc.getDocWords().keySet()) {
                        wordTrie.put(string, previousDoc);
                    }
                    return (store.put(uri, previousDoc) != null);
                }
                //otherwise it put a brand new doc in, so undo removes the doc and its words frmo the trie
                else {
                    wordsOutOfTrie(uri);
                    return store.remove(uri);
                }
            };
            Function<URI,Boolean> redo = (URI uri2) -> {
                store.put(uri2, doc);
                wordsToTrie(altDocString, doc, wordTrie);
                return true;
            };
            Command command = new Command(uri, undo, redo);
            commandStack.push(command);

            return docString.hashCode();
        }

        else {
            DocumentImpl doc = new DocumentImpl(zipCompress(docString), docString.hashCode(), uri, getDefaultCompressionFormat(), docWords);
            store.put(uri, doc);

            //put in trie
            wordsToTrie(altDocString, doc, wordTrie);

            Function<URI,Boolean> undo = (URI uri1) -> {
                if (previousDoc != null) {
                    for (String string : previousDoc.getDocWords().keySet()) {
                        wordTrie.put(string, previousDoc);
                    }
                    return (store.put(uri, previousDoc) != null);
                }
                else {
                    wordsOutOfTrie(uri);
                    return store.remove(uri);
                }
            };
            Function<URI,Boolean> redo = (URI uri2) -> {
                store.put(uri2, doc);
                wordsToTrie(altDocString, doc, wordTrie);
                return true;
            };
            Command command = new Command(uri, undo, redo);
            commandStack.push(command);
            return doc.hashCode();
        }
    }

    public int putDocument(InputStream input, URI uri, CompressionFormat format) {
        byte[] compressedFile = null;
        String docString = null;
        DocumentImpl previousDoc = store.get(uri);
        HashMap<String, Integer> docWords;

        //convert input stream into a string
        try {
            docString = IOUtils.toString(input);
        }
        catch (IOException e){
        }

        String altDocString = docString;
        //counts words in string, creates a hashmap with each word and its frequency
        docWords = wordsToHashMap(docString);

        //compress the string
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

        DocumentImpl doc = new DocumentImpl(compressedFile, docString.hashCode(), uri, format, docWords);

        if ((store.get(uri) != null) && (store.get(uri).getDocumentHashCode() == docString.hashCode())) {
            return doc.hashCode();
        }

        //put uri and document into the hashtable
        store.put(uri, doc);

        //put in trie
        wordsToTrie(altDocString, doc, wordTrie);

        Function<URI,Boolean> undo = (URI uri1) -> {
            //if this "put" just put a new value to an already existing key, then put previous doc and its words back into store and trie
            if (previousDoc != null) {
                for (String string : previousDoc.getDocWords().keySet()) {
                    wordTrie.put(string, previousDoc);
                }
                return (store.put(uri, previousDoc) != null);
            }
            //otherwise it put a brand new doc in, so undo removes the doc and its words frmo the trie
            else {
                wordsOutOfTrie(uri);
                return store.remove(uri);
            }
        };
        Function<URI,Boolean> redo = (URI uri2) -> {
            store.put(uri2, doc);
            wordsToTrie(altDocString, doc, wordTrie);
            return true;
        };
        Command command = new Command(uri, undo, redo);
        commandStack.push(command);

        return doc.hashCode();
    }

    public String getDocument(URI uri) {
        String uncompressedDoc = null;
        if (store.get(uri) == null) {
            return null;
        }

        else {
            switch (store.get(uri).getCompressionFormat()) {
                case ZIP:
                    uncompressedDoc = zipUncompress(store.get(uri).getDocument());
                    break;
                case JAR:
                    uncompressedDoc = jarUncompress(store.get(uri).getDocument());
                    break;
                case SEVENZ:
                    uncompressedDoc = sevenzUncompress(store.get(uri).getDocument());
                    break;
                case GZIP:
                    uncompressedDoc = gzipUncompress(store.get(uri).getDocument());
                    break;
                case BZIP2:
                    uncompressedDoc = bzip2Uncompress(store.get(uri).getDocument());
                    break;
            }
            return uncompressedDoc;
        }
    }

    public byte[] getCompressedDocument(URI uri) {
        return store.get(uri).getDocument();
    }

    public boolean deleteDocument(URI uri) {
        if (store.get(uri) == null) {
            return false;
        }
        else {
            DocumentImpl docHolder = store.get(uri);
            //delete its words from trie
            wordsOutOfTrie(uri);
            //remove doc from store
            store.remove(uri);

            Function<URI,Boolean> undo = (URI uri1) -> {
                //first put the document back in the store
                store.put(uri,docHolder);
                //then put the documents words back into the trie
                for (String string : store.get(uri).getDocWords().keySet()) {
                    wordTrie.put(string, store.get(uri));
                }
                return (store.put(uri,docHolder) != null);                      //i think
            };
            Function<URI,Boolean> redo = (URI uri2) -> {
                //re-remove words from trie
                wordsOutOfTrie(uri);
                //re-remove doc from store
                return store.remove(uri);
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

        //if stack gets to size zero, it will return false
        while (commandStack.size() != 0) {
                //if we reach the command we want, undo it, then push any commands that had been popped off back onto the stack
                if (commandStack.peek().getUri() == uri) {
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

}