package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;

import java.io.*;
import java.net.URI;
import java.util.HashMap;

public class DocumentImpl implements Document {
    private byte[] compressedFile;
    private int hashCode;
    private URI uri;
    private DocumentStore.CompressionFormat compressionFormat;
    private HashMap<String, Integer> docWords;

    public DocumentImpl(byte[] compressedFile, int hashCode, URI uri, DocumentStore.CompressionFormat compressionFormat, HashMap<String, Integer> docWords) {
        this.compressedFile = compressedFile;
        this.hashCode = hashCode;
        this.uri = uri;
        this.compressionFormat = compressionFormat;
        this.docWords = docWords;
    }

    public byte[] getDocument() {
        return compressedFile;
    }

    public int getDocumentHashCode() {
        return hashCode;
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

    protected HashMap<String, Integer> getDocWords() {
        return docWords;
    }
}