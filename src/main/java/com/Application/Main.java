package com.Application;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.win32.W32APIOptions;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static java.lang.Thread.sleep;


public class Main {
    static String CsTitle = "Counter-Strike: Global Offensive - Direct3D 9";
    //static String CsTitle = "Bit Heroes";
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

                    //The program have to check what is the window in foreground.

                    WinDef.HWND windowBefore = User32.INSTANCE.GetForegroundWindow();

                    System.out.println("Tela anterior: "+windowBefore);

                    //When the match is found, we have to check the coordinates and the size of the window.

                    Rectangle window = WindowUtils.getWindowLocationAndSize(hWnd);
                    int X = window.x;
                    int Y = window.y;
                    int heigth = window.height;
                    int width = window.width;

                    if(X <= 0){             //Estava retornando -32000 em X e Y quando a janela
                        X = 0;              //se encontrava proxima ao canto.
                    } else if ( Y <= 0 ){
                        Y = 0;
                    }
                    System.out.println("Window X: "+X+", Y: "+Y+", Heigth: "+heigth+", Width: "+width);

                    //if(hWnd != windowBefore){
                        User32Extended.INSTANCE.SetForegroundWindow(hWnd);
                        User32Extended.INSTANCE.ShowWindow(hWnd, User32.SW_RESTORE);
                        System.out.println("Setado: "+hWnd);
                            {Thread.sleep(500);}
                    /*} else {
                        System.out.println("Não setado: "+hWnd);
                    }*/

                    //So now, the robot can make actions to accept the match.

                        {Thread.sleep(500);}
                    robot.mouseMove(X+(width/2), Y+(9*(heigth/20)));
                    System.out.println("moved");
                        {Thread.sleep(200);}
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    System.out.println("pressed");
                        {Thread.sleep(200);}
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    System.out.println("released");
                        {Thread.sleep(500);}

                    //Back to the window the user were before.
/*
                    //ALT Pressing
                    WinDef.HWND htopnd = User32.INSTANCE.FindWindowA(null, null);
                    User32.INSTANCE.SendMessage(htopnd, User32.WM_KEYDOWN, new
                            WinDef.WPARAM(User32.VK_MENU), new WinDef.LPARAM(0));
                    System.out.println("ok + alt");

                    //TAB Pressing
                    User32.INSTANCE.SendMessage(htopnd, User32.WM_KEYDOWN, new
                            WinDef.WPARAM(User32.VK_TAB), new WinDef.LPARAM(0));
                    System.out.println("ok + tab");

                    //ALT Releasing
                    User32.INSTANCE.SendMessage(htopnd, User32.WM_KEYDOWN, new
                            WinDef.WPARAM(User32.VK_MENU), new WinDef.LPARAM(0));
                    System.out.println("ok - alt");

                    //TAB Releasing
                    User32.INSTANCE.SendMessage(htopnd, User32.WM_KEYDOWN, new
                            WinDef.WPARAM(User32.VK_TAB), new WinDef.LPARAM(0));
                    System.out.println("ok - tab");
 */
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

        WinDef.HWND FindWindowA(String lpClass, String lpWindowName);

        boolean GetWindowRect(WinDef.HWND hwnd, WinDef.RECT lpRect);

        WinDef.HWND GetForegroundWindow();
        int SW_SHOWNORMAL = 1;
        int SW_SHOWMINIMIZED = 2;
        int SW_SHOWMAXIMIZED = 3;
        int SW_SHOW = 5;
        int SW_RESTORE = 9;

        WinDef.LRESULT SendMessage(WinDef.HWND hwnd, int Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);
        int WM_KEYDOWN = 0x0100;
        int VK_MENU = 0x12; //Alt
        int VK_TAB = 0x09; //Tab
    }
    
    public interface User32Extended extends User32{
        User32Extended INSTANCE = Native.load("user32", User32Extended.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SetForegroundWindow(WinDef.HWND hWnd);
        boolean ShowWindow(WinDef.HWND hWnd, int nCmdShow);
    }

}