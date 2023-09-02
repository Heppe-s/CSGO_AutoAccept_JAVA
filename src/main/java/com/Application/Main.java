package com.Application;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;


public class Main {
    static String CsTitle = "Counter-Strike: Global Offensive - Direct3D 9";

    public static void main(String[] args) {
        boolean checker;

        Pointer hWnd = User32.INSTANCE.FindWindowA(null, CsTitle);
        if(hWnd == null){
            System.out.println("A janela: "+CsTitle+" não foi encontrada.");
        } else {
            System.out.println("A janela: "+CsTitle+" foi encontrada, handle: "+hWnd);
        }

        checker = hWnd != null;
        System.out.println(checker);
        }
        

    public interface User32 extends StdCallLibrary{
        User32 INSTANCE = Native.loadLibrary("user32", User32.class);

        Pointer FindWindowA(String lpClass, String lpWindowName);

    }

}