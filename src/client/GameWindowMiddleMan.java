package client;

import client.util.Config;
import client.util.DataConversions;

import java.awt.*;
import java.io.IOException;
import java.math.BigInteger;

public abstract class GameWindowMiddleMan extends GameWindow {

    protected final void login(String user, String pass, boolean reconnecting) {
        if (socketTimeout > 0) {
            loginScreenPrint("Please wait...", "Connecting to server");
            try {
                Thread.sleep(2000L);
            } catch (Exception _ex) {
            }
            loginScreenPrint("Sorry! The server is currently full.", "Please try again later");
            return;
        }
        try {
            username = user;
            user = DataOperations.addCharacters(user, 20);
            password = pass;
            pass = DataOperations.addCharacters(pass, 20);
            if (user.trim().length() == 0) {
                loginScreenPrint("You must enter both a username", "and a password - Please try again");
                return;
            }
            if (reconnecting) {
                gameBoxPrint("Connection lost! Please wait...", "Attempting to re-establish");
            } else {
                loginScreenPrint("Please wait...", "Connecting to server");
            }
            streamClass = new StreamClass(makeSocket(Config.SERVER_IP, Config.SERVER_PORT), this);
            streamClass.maxPacketReadCount = maxPacketReadCount;
            streamClass.createPacket(0);
            if (reconnecting) {
                streamClass.writeByte(1);
            } else {
                streamClass.writeByte(0);
            }
            streamClass.writeShort(clientVersion);
            streamClass.writeString(user);
            streamClass.writeString(pass);
            streamClass.finalisePacket();
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
            if (reconnecting) {
                user = "";
                pass = "";
                resetIntVars();
            }
            return;
        } catch (Exception exception) {
            System.out.println(String.valueOf(exception));
        }
        if (reconnectTries > 0) {
            try {
                Thread.sleep(5000L);
            } catch (Exception _ex) {
            }
            reconnectTries--;
            login(username, password, reconnecting);
        }
        if (reconnecting) {
            username = "";
            password = "";
            resetIntVars();
        } else {
            loginScreenPrint("Sorry! Unable to connect.", "Check internet settings or try another world");
        }
    }

    protected final void sendLogoutPacket() {
        if (streamClass != null) {
            try {
                streamClass.createPacket(39);
                streamClass.finalisePacket();
            } catch (IOException ioe) {
            }
        }
        username = "";
        password = "";
        resetIntVars();
    }

    protected void lostConnection() {
        System.out.println("Lost connection");
        reconnectTries = 10;
        login(username, password, true);
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

    protected final void sendPingPacketReadPacketData() {
        long l = System.currentTimeMillis();
        if (streamClass.containsData()) {
            lastPing = l;
        }
        if (l - lastPing > 5000L) {
            lastPing = l;
            streamClass.createPacket(5);
            streamClass.formatPacket();
        }
        try {
            streamClass.writePacket(20);
        } catch (IOException _ex) {
            lostConnection();
            return;
        }
        int packetLength = streamClass.readPacket(packetData);
        if (packetLength > 0) {
            checkIncomingPacket(packetData[0] & 0xff, packetLength);
        }
    }
    private long lastPacket = System.currentTimeMillis();

    protected final void checkIncomingPacket(int command, int length) {
        if (command == 48) {
            String s = new String(packetData, 1, length - 1);
            handleServerMessage(s);
        }
        if (command == 222) {
            sendLogoutPacket();
        }
        if (command == 136) {
            cantLogout();
            return;
        }
        if (command == 249) {
            friendsCount = DataOperations.getUnsignedByte(packetData[1]);
            for (int k = 0; k < friendsCount; k++) {
                friendsListLongs[k] = DataOperations.getUnsigned8Bytes(packetData, 2 + k * 9);
                friendsListOnlineStatus[k] = DataOperations.getUnsignedByte(packetData[10 + k * 9]);
            }

            reOrderFriendsListByOnlineStatus();
            return;
        }
        if (command == 25) {
            long friend = DataOperations.getUnsigned8Bytes(packetData, 1);
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
                    length = 0;
                    reOrderFriendsListByOnlineStatus();
                    return;
                }
            }

            friendsListLongs[friendsCount] = friend;
            friendsListOnlineStatus[friendsCount] = status;
            friendsCount++;
            reOrderFriendsListByOnlineStatus();
            return;
        }
        if (command == 2) {
            ignoreListCount = DataOperations.getUnsignedByte(packetData[1]);
            for (int i1 = 0; i1 < ignoreListCount; i1++) {
                ignoreListLongs[i1] = DataOperations.getUnsigned8Bytes(packetData, 2 + i1 * 8);
            }
            return;
        }
        if (command == 158) {
            blockChatMessages = packetData[1];
            blockPrivateMessages = packetData[2];
            blockTradeRequests = packetData[3];
            blockDuelRequests = packetData[4];
            return;
        }
        if (command == 170) {
            long user = DataOperations.getUnsigned8Bytes(packetData, 1);
            String s1 = DataConversions.byteToString(packetData, 9, length - 9);
            handleServerMessage("@pri@" + DataOperations.longToString(user) + " tells you: " + s1);
            return;
        } else {
            handleIncomingPacket(command, length, packetData);
            return;
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
        streamClass.createPacket(176);
        streamClass.writeByte(chatMessages);
        streamClass.writeByte(privateMessages);
        streamClass.writeByte(tradeRequests);
        streamClass.writeByte(duelRequests);
        streamClass.formatPacket();
    }

