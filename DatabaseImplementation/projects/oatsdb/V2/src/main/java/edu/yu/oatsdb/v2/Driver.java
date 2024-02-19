package edu.yu.oatsdb.v2;

import edu.yu.oatsdb.base.*;
import edu.yu.oatsdb.base.OATSDBType;

import java.util.Map;

public class Driver {

    public static void main(String[] args) {

        try {
            OATSDBType o = OATSDBType.V2;

            DBMS dbms = OATSDBType.dbmsFactory(o);
            TxMgr txmgr = OATSDBType.txMgrFactory(o);
            DBMSImpl.Instance.clear();

            txmgr.begin();

//            Map map = dbms.getMap("Map One", String.class, Integer.class);
//            Map map2 = dbms.getMap("Map Two", String.class, Integer.class);
//            map.put("Rami Pinchot", 24);
//            System.out.println(map);
//            System.out.println(map2);
//
            dbms.createMap("Map One", String.class, Integer.class);
            dbms.createMap("Map Two", String.class, Integer.class);
            DBMSImpl.MyMap mapOne = (DBMSImpl.MyMap) dbms.getMap("Map One", String.class, Integer.class);
            DBMSImpl.MyMap mapTwo = (DBMSImpl.MyMap) dbms.getMap("Map Two", String.class, Integer.class);

            mapOne.put("Edan Pinchot", 22);
            Map m = mapOne.getLockedEntries();
            mapOne.put("Lior Pinchot", 18);
            mapOne.put("Arianne Pinchot", 26);
            m = mapOne.getLockedEntries();

            mapTwo.put("Jake Glass", 22);
            mapTwo.put("Danni Glass", 17);
            mapTwo.put("Lisa Glass", 44);
            mapTwo.remove("Lisa Glass");

            txmgr.commit();

            DBMSImpl.Instance.clear();

            System.out.println("HI");
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

}