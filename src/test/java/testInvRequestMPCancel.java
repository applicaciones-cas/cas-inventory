
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.inventory.stock.InvStockReqCancel;
import org.guanzon.cas.inventory.stock.request.RequestControllerFactory;
import org.guanzon.cas.inventory.stock.request.cancel.InvRequestCancel;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Unclejo
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testInvRequestMPCancel {
 
    static GRider instance;
    static InvRequestCancel record;
    static String lsTransNox = "";
    @BeforeClass
    public static void setUpClass() {
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Maven_Systems";
        }
        else{
            path = "/srv/GGC_Maven_Systems";
        }
        
        if (!loadProperties()) {
            System.err.println("Unable to load config.");
            System.exit(1);
        } else {
            System.out.println("Config file loaded successfully.");
        }
        System.setProperty("sys.default.path.config", path);

        instance = new GRider("gRider");

        if (!instance.logUser("gRider", "M001000001")){
            System.err.println(instance.getErrMsg());
            System.exit(1);
        }
        
        System.setProperty("sys.default.path.config", path);
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/");
        

        System.out.println("Connected");
        instance = MiscUtil.Connect();
        record = new InvRequestCancel(instance, false);
        record.setType(RequestControllerFactory.RequestType.MPUnits);
        record.setWithUI(false);
        record.setCategoryType(RequestControllerFactory.RequestCategoryType.WITHOUT_ROQ);
        record.setTransactionStatus("01234");
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
    public void testNewTransaction() {
        JSONObject loJSON;
        loJSON = record.newTransaction();
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        System.out.println("transNox == " + record.getMasterModel().getTransactionNumber());
        loJSON = record.searchMaster(5, "C00124000001", true);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }   
        if(record.getDetailModel().size()<=0){
            loJSON = record.AddModelDetail();
            if ("error".equals((String) loJSON.get("result"))) {
                Assert.fail((String) loJSON.get("message"));
            }
        }
//        Uncomment this code if setDetail on BrowseRequest is commented the code  
        loJSON = record.searchDetail(0, 3, "MOTC168BLUE", true);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        loJSON = record.AddModelDetail();
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        loJSON = record.searchDetail(1, 3, "N6020BLACK", true);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
//        
//        This function checks if all inventory stock request details are loaded.
//        comment this code if setDetail on BrowseRequest is commented the code 
        for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
            
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getTransactionNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        loJSON = record.getDetailModel(lnCtr).setQuantity(5);
                        if ("error".equals((String) loJSON.get("result"))) {
                            Assert.fail((String) loJSON.get("message"));
                        }
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        loJSON = record.getDetailModel(lnCtr).setQuantity(5);
                        if ("error".equals((String) loJSON.get("result"))) {
                            Assert.fail((String) loJSON.get("message"));
                        }
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("N6020BLACK", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 02 notes entry 2.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    default:
                        break;
                }
        }
        
        loJSON = record.saveTransaction();
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
//        
    }
    
    @Test
    public void testSearchTransaction() {
        JSONObject loJSON;
        loJSON = record.searchTransaction("sTransNox", "M00124000008", true);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        if(record.getDetailModel().size()>0){
            for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getOrderNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("N6020BLACK", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 02 notes entry 2.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    default:
                        break;
                }
            }
        }
        
    }
    @Test
    public void testCloseTransaction() {
        JSONObject loJSON;
        loJSON = record.openTransaction("M00124000008");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        System.out.println("record = " + record.getDetailModel().size());
        if(record.getDetailModel().size()>0){
            for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getOrderNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("N6020BLACK", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 02 notes entry 2.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    default:
                        break;
                }
            }
        }
        loJSON = record.closeTransaction("M00124000008");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
    }
    
     @Test
    public void testPostTransaction() {
        JSONObject loJSON;
        loJSON = record.openTransaction("M00124000008");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        if(record.getDetailModel().size()>0){
            for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getOrderNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("N6020BLACK", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 02 notes entry 2.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    default:
                        break;
                }
            }
        }
        loJSON = record.postTransaction("M00124000008");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
    }
    @Test
    public void testCanceTransaction() {
        JSONObject loJSON;
        loJSON = record.openTransaction("M00124000008");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        if(record.getDetailModel().size()>0){
            for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getOrderNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("N6020BLACK", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 02 notes entry 2.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    default:
                        break;
                }
        }
        }
        loJSON = record.cancelTransaction("M00124000008");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
    }
    @Test
    public void testVoidTransaction() {
        JSONObject loJSON;
        loJSON = record.openTransaction("M00124000008");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        if(record.getDetailModel().size()>0){
            for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getOrderNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(5, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("N6020BLACK", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 02 notes entry 2.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    default:
                        break;
                }
            }
        }
        loJSON = record.voidTransaction("M00124000008");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
    }
    @AfterClass
    public static void tearDownClass() {
        record = null;
        instance = null;
    }
}

