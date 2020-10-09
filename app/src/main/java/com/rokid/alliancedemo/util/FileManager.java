package com.rokid.alliancedemo.util;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class FileManager {

    private String fileName;
    private FileOutputStream fileOutputStream;
    private BufferedOutputStream bufferedOutputStream;


    public FileManager(String fileName) {
        this.fileName = fileName;
        try {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveFileData(byte[] data, int offset, int length) {
        try {
            bufferedOutputStream.write(data, offset, length);
            bufferedOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveFileData(byte[] data) {
        try {
            bufferedOutputStream.write(data);
            bufferedOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            bufferedOutputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
        } finally {
            try {
                bufferedOutputStream.close();
                fileOutputStream.close();
            } catch (Exception e1) {
            }
        }
    }

    public void deleteFile() {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
