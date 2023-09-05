package com.Application;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static java.lang.Thread.sleep;


public class Main {
    static String CsTitle = "Counter-Strike: Global Offensive - Direct3D 9";
    // Colors to the console
    public static final String ANSI_RESET = "\u001B[0m";
    static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) throws IOException, AWTException, InterruptedException {
        boolean checker;

        //setFocusToWindowsApp(CsTitle, 0);

        Robot robot = new Robot();

        WinDef.HWND hWnd = User32.INSTANCE.FindWindowA(null, CsTitle);
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
            // aa infinity loop to read the console
            // if line is not null continue
            while ((line = in.readLine()) != null) {
                if (line.contains("SDR server steamid:") && line.contains("vport") && line.contains("] connected")) {
                    System.out.println("A MATCH HAS BEEN FOUNDED");

                    //The program have to check what is the window in foreground. 05/09/23 {

                    //String windowBefore = User32.INSTANCE.FindWindow(null, );

                    //When the match is found, we have to check the coordinates and the size of the window.


                    Rectangle window = new Rectangle(getWindowLocationAndSize(hWnd));
                    int X = window.x;
                    int Y = window.y;
                    int heigth = window.height;
                    int width = window.width;

                    if(X <= 0 || Y<=0){ //Estava retornando -32000 em X e Y quando eu rodava e a janela
                        X = 0;          //se encontrava proxima ao canto.
                        Y = 0;
                    }

                    //So now, the robot can make actions to accept the match.
                    //setFocusToWindowsApp(CsTitle, 0);
                        {sleep(200);}
                    robot.mouseMove(X+(width/2), Y+(heigth/2));
                        {sleep(200);}
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        {sleep(200);}
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        {sleep(200);}

                    //Back to the window the user were before.




                    // }05/09/23
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

    public static Rectangle getWindowLocationAndSize(final WinDef.HWND hwnd) {
        final WinDef.RECT lpRect = new WinDef.RECT();
        if (!User32.INSTANCE.GetWindowRect(hwnd, lpRect))
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        return new Rectangle(lpRect.left, lpRect.top, Math.abs(lpRect.right
                - lpRect.left), Math.abs(lpRect.bottom - lpRect.top));
    }

    // }05/09/23

    public interface User32 extends StdCallLibrary{
        User32 INSTANCE = Native.loadLibrary("user32", User32.class);


        WinDef.HWND FindWindowA(String lpClass, String lpWindowName);


        boolean GetWindowRect(WinDef.HWND hwnd, WinDef.RECT lpRect);
    }

}