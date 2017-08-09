/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.creatoro.goals.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.creatoro.goals.AppContext;
import com.creatoro.goals.commands.Command;
import com.creatoro.goals.commands.CommandParser;
import com.creatoro.goals.commands.CommandRunner;
import com.creatoro.goals.preferences.Preferences;
import com.creatoro.goals.utils.SSLUtils;

import com.creatoro.goals.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;

import javax.inject.Inject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.SocketIOException;

import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_CONNECTING;
import static io.socket.client.Socket.EVENT_CONNECT_ERROR;
import static io.socket.client.Socket.EVENT_CONNECT_TIMEOUT;
import static io.socket.client.Socket.EVENT_DISCONNECT;
import static io.socket.client.Socket.EVENT_PING;
import static io.socket.client.Socket.EVENT_PONG;
import static io.socket.client.Socket.EVENT_RECONNECT;
import static io.socket.client.Socket.EVENT_RECONNECT_ATTEMPT;
import static io.socket.client.Socket.EVENT_RECONNECT_ERROR;
import static io.socket.client.Socket.EVENT_RECONNECT_FAILED;
import static io.socket.client.Socket.Listener;

@com.creatoro.goals.AppScope
public class SyncManager implements CommandRunner.Listener
{
    public static final String EVENT_AUTH = "auth";

    public static final String EVENT_AUTH_OK = "authOK";

    public static final String EVENT_EXECUTE_EVENT = "execute";

    public static final String EVENT_FETCH = "fetch";

    public static final String EVENT_FETCH_OK = "fetchOK";

    public static final String EVENT_POST_EVENT = "postEvent";

    @NonNull
    private String clientId;

    @NonNull
    private String groupKey;

    @NonNull
    private Socket socket;

    @NonNull
    private LinkedList<Event> pendingConfirmation;

    @NonNull
    private LinkedList<Event> pendingEmit;

    private boolean readyToEmit = false;

    @NonNull
    private final Preferences prefs;

    @NonNull
    private CommandRunner commandRunner;

    @NonNull
    private CommandParser commandParser;

    private boolean isListening;

    @Inject
    public SyncManager(@AppContext @NonNull Context context,
                       @NonNull Preferences prefs,
                       @NonNull CommandRunner commandRunner,
                       @NonNull CommandParser commandParser)
    {
        Log.i("SyncManager", this.toString());

        this.prefs = prefs;
        this.commandRunner = commandRunner;
        this.commandParser = commandParser;
        this.isListening = false;

        pendingConfirmation = new LinkedList<>();
        pendingEmit = new LinkedList<>(Event.getAll());

        groupKey = prefs.getSyncKey();
        clientId = prefs.getSyncClientId();
        String serverURL = prefs.getSyncAddress();

        Log.d("SyncManager", clientId);
        connect(context, serverURL);
    }

    @Override
    public void onCommandExecuted(@NonNull Command command,
                                  @Nullable Long refreshKey)
    {
        if (command.isRemote()) return;

        JSONObject msg = command.toJson();
        Long now = new Date().getTime();
        Event e = new Event(command.getId(), now, msg.toString());
        e.save();

        Log.i("SyncManager", "Adding to outbox: " + msg.toString());

        pendingEmit.add(e);
        if (readyToEmit) emitPending();
    }

    public void onNetworkStatusChanged(boolean isConnected)
    {
        if(!isListening) return;
        if(isConnected) socket.connect();
        else socket.disconnect();
    }

    public void startListening()
    {
        if (!prefs.isSyncFeatureEnabled()) return;
        if (groupKey.isEmpty()) return;
        if (isListening) return;

        isListening = true;
        socket.connect();
        commandRunner.addListener(this);
    }

    public void stopListening()
    {
        if(!isListening) return;

        commandRunner.removeListener(this);
        socket.close();
        isListening = false;
    }

