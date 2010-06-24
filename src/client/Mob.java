package client;


final class Mob {

    Mob() {
        waypointsX = new int[10];
        waypointsY = new int[10];
        animationCount = new int[12];
        mobLevel = -1;
        unusedBool = false;
        unusedInt = -1;
    }

    public int adminLevel;
    public long nameLong;
    public String name;
    public int serverIndex;
    public int mobIntUnknown;
    public int currentX;
    public int currentY;
    public int type;
    public int stepCount;
    public int currentSprite;
    public int nextSprite;
    public int waypointEndSprite;
    public int waypointCurrent;
    public int waypointsX[];
    public int waypointsY[];
    public int animationCount[];
    public String lastMessage;
    public int lastMessageTimeout;
    public int itemBubbleID;
    public int itemBubbleTimeout;
    public int damageSplatNumber;
    public int hitPointsCurrent;
    public int hitPointsBase;
    public int combatTimer;
    public int mobLevel;
    public int colourHairType;
    public int colourTopType;
    public int colourBottomType;
    public int colourSkinType;
    public int attackingCameraInt;
    public int attackingMobIndex;
    public int attackingNpcIndex;
    public int anInt176;
    public boolean unusedBool;
    public int unusedInt;
    public int isSkulled;
}
