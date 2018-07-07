package jp.asmnoak.mpdplay;

import android.os.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static jp.asmnoak.mpdplay.MainActivity.command;
import static jp.asmnoak.mpdplay.MainActivity.ipaddr;
import static jp.asmnoak.mpdplay.MainActivity.mpdport;
import static jp.asmnoak.mpdplay.MainActivity.rcvdata;

public class ComThread extends Thread {
    private Socket s;
    private Handler handler;
    public Handler revHandler;
    BufferedReader br = null;
    PrintWriter wr = null;
    @Override
    public void run() {
        try {
            s = new Socket(ipaddr, mpdport);
            br = new BufferedReader(new InputStreamReader(
                    s.getInputStream()));
            wr = new PrintWriter(
                    s.getOutputStream(), true);
            String rcv = br.readLine();  // OK MPD
            rcv = "";
            wr.println(command);
            String st = "";
            while ((rcv = br.readLine()) != null) {
                if (rcv.equals("OK")) break;
                st = st + rcv + ";";
            }
            rcvdata = st;
        } catch (SocketTimeoutException e) {
            System.out.println("TIME OUT！！");
        } catch (SocketException e) {
            System.out.println("通信エラー！！");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        try {
            if (s!=null) s.close();
            if (br!=null) br.close();
            if (wr!=null) wr.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
