package edu.yu.cs.com1320.project;

import java.io.*;
import java.net.URI;

public class DocumentImpl implements Document {
    private byte[] compressedFile;
    private int hashCode;
    private URI uri;
    private DocumentStore.CompressionFormat compressionFormat;

    public DocumentImpl(byte[] compressedFile, int hashCode, URI uri, DocumentStore.CompressionFormat compressionFormat) {
        this.compressedFile = compressedFile;
        this.hashCode = hashCode;
        this.uri = uri;
        this.compressionFormat = compressionFormat;
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
}