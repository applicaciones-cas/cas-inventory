package org.guanzon.cas.inventory.stock.request.approval;

import org.guanzon.cas.inventory.stock.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Request_Master;
import org.guanzon.cas.inventory.stock.request.RequestApprovalController;
import org.guanzon.cas.inventory.stock.request.RequestControllerFactory;
import org.json.simple.JSONObject;

/**
 *
 * @author Unclejo
 */
public class Inv_Request_Approval implements RequestApprovalController  {

    GRider poGRider;
    boolean pbWthParent;
    int pnEditMode;
    String psTranStatus;
    RequestApprovalController poTrans;
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
        poTrans = factory.makeApproval(type, poGRider, p_bWithUI);
        poTrans.setType(type);
        
    }
    

    @Override
    public void setCategoryType(RequestControllerFactory.RequestCategoryType type) {
        categ_type = type;
        poTrans.setCategoryType(categ_type);
    }
    
    public void setWithUI(boolean fbValue){
        p_bWithUI = fbValue;

    }
    public Inv_Request_Approval(GRider foGRider, boolean fbWthParent) {
        poGRider = foGRider;
        pbWthParent = fbWthParent;
        
        pnEditMode = EditMode.UNKNOWN;
    }

    @Override
    public int getItemCount() {
       return poTrans.getItemCount();
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
    public ArrayList<Model_Inv_Stock_Request_Detail> getDetailModel() {
        return poTrans.getDetailModel();
    }

    @Override
    public ArrayList<Model_Inv_Stock_Request_Master> getMasterModelList() {
        return poTrans.getMasterModelList();
    }
    
    @Override
    public JSONObject LoadModelMasterList() {
        return poTrans.LoadModelMasterList();
    }

    @Override
    public void cancelUpdate() {
        poTrans.cancelUpdate();
    }

    @Override
    public JSONObject BrowseRequest(String fsColumn, String fsValue, boolean fbByCode) {
        return poTrans.BrowseRequest(fsColumn, fsValue, fbByCode);
    }
    
    
}
