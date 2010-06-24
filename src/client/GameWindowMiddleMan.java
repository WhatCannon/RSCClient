package client;

import client.util.Config;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;

public abstract class GameWindowMiddleMan extends GameWindow {

    private long lastPing = System.currentTimeMillis();

    protected final void login(String user, String pass, boolean reconnecting) {
        loginScreenPrint("Please wait...", "Connecting to server");
        if ((user.trim().length() < 4) || (pass.trim().length() < 4)) {
            loginScreenPrint("You must enter both a username", "and a password - Please try again");
            return;
        }
        try {
            username = user;
            user = DataOperations.addCharacters(user, 20);
            password = pass;
            pass = DataOperations.addCharacters(pass, 20);
            streamClass = new StreamClass(new Socket(Config.SERVER_IP, Config.SERVER_PORT), this);
            PacketBuilder currentPacket = new PacketBuilder(0);
            if (reconnecting) {
                currentPacket.writeByte(1);
            } else {
                currentPacket.writeByte(0);
            }
            currentPacket.writeShort(clientVersion);
            currentPacket.writeString(user);
            currentPacket.writeString(pass);
            streamClass.writePacket(currentPacket.toByteArray());
            int loginResponse = streamClass.readByte();
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
                    reconnectTries = 0;
                    resetVars();
                    break;
                case 1:
                    reconnectTries = 0;
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
                    reconnectTries = 0;
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
        username = "";
        password = "";
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
            if((System.currentTimeMillis() - lastPing) >= 5000) {
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
        switch (packetToHandle.getHeader()) {
            case 2:
                ignoreListCount = PacketOperations.getByte(packetToHandle.getPacketData(), 0);
                for (int currentEntry = 0; currentEntry < ignoreListCount; currentEntry++) {
                    ignoreList[currentEntry] = PacketOperations.getString(packetToHandle.getPacketData(), (currentEntry * 20), 20);
                }
                break;
            case 25:
                long friend = DataOperations.getUnsignedLong(packetData, 1);
                int status = packetData[9] & 0xff;
                for (int i2 = 0; i2 < friendsCount; i2++) {
                    if (friendsListLongs[i2] == friend) {
                        if (friendsListOnlineStatus[i2] == 0 && status != 0) {
                            handleServerMessage("@pri@" + DataOperations.longToString(friend) + " has logged in");
                        }
                        if (friendsListOnlineStatus[i2] != 0 && status == 0) {
                            handleServerMessage("@pri@" + DataOperations.longToString(friend) + " has logged out");
                        }
                        friendsListOnlineStatus[i2] = status;
                        friendsListLongs[friendsCount] = friend;
                        friendsListOnlineStatus[friendsCount] = status;
                        friendsCount++;
                        reOrderFriendsListByOnlineStatus();
                        return;
                    }
                }
                break;
            case 48:
                handleServerMessage(PacketOperations.getString(packetToHandle.getPacketData(), 0, packetToHandle.getPacketData().length));
                break;
            case 136:
                cantLogout();
                break;
            case 158:
                blockChatMessages = packetData[1];
                blockPrivateMessages = packetData[2];
                blockTradeRequests = packetData[3];
                blockDuelRequests = packetData[4];
                break;
            case 170:
                String user = PacketOperations.getString(packetToHandle.getPacketData(), 0, 20).trim();
                String message = PacketOperations.getString(packetToHandle.getPacketData(), 20, 30).trim();
                handleServerMessage("@pri@" + user + " tells you: " + message);
                break;
            case 222:
                sendLogoutPacket();
                break;
            case 249:
                friendsCount = DataOperations.getUnsignedByte(packetData[1]);
                for (int k = 0; k < friendsCount; k++) {
                    friendsListLongs[k] = DataOperations.getUnsignedLong(packetData, 2 + k * 9);
                    friendsListOnlineStatus[k] = DataOperations.getUnsignedByte(packetData[10 + k * 9]);
                }

                reOrderFriendsListByOnlineStatus();
                break;
            default:
                handleIncomingPacket(packetToHandle);
                break;
        }
    }

    private final void reOrderFriendsListByOnlineStatus() {
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < friendsCount - 1; i++) {
                if (friendsListOnlineStatus[i] < friendsListOnlineStatus[i + 1]) {
                    int j = friendsListOnlineStatus[i];
                    friendsListOnlineStatus[i] = friendsListOnlineStatus[i + 1];
                    friendsListOnlineStatus[i + 1] = j;
                    long l = friendsListLongs[i];
                    friendsListLongs[i] = friendsListLongs[i + 1];
                    friendsListLongs[i + 1] = l;
                    flag = true;
                }
            }

        }
    }

