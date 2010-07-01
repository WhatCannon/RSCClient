package client;

import client.IO.Packet;
import client.IO.PacketBuilder;
import client.util.Config;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;

public abstract class GameWindowMiddleMan extends GameWindow {

    private long lastPing = System.currentTimeMillis();

    protected final void login(String user, String pass, boolean reconnecting) {
        loginScreenPrint("Please wait...", "Connecting to server");
        if ((user.trim().length() < 4) || (pass.trim().length() < 4)) {
            loginScreenPrint("You must enter both a username", "and a password - Please try again");
            return;
        }
        try {
            streamClass = new StreamClass(new Socket(Config.SERVER_IP, Config.SERVER_PORT), this);
            PacketBuilder currentPacket = new PacketBuilder(0);
            currentPacket.writeInt(45);
            currentPacket.writeString(user);
            currentPacket.writeString(pass);
            streamClass.writePacket(currentPacket.toByteArray());
            int loginResponse = streamClass.readInt();
            System.out.println("Login Response:" + loginResponse);
            // -1 = Couldn't connect to server.
            // 0 = Login was valid.
            // 1 = Reconnected.
            // 2 = Login was invalid.
            // 3 = Account was already logged in.
            // 4 = Client outdated.
            // 5 = Server rejected connection.
            // 6 = Account disabled.
            // 7 = Server failed to decode profile.
            // 8 = IP in use.
            // 9 = IP in use.
            // 10 = Server was full.
            // 99 = Mod/admin valid login.
            switch (loginResponse) {
                case -1:
                    loginScreenPrint("Error unable to login.", "Server timed out");
                    break;
                case 0:
                    resetVars();
                    break;
                case 1:
                    break;
                case 2:
                    loginScreenPrint("Invalid username or password.", "Try again, or create a new account");
                    break;
                case 3:
                    loginScreenPrint("That username is already logged in.", "Wait 60 seconds then retry");
                    break;
                case 4:
                    loginScreenPrint("The client has been updated.", "Please download the newest one");
                    break;
                case 5:
                    loginScreenPrint("Error unable to login.", "Server rejected session");
                    break;
                case 6:
                    loginScreenPrint("Account disabled.", "Contact an admin for details");
                    break;
                case 7:
                    loginScreenPrint("Error - failed to decode profile.", "Contact an admin");
                    break;
                case 8:
                    loginScreenPrint("IP Already in use.", "You may only login once at a time");
                    break;
                case 9:
                    loginScreenPrint("Account already in use.", "You may only login to one character at a time");
                    break;
                case 10:
                    loginScreenPrint("Server full!.", "Please try again later.");
                    break;
                case 99:
                    resetVars();
                    break;
                default:
                    loginScreenPrint("Error unable to login.", "Unrecognised response code");
                    break;
            }
        } catch (Exception exception) {
            System.out.println(String.valueOf(exception));
        }
    }

    protected final void sendLogoutPacket() {
        if (streamClass != null) {
            PacketBuilder currentPacket = new PacketBuilder(39);
            streamClass.writePacket(currentPacket.toByteArray());
        }
        resetIntVars();
    }

    protected void lostConnection() {
//        System.out.println("Lost connection");
//        reconnectTries = 10;
//        login(username, password, true);
    }

    protected final void gameBoxPrint(String s, String s1) {
        Graphics g = getGraphics();
        Font font = new Font("Helvetica", 1, 15);
        char c = '\u0200';
        char c1 = '\u0158';
        g.setColor(Color.black);
        g.fillRect(c / 2 - 140, c1 / 2 - 25, 280, 50);
        g.setColor(Color.white);
        g.drawRect(c / 2 - 140, c1 / 2 - 25, 280, 50);
        drawString(g, s, font, c / 2, c1 / 2 - 10);
        drawString(g, s1, font, c / 2, c1 / 2 + 10);
    }

    protected final void readPacket() {
        try {
            if ((System.currentTimeMillis() - lastPing) >= 5000) {
                streamClass.writePacket(new PacketBuilder(5).toByteArray());
                lastPing = System.currentTimeMillis();
            }
            Packet packetToHandle = streamClass.readPacket();
            if (packetToHandle != null) {
                checkIncomingPacket(packetToHandle);
            }
        } catch (IOException IOe) {
            IOe.printStackTrace();
        }
    }

