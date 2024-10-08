/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inventory.base.Inventory;
import org.guanzon.cas.inventory.base.InvLedger;
import org.guanzon.cas.inventory.base.InvSerial;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author User
 */
public class testInventorySerial{

    static GRider instance;
    static InvSerial record;

    @BeforeClass
    public static void setUpClass() {
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
        
        System.setProperty("sys.default.path.config", path);
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/");
        
        if (!loadProperties()) {
            System.err.println("Unable to load config.");
            System.exit(1);
        } else {
            System.out.println("Config file loaded successfully.");
        }

        System.out.println("Connected");
        instance = MiscUtil.Connect();
        record = new InvSerial(instance, false);
        record.setWithUI(false);
    }
    private static boolean loadProperties() {
        try {
            Properties po_props = new Properties();
            po_props.load(new FileInputStream("D:\\GGC_Maven_Systems\\config\\cas.properties"));

            System.setProperty("store.branch.code", po_props.getProperty("store.branch.code"));
            System.setProperty("store.inventory.industry", po_props.getProperty("store.inventory.category"));
            
            return true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    @Test
    public void testNewRecord() {
        JSONObject loJSON;
//        loJSON = record.openRecord("M00124000003");
        loJSON = record.newRecord();
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
//        if(record.getMaster().size() >= 1){
//            if(record.getMaster().get(record.getMaster().size()-1).getSerialID()!= null){
//                loJSON = record.addLedger();
//                if ("error".equals((String) loJSON.get("result"))) {
//                    Assert.fail((String) loJSON.get("message"));
//                }
//            }
//        }
//       
//        
//        loJSON = record.getMaster().get(record.getMaster().size()-1).setSerial01("G3J1E-0300659");
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
//        
//        loJSON = record.getMaster().get(record.getMaster().size()-1).setSerial02("MH3SG4650J0034231");
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
//        
//        loJSON = record.getMaster().get(record.getMaster().size()-1).setUnitPrice(123);
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
//        loJSON = record.getMaster().get(record.getMaster().size()-1).setStockID("M00124000001");
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
//        loJSON = record.getMaster().get(record.getMaster().size()-1).setLocation("1");
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
//        loJSON = record.getMaster().get(record.getMaster().size()-1).setSoldStat("1");
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
////        loJSON = record.getMaster().get(record.getMaster().size()-1).set("1");
////        if ("error".equals((String) loJSON.get("result"))) {
////            Assert.fail((String) loJSON.get("message"));
////        }
//        loJSON = record.getMaster().get(record.getMaster().size()-1).setCompnyID("M001");
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
//
//        loJSON = record.getMaster().get(record.getMaster().size()-1).setWarranty("");
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
//        loJSON = record.getMaster().get(record.getMaster().size()-1).setBranchCode(instance.getBranchCode());
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
//        loJSON = record.saveRecord();
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
    }
    
    @Test
    public void testOpenRecord() {
        JSONObject loJSON;
        
        loJSON = record.searchRecord(2,"G3J1E-0300605");
//        loJSON = record.newRecord();
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        
        Assert.assertEquals("M00124000006", record.getMaster().get(record.getMaster().size()-1).getSerialID());
        Assert.assertEquals("M001", record.getMaster().get(record.getMaster().size()-1).getBranchCode());
        Assert.assertEquals("G3J1E-0300605", record.getMaster().get(record.getMaster().size()-1).getSerial01());
        Assert.assertEquals("MH3SG4650J0034235", record.getMaster().get(record.getMaster().size()-1).getSerial02());
        Assert.assertEquals(1800.00, Double.parseDouble(record.getMaster().get(record.getMaster().size()-1).getUnitPrice().toString()), 0.0);
        Assert.assertEquals("M00124000001", record.getMaster().get(record.getMaster().size()-1).getStockID());
        Assert.assertEquals("3", record.getMaster().get(record.getMaster().size()-1).getLocation());
        Assert.assertEquals("1", record.getMaster().get(record.getMaster().size()-1).getSoldStat());
        Assert.assertEquals("6", record.getMaster().get(record.getMaster().size()-1).getUnitType());
        Assert.assertEquals("0002", record.getMaster().get(record.getMaster().size()-1).getCompnyID());
        Assert.assertEquals("997685", record.getMaster().get(record.getMaster().size()-1).getWarranty());
        Assert.assertEquals("230000012708", record.getMaster().get(record.getMaster().size()-1).getBarcode());
        Assert.assertEquals("sasasasa", record.getMaster().get(record.getMaster().size()-1).getDescript());
        Assert.assertEquals("Suzuyama", record.getMaster().get(record.getMaster().size()-1).getBrandName());
        Assert.assertEquals("RAIDER GTR 180", record.getMaster().get(record.getMaster().size()-1).getModelName());
        Assert.assertEquals("ORANGE", record.getMaster().get(record.getMaster().size()-1).getColorName());
        Assert.assertEquals("", record.getMaster().get(record.getMaster().size()-1).getMeasure());
        Assert.assertEquals("GMC Dagupan - Honda", record.getMaster().get(record.getMaster().size()-1).getBranchName());
        Assert.assertEquals("Guanzon Merchandising Corp.", record.getMaster().get(record.getMaster().size()-1).getCompnyName());
    }

    @AfterClass
    public static void tearDownClass() {
        record = null;
        instance = null;
    }
}


    