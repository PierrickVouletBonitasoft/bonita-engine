/**
 * Copyright (C) 2011, 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class IOUtil {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final int BUFFER_SIZE = 100000;

    public static final String fEncoding = "UTF-8";

    public static byte[] generateJar(final Class<?>... classes) throws IOException {
        return generateJar(getResources(classes));
    }

    public static Map<String, byte[]> getResources(final Class<?>... classes) throws IOException {
        if (classes == null || classes.length == 0) {
            final String message = "No classes available";
            throw new IOException(message);
        }
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        for (final Class<?> clazz : classes) {
            resources.put(clazz.getName().replace(".", "/") + ".class", getClassData(clazz));
            for (final Class<?> internalClass : clazz.getDeclaredClasses()) {
                resources.put(internalClass.getName().replace(".", "/") + ".class", getClassData(internalClass));
            }
        }
        return resources;
    }

    public static byte[] getClassData(final Class<?> clazz) throws IOException {
        if (clazz == null) {
            final String message = "Class is null";
            throw new IOException(message);
        }
        final String resource = clazz.getName().replace('.', '/') + ".class";
        final InputStream inputStream = clazz.getClassLoader().getResourceAsStream(resource);
        byte[] data = null;
        try {
            if (inputStream == null) {
                throw new IOException("Impossible to get stream from class: " + clazz.getName() + ", className= " + resource);
            }
            data = IOUtil.getAllContentFrom(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return data;
    }

    public static byte[] generateJar(final Map<String, byte[]> resources) throws IOException {
        if (resources == null || resources.size() == 0) {
            final String message = "No resources available";
            throw new IOException(message);
        }

        ByteArrayOutputStream baos = null;
        JarOutputStream jarOutStream = null;

        try {
            baos = new ByteArrayOutputStream();
            jarOutStream = new JarOutputStream(new BufferedOutputStream(baos));
            for (final Map.Entry<String, byte[]> resource : resources.entrySet()) {
                jarOutStream.putNextEntry(new JarEntry(resource.getKey()));
                jarOutStream.write(resource.getValue());
            }
            jarOutStream.flush();
            baos.flush();
        } finally {
            if (jarOutStream != null) {
                jarOutStream.close();
            }
            if (baos != null) {
                baos.close();
            }
        }

        return baos.toByteArray();
    }

    /**
     * Return the whole underlying stream content into a single String.
     * Warning: the whole content of stream will be kept in memory!! Use with
     * care!
     * 
     * @param in
     *            the stream to read
     * @return the whole content of the stream in a single String.
     * @throws IOException
     *             if an I/O exception occurs
     */
    public static byte[] getAllContentFrom(final InputStream in) throws IOException {
        if (in == null) {
            throw new IOException("The InputStream is null!");
        }
        final byte[] buffer = new byte[BUFFER_SIZE];
        final byte[] resultArray;
        BufferedInputStream bis = null;
        ByteArrayOutputStream result = null;

        try {
            bis = new BufferedInputStream(in);
            result = new ByteArrayOutputStream();
            int amountRead;
            while ((amountRead = bis.read(buffer)) > 0) {
                result.write(buffer, 0, amountRead);
            }
            resultArray = result.toByteArray();
            result.flush();

        } finally {
            if (bis != null) {
                bis.close();
            }
            if (result != null) {
                result.close();
            }
        }
        return resultArray;
    }

    /**
     * Equivalent to {@link #getAllContentFrom(InputStream) getAllContentFrom(new
     * FileInputStream(file))};
     * 
     * @param file
     *            the file to read
     * @return the whole content of the file in a single String.
     * @throws IOException
     *             If an I/O exception occurs
     */
    public static byte[] getAllContentFrom(final File file) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            return getAllContentFrom(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Return the whole underlying stream content into a single String.
     * Warning: the whole content of stream will be kept in memory!! Use with
     * care!
     * 
     * @param url
     *            the URL to read
     * @return the whole content of the stream in a single String.
     * @throws IOException
     *             if an I/O exception occurs
     */
    public static byte[] getAllContentFrom(final URL url) throws IOException {
        final InputStream in = url.openStream();
        try {
            return getAllContentFrom(in);
        } finally {
            in.close();
        }
    }

    public static boolean deleteDir(final File dir) throws IOException {
        return deleteDir(dir, 1, 0);
    }

    public static boolean deleteDir(final File dir, final int attempts, final long sleepTime) throws IOException {
        boolean result = true;
        if (!dir.exists()) {
            return false;
        }
        if (!dir.isDirectory()) {
            throw new IOException("Unable to delete directory: " + dir + ", it is not a directory");
        }
        final File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                deleteDir(files[i], attempts, sleepTime);
            } else {
                result = result && deleteFile(files[i], attempts, sleepTime);
            }
        }
        result = result && deleteFile(dir, attempts, sleepTime);
        return result;
    }

    public static boolean deleteFile(final File f, final int attempts, final long sleepTime) {
        int retries = attempts;
        while (retries > 0) {
            if (f.delete()) {
                break;
            }
            retries--;
            try {
                Thread.sleep(sleepTime);
            } catch (final InterruptedException e) {
            }
        }
        return retries > 0;
    }

    public static byte[] zip(final Map<String, byte[]> files) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            for (final Entry<String, byte[]> file : files.entrySet()) {
                zos.putNextEntry(new ZipEntry(file.getKey()));
                zos.write(file.getValue());
                zos.closeEntry();
            }
            return baos.toByteArray();
        } finally {
            zos.close();
        }
    }

    /**
     * Create a structured zip archive recursively.
     * The string must be OS specific String to represent path.
     * 
     * @param dir2zip
     * @param zos
     * @param root
     * @param filenameFilter
     * @throws IOException
     */
    public static void zipDir(final String dir2zip, final ZipOutputStream zos, final String root) throws IOException {
        final File zipDir = new File(dir2zip);
        String[] dirList;
        dirList = zipDir.list();
        final byte[] readBuffer = new byte[BUFFER_SIZE];
        int bytesIn = 0;
        for (int i = 0; i < dirList.length; i++) {
            final File f = new File(zipDir, dirList[i]);
            final String path = f.getPath();
            if (f.isDirectory()) {
                final String filePath = path;
                zipDir(filePath, zos, root);
                continue;
            }
            final FileInputStream fis = new FileInputStream(f);
            final ZipEntry anEntry = new ZipEntry(path.substring(root.length() + 1, path.length()).replace(String.valueOf(File.separatorChar), "/"));
            zos.putNextEntry(anEntry);
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
            fis.close();
            zos.flush();
            zos.closeEntry();
        }
    }

    /**
     * Read the contents from the given FileInputStream. Return the result as a String.
     * 
     * @param inputStream
     *            the stream to read from
     * @return the content read from the inputStream, as a String
     */
    public static String read(final InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream is null");
        }
        final Scanner scanner = new Scanner(inputStream, fEncoding);
        return read(scanner);
    }

    private static String read(final Scanner scanner) {
        final StringBuilder text = new StringBuilder();
        try {
            boolean isFirst = true;
            while (scanner.hasNextLine()) {
                if (isFirst) {
                    text.append(scanner.nextLine());
                } else {
                    text.append(LINE_SEPARATOR + scanner.nextLine());
                }
                isFirst = false;
            }
        } finally {
            scanner.close();
        }
        return text.toString();
    }

    /**
     * Read the contents of the given file.
     * 
     * @param file
     */
    public static String read(final File file) throws IOException {
        final Scanner scanner = new Scanner(new FileInputStream(file), fEncoding);
        return read(scanner);
    }

    public static void unzipToFolder(final InputStream inputStream, final File outputFolder) throws IOException {
        final ZipInputStream zipInputstream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;

        try {
            while ((zipEntry = zipInputstream.getNextEntry()) != null) {
                extractZipEntry(zipInputstream, zipEntry, outputFolder);
            }
        } finally {
            zipInputstream.close();
        }
    }

    private static boolean mkdirs(final File file) {
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    private static void extractZipEntry(final ZipInputStream zipInputstream, final ZipEntry zipEntry, final File outputFolder) throws FileNotFoundException,
            IOException {
        try {
            final String entryName = zipEntry.getName();
            // entryName = entryName.replace('/', File.separatorChar);
            // entryName = entryName.replace('\\', File.separatorChar);

            // For each entry, a file is created in the output directory "folder"
            final File outputFile = new File(outputFolder.getAbsolutePath(), entryName);

            // If the entry is a directory, it creates in the output folder, and we go to the next entry (return).
            if (zipEntry.isDirectory()) {
                mkdirs(outputFile);
                return;
            }
            writeZipInputToFile(zipInputstream, outputFile);
        } finally {
            zipInputstream.closeEntry();
        }
    }

    private static void writeZipInputToFile(final ZipInputStream zipInputstream, final File outputFile) throws FileNotFoundException, IOException {
        // The input is a file. An FileOutputStream is created to write the content of the new file.
        mkdirs(outputFile.getParentFile());
        final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        try {
            try {
                // The contents of the new file, that is read from the ZipInputStream using a buffer (byte []), is written.
                int bytesRead;
                final byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = zipInputstream.read(buffer)) > -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                fileOutputStream.close();
            }
        } catch (final IOException ioe) {
            // In case of error, the file is deleted
            outputFile.delete();
            throw ioe;
        }
    }

    public static void writeContentToFile(final String content, final File outputFile) throws IOException {
        final Writer out = new OutputStreamWriter(new FileOutputStream(outputFile), fEncoding);
        try {
            out.write(content);
        } finally {
            out.close();
        }
    }

}
