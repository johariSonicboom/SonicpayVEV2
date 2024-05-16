package com.sonicboom.sonicpayvui;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.pax.gl.commhelper.IComm;
import com.pax.gl.commhelper.ICommTcpClient;
import com.pax.gl.commhelper.exception.CommException;
import com.pax.gl.commhelper.impl.GLCommDebug;
import com.pax.gl.commhelper.impl.PaxGLComm;
import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class GLTCPClient {
    private static final String TAG = "GLTCPClient";

    ICommTcpClient iComm;

    public GLTCPClient(Context context, String ip, int port){
        GLCommDebug.setDebugLevel(GLCommDebug.EDebugLevel.DEBUG_LEVEL_ALL);
        if (iComm==null) {
            iComm = PaxGLComm.getInstance(context).createTcpClient(ip, port);
            iComm.setRecvTimeout(Integer.parseInt(new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.tcp_controller_receive_timeout)).equals("") ? "5000" : new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.tcp_controller_receive_timeout))));
            iComm.setSendTimeout(Integer.parseInt(new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.tcp_controller_send_timeout)).equals("") ? "2000" : new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.tcp_controller_send_timeout))));
        }
    }

    public void SendMessage(String input){
        if (iComm!=null) {
            new Thread(() -> {
                try {
                    connect();
                    send(input);
                    String response = receive();
                    //Connection closed by peer
                    disconnect();
                } catch (Exception e) {
                    LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
                }
            }).start();
        }
    }

    public void connect(){
        try {
            if (iComm!=null) {
                LogUtils.d(TAG, "conn start");
                iComm.connect();
                LogUtils.d(TAG, "conn success");

            }
        } catch (CommException e) {
            LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
        }
    }

    public void send(String input){
        try {
            if (iComm!=null) {
                LogUtils.d(TAG, "conn status:" + (iComm.getConnectStatus() == IComm.EConnectStatus.CONNECTED));
                byte[] sendContent = input.getBytes();
                iComm.send(sendContent);
                LogUtils.i(TAG, "send success:" + new String(sendContent, StandardCharsets.UTF_8));
            }
        } catch (CommException e) {
            LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
        }
    }

    public String receive() {
        String receiveContent = "";
        try {
            if (iComm!=null) {
                byte[] recvContent = iComm.recv(2048);
                receiveContent = new String(recvContent, StandardCharsets.UTF_8);
                LogUtils.i(TAG, "recv success:" + receiveContent);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
        }
        return receiveContent;
    }

    public void disconnect() {
        try {
            if (iComm!=null){
                iComm.disconnect();
                LogUtils.d(TAG,"disconnect success");
            }
        } catch (CommException e) {
            LogUtils.e(TAG,"Exception: " + Log.getStackTraceString(e));
        }
    }
}
