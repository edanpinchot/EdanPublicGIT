package edu.yu.oatsdb.v2;

public class MapAndShadow {
    private DBMSImpl.MyMap actualMap;
    private DBMSImpl.MyMap shadowMap;

    public MapAndShadow() {
    }

    public MapAndShadow(DBMSImpl.MyMap actualMap, DBMSImpl.MyMap shadowMap) {
        this.actualMap = actualMap;
        this.shadowMap = shadowMap;
    }

    public void setActualMap(DBMSImpl.MyMap actualMap) {
        this.actualMap = actualMap;
    }

    public void setShadowMap(DBMSImpl.MyMap shadowMap) {
        this.shadowMap = shadowMap;
    }

    public DBMSImpl.MyMap getActualMap() {
        return actualMap;
    }

    public DBMSImpl.MyMap getShadowMap() {
        return shadowMap;
    }
}
