package client.util;

import com.thoughtworks.xstream.XStream;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PersistenceManager {

    private static final XStream xstream = new XStream();

    static {
        addAlias("NPCDef", "client.entityhandling.defs.NPCDef");
        addAlias("ItemDef", "client.entityhandling.defs.ItemDef");
        addAlias("TextureDef", "client.entityhandling.defs.extras.TextureDef");
        addAlias("AnimationDef", "client.entityhandling.defs.extras.AnimationDef");
        addAlias("ItemDropDef", "client.entityhandling.defs.extras.ItemDropDef");
        addAlias("SpellDef", "client.entityhandling.defs.SpellDef");
        addAlias("PrayerDef", "client.entityhandling.defs.PrayerDef");
        addAlias("TileDef", "client.entityhandling.defs.TileDef");
        addAlias("DoorDef", "client.entityhandling.defs.DoorDef");
        addAlias("ElevationDef", "client.entityhandling.defs.ElevationDef");
        addAlias("GameObjectDef", "client.entityhandling.defs.GameObjectDef");
        addAlias("spriteeditor.Sprite", "client.model.Sprite");
    }

    private static void addAlias(String name, String className) {
        try {
            xstream.alias(name, Class.forName(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Object load(File file) {
        try {
            InputStream is = new GZIPInputStream(new FileInputStream(file));
            Object rv = xstream.fromXML(is);
            return rv;
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        return null;
    }

    public static void write(File file, Object o) {
        try {
            OutputStream os = new GZIPOutputStream(new FileOutputStream(file));
            xstream.toXML(o, os);
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }
}
