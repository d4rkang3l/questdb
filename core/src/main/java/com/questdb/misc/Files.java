/*******************************************************************************
 *    ___                  _   ____  ____
 *   / _ \ _   _  ___  ___| |_|  _ \| __ )
 *  | | | | | | |/ _ \/ __| __| | | |  _ \
 *  | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *   \__\_\\__,_|\___||___/\__|____/|____/
 *
 * Copyright (C) 2014-2017 Appsicle
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.questdb.misc;

import com.questdb.ex.JournalException;
import com.questdb.ex.JournalRuntimeException;
import com.questdb.std.str.LPSZ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public final class Files {

    public static final Charset UTF_8;
    public static final int DT_UNKNOWN = 0;
    public static final int DT_FIFO = 1;
    public static final int DT_CHR = 2;
    public static final int DT_DIR = 4;
    public static final int DT_BLK = 6;
    public static final int DT_REG = 8;
    public static final int DT_LNK = 10;
    public static final int DT_SOCK = 12;
    public static final int DT_WHT = 14;

    private Files() {
    } // Prevent construction.

    public native static void append(long fd, long address, int len);

    public native static int close(long fd);

    public static long copy(long fdFrom, long fdTo, long bufPtr, int bufSize) {
        long total = 0;
        long l;
        while ((l = Files.sequentialRead(fdFrom, bufPtr, bufSize)) > 0) {
            Files.append(fdTo, bufPtr, (int) l);
            total += l;
        }
        return total;
    }

    public static boolean delete(File file) {
        try {
            deleteOrException(file);
            return true;
        } catch (JournalException e) {
            return false;
        }
    }

    public static void deleteOrException(File file) throws JournalException {
        if (!file.exists()) {
            return;
        }
        deleteDirContentsOrException(file);

        int retryCount = 3;
        boolean deleted = false;
        while (retryCount > 0 && !(deleted = file.delete())) {
            retryCount--;
            Thread.yield();
        }

        if (!deleted) {
            throw new JournalException("Cannot delete file %s", file);
        }
    }

    public static boolean exists(LPSZ lpsz) {
        return getLastModified(lpsz) != -1;
    }

    public native static void findClose(long findPtr);

    public static long findFirst(LPSZ lpsz) {
        return findFirst(lpsz.address());
    }

    public native static long findName(long findPtr);

    public native static boolean findNext(long findPtr);

    public native static int findType(long findPtr);

    public static long getLastModified(LPSZ lpsz) {
        return getLastModified(lpsz.address());
    }

    public native static long getStdOutFd();

    public static boolean isDots(CharSequence name) {
        return Chars.equals(name, '.') || Chars.equals(name, "..");
    }

    public static long length(LPSZ lpsz) {
        return length(lpsz.address());
    }

    public static File makeTempDir() {
        File result;
        try {
            result = File.createTempFile("questdb", "");
            deleteOrException(result);
            mkDirsOrException(result);
        } catch (Exception e) {
            throw new JournalRuntimeException("Exception when creating temp dir", e);
        }
        return result;
    }

    public static File makeTempFile() {
        try {
            return File.createTempFile("questdb", "");
        } catch (IOException e) {
            throw new JournalRuntimeException(e);
        }
    }

    public static void mkDirsOrException(File dir) {
        if (!dir.mkdirs()) {
            throw new JournalRuntimeException("Cannot create temp directory: %s", dir);
        }
    }

    public static long openAppend(LPSZ lpsz) {
        return openAppend(lpsz.address());
    }

    public static long openRO(LPSZ lpsz) {
        return openRO(lpsz.address());
    }

    public static long openRW(LPSZ lpsz) {
        return openRW(lpsz.address());
    }

    public native static long read(long fd, long address, int len, long offset);

    public static String readStringFromFile(File file) throws JournalException {
        try {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte buffer[]
                        = new byte[(int) fis.getChannel().size()];
                int totalRead = 0;
                int read;
                while (totalRead < buffer.length
                        && (read = fis.read(buffer, totalRead, buffer.length - totalRead)) > 0) {
                    totalRead += read;
                }
                return new String(buffer, UTF_8);
            }
        } catch (IOException e) {
            throw new JournalException("Cannot read from %s", e, file.getAbsolutePath());
        }
    }

    public static boolean remove(LPSZ lpsz) {
        return remove(lpsz.address());
    }

    public static boolean rename(LPSZ oldName, LPSZ newName) {
        return rename(oldName.address(), newName.address());
    }

    public native static long sequentialRead(long fd, long address, int len);

    public static boolean setLastModified(LPSZ lpsz, long millis) {
        return setLastModified(lpsz.address(), millis);
    }

    public static boolean touch(LPSZ lpsz) {
        long fd = openRW(lpsz);
        boolean result = fd > 0;
        if (result) {
            close(fd);
        }
        return result;
    }

    public native static boolean truncate(long fd, long size);

    public native static long write(long fd, long address, int len, long offset);

    // used in tests
    public static void writeStringToFile(File file, String s) throws JournalException {
        try {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(s.getBytes(UTF_8));
            }
        } catch (IOException e) {
            throw new JournalException("Cannot write to %s", e, file.getAbsolutePath());
        }
    }

    private native static boolean remove(long lpsz);

    private native static long getLastModified(long lpszName);

    private native static long length(long lpszName);

    private native static long openRO(long lpszName);

    private native static long openRW(long lpszName);

    private native static long openAppend(long lpszName);

    private native static long findFirst(long lpszName);

    private native static boolean setLastModified(long lpszName, long millis);

    private static void deleteDirContentsOrException(File file) throws JournalException {
        if (!file.exists()) {
            return;
        }
        try {
            if (notSymlink(file)) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        deleteOrException(files[i]);
                    }
                }
            }
        } catch (IOException e) {
            throw new JournalException("Cannot delete dir contents: %s", file, e);
        }
    }

    private static boolean notSymlink(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (File.separatorChar == '\\') {
            return true;
        }

        File fileInCanonicalDir;
        if (file.getParentFile() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    private static native boolean rename(long lpszOld, long lpszNew);

    static {
        UTF_8 = Charset.forName("UTF-8");
    }
}
