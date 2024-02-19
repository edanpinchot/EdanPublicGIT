package edu.yu.cs.com1320.project;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;

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
    private HashTableImpl<URI, Document> store;
    private CompressionFormat defaultCompressionFormat;
    private StackImpl<Command> commandStack;

    public DocumentStoreImpl() {
        this.store = new HashTableImpl(2);
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
            while((ent = zipIn.getNextZipEntry()) != null){
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

    protected String sevenzUncompress(byte[] compressedFile) {
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

        try {
            docString = IOUtils.toString(input);
        }
        catch (IOException e){
        }

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
            DocumentImpl doc = new DocumentImpl(compressedFile, docString.hashCode(), uri, defaultCompressionFormat);
            if (store.get(uri).getDocumentHashCode() == docString.hashCode()) {
                return docString.hashCode();
            }
            store.arrayDouble();
            store.put(uri, doc);
            //create an undo function, a redo function, then a command instance, then push it onto the stack
            return docString.hashCode();
        }

        else {
            DocumentImpl doc = new DocumentImpl(zipCompress(docString), docString.hashCode(), uri, getDefaultCompressionFormat());
            store.arrayDouble();
            store.put(uri, doc);
            //create an undo function, a redo function, then a command instance, then push it onto the stack
            return doc.hashCode();
        }
    }

    public int putDocument(InputStream input, URI uri, CompressionFormat format) {
        byte[] compressedFile = null;
        String docString = null;

        try {
            docString = IOUtils.toString(input);
        }
        catch (IOException e){
        }

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

        DocumentImpl doc = new DocumentImpl(compressedFile, docString.hashCode(), uri, format);

        if ((store.get(uri) != null) && (store.get(uri).getDocumentHashCode() == docString.hashCode())) {
            return doc.hashCode();
        }

        store.arrayDouble();
        store.put(uri, doc);
        //create an undo function, a redo function, then a command instance, then push it onto the stack
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
            store.remove(uri);
            //create an undo function, a redo function, then a command instance, then push it onto the stack
            return true;
        }

    }

    public boolean undo() throws IllegalStateException {
        commandStack.peek().undo();
        return true;
    }

    public boolean undo(URI uri) throws IllegalStateException {
        return true;
    }

}
