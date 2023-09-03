package com.Application;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;



public class Main {
    static String CsTitle = "Counter-Strike: Global Offensive - Direct3D 9";
    // Colors to the console
    public static final String ANSI_RESET = "\u001B[0m";
    static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) throws IOException {
        boolean checker;

        Pointer hWnd = User32.INSTANCE.FindWindowA(null, CsTitle);
        if(hWnd == null){
            System.out.println("A janela: "+CsTitle+" não foi encontrada.");
        } else {
            System.out.println("A janela: "+CsTitle+" foi encontrada, handle: "+hWnd);
        }

        checker = hWnd != null;
        System.out.println(checker);

        try {
            // create a connection with the CS:GO telnet and this program per localhost:2121
            Socket socket = new Socket("localhost", 2121);

            // Create a text buffer with the stream of the cs:go console
            // the organization is:
            // Text ( BufferedReader ) < Chars ( InputStreamReader ) < bytes ( getInputStream )
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            // a infinity loop to read the console
            // if line is not null continue
            while ((line = in.readLine()) != null) {
                if (line.contains("SDR server steamid:") && line.contains("vport") && line.contains("] connected")) {
                    System.out.println("A MATCH HAS BEEN FOUNDED");
                }
            }

            // close socket on not have lines to read
            socket.close();
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                System.out.println(ANSI_YELLOW + "Adicione a opção \"-netconport 2121\" nas opções de inicialização do CS:GO" + ANSI_RESET);
                return;
            }
            throw e;
        }
    }
        

    public interface User32 extends StdCallLibrary{
        User32 INSTANCE = Native.loadLibrary("user32", User32.class);

        Pointer FindWindowA(String lpClass, String lpWindowName);

    }

}