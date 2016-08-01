package com.wangjingke.madresgps;

public class CheckID {

    public static String extractID(String input) {
        if (input.toUpperCase().startsWith("STARTMADRES")) {
            String prefix = input.substring(0, 11);
            String tempID = input.replaceAll(prefix, "").trim();
            if (tempID.toUpperCase().startsWith("MAD")) {
                return tempID.toUpperCase();
            } else {
                return "invalid ID";
            }
        } else {
            return "invalid ID";
        }
    }

    public static boolean start(String input) {return input.toUpperCase().startsWith("STARTMADRES");}

    public static boolean stop(String input) {return input.toUpperCase().startsWith("STOPMADRES");}

    public static boolean clean(String input) {return input.toUpperCase().startsWith("CLEANMADRES");}
}