    protected final void sendUpdatedPrivacyInfo(int chatMessages, int privateMessages, int tradeRequests, int duelRequests) {
        PacketBuilder currentPacket = new PacketBuilder(176);
        currentPacket.writeByte(chatMessages);
        currentPacket.writeByte(privateMessages);
        currentPacket.writeByte(tradeRequests);
        currentPacket.writeByte(duelRequests);
        streamClass.writePacket(currentPacket.toByteArray());
    }

    protected final void addToIgnoreList(String name) {
        PacketBuilder currentPacket = new PacketBuilder(25);
        currentPacket.writeString(name);
        streamClass.writePacket(currentPacket.toByteArray());
        for (int i = 0; i < ignoreListCount; i++) {
            if (ignoreList[i].equals(name)) {
                return;
            }
        }

        if (ignoreListCount >= ignoreList.length - 1) {
            return;
        } else {
            ignoreList[ignoreListCount++] = name;
            return;
        }
    }

    protected final void removeFromIgnoreList(String name) {
        PacketBuilder currentPacket = new PacketBuilder(108);
        currentPacket.writeString(name);
        streamClass.writePacket(currentPacket.toByteArray());
        for (int i = 0; i < ignoreListCount; i++) {
            if (ignoreList[i].equals(name)) {
                ignoreListCount--;
                for (int j = i; j < ignoreListCount; j++) {
                    ignoreList[j] = ignoreList[j + 1];
                }

                return;
            }
        }
    }

    protected final void addToFriendsList(String name) {
        PacketBuilder currentPacket = new PacketBuilder(168);
        currentPacket.writeLong(DataOperations.stringLength12ToLong(name));
        streamClass.writePacket(currentPacket.toByteArray());
        long l = DataOperations.stringLength12ToLong(name);
        for (int i = 0; i < friendsCount; i++) {
            if (friendsListLongs[i] == l) {
                return;
            }
        }

        if (friendsCount >= friendsListLongs.length - 1) {
            return;
        } else {
            friendsListLongs[friendsCount] = l;
            friendsListOnlineStatus[friendsCount] = 0;
            friendsCount++;
            return;
        }
    }

    protected final void removeFromFriends(long l) {
        PacketBuilder currentPacket = new PacketBuilder(52);
        currentPacket.writeLong(l);
        streamClass.writePacket(currentPacket.toByteArray());
        for (int i = 0; i < friendsCount; i++) {
            if (friendsListLongs[i] != l) {
                continue;
            }
            friendsCount--;
            for (int j = i; j < friendsCount; j++) {
                friendsListLongs[j] = friendsListLongs[j + 1];
                friendsListOnlineStatus[j] = friendsListOnlineStatus[j + 1];
            }

            break;
        }

        handleServerMessage("@pri@" + DataOperations.longToString(l) + " has been removed from your friends list");
    }

    protected final void sendPrivateMessage(long user, byte message[], int messageLength) {
        PacketBuilder currentPacket = new PacketBuilder(254);
        currentPacket.writeLong(user);
        currentPacket.writeBytes(message, 0, messageLength);
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

    public GameWindowMiddleMan() {
        username = "";
        password = "";
        packetData = new byte[5000];
        friendsListLongs = new long[400];
        friendsListOnlineStatus = new int[400];
        ignoreList = new String[200];
    }
    public static int clientVersion = 1;
    public static int maxPacketReadCount;
    String username;
    String password;
    protected StreamClass streamClass;
    protected byte[] packetData;
    int reconnectTries;
    public int friendsCount;
    public long[] friendsListLongs;
    public int[] friendsListOnlineStatus;
    public int ignoreListCount;
    public String[] ignoreList;
    public int blockChatMessages;
    public int blockPrivateMessages;
    public int blockTradeRequests;
    public int blockDuelRequests;
    public int socketTimeout;
}