    private void connect(@AppContext @NonNull Context context, String serverURL)
    {
        try
        {
            IO.setDefaultSSLContext(SSLUtils.getCACertSSLContext(context));
            socket = IO.socket(serverURL);

            logSocketEvent(socket, EVENT_CONNECT, "Connected");
            logSocketEvent(socket, EVENT_CONNECT_TIMEOUT, "Connect timeout");
            logSocketEvent(socket, EVENT_CONNECTING, "Connecting...");
            logSocketEvent(socket, EVENT_CONNECT_ERROR, "Connect error");
            logSocketEvent(socket, EVENT_DISCONNECT, "Disconnected");
            logSocketEvent(socket, EVENT_RECONNECT, "Reconnected");
            logSocketEvent(socket, EVENT_RECONNECT_ATTEMPT, "Reconnecting...");
            logSocketEvent(socket, EVENT_RECONNECT_ERROR, "Reconnect error");
            logSocketEvent(socket, EVENT_RECONNECT_FAILED, "Reconnect failed");
            logSocketEvent(socket, EVENT_DISCONNECT, "Disconnected");
            logSocketEvent(socket, EVENT_PING, "Ping");
            logSocketEvent(socket, EVENT_PONG, "Pong");

            socket.on(EVENT_CONNECT, new OnConnectListener());
            socket.on(EVENT_DISCONNECT, new OnDisconnectListener());
            socket.on(EVENT_EXECUTE_EVENT, new OnExecuteCommandListener());
            socket.on(EVENT_AUTH_OK, new OnAuthOKListener());
            socket.on(EVENT_FETCH_OK, new OnFetchOKListener());
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void emitPending()
    {
        try
        {
            for (Event e : pendingEmit)
            {
                Log.i("SyncManager", "Emitting: " + e.message);
                socket.emit(EVENT_POST_EVENT, new JSONObject(e.message));
                pendingConfirmation.add(e);
            }

            pendingEmit.clear();
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void logSocketEvent(Socket socket, String event, final String msg)
    {
        socket.on(event, args ->
        {
            Log.i("SyncManager", msg);
            for (Object o : args)
                if (o instanceof SocketIOException)
                    ((SocketIOException) o).printStackTrace();
        });
    }

    private void updateLastSync(Long timestamp)
    {
        prefs.setLastSync(timestamp + 1);
    }

    private class OnAuthOKListener implements Listener
    {
        @Override
        public void call(Object... args)
        {
            Log.i("SyncManager", "Auth OK");
            Log.i("SyncManager", "Requesting commands since last sync");

            Long lastSync = prefs.getLastSync();
            socket.emit(EVENT_FETCH, buildFetchMessage(lastSync));
        }

        private JSONObject buildFetchMessage(Long lastSync)
        {
            try
            {
                JSONObject json = new JSONObject();
                json.put("since", lastSync);
                return json;
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private class OnConnectListener implements Listener
    {
        @Override
        public void call(Object... args)
        {
            Log.i("SyncManager", "Sending auth message");
            socket.emit(EVENT_AUTH, buildAuthMessage());
        }

        private JSONObject buildAuthMessage()
        {
            try
            {
                JSONObject json = new JSONObject();
                json.put("groupKey", groupKey);
                json.put("clientId", clientId);
                json.put("version", BuildConfig.VERSION_NAME);
                return json;
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private class OnDisconnectListener implements Listener
    {
        @Override
        public void call(Object... args)
        {
            readyToEmit = false;
            for(Event e : pendingConfirmation) pendingEmit.add(e);
            pendingConfirmation.clear();
        }
    }

    private class OnExecuteCommandListener implements Listener
    {
        @Override
        public void call(Object... args)
        {
            try
            {
                Log.d("SyncManager",
                    String.format("Received command: %s", args[0].toString()));
                JSONObject root = new JSONObject(args[0].toString());
                updateLastSync(root.getLong("timestamp"));
                executeCommand(root);
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }

        private void executeCommand(JSONObject root) throws JSONException
        {
            Command received = commandParser.parse(root);
            received.setRemote(true);

            for (Event e : pendingConfirmation)
            {
                if (e.serverId.equals(received.getId()))
                {
                    Log.i("SyncManager", "Pending command confirmed");
                    pendingConfirmation.remove(e);
                    e.delete();
                    return;
                }
            }

            Log.d("SyncManager", "Executing received command");
            commandRunner.execute(received, null);
        }
    }

    private class OnFetchOKListener implements Listener
    {
        @Override
        public void call(Object... args)
        {
            try
            {
                Log.i("SyncManager", "Fetch OK");

                JSONObject json = (JSONObject) args[0];
                updateLastSync(json.getLong("timestamp"));

                emitPending();
                readyToEmit = true;
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
