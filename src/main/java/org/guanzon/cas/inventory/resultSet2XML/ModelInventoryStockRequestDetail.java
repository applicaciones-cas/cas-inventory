/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.guanzon.cas.inventory.resultSet2XML;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;

/**
 *
 * @author User
 */
public class ModelInventoryStockRequestDetail {
    public static void main (String [] args){
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Maven_Systems";
        }
        else{
            path = "/srv/GGC_Maven_Systems";
        }
        System.setProperty("sys.default.path.config", path);

        GRider instance = new GRider("gRider");

        if (!instance.logUser("gRider", "M001000001")){
            System.err.println(instance.getErrMsg());
            System.exit(1);
        }

        System.out.println("Connected");
        System.setProperty("sys.table", "Inv_Stock_Request_Detail");
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/Model_" + System.getProperty("sys.table") + ".xml");
//        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/Model_Inventory.xml");
        
        String lsSQL = "SELECT" +
                "  a.sTransNox" +
                ", a.nEntryNox" +
                ", a.sStockIDx" +
                ", a.nQuantity" +
                ", a.cClassify" +
                ", a.nRecOrder" +
                ", a.nQtyOnHnd" +
                ", a.nResvOrdr" +
                ", a.nBackOrdr" +
                ", a.nOnTranst" +
                ", a.nAvgMonSl" +
                ", a.nMaxLevel" +
                ", a.nApproved" +
                ", a.nCancelld" +
                ", a.nIssueQty" +
                ", a.nOrderQty" +
                ", a.nAllocQty" +
                ", a.nReceived" +
                ", a.sNotesxxx" +
                ", a.dModified" +
                ", b.sBarCodex xBarCodex" +
                ", b.sDescript xDescript" +
                ", c.sDescript xCategr01" +
                ", d.sDescript xCategr02" +
                ", e.sDescript xInvTypNm" +
                ", f.sDescript xBrandNme" +
                ", g.sDescript xModelNme" +
                ", g.sDescript xModelDsc" +
                ", h.sDescript xColorNme" +
                ", i.sMeasurNm xMeasurNm" +
                ", j.nMinLevel" +
                ", j.sLocatnID" +
                ", f.sBrandIDx" +
                ", g.sModelIDx" +
                ", g.sModelCde xModelCde" +
                ", g.nYearModl" +
                ", o.sVrntName xVrntName" +
                ", o.sDescript sVrntDesc" +
                ", n.sDescript sSeriesNm" +
                " FROM Inv_Stock_Request_Detail a " +
                " LEFT JOIN Inventory b ON a.sStockIDx = b.sStockIDx" +
                " LEFT JOIN Category c ON b.sCategCd1 = c.sCategrCd" +
                " LEFT JOIN Category_Level2 d ON b.sCategCd2 = d.sCategrCd" +
                " LEFT JOIN Inv_Type e ON d.sInvTypCd = e.sInvTypCd" +
                " LEFT JOIN Brand f ON b.sBrandIDx = f.sBrandIDx" +
                " LEFT JOIN Model g ON b.sModelIDx = g.sModelIDx" +
                " LEFT JOIN Model_Series n ON g.sSeriesID = n.sSeriesID" +
                " LEFT JOIN Model_Variant o ON g.sVrntIDxx = o.sVrntIDxx" +
                " LEFT JOIN Color h ON b.sColorIDx = h.sColorIDx" +
                " LEFT JOIN Measure i ON b.sMeasurID = i.sMeasurID " +
                " LEFT JOIN Inv_Master j ON b.sStockIDx = j.sStockIDx " +
                " LEFT JOIN Warehouse k ON j.sWhouseID = k.sWhouseID" +
                " LEFT JOIN Inv_Location l ON j.sLocatnID = l.sLocatnID" +
                " LEFT JOIN Section m ON m.sSectnIDx = l.sSectnIDx " +
                " WHERE 0=1";
        
//        String lsSQL = "SELECT" +
//                "  a.sTransNox" +
//                ", a.nEntryNox" +
//                ", a.sStockIDx" +
//                ", a.nQuantity" +
//                ", a.cClassify" +
//                ", a.nRecOrder" +
//                ", a.nQtyOnHnd" +
//                ", a.nResvOrdr" +
//                ", a.nBackOrdr" +
//                ", a.nOnTranst" +
//                ", a.nAvgMonSl" +
//                ", a.nMaxLevel" +
//                ", a.nApproved" +
//                ", a.nCancelld" +
//                ", a.nIssueQty" +
//                ", a.nOrderQty" +
//                ", a.nAllocQty" +
//                ", a.nReceived" +
//                ", a.sNotesxxx" +
//                ", a.dModified" +
//                ", b.sBarCodex xBarCodex" +
//                ", b.sDescript xDescript" +
//                ", c.sDescript xCategr01" +
//                ", d.sDescript xCategr02" +
//                ", e.sDescript xInvTypNm" +
//                ", f.sDescript xBrandNme" +
//                ", g.sDescript xModelNme" +
//                ", g.sDescript xModelDsc" +
//                ", h.sDescript xColorNme" +
//                ", i.sMeasurNm xMeasurNm" +
//                ", j.nMinLevel" +
//                ", j.sLocatnID" +
//            " FROM Inv_Stock_Request_Detail a" + 
//                " LEFT JOIN Inventory b ON a.sStockIDx = b.sStockIDx" +
//                " LEFT JOIN Category c ON b.sCategCd1 = c.sCategrCd" +
//                " LEFT JOIN Category_Level2 d ON b.sCategCd2 = d.sCategrCd" +
//                " LEFT JOIN Inv_Type e ON d.sInvTypCd = e.sInvTypCd" +
//                " LEFT JOIN Brand f ON b.sBrandIDx = f.sBrandIDx" +
//                " LEFT JOIN Model g ON b.sModelIDx = g.sModelIDx" +
//                " LEFT JOIN Color h ON b.sColorIDx = h.sColorIDx" +
//                " LEFT JOIN Measure i ON b.sMeasurID = i.sMeasurID"+
//                " LEFT JOIN Inv_Master j ON b.sStockIDx = j.sStockIDx" +
//                        " WHERE 0=1";
        
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        try {
            if (MiscUtil.resultSet2XML(instance, loRS, System.getProperty("sys.default.path.metadata"), System.getProperty("sys.table"), "")){
                System.out.println("ResultSet exported.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}
