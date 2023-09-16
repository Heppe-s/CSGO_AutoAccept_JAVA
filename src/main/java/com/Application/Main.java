package com.Application;

import com.sun.jna.Native;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


public class Main {
    static String CsTitle = "Counter-Strike: Global Offensive - Direct3D 9";
    // Colors to the console
    public static final String ANSI_RESET = "\u001B[0m";
    static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) throws IOException, AWTException{
        boolean checker;

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

                    //Taking the screen size
                    Toolkit tk = Toolkit.getDefaultToolkit();
                    Dimension screenSize = tk.getScreenSize();

                    //The program have to check what is the window in foreground.
                    WinDef.HWND windowBefore = User32.INSTANCE.GetForegroundWindow();
                    System.out.println("Tela anterior: "+windowBefore);

                    //When the match is found, we have to check the coordinates and the size of the window.
                    Rectangle window = WindowUtils.getWindowLocationAndSize(hWnd);
                    int X = window.x;
                    int Y = window.y;
                    int height = window.height;
                    int width = window.width;

                    if(X <= 0){             //Estava retornando -32000 em X e Y quando a janela
                        X = 0;              //se encontrava proxima ao canto.
                    }
                    if ( Y <= 0 ){
                        Y = 0;
                    }

/*
                    WinDef.RECT acceptRect = new WinDef.RECT();
                    acceptRect.top = mousePosY;
                    acceptRect.bottom = 50;
                    acceptRect.left = mousePosX;
                    acceptRect.right = 50;

                    WinDef.RECT windowRect = new WinDef.RECT();
                    windowRect.top = 0;
                    windowRect.bottom = screenSize.height;
                    windowRect.left = 0;
                    windowRect.right = screenSize.width;
*/

                    //So now, the robot can make actions to accept the match.
                    //The program needs to check if it's fullscreen or not to do the right
                    //sequence of actions.
                    //To check if it's window or fullscreen:
                    if(hWnd != windowBefore){    //If statement not working.

                        User32Extended.INSTANCE.ShowWindow(windowBefore, User32.SW_SHOWMINIMIZED);
                        User32Extended.INSTANCE.ShowWindow(hWnd, User32.SW_RESTORE);
                        User32Extended.INSTANCE.ShowWindow(hWnd, User32.SW_SHOW);
                        User32Extended.INSTANCE.SetForegroundWindow(hWnd);

                        System.out.println("Setado: "+hWnd);

                        if(height<=200 && width<=200){
                            robotActs(X, Y, screenSize.width, screenSize.height);
                        } else {
                            robotActs(X, Y, width, height);
                        }
                        //Back to the window the user were before.
                        alt_tab();
                    } else {
                        System.out.println("Não setado: "+windowBefore);

                        if(height<=200 && width<=200){
                            robotActs(X, Y, screenSize.width, screenSize.height);
                        } else {
                            robotActs(X, Y, width, height);
                        }
                    }
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

    public static void alt_tab() {
        Robot robot;
        try {
            robot = new Robot();
            robot.delay(800);
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
            robot.delay(500);
            robot.keyRelease(KeyEvent.VK_ALT);
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void robotActs(int paramX, int paramY, int width, int height) throws AWTException {
        Robot robot = new Robot();

        int calcX = paramX+(width/2);
        int calcY = paramY+(9*(height/20));

        robot.delay(600);
        robot.mouseMove(calcX, calcY);
        //User32.INSTANCE.ClipCursor(acceptRect);
        System.out.println("moved to: X = "+calcX+", Y = "+calcY);
        System.out.println("X = "+paramX+", Y = "+paramY);
        robot.delay(1000);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        System.out.println("pressed");
        robot.delay(100);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        System.out.println("released");
        robot.delay(100);
        //User32.INSTANCE.ClipCursor(windowRect);
    }

    public interface User32 extends StdCallLibrary{
        User32 INSTANCE = Native.loadLibrary("user32", User32.class);
        WinDef.HWND FindWindowA(String lpClass, String lpWindowName);
        boolean GetWindowRect(WinDef.HWND hwnd, WinDef.RECT lpRect);
        WinDef.HWND GetForegroundWindow();
        boolean ClipCursor(WinDef.RECT lpRect);
        int SW_SHOWMINIMIZED = 2;
        int SW_SHOW = 5;
        int SW_RESTORE = 9;
    }
    
    public interface User32Extended extends User32{
        User32Extended INSTANCE = Native.load("user32", User32Extended.class, W32APIOptions.DEFAULT_OPTIONS);
        boolean SetForegroundWindow(WinDef.HWND hWnd);
        boolean ShowWindow(WinDef.HWND hWnd, int nCmdShow);
    }
}