    protected final void addToIgnoreList(String s) {
        long l = DataOperations.stringLength12ToLong(s);
        streamClass.createPacket(25);
        streamClass.writeLong(l);
        streamClass.formatPacket();
        for (int i = 0; i < ignoreListCount; i++) {
            if (ignoreListLongs[i] == l) {
                return;
            }
        }

        if (ignoreListCount >= ignoreListLongs.length - 1) {
            return;
        } else {
            ignoreListLongs[ignoreListCount++] = l;
            return;
        }
    }

    protected final void removeFromIgnoreList(long l) {
        streamClass.createPacket(108);
        streamClass.writeLong(l);
        streamClass.formatPacket();
        for (int i = 0; i < ignoreListCount; i++) {
            if (ignoreListLongs[i] == l) {
                ignoreListCount--;
                for (int j = i; j < ignoreListCount; j++) {
                    ignoreListLongs[j] = ignoreListLongs[j + 1];
                }

                return;
            }
        }

    }

    protected final void addToFriendsList(String s) {
        streamClass.createPacket(168);
        streamClass.writeLong(DataOperations.stringLength12ToLong(s));
        streamClass.formatPacket();
        long l = DataOperations.stringLength12ToLong(s);
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
        streamClass.createPacket(52);
        streamClass.writeLong(l);
        streamClass.formatPacket();
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
        streamClass.createPacket(254);
        streamClass.writeLong(user);
        streamClass.writeBytes(message, 0, messageLength);
        streamClass.formatPacket();
    }

    protected final void sendChatMessage(byte abyte0[], int i) {
        streamClass.createPacket(145);
        streamClass.writeBytes(abyte0, 0, i);
        streamClass.formatPacket();
    }

    protected final void sendChatString(String s) {
        streamClass.createPacket(90);
        streamClass.writeString(s);
        streamClass.formatPacket();
    }

    protected abstract void loginScreenPrint(String s, String s1);

    protected abstract void resetVars();

    protected abstract void resetIntVars();

    protected abstract void cantLogout();

    protected abstract void handleIncomingPacket(int command, int length, byte[] abyte0);

    protected abstract void handleServerMessage(String s);

    public GameWindowMiddleMan() {
        username = "";
        password = "";
        packetData = new byte[5000];
        friendsListLongs = new long[400];
        friendsListOnlineStatus = new int[400];
        ignoreListLongs = new long[200];
    }
    public static int clientVersion = 1;
    public static int maxPacketReadCount;
    String username;
    String password;
    protected StreamClass streamClass;
    protected byte[] packetData;
    int reconnectTries;
    long lastPing;
    public int friendsCount;
    public long[] friendsListLongs;
    public int[] friendsListOnlineStatus;
    public int ignoreListCount;
    public long[] ignoreListLongs;
    public int blockChatMessages;
    public int blockPrivateMessages;
    public int blockTradeRequests;
    public int blockDuelRequests;
    private static BigInteger key = new BigInteger("1370158896620336158431733257575682136836100155721926632321599369132092701295540721504104229217666225601026879393318399391095704223500673696914052239029335");
    private static BigInteger modulus = new BigInteger("1549611057746979844352781944553705273443228154042066840514290174539588436243191882510185738846985723357723362764835928526260868977814405651690121789896823");
    public int socketTimeout;
}