    protected final void checkIncomingPacket(Packet packetToHandle) {
        switch (packetToHandle.getPacketHeader()) {
            case 2:
                int ignoreListCount = packetToHandle.getInt();
                ignoreList.clear();
                for (int currentEntry = 0; currentEntry < ignoreListCount; currentEntry++) {
                    ignoreList.add(packetToHandle.getString());
                }
                Collections.sort(ignoreList, String.CASE_INSENSITIVE_ORDER);
                break;
            case 25:
                // TODO Friend login/logout update status.
                break;
            case 48:
                handleServerMessage(packetToHandle.getString());
                break;
            case 136:
                cantLogout();
                break;
            case 158:
                blockChatMessages = packetToHandle.getBoolean();
                blockPrivateMessages = packetToHandle.getBoolean();
                blockTradeRequests = packetToHandle.getBoolean();
                blockDuelRequests = packetToHandle.getBoolean();
                break;
            case 170:
                handleServerMessage("@pri@" + packetToHandle.getString() + " tells you: " + packetToHandle.getString());
                break;
            case 222:
                sendLogoutPacket();
                break;
            case 249:
                int friendListCount = packetToHandle.getShort();
                friendList.clear();
                for (int currentEntry = 0; currentEntry < friendListCount; currentEntry++) {
                    friendList.add(packetToHandle.getString());
                }
                Collections.sort(friendList, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                handleIncomingPacket(packetToHandle);
                break;
        }
    }

    protected final void sendUpdatedPrivacyInfo(boolean chatMessages, boolean privateMessages, boolean tradeRequests, boolean duelRequests) {
        PacketBuilder currentPacket = new PacketBuilder(176);
        currentPacket.writeBoolean(chatMessages);
        currentPacket.writeBoolean(privateMessages);
        currentPacket.writeBoolean(tradeRequests);
        currentPacket.writeBoolean(duelRequests);
        streamClass.writePacket(currentPacket.toByteArray());
    }

    protected final void addToIgnoreList(String nameToAdd) {
        if (!ignoreList.contains(nameToAdd)) {
            ignoreList.add(nameToAdd);
            Collections.sort(ignoreList, String.CASE_INSENSITIVE_ORDER);
            handleServerMessage("@pri@" + nameToAdd + " has been added to your ignore list.");
        }
        PacketBuilder currentPacket = new PacketBuilder(25);
        currentPacket.writeString(nameToAdd);
        streamClass.writePacket(currentPacket.toByteArray());
    }

    protected final void removeFromIgnoreList(String nameToRemove) {
        if (ignoreList.contains(nameToRemove)) {
            ignoreList.remove(nameToRemove);
            Collections.sort(ignoreList, String.CASE_INSENSITIVE_ORDER);
            handleServerMessage("@pri@" + nameToRemove + " has been removed from your ignore list.");
        }
        PacketBuilder currentPacket = new PacketBuilder(108);
        currentPacket.writeString(nameToRemove);
        streamClass.writePacket(currentPacket.toByteArray());
    }

    protected final void addToFriendsList(String nameToAdd) {
        if (!friendList.contains(nameToAdd)) {
            friendList.add(nameToAdd);
            Collections.sort(friendList, String.CASE_INSENSITIVE_ORDER);
            handleServerMessage("@pri@" + nameToAdd + " has been added to your friends list.");
        }
        PacketBuilder currentPacket = new PacketBuilder(168);
        currentPacket.writeString(nameToAdd);
        streamClass.writePacket(currentPacket.toByteArray());
    }

    protected final void removeFromFriends(String nameToRemove) {
        if (friendList.contains(nameToRemove)) {
            friendList.remove(nameToRemove);
            Collections.sort(friendList, String.CASE_INSENSITIVE_ORDER);
            handleServerMessage("@pri@" + nameToRemove + " has been removed from your friends list.");
        }
        PacketBuilder currentPacket = new PacketBuilder(52);
        currentPacket.writeString(nameToRemove);
        streamClass.writePacket(currentPacket.toByteArray());
    }

    protected final void sendPrivateMessage(String recipientOfMessage, String chatMessage) {
        PacketBuilder currentPacket = new PacketBuilder(254);
        currentPacket.writeString(recipientOfMessage);
        currentPacket.writeString(chatMessage);
        streamClass.writePacket(currentPacket.toByteArray());
    }

    protected final void sendChatMessage(String message) {
        PacketBuilder currentPacket = new PacketBuilder(145);
        currentPacket.writeShort(message.length());
        currentPacket.writeString(message);
        streamClass.writePacket(currentPacket.toByteArray());
    }

    protected final void sendChatString(String message) {
        PacketBuilder currentPacket = new PacketBuilder(90);
        currentPacket.writeShort(message.length());
        currentPacket.writeString(message);
        streamClass.writePacket(currentPacket.toByteArray());
    }

    protected abstract void loginScreenPrint(String s, String s1);

    protected abstract void resetVars();

    protected abstract void resetIntVars();

    protected abstract void cantLogout();

    protected abstract void handleIncomingPacket(Packet packetToHandle);

    protected abstract void handleServerMessage(String s);
    public static int clientVersion = 1;
    protected StreamClass streamClass;
    public LinkedList<String> friendList = new LinkedList<String>();
    public LinkedList<String> ignoreList = new LinkedList<String>();
    public boolean blockChatMessages;
    public boolean blockPrivateMessages;
    public boolean blockTradeRequests;
    public boolean blockDuelRequests;
    public int socketTimeout;
}
