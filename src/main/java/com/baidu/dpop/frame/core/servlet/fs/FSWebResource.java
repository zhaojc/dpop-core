package com.baidu.dpop.frame.core.servlet.fs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.commons.io.FileUtils;

import com.baidu.dpop.frame.core.servlet.UnsatisfiableRangeException;
import com.baidu.dpop.frame.core.servlet.WebResource;

/**
 * 文件资源。将一个文件作为一个web资源提供。
 */
public class FSWebResource implements WebResource {

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private final File file;

    public FSWebResource(File file) {
        this.file = file;
    }

    @Override
    public long getLastModified() {
        return file.lastModified();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public long getContentLength() {
        return file.length();
    }

    @Override
    public String getETag() {
        return String.format("\"%04X-%08X-%08X\"", file.hashCode(), file.length(), file.lastModified());
    }

    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public void copyTo(OutputStream out) throws IOException {
        InputStream in = FileUtils.openInputStream(file);
        InputStream buffered = new BufferedInputStream(in, DEFAULT_BUFFER_SIZE);
        try {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            while (true) {
                int len = buffered.read(buffer);
                if (len <= 0) {
                    break;
                }

                out.write(buffer, 0, len);
            }
        } finally {
            buffered.close();
        }
    }

    @Override
    public void copyTo(OutputStream out, long start, long end) throws IOException {
        if (start < 0 || end < 0 || start > end) {
            throw new UnsatisfiableRangeException();
        }

        long fileLen = file.length();
        if (end > fileLen) {
            throw new UnsatisfiableRangeException();
        }

        if (start == end) {
            return;
        }

        long remains = end - start;
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        try {
            raf.seek(start);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            while (remains > 0) {
                int actualRead = raf.read(buffer, 0, buffer.length);
                out.write(buffer, 0, actualRead);
                remains -= actualRead;
            }
        } finally {
            raf.close();
        }
    }
}
