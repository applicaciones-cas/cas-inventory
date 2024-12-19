
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.cas.inventory.models.PurchaseOrder;
import org.guanzon.cas.inventory.stock.InvStockRequest;
import org.guanzon.cas.inventory.stock.Inv_Request;
import org.guanzon.cas.inventory.stock.request.RequestControllerFactory;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 *
 * @author unclejo
 */


@FixMethodOrder(MethodSorters.DEFAULT)
public class testInvRequestMP {
 
    static GRider instance;
    static Inv_Request record;
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
        record = new Inv_Request(instance, false);
        record.setType(RequestControllerFactory.RequestType.MPUnits);
        record.setCategoryType(RequestControllerFactory.RequestCategoryType.WITHOUT_ROQ);
        record.setTransactionStatus("01234");
        record.isHistory(false);
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

        record.setWithUI(false);
        loJSON = record.newTransaction();
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }    
        
        lsTransNox = record.getMasterModel().getTransactionNumber();
        //set master information
        loJSON = record.getMasterModel().setBranchCode("M001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(lsTransNox, record.getMasterModel().getTransactionNumber());
        
        loJSON = record.getMasterModel().setCategoryCode("0001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals("0001", record.getMasterModel().getCategoryCode());
        
        loJSON = record.getMasterModel().setTransaction(SQLUtil.toDate("2024-08-24",SQLUtil.FORMAT_SHORT_DATE));
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(CommonUtils.toDate("2024-08-24"), record.getMasterModel().getTransaction());
        
        loJSON = record.getMasterModel().setReferenceNumber("");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals("", record.getMasterModel().getReferenceNumber());
        
        loJSON = record.getMasterModel().setRemarks("This is a Test for Purchase Order");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals("This is a Test for Purchase Order", record.getMasterModel().getRemarks());
        
        loJSON = record.getMasterModel().setIssNotes("This is a Notes");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals("This is a Notes", record.getMasterModel().getIssNotes());
        
        loJSON = record.getMasterModel().setCurrentInventory(12);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(12, record.getMasterModel().getCurrentInventory(),0.0);
        
        loJSON = record.getMasterModel().setEstimatedInventory(12);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(12, record.getMasterModel().getEstimatedInventory(),0.0);
        
        loJSON = record.getMasterModel().setApproved("");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals("", record.getMasterModel().getApproved());
        
        loJSON = record.getMasterModel().setApproveCode(null);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(null, record.getMasterModel().getApproveCode());
        
        loJSON = record.getMasterModel().setEntryNumber(1);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(1, record.getMasterModel().getEntryNumber(),0);
        
        loJSON = record.getMasterModel().setSourceCode("SlSl");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals("SlSl", record.getMasterModel().getSourceCode());
        
        loJSON = record.getMasterModel().setSourceNumber("C00124000001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals("C00124000001", record.getMasterModel().getSourceNumber());
        
        loJSON = record.getMasterModel().setConfirm("0");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals("0", record.getMasterModel().getConfirm());
        
        loJSON = record.getMasterModel().setTransactionStatus(TransactionStatus.STATE_OPEN);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(TransactionStatus.STATE_OPEN, record.getMasterModel().getTransactionStatus());
        
        loJSON = record.getMasterModel().setStartEncDate(instance.getServerDate());
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(instance.getServerDate(), record.getMasterModel().getStartEncDate());
        
//        used only in Posting method
//        loJSON = record.getMasterModel().setPostedDate(instance.getServerDate());
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
        loJSON = record.getMasterModel().setModifiedBy(instance.getUserID());
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(instance.getUserID(), record.getMasterModel().getModifiedBy());
        
//        loJSON = record.getMasterModel().setModifiedDate(instance.getServerDate());
//        if ("error".equals((String) loJSON.get("result"))) {
//            Assert.fail((String) loJSON.get("message"));
//        }
//        Assert.assertEquals(instance.getServerDate(), record.getMasterModel().getModifiedDate());
//        
        //set detail information 
        loJSON = record.searchDetail(0, 3, "MOTC168BLUE", true);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setTransactionNumber(record.getMasterModel().getTransactionNumber());
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(record.getMasterModel().getTransactionNumber(), record.getDetailModel().get(record.getDetailModel().size()-1).getTransactionNumber());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setEntryNumber(record.getDetailModel().size());
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(record.getDetailModel().size(), record.getDetailModel().get(record.getDetailModel().size()-1).getEntryNumber());
        
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setQuantity(5);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(5, record.getDetailModel().get(record.getDetailModel().size()-1).getQuantity());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setRecordOrder(5);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(5, record.getDetailModel().get(record.getDetailModel().size()-1).getRecordOrder());
                
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setOnTransit(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getOnTransit());
                
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setApproved(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getApproved());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setCancelled(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getCancelled());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setIssueQuantity(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getIssueQuantity());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setOrderQuantity(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getOrderQuantity());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setAllocatedQuantity(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getAllocatedQuantity());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setReceived(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getReceived());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setNotes("This is detail 01 notes entry 1.");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(record.getDetailModel().size()-1).getNotes());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setModifiedDate(instance.getServerDate());
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(instance.getServerDate(), record.getDetailModel().get(record.getDetailModel().size()-1).getModifiedDate());
        
        Assert.assertEquals("C00124000026", record.getDetailModel().get(record.getDetailModel().size()-1).getStockID());
        Assert.assertEquals("F", record.getDetailModel().get(record.getDetailModel().size()-1).getClassify());
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getQuantityOnHand());
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getReservedOrder());
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getBackOrder());
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getAverageMonthlySalary());
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getMaximumLevel());
        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(record.getDetailModel().size()-1).getBarcode());
        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(record.getDetailModel().size()-1).getDescription());
        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(record.getDetailModel().size()-1).getCategoryName());
        Assert.assertEquals("Cellphone", record.getDetailModel().get(record.getDetailModel().size()-1).getCategoryName2());
        Assert.assertEquals("Finished Products", record.getDetailModel().get(record.getDetailModel().size()-1).getCategoryType());
        
        //set detail 2 information
        loJSON = record.AddModelDetail();
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        
        loJSON = record.searchDetail(1, 3, "N6020BLACK", true);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setTransactionNumber(record.getMasterModel().getTransactionNumber());
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(record.getMasterModel().getTransactionNumber(), record.getDetailModel().get(record.getDetailModel().size()-1).getTransactionNumber());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setEntryNumber(record.getDetailModel().size());
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(record.getDetailModel().size(), record.getDetailModel().get(record.getDetailModel().size()-1).getEntryNumber());
        
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setQuantity(5);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(5, record.getDetailModel().get(record.getDetailModel().size()-1).getQuantity());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setRecordOrder(5);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(5, record.getDetailModel().get(record.getDetailModel().size()-1).getRecordOrder());
                
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setOnTransit(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getOnTransit());
                
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setApproved(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getApproved());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setCancelled(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getCancelled());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setIssueQuantity(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getIssueQuantity());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setOrderQuantity(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getOrderQuantity());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setAllocatedQuantity(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getAllocatedQuantity());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setReceived(0);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getReceived());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setNotes("This is detail 02 notes entry 2.");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals("This is detail 02 notes entry 2.", record.getDetailModel().get(record.getDetailModel().size()-1).getNotes());
        
        loJSON = record.getDetailModel().get(record.getDetailModel().size()-1).setModifiedDate(instance.getServerDate());
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        Assert.assertEquals(instance.getServerDate(), record.getDetailModel().get(record.getDetailModel().size()-1).getModifiedDate());
       
        Assert.assertEquals("C00124000025", record.getDetailModel().get(record.getDetailModel().size()-1).getStockID());
        Assert.assertEquals("F", record.getDetailModel().get(record.getDetailModel().size()-1).getClassify());
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getQuantityOnHand());
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getReservedOrder());
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getBackOrder());
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getAverageMonthlySalary());
        Assert.assertEquals(0, record.getDetailModel().get(record.getDetailModel().size()-1).getMaximumLevel());
        Assert.assertEquals("N6020BLACK", record.getDetailModel().get(record.getDetailModel().size()-1).getBarcode());
        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(record.getDetailModel().size()-1).getDescription());
        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(record.getDetailModel().size()-1).getCategoryName());
        Assert.assertEquals("Cellphone", record.getDetailModel().get(record.getDetailModel().size()-1).getCategoryName2());
        Assert.assertEquals("Finished Products", record.getDetailModel().get(record.getDetailModel().size()-1).getCategoryType());
        
        loJSON = record.saveTransaction();
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
    }
    
    @Test
    public void testOpenDetail() {
         JSONObject loJSON;
        loJSON = record.searchTransaction("sTransNox", "C00124000001", true);
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        if(record.getDetailModel().size()>0){
            for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
                
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getTransactionNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals(1, record.getDetailModel().get(lnCtr).getEntryNumber());
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(70, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals(2, record.getDetailModel().get(lnCtr).getEntryNumber());
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(22, record.getDetailModel().get(lnCtr).getQuantity());
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
    public void testUpdateDetail() {
         JSONObject loJSON;
        loJSON = record.openTransaction("C00124000001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        loJSON = record.updateTransaction();
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        record.getDetailModel().get(0).setQuantity(3);
        Assert.assertEquals(3, record.getDetailModel().get(0).getQuantity());
        
        record.getDetailModel().get(1).setQuantity(4);
        Assert.assertEquals(4, record.getDetailModel().get(1).getQuantity());
        
        record.getDetailModel().get(2).setQuantity(5);
        Assert.assertEquals(5, record.getDetailModel().get(2).getQuantity());
        
        loJSON = record.saveTransaction();
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        
    }
    
    
    @Test
    public void testCloseTransaction() {
         JSONObject loJSON;
        loJSON = record.openTransaction("C00124000001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        if(record.getDetailModel().size()>0){
            for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
                
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getTransactionNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals(1, record.getDetailModel().get(lnCtr).getEntryNumber());
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(70, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals(2, record.getDetailModel().get(lnCtr).getEntryNumber());
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(22, record.getDetailModel().get(lnCtr).getQuantity());
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
        loJSON = record.closeTransaction("C00124000001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        
    }
    @Test
    public void testPostTransaction() {
         JSONObject loJSON;
        loJSON = record.openTransaction("C00124000001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        if(record.getDetailModel().size()>0){
            for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
                
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getTransactionNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals(1, record.getDetailModel().get(lnCtr).getEntryNumber());
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(70, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals(2, record.getDetailModel().get(lnCtr).getEntryNumber());
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(22, record.getDetailModel().get(lnCtr).getQuantity());
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
        loJSON = record.postTransaction("C00124000001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        
    }
    @Test
    public void testVoidTransaction() {
         JSONObject loJSON;
        loJSON = record.openTransaction("C00124000001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        if(record.getDetailModel().size()>0){
            for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
                
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getTransactionNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals(1, record.getDetailModel().get(lnCtr).getEntryNumber());
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(70, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals(2, record.getDetailModel().get(lnCtr).getEntryNumber());
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(22, record.getDetailModel().get(lnCtr).getQuantity());
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
        loJSON = record.voidTransaction("C00124000001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        
    }
    @Test
    public void testCancelTransaction() {
         JSONObject loJSON;
        loJSON = record.openTransaction("C00124000001");
        if ("error".equals((String) loJSON.get("result"))) {
            Assert.fail((String) loJSON.get("message"));
        }
        if(record.getDetailModel().size()>0){
            for(int lnCtr = 0; lnCtr < record.getDetailModel().size(); lnCtr++){
                
                Assert.assertEquals("C00124000001", record.getDetailModel().get(lnCtr).getTransactionNumber());
                switch(lnCtr){
                    case 0:
                        Assert.assertEquals(1, record.getDetailModel().get(lnCtr).getEntryNumber());
                        Assert.assertEquals("C00124000026", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(70, record.getDetailModel().get(lnCtr).getQuantity());
                        Assert.assertEquals("MOTC168BLUE", record.getDetailModel().get(lnCtr).getBarcode());
                        Assert.assertEquals("Open Line W/ Warranty", record.getDetailModel().get(lnCtr).getDescription());
                        Assert.assertEquals("Mobile Phone", record.getDetailModel().get(lnCtr).getCategoryName());
                        Assert.assertEquals("Cellphone", record.getDetailModel().get(lnCtr).getCategoryName2());
                        Assert.assertEquals("Finished Products", record.getDetailModel().get(lnCtr).getCategoryType());
                        Assert.assertEquals("This is detail 01 notes entry 1.", record.getDetailModel().get(lnCtr).getNotes());
                        break;
                    case 1:
                        Assert.assertEquals(2, record.getDetailModel().get(lnCtr).getEntryNumber());
                        Assert.assertEquals("C00124000025", record.getDetailModel().get(lnCtr).getStockID());
                        Assert.assertEquals(22, record.getDetailModel().get(lnCtr).getQuantity());
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
        loJSON = record.cancelTransaction("C00124000001");
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
