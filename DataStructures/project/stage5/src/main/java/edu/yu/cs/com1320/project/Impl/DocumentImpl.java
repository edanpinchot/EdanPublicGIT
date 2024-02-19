package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DocumentImpl implements Document {
    private byte[] compressedFile;
    private int hashCode;
    private URI uri;
    private DocumentStore.CompressionFormat compressionFormat;
    private Map<String, Integer> docWords;
    private long lastUseTime;

    public DocumentImpl(byte[] compressedFile, int hashCode, URI uri, DocumentStore.CompressionFormat compressionFormat, HashMap<String, Integer> docWords) {
        this.compressedFile = compressedFile;
        this.hashCode = Math.abs(hashCode);
        this.uri = uri;
        this.compressionFormat = compressionFormat;
        this.docWords = docWords;
    }

    public byte[] getDocument() {
        return compressedFile;
    }

    public int getDocumentHashCode() {
        return Math.abs(hashCode);
    }

    public URI getKey() {
        return uri;
    }

    public DocumentStore.CompressionFormat getCompressionFormat() {
        return compressionFormat;
    }

    public int wordCount(String word) {
        if (!docWords.containsKey(word)) {
            return 0;
        }

        return docWords.get(word);
    }

    public void setLastUseTime(long timeInMilliseconds) {
        this.lastUseTime = timeInMilliseconds;
    }

    public long getLastUseTime() {
        return lastUseTime;
    }

    public int compareTo(Document d) {
        if (this.lastUseTime == d.getLastUseTime()) {
            return 0;
        }
        else if (this.lastUseTime > d.getLastUseTime()) {
            return 1;
        }
        else {
            return -1;
        }
    }

    public Map<String, Integer> getWordMap() {
        return docWords;
    }

    public void setWordMap(Map<String,Integer> wordMap) {
        this.docWords = wordMap;
    }
}