package edu.yu.oatsdb.v0;

import edu.yu.oatsdb.base.*;
import edu.yu.oatsdb.base.OATSDBType;

import java.util.Map;

public class Driver {

    public static void main(String[] args) {

        try {
            OATSDBType o = OATSDBType.V0;
            DBMS dbms = OATSDBType.dbmsFactory(o);
            TxMgr txmgr = OATSDBType.txMgrFactory(o);

            txmgr.begin();

            dbms.createMap("One", Integer.class, String.class);
            dbms.createMap("Two", Integer.class, String.class);

            dbms.getMap("Two", Integer.class, String.class).put(22, "Edan");
            dbms.getMap("Two", Integer.class, String.class).put(26, "Arianne");
            dbms.getMap("Two", Integer.class, String.class).put(18, "Lior");

            Map map = dbms.getMap("Two", Integer.class, String.class);
            for (Object values : map.values()) {
                System.out.println(values);
            }

            txmgr.commit();
        }

        catch (Exception e){
            e.printStackTrace();
        }

    }

}
