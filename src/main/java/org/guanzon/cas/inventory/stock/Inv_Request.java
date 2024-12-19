package org.guanzon.cas.inventory.stock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Request_Master;
import org.guanzon.cas.inventory.stock.request.RequestController;
import org.guanzon.cas.inventory.stock.request.RequestControllerFactory;
import org.json.simple.JSONObject;

/**
 *
 * @author Unclejo
 */
public class Inv_Request implements RequestController  {

    GRider poGRider;
    boolean pbWthParent;
    int pnEditMode;
    String psTranStatus;
    RequestController poTrans;
    RequestControllerFactory factory = new RequestControllerFactory();
        
    RequestControllerFactory.RequestType type; // Example type
    RequestControllerFactory.RequestCategoryType categ_type; // Example type

    private boolean p_bWithUI = true;
    JSONObject poJSON;
    
    // Get the appropriate controller
    /**
     *
     * @param types
     */
    @Override
    public void setType(RequestControllerFactory.RequestType types){
        type = types;
        poTrans = factory.make(type, poGRider, p_bWithUI);
        poTrans.setType(type);
        
    }
    

    @Override
    public void setCategoryType(RequestControllerFactory.RequestCategoryType type) {
        categ_type = type;
        poTrans.setCategoryType(categ_type);
    }
    
    @Override
    public void isHistory(boolean fbValue) {
        poTrans.isHistory(fbValue);
    }
    public void setWithUI(boolean fbValue){
        p_bWithUI = fbValue;
        poTrans.setWithUI(fbValue);

    }
    public Inv_Request(GRider foGRider, boolean fbWthParent) {
        poGRider = foGRider;
        pbWthParent = fbWthParent;
        
        pnEditMode = EditMode.UNKNOWN;
    }

    @Override
    public int getItemCount() {
       return poTrans.getItemCount();
    }
    
    @Override
    public int getOtherItemCount() {
       return poTrans.getOtherItemCount();
    }

    @Override
    public Model_Inv_Stock_Request_Detail getDetailModel(int fnRow) {
        return poTrans.getDetailModel(fnRow);
    }

    @Override
    public JSONObject setDetail(int fnRow, int fnCol, Object foData) {
        return poTrans.setDetail(fnRow, fnCol, foData);
    }

    @Override
    public JSONObject setDetail(int fnRow, String fsCol, Object foData) {
        return poTrans.setDetail(fnRow, fsCol, foData);
        
    }

    @Override
    public JSONObject searchDetail(int fnRow, String fsCol, String fsValue, boolean bln) {
        return poTrans.searchDetail(fnRow, fsCol, fsValue, bln);
    }

    @Override
    public JSONObject searchDetail(int fnRow, int fnCol, String fsValue, boolean bln)  {
        return poTrans.searchDetail(fnRow, fnCol, fsValue, bln);
        
    }

    @Override
    public JSONObject newTransaction() {
        return poTrans.newTransaction();
    }

    @Override
    public JSONObject openTransaction(String string) {
        return poTrans.openTransaction(string);
    }

    @Override
    public JSONObject updateTransaction() {
        return poTrans.updateTransaction();
    }

    @Override
    public JSONObject saveTransaction() {
        return poTrans.saveTransaction();
    }

    @Override
    public JSONObject deleteTransaction(String string) {
        return poTrans.deleteTransaction(string);
    }

    @Override
    public JSONObject closeTransaction(String string) {
        return poTrans.closeTransaction(string);
    }

    @Override
    public JSONObject postTransaction(String string) {
        return poTrans.postTransaction(string);
    }

    @Override
    public JSONObject voidTransaction(String string) {
        return poTrans.voidTransaction(string);
    }

    @Override
    public JSONObject cancelTransaction(String fsValue) {
        return poTrans.cancelTransaction(fsValue);
    }

    @Override
    public JSONObject searchWithCondition(String fsValue) {
        return poTrans.searchWithCondition(fsValue);
    }

    @Override
    public JSONObject searchTransaction(String fsCol, String fsValue, boolean bln) {
        return poTrans.searchTransaction(fsCol, fsValue, bln);
    }

    @Override
    public JSONObject searchMaster(String fsCol, String fsValue, boolean bln) {
        return poTrans.searchMaster(fsCol, fsValue, bln);
    }

    @Override
    public JSONObject searchMaster(int fnCol, String fsValue, boolean bln) {
        return poTrans.searchMaster(fnCol, fsValue, bln);
    }

    @Override
    public Model_Inv_Stock_Request_Master getMasterModel() {
        return poTrans.getMasterModel();
    }

    @Override
    public JSONObject setMaster(int fnCol, Object foData) {
        return poTrans.setMaster(fnCol, foData);
    }

    @Override
    public JSONObject setMaster(String fsCol, Object foData) {
        return poTrans.setMaster(fsCol, foData);
        
    }

    @Override
    public int getEditMode() {
        return poTrans.getEditMode();
    }
    
    @Override
    public void setTransactionStatus(String fsValue) {
        poTrans.setTransactionStatus(fsValue);
    }

    @Override
    public JSONObject OpenModelDetail(String fsTransNo) {
        return poTrans.OpenModelDetail(fsTransNo);
    }

    @Override
    public JSONObject SearchDetailRequest(String fsTransNo, String fsStockID) {
        return poTrans.SearchDetailRequest(fsTransNo, fsStockID);
    }

    @Override
    public JSONObject OpenModelDetailByStockID(String fsTransNo, String fsStockID) {
        return poTrans.OpenModelDetailByStockID(fsTransNo, fsStockID);
    }
    @Override
    public JSONObject AddModelDetail() {
        return poTrans.AddModelDetail();
    }

    @Override
    public void RemoveModelDetail(int fnRow) {
        poTrans.RemoveModelDetail(fnRow);
    }

    @Override
    public ArrayList<Model_Inv_Stock_Request_Detail> getDetailModel() {
       return poTrans.getDetailModel();
    }
    @Override
    public void cancelUpdate(){
        poTrans.cancelUpdate();
    }

    @Override
    public JSONObject loadAllInventoryMinimumLevel() {
        return poTrans.loadAllInventoryMinimumLevel();
    }

    @Override
    public JSONObject setDetailOthers(int fnRow, String fsCol, Object foData) {
        return poTrans.setDetailOthers(fnRow, fsCol, foData);
    }

    @Override
    public JSONObject setDetailOthers(int fnRow, int fnCol, Object foData) {
        return poTrans.setDetailOthers(fnRow, fnCol, foData);
    }
    @Override
    public ArrayList<Model_Inv_Stock_Request_Detail> getDetailModelOthers() {
       return poTrans.getDetailModelOthers();
    }

    @Override
    public JSONObject AddDetailOthers(int fnRow) {
        return poTrans.AddDetailOthers(fnRow);
    }
    
}
