package edu.yu.oatsdb.v1;

import edu.yu.oatsdb.base.*;
import edu.yu.oatsdb.base.OATSDBType;

import java.util.Map;

public class Driver {

    public static void main(String[] args) {

        try {
            OATSDBType o = OATSDBType.V1;

            DBMS dbms = OATSDBType.dbmsFactory(o);
            TxMgr txmgr = OATSDBType.txMgrFactory(o);

            txmgr.begin();

            dbms.createMap("Map One", String.class, Integer.class);
            dbms.createMap("Map Two", String.class, Integer.class);
            Map mapOne = dbms.getMap("Map One", String.class, Integer.class);
            Map mapTwo = dbms.getMap("Map Two", String.class, Integer.class);
            mapOne.put("Edan Pinchot", 22);
            mapOne.put("Lior Pinchot", 18);
            mapOne.put("Arianne Pinchot", 26);
            mapOne.get("Edan Pinchot");

            mapTwo.put("Jake Glass", 22);
            mapTwo.put("Danni Glass", 17);
            mapTwo.put("Lisa Glass", 44);
            mapTwo.remove("Lisa Glass");


            txmgr.commit();

//            mapTwo.put("Lisa Glass", 44);
            System.out.println("HI");
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

}