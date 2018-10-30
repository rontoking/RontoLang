package com.rontoking.rontolang.interpreter;

import com.esotericsoftware.kryonet.Connection;

import java.io.File;

public class MimeType {
    public String extension, mimeType;
    public boolean isText;
    private static MimeType[] array;
    private static String text;

    private static final String MIME_TYPES_RAW = "html=>text/html;\n" +
            "htm=>text/html;\n" +
            "js=>application/javascript;\n" +
            "css=>text/css;\n" +
            "avi=>video/x-msvideo;\n" +
            "bin=>application/octet-stream;\n" +
            "csv=>text/csv;\n" +
            "txt=>text/plain;\n" +
            "doc=>application/msword;\n" +
            "eot=>application/vnd.ms-fontobject;\n" +
            "gif=>image/gif;\n" +
            "ico=>image/x-icon;\n" +
            "ics=>text/calendar;\n" +
            "jar=>application/java-archive;\n" +
            "jpeg=>image/jpeg;\n" +
            "jpg=>image/jpeg;\n" +
            "json=>application/json;\n" +
            "mid=>audio/midi;\n" +
            "midi=>audio/midi;\n" +
            "mpeg=>video/mpeg;\n" +
            "mpkg=>application/vnd.apple.installer+xml;\n" +
            "oga=>audio/ogg;\n" +
            "ogv=>video/ogg;\n" +
            "ogx=>application/ogg;\n" +
            "ogg=>application/ogg;\n" +
            "otf=>font/otf;\n" +
            "png=>image/png;\n" +
            "pdf=>application/pdf;\n" +
            "ppt=>application/vnd.ms-powerpoint;\n" +
            "rar=>application/x-rar-compressed;\n" +
            "svg=>image/svg+xml;\n" +
            "swf=>application/x-shockwave-flash;\n" +
            "tar=>application/x-tar;\n" +
            "ttf=>font/ttf;\n" +
            "wav=>audio/x-wav;\n" +
            "woff=>font/woff;\n" +
            "woff2=>font/woff2;\n" +
            "xhtml=>application/xhtml+xml;\n" +
            "xls=>application/vnd.ms-excel;\n" +
            "xlsx=>application/vnd.ms-excel;\n" +
            "xml=>application/xml;\n" +
            "zip=>application/zip;\n" +
            "7z=>application/x-7z-compressed;\n" +
            "txt=>text/plain";

    public MimeType(String extension, String mimeType){
        this.extension = extension;
        this.mimeType = mimeType;
        this.isText = this.mimeType.contains("text");
    }

    public static void load(){
        text = MIME_TYPES_RAW.replaceAll("\\s+","");
        String[] pairs = text.split(";");
        array = new MimeType[pairs.length];
        for(int i = 0; i < pairs.length; i++){
            array[i] = new MimeType(pairs[i].split("=>")[0], pairs[i].split("=>")[1]);
        }
    }

    public static String getType(String name){
        for(int i = 0; i < array.length; i++){
            if(extension(name).equals(array[i].extension))
                return array[i].mimeType;
        }
        return "application/octet-stream";
    }

    public static boolean isFileText(File file){
        return getType(file.getName()).contains("text");
    }

    public static String extension(String name){
        String[] farray = name.split("\\.");
        if(farray.length > 1)
            return farray[farray.length - 1];
        return "";
    }
}
