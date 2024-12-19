package org.guanzon.cas.inventory.stock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.inventory.base.InvMaster;
import org.guanzon.cas.inventory.base.Inventory;
import org.guanzon.cas.inventory.base.InventoryTrans;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Request_Master;
import org.guanzon.cas.inventory.stock.request.RequestController;
import org.guanzon.cas.inventory.stock.request.RequestControllerFactory;
import org.guanzon.cas.inventory.stock.request.cancel.InvRequestCancel;
import org.guanzon.cas.parameters.Brand;
import org.guanzon.cas.parameters.Category;
import org.guanzon.cas.parameters.Inv_Type;
import org.guanzon.cas.parameters.Model;
import org.guanzon.cas.validators.inventory.Validator_Inv_Stock_Request_MC_Detail;
import org.json.simple.JSONObject;

/**
 *
 * @author Unclejo
 */
public class Inv_Request_MC implements RequestController {

    GRider poGRider;
    boolean pbWthParent;
    int pnEditMode;
    String psTranStatus;

    private boolean p_bWithUI;
    Model_Inv_Stock_Request_Master poModelMaster;
    ArrayList<Model_Inv_Stock_Request_Detail> poModelDetail;
    ArrayList<Model_Inv_Stock_Request_Detail> poModelDetailOthers;
    RequestControllerFactory.RequestType type;
    RequestControllerFactory.RequestCategoryType category_type;
    // Create a backup list to store deleted records temporarily
    private List<Model_Inv_Stock_Request_Detail> backupRecords = new ArrayList<>();

    int roqSaveCount = 0;
    JSONObject poJSON;
    private boolean pbIsHistory = false;

    @Override
    public void isHistory(boolean fbValue) {
        pbIsHistory = fbValue;
    }
    
    @Override
    public void setWithUI(boolean fbValue){
        System.out.println("fbValue = " + fbValue);
        p_bWithUI = fbValue;
    }
    public Inv_Request_MC(GRider foGRider, boolean fbWthParent) {
        poGRider = foGRider;
        pbWthParent = fbWthParent;

        poModelMaster = new Model_Inv_Stock_Request_Master(foGRider);
        poModelDetail = new ArrayList<>();
        poModelDetail.add(new Model_Inv_Stock_Request_Detail(foGRider));
        poModelDetailOthers = new ArrayList<>();
        poModelDetailOthers.add(new Model_Inv_Stock_Request_Detail(foGRider));
        pnEditMode = EditMode.UNKNOWN;
    }

    @Override
    public JSONObject newTransaction() {

        poJSON = new JSONObject();
        poJSON = poModelMaster.newRecord();

        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load.");
            return poJSON;
        }
        Category loCateg = new Category(poGRider, true);
        switch (poGRider.getDivisionCode()) {
            case "0"://mobilephone
                loCateg.openRecord("0002");
                break;

            case "1"://motorycycle
                loCateg.openRecord("0001");
                break;

            case "2"://Auto Group - Honda Cars
            case "5"://Auto Group - Nissan
            case "6"://Auto Group - Any
                loCateg.openRecord("0003");
                break;

            case "3"://Hospitality
            case "4"://Pedritos Group
                loCateg.openRecord("0004");
                break;

            case "7"://Guanzon Services Office
                 break;

            case "8"://Main Office
                break;
        }
        poModelMaster.setCategoryCode((String) loCateg.getMaster("sCategrCd"));
        poModelMaster.setCategoryName((String) loCateg.getMaster("sDescript"));
        
        if(category_type == RequestControllerFactory.RequestCategoryType.WITHOUT_ROQ){
            poModelDetail = new ArrayList<>();
            poModelDetail.add(new Model_Inv_Stock_Request_Detail(poGRider));
            poModelDetail.get(getItemCount() - 1).newRecord();
            
            poModelDetailOthers = new ArrayList<>();
            poModelDetailOthers.add(new Model_Inv_Stock_Request_Detail(poGRider));
            poModelDetailOthers.get(getItemCount() - 1).newRecord();
            poJSON = poModelDetail.get(getItemCount() - 1).setTransactionNumber(poModelMaster.getTransactionNumber());
        }else{
            poModelDetail = new ArrayList<>();
            poModelDetail.add(new Model_Inv_Stock_Request_Detail(poGRider));
            poModelDetail.get(getItemCount() - 1).newRecord();
            
            poModelDetailOthers = new ArrayList<>();
            poModelDetailOthers.add(new Model_Inv_Stock_Request_Detail(poGRider));
            poModelDetailOthers.get(getItemCount() - 1).newRecord();
            poJSON = poModelDetail.get(getItemCount() - 1).setTransactionNumber(poModelMaster.getTransactionNumber());
            poJSON = loadAllInventoryMinimumLevel();
        }
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load.");
            return poJSON;

        }
        
        pnEditMode = EditMode.ADDNEW;

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject openTransaction(String fsValue) {
        
        poJSON = new JSONObject();
        poModelMaster.openRecord(fsValue);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = OpenModelDetail(poModelMaster.getTransactionNumber());
        
        pnEditMode = EditMode.READY;

        return poJSON;

    }

    @Override
    public JSONObject updateTransaction() {
        poJSON = new JSONObject();
        
        if (poModelMaster.getTransactionStatus().equalsIgnoreCase(TransactionStatus.STATE_CLOSED)){
            poJSON.put("result", "error");
            poJSON.put("message", "This transaction was already close.");
            return poJSON;
        }
        poJSON = isProcessed("update");
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode.");
            return poJSON;
        }
        pnEditMode = EditMode.UPDATE;
        
//        AddModelDetail();
        poJSON.put("result", "success");
        poJSON.put("message", "Update mode success.");
         System.out.print("  updateRecord editmode2 == " + pnEditMode + " ");
        return AddModelDetail();

    }

    @Override
    public JSONObject saveTransaction() {
        
        poJSON = new JSONObject();
        if (!pbWthParent) {
            poGRider.beginTrans();
        }

        poJSON = deleteRecord();
        if ("error".equals((String) poJSON.get("result"))) {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }
            return poJSON;
        }
        poJSON = SaveDetail();
        if("error".equals((String)poJSON.get("result"))){
            if (!pbWthParent) {
                restoreData();
                poGRider.rollbackTrans();
            }
            return poJSON;
        }
        if(category_type == RequestControllerFactory.RequestCategoryType.WITH_ROQ){
            poModelMaster.setEntryNumber(roqSaveCount);
        }else{
            poModelMaster.setEntryNumber(poModelDetail.size());
        }
        poJSON = poModelMaster.saveRecord();
        if ("success".equals((String) poJSON.get("result"))) {
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
            
            poJSON.put("result", "success");
            poJSON.put("message", "Record saved successfully.");
        } else {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to Save Transaction.");
            }
        }

        return poJSON;
    }

    @Override
    public JSONObject deleteTransaction(String fsValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject closeTransaction(String fsValue) {
        poJSON = new JSONObject();
        if (poModelMaster.getEditMode() == EditMode.READY || poModelMaster.getEditMode() == EditMode.UPDATE) {
            
            if (poModelMaster.getTransactionStatus().equalsIgnoreCase(TransactionStatus.STATE_CLOSED)){
                poJSON.put("result", "error");
                poJSON.put("message", "This transaction was already close.");
                return poJSON;
            }
            if ("error".equals((String) isProcessed("close").get("result"))) {
                return poJSON;
            }
            poJSON = poModelMaster.setTransactionStatus(TransactionStatus.STATE_CLOSED);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            

            poJSON = poModelMaster.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject postTransaction(String fsValue) {
        poJSON = new JSONObject();

        if (poModelMaster.getEditMode() == EditMode.READY
                || poModelMaster.getEditMode() == EditMode.UPDATE) {
            if ("error".equals((String) isProcessed("post").get("result"))) {
                return poJSON;
            }
            poJSON = poModelMaster.setTransactionStatus(TransactionStatus.STATE_POSTED);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            
            if (poGRider.getUserLevel() < UserRight.SUPERVISOR) {
                JSONObject loJSON =  ShowDialogFX.getUserApproval(poGRider);
                if ("success".equals((String) loJSON.get("result"))) {
                    if ((int) loJSON.get("nUserLevl") < UserRight.SUPERVISOR) {
                        restoreData();
                        poJSON.put("result", "error");
                        poJSON.put("message","Only managerial accounts can approved transactions.(Authentication failed!!!)");
                        return poJSON;
                    }
                    System.out.println("loJSON = " + loJSON.toJSONString());
                    poModelMaster.setApproved((String)loJSON.get("sUserIDxx"));
                    poModelMaster.setApprovedDate(poGRider.getServerDate());
    //                            poModelMaster.setApproveCode();
                }else{
                    restoreData();
                    poJSON.put("result", "error");
                    poJSON.put("message","Seek Manager's Approval for this Stock Request!.(Authentication required!!!)");
                    return poJSON;
                }
            }else{
                poModelMaster.setApproved(poGRider.getUserID());
                poModelMaster.setApprovedDate(poGRider.getServerDate());
            }
            
            poJSON = saveInventoryTrans();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            poJSON = poModelMaster.saveRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject voidTransaction(String string) {
        poJSON = new JSONObject();

        if (poModelMaster.getEditMode() == EditMode.READY
                || poModelMaster.getEditMode() == EditMode.UPDATE) {
            if ("error".equals((String) isProcessed("void").get("result"))) {
                return poJSON;
            }
            poJSON = poModelMaster.setTransactionStatus(TransactionStatus.STATE_VOID);

            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poJSON = poModelMaster.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject cancelTransaction(String fsTransNox) {
        poJSON = new JSONObject();

        if (poModelMaster.getEditMode() == EditMode.READY
                || poModelMaster.getEditMode() == EditMode.UPDATE) {
            if ("error".equals((String) isProcessed("cancel").get("result"))) {
                return poJSON;
            }
            poJSON = poModelMaster.setTransactionStatus(TransactionStatus.STATE_CANCELLED);

            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            System.out.println("pbIsHistory = " + pbIsHistory);
            if(pbIsHistory){
                InvRequestCancel loCancel = new InvRequestCancel(poGRider, pbWthParent);
                loCancel.setType(type);
                loCancel.setCategoryType(category_type);
                loCancel.setTransactionStatus(psTranStatus);
                poJSON = loCancel.newTransaction();

                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                loCancel.getMasterModel().setBranchCode((String)poModelMaster.getBranchCode());
                loCancel.getMasterModel().setCategoryCode((String)poModelMaster.getCategoryCode());
                loCancel.getMasterModel().setTransaction(poGRider.getServerDate());
                loCancel.getMasterModel().setOrderNumber(poModelMaster.getTransactionNumber());
                loCancel.getMasterModel().setRemarks(poModelMaster.getRemarks());
                loCancel.getMasterModel().setApproved(poModelMaster.getApproved());
                loCancel.getMasterModel().setApprovedDate(poModelMaster.getApprovedDate());
                loCancel.getMasterModel().setApproveCode(poModelMaster.getApproveCode());

                for(int lnCtr = 0; lnCtr < poModelDetail.size(); lnCtr++){
                    loCancel.getDetailModel(lnCtr).setOrderNumber(poModelMaster.getTransactionNumber());
                    loCancel.getDetailModel(lnCtr).setStockID(poModelDetail.get(lnCtr).getStockID());
                    loCancel.getDetailModel(lnCtr).setBarcode(poModelDetail.get(lnCtr).getBarcode());
                    loCancel.getDetailModel(lnCtr).setQuantity(Integer.parseInt(poModelDetail.get(lnCtr).getQuantity().toString()));
                    loCancel.getDetailModel(lnCtr).setNotes(poModelDetail.get(lnCtr).getNotes());

                    loCancel.AddModelDetail();
                }


                poJSON = loCancel.saveTransaction();

                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
            
            
            poJSON = poModelMaster.saveRecord();
            
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            
            poJSON.put("result", "success");
            poJSON.put("message", "Record successfully cancelled.");
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }
    private JSONObject isProcessed(String lsMessage){
        poJSON = new JSONObject();
        if (poModelMaster.getTransactionStatus().equalsIgnoreCase(TransactionStatus.STATE_POSTED)
            || poModelMaster.getTransactionStatus().equalsIgnoreCase(TransactionStatus.STATE_CANCELLED)
            || poModelMaster.getTransactionStatus().equalsIgnoreCase(TransactionStatus.STATE_VOID)) {
            
            poJSON.put("result", "error");
            poJSON.put("message","Unable to " + lsMessage + " proccessed transaction.");
            return poJSON;
        }
        poJSON.put("result", "success");
        poJSON.put("message","Okay.");
        return poJSON;
    }

    @Override
    public int getItemCount() {
        return poModelDetail.size();
    }
    
    @Override
    public int getOtherItemCount() {
        return poModelDetailOthers.size();
    }

    @Override
    public Model_Inv_Stock_Request_Detail getDetailModel(int fnRow) {
        return poModelDetail.get(fnRow);

    }
//    @Override
    public ArrayList<Model_Inv_Stock_Request_Detail> getDetailModel() {
        return poModelDetail;
    }

    @Override
    public JSONObject setDetail(int fnRow, int fnCol, Object foData) {
        return poModelDetail.get(fnRow).setValue(fnCol, foData);
    }

    @Override     
    public JSONObject setDetail(int fnRow, String fsCol, Object foData) {
        return setDetail(fnRow, poModelDetail.get(fnRow).getColumn(fsCol), foData);
    }

    @Override
    public JSONObject searchDetail(int fnRow, String fsCol, String fsValue, boolean fbByCode) {
        return searchDetail(fnRow, poModelDetail.get(fnRow).getColumn(fsCol), fsValue, fbByCode);
    }

    @Override
    public JSONObject searchDetail(int fnRow, int fnCol, String fsValue, boolean fbByCode) {
        poJSON = new JSONObject();

        switch (fnCol) {
            case 3: //sBarCodex
                if(poModelMaster.getCategoryCode().isEmpty()){
                    poJSON.put("result", "error");
                    poJSON.put("message", "Please choose a category first..");
                    return poJSON;
                }
                System.out.println("searchDetail p_bWithUI = " + p_bWithUI);
                Inventory loInventory = new Inventory(poGRider, true);
                loInventory.setRecordStatus(psTranStatus);
                loInventory.setWithUI(p_bWithUI);
                String lsCondition = "a.sCategCd1 = " + SQLUtil.toSQL(poModelMaster.getCategoryCode()) + " AND a.sCategCd2 != " + SQLUtil.toSQL("0007");
                if(!poModelDetail.get(fnRow).getBrandName().isEmpty()){
                    lsCondition = MiscUtil.addCondition(lsCondition, "");
                }
                if(!poModelDetail.get(fnRow).getModelName().isEmpty()){
                    lsCondition = MiscUtil.addCondition(lsCondition, "");
                }
                poJSON = loInventory.searchRecordWithContition(fsValue, lsCondition, fbByCode);
                System.out.println("poJSON = " + poJSON);
                if (poJSON != null) {
                    for(int lnCtr = 0; lnCtr < poModelDetail.size(); lnCtr++){
                        if(poModelDetail.get(lnCtr).getStockID().equalsIgnoreCase((String) loInventory.getModel().getStockID())){
                             setDetail(lnCtr, "nQuantity", (Double.parseDouble(String.valueOf(poModelDetail.get(lnCtr).getQuantity())) + 1));
                            poJSON = new JSONObject();
                            poJSON.put("result", "error");
                            poJSON.put("message", "Inventory item request already added.");
                            return poJSON;
                        }
                    }

                    setDetail(fnRow, 3, (String) loInventory.getModel().getStockID());
                    setDetail(fnRow, "xBarCodex", (String) loInventory.getModel().getBarcode());
                    setDetail(fnRow, "xDescript", (String) loInventory.getModel().getDescription());
                    setDetail(fnRow, "xCategr01", (String) loInventory.getModel().getCategName1());
                    setDetail(fnRow, "xCategr02", (String) loInventory.getModel().getCategName2());
                    setDetail(fnRow, "xInvTypNm", (String) loInventory.getModel().getInvTypNm());
                    setDetail(fnRow, "xBrandNme", (String) loInventory.getModel().getBrandName());
                    setDetail(fnRow, "xModelNme", (String) loInventory.getModel().getModelName());
                    setDetail(fnRow, "xModelDsc", (String) loInventory.getMaster("xModelDsc"));
                    setDetail(fnRow, "xColorNme", (String) loInventory.getModel().getColorName());
                    setDetail(fnRow, "xMeasurNm", (String) loInventory.getModel().getMeasureName());
                    InvMaster loInvMaster = new InvMaster(poGRider, true);
                    loInvMaster.setRecordStatus(psTranStatus);
                    loInvMaster.setWithUI(p_bWithUI);
                    poJSON = loInvMaster.openRecord(loInventory.getModel().getStockID());
                        
//                    if (poJSON != null) {
                    if ("success".equals((String) poJSON.get("result"))) {
                        setDetail(fnRow, "cClassify", (String) loInvMaster.getModel().getClassify());
                        setDetail(fnRow, "nQtyOnHnd",  loInvMaster.getModel().getQtyOnHnd());
                        setDetail(fnRow, "nResvOrdr",  loInvMaster.getModel().getResvOrdr());
                        setDetail(fnRow, "nBackOrdr",  loInvMaster.getModel().getBackOrdr());
                        setDetail(fnRow, "nAvgMonSl",  loInvMaster.getModel().getAvgMonSl());
                        setDetail(fnRow, "nMaxLevel",  loInvMaster.getModel().getMaxLevel());
                    }else{
                        setDetail(fnRow, 3, "");
                        setDetail(fnRow, "xBarCodex", "");
                        setDetail(fnRow, "xDescript", "");
                        setDetail(fnRow, "xCategr01", "");
                        setDetail(fnRow, "xCategr02", "");
                        setDetail(fnRow, "xInvTypNm", "");
                        setDetail(fnRow, "xBrandNme", "");
                        setDetail(fnRow, "xModelNme", "");
                        setDetail(fnRow, "xModelDsc", "");
                        setDetail(fnRow, "xColorNme", "");
                        setDetail(fnRow, "xMeasurNm", "");
                    }
                    return poJSON;
                } else {
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "No record found.");
                    return poJSON;
                }
                
            case 26://brand
                Brand loBrand = new Brand(poGRider, true);
                loBrand.setRecordStatus(psTranStatus);
                poJSON = loBrand.searchRecord(fsValue, fbByCode);
                System.out.println("poJSON = " + poJSON);
                if (poJSON != null) {
//                    setDetail(fnRow, "sBrandIDx", (String) loBrand.getModel().getBrandID());
                    setDetail(fnRow, "xBrandNme", (String) loBrand.getModel().getDescription());
                } else {
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "No record found.");
                    return poJSON;
                }
                break;
            case 27://model
                Model loModel = new Model(poGRider, true);
                loModel.setRecordStatus(psTranStatus);
                poJSON = loModel.searchRecord(fsValue, fbByCode);
                System.out.println("poJSON = " + poJSON);
                if (poJSON != null) {
//                    setDetail(fnRow, "sModelIDx", (String) loModel.getModel().getModelID());
                    setDetail(fnRow, "xModelNme", (String) loModel.getModel().getModelDescription());
                } else {
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "No record found.");
                    return poJSON;
                }
                break;
        }
        return poJSON;
    }

    @Override
    public JSONObject searchWithCondition(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public JSONObject searchTransaction(String fsColumn, String fsValue, boolean fbByCode) {
        String lsCondition = "";
        String lsFilter = "";
        if (psTranStatus.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStatus.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psTranStatus.charAt(lnCtr)));
            }

            lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "a.cTranStat = " + SQLUtil.toSQL(psTranStatus);
        }

        String lsSQL = MiscUtil.addCondition(getSQL(), " a.sTransNox LIKE "
                + SQLUtil.toSQL(fsValue + "%") + " AND f.sCategCd1 = '0001' AND f.sCategCd2 != '0007' AND " + 
                "LEFT(a.sTransNox,4) LIKE " + SQLUtil.toSQL(poGRider.getBranchCode() + "%") +
                " AND " + lsCondition + "  GROUP BY a.sTransNox ASC") +
                " HAVING (SUM(e.nQuantity - (e.nIssueQty + e.nCancelld + e.nOrderQty))) > 0";
                
//        String lsSQL = MiscUtil.addCondition(getSQL(), " a.sTransNox LIKE "
//                + SQLUtil.toSQL(fsValue + "%") + " AND " + lsCondition + " AND f.sCategCd1 = '0001' AND f.sCategCd2 != '0007' GROUP BY a.sTransNox ASC");
    
        poJSON = new JSONObject();
        System.out.println("searchTransaction = " + lsSQL);
        if (p_bWithUI){
            poJSON = ShowDialogFX.Search(poGRider,
                    lsSQL,
                    fsValue,    
                    "Transaction No»Date»Refer No",
                    "sTransNox»dTransact»sReferNox",
                    "a.sTransNox»a.dTransact»a.sReferNox",
                    fbByCode ? 0 : 1);
            
            if (poJSON == null || "error".equals((String) poJSON.get("result"))) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;
            } else {
                return openTransaction((String) poJSON.get("sTransNox"));
            }
        }
        //use for testing 
        lsSQL += " LIMIT 1";
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            if (!loRS.next()){
                MiscUtil.close(loRS);
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;
            }
            
            lsSQL = loRS.getString("sTransNox");
            MiscUtil.close(loRS);
        } catch (SQLException ex) {
            Logger.getLogger(Inv_Request_SP.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return openTransaction(lsSQL);
    }

    @Override
    public JSONObject searchMaster(String fsCol, String fsVal, boolean fbByCode) {
        return searchMaster(poModelMaster.getColumn(fsCol), fsVal, fbByCode);
    }

    @Override
    public JSONObject searchMaster(int fnColumn, String fsValue, boolean fbByCode) {
        poJSON = new JSONObject();
        switch(fnColumn){
            case 3: //sCategrCd
                Category loCategory = new Category(poGRider, true);
                loCategory.setRecordStatus(psTranStatus);
                poJSON = loCategory.searchRecord(fsValue, fbByCode);

                if (poJSON != null){
                    setMaster(fnColumn, (String) loCategory.getMaster("sCategrCd"));
                    return setMaster("xCategrNm", (String)loCategory.getMaster("sDescript"));
                } else {
                    
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "No record found.");
                    return poJSON;
                }
            case 4: //sInvTypCd
                Inv_Type loType = new Inv_Type(poGRider, true);
                loType.setRecordStatus(psTranStatus);
                poJSON = loType.searchRecord(fsValue, fbByCode);

                if (poJSON != null){

                    setMaster(fnColumn, (String) loType.getMaster("sInvTypCd"));
                    return setMaster("xInvTypNm", (String)loType.getMaster("sDescript"));
                } else {
                    
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "No record found.");
                    return poJSON;
                }
                
            default:
                return null;
                
        }
    }

    @Override
    public Model_Inv_Stock_Request_Master getMasterModel() {
        return poModelMaster;
    }

    @Override
    public JSONObject setMaster(int fnCol, Object foData) {
        return poModelMaster.setValue(fnCol, foData);
    }

    @Override
    public JSONObject setMaster(String fsCol, Object foData) {
        return poModelMaster.setValue(fsCol, foData);
    }

    @Override
    public int getEditMode() {
        return pnEditMode;
    }

    @Override
    public void setTransactionStatus(String fsValue) {
        psTranStatus = fsValue;
    }

    public JSONObject OpenModelDetail(String fsTransNo) {

        try {
            String lsSQL = MiscUtil.addCondition(new Model_Inv_Stock_Request_Detail(poGRider).getSQL(), "a.sTransNox = " + SQLUtil.toSQL(fsTransNo));
            lsSQL = MiscUtil.addCondition(lsSQL, "j.sBranchCd = " + SQLUtil.toSQL(poModelMaster.getBranchCode()));
            lsSQL = lsSQL + " AND (a.nQuantity - (a.nIssueQty + a.nCancelld + a.nOrderQty)) > 0";
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if(category_type == RequestControllerFactory.RequestCategoryType.WITHOUT_ROQ){
                poModelDetail = new ArrayList<>();
                while (loRS.next()) {
                    poModelDetail.add(new Model_Inv_Stock_Request_Detail(poGRider));
                    poJSON = poModelDetail.get(poModelDetail.size() - 1).openRecord(loRS.getString("sTransNox"), loRS.getString("sStockIDx"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }else{
                poModelDetailOthers = new ArrayList<>();
                while (loRS.next()) {
                    poModelDetailOthers.add(new Model_Inv_Stock_Request_Detail(poGRider));
                    poJSON = poModelDetailOthers.get(poModelDetailOthers.size() - 1).openRecord(loRS.getString("sTransNox"), loRS.getString("sStockIDx"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
                
            }
            
            

            return poJSON;

        } catch (SQLException ex) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", ex.getMessage());

            return poJSON;
        } 
    }
    

    public JSONObject SearchDetailRequest(String fsTransNo, String fsStockID) {

        poJSON = new JSONObject();
        try {
            String lsSQL = MiscUtil.addCondition(poModelDetail.get(0).makeSQL(), "sTransNox = " + SQLUtil.toSQL(fsTransNo));
            lsSQL = MiscUtil.addCondition(lsSQL, "sStockIDx = " + SQLUtil.toSQL(fsStockID));
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poModelDetail =  new ArrayList<>();
            while (loRS.next()) {

                poModelDetail.add(new Model_Inv_Stock_Request_Detail(poGRider));
                poJSON = poModelDetail.get(poModelDetail.size() - 1).openRecord(loRS.getString("sTransNox"));
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                
                poJSON.put("result", "success");
                poJSON.put("message", "Record successfully loaded to Detail.");
            }

            return poJSON;

        } catch (SQLException ex) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", ex.getMessage());

            return poJSON;
        }
    }
    

    //process for inventory stock request
    /* This function use for browsing Inventory Stock Request 
     * fetching inventory request detail and set data for Inventory Stock Request Cancel
     * fetching detail by stock id
    */
    public JSONObject OpenModelDetailByStockID(String fsTransNo, String fsStockID) {

        try {
            String lsSQL = MiscUtil.addCondition(new Model_Inv_Stock_Request_Detail(poGRider).makeSelectSQL(), "sTransNox = " + SQLUtil.toSQL(fsTransNo));
            lsSQL = MiscUtil.addCondition(lsSQL, "sStockIDx = " + SQLUtil.toSQL(fsStockID));
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poModelDetail =  new ArrayList<>();
            while (loRS.next()) {

                poModelDetail.add(new Model_Inv_Stock_Request_Detail(poGRider));
                poJSON = poModelDetail.get(poModelDetail.size() - 1).openRecord(loRS.getString("sTransNox"), loRS.getString("sStockIDx"));
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
            
            return poJSON;

        } catch (SQLException ex) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", ex.getMessage());

            return poJSON;
        }
    }

    public JSONObject AddModelDetail() {
        poJSON = new JSONObject();
        if(category_type == RequestControllerFactory.RequestCategoryType.WITHOUT_ROQ){
            if (poModelDetail.isEmpty()){
                poModelDetail.add(new Model_Inv_Stock_Request_Detail(poGRider));
                poModelDetail.get(0).newRecord();
                poModelDetail.get(0).setTransactionNumber(poModelMaster.getTransactionNumber());
                poJSON.put("result", "success");
                poJSON.put("message", "Inventory request add record.");


            } else {
                Validator_Inv_Stock_Request_MC_Detail validator = new Validator_Inv_Stock_Request_MC_Detail(poModelDetail.get(poModelDetail.size()-1));
                if (!validator.isEntryOkay()){
                    poJSON.put("result", "error");
                    poJSON.put("message", validator.getMessage());
                    return poJSON;

                }
                poModelDetail.add(new Model_Inv_Stock_Request_Detail(poGRider));
                poModelDetail.get(poModelDetail.size()-1).newRecord();
                poModelDetail.get(poModelDetail.size() - 1).setTransactionNumber(poModelMaster.getTransactionNumber());

                poJSON.put("result", "success");
                poJSON.put("message", "Inventory request add record.");
            }
            System.out.println(poModelDetail.size());
            
        }else{
            poJSON = AddModelDetailROQ();
        }

        return poJSON;
    }
    private JSONObject AddModelDetailROQ(){
        poJSON = new JSONObject();
        if (poModelDetail.isEmpty()){
            poModelDetail.add(new Model_Inv_Stock_Request_Detail(poGRider));
            poModelDetail.get(0).newRecord();
            poModelDetail.get(0).setTransactionNumber(poModelMaster.getTransactionNumber());
            poJSON.put("result", "success");
            poJSON.put("message", "Inventory request add record.");
            

        } else {
            
            poModelDetail.add(new Model_Inv_Stock_Request_Detail(poGRider));
            poModelDetail.get(poModelDetail.size()-1).newRecord();
            poModelDetail.get(poModelDetail.size() - 1).setTransactionNumber(poModelMaster.getTransactionNumber());

            poJSON.put("result", "success");
            poJSON.put("message", "Inventory request add record.");
        }
        System.out.println(poModelDetail.size());

        return poJSON;
    }
    private JSONObject SaveDetail(){
        poJSON = new JSONObject();
        // Check if there are items to process
//        if (getItemCount() >= 1) {
//            // Loop in reverse order to avoid index shifting when removing elements
//            
//           
//            
//        } else {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Unable to Save empty Transaction.");
//            return poJSON;
//        }
 // After cleaning, check if any valid items are left
            if(category_type == RequestControllerFactory.RequestCategoryType.WITHOUT_ROQ){
                for (int lnCtr = getItemCount() - 1; lnCtr >= 0; lnCtr--) {
                    // Check if StockID or Barcode is empty
                    if (poModelDetail.get(lnCtr).getStockID().isEmpty() || poModelDetail.get(lnCtr).getBarcode().isEmpty()) {
                        // Remove the empty record
                        poModelDetail.remove(lnCtr);
                    }
                }
                    
                if (getItemCount() > 0) {
                    poJSON = saveDetailWithoutROQ();
                } else {
                    restoreData();
                    poJSON.put("result", "error");
                    poJSON.put("message", "Unable to Save empty Transaction.");
                    return poJSON;
                }
            }else{
                for (int lnCtr = getOtherItemCount()- 1; lnCtr >= 0; lnCtr--) {
            // Check if StockID or Barcode is empty
                    if (poModelDetailOthers.get(lnCtr).getStockID().isEmpty() || poModelDetailOthers.get(lnCtr).getBarcode().isEmpty()) {
                        // Remove the empty record
                        poModelDetailOthers.remove(lnCtr);
                    }
                }
                if (getOtherItemCount() > 0) {
                    poJSON = saveDetailWithROQ();
                } else {
                    restoreROQData();
                    poJSON.put("result", "error");
                    poJSON.put("message", "Unable to Save empty Transaction.");
                    return poJSON;
                }
            }
        
        return poJSON;
    }
    private JSONObject saveDetailWithoutROQ(){
        poJSON = new JSONObject();
        
        for (int lnCtr = 0; lnCtr < getItemCount(); lnCtr++) {
            poModelDetail.get(lnCtr).setEditMode(EditMode.ADDNEW);
            poModelDetail.get(lnCtr).setEntryNumber(lnCtr + 1);
            Validator_Inv_Stock_Request_MC_Detail validator = new Validator_Inv_Stock_Request_MC_Detail(poModelDetail.get(poModelDetail.size()-1));
                if (!validator.isEntryOkay()){
                    restoreData();
                    poJSON.put("result", "error");
                    poJSON.put("message", validator.getMessage());
                    return poJSON;

                }
            poJSON = poModelDetail.get(lnCtr).saveRecord();


            if ("error".equals((String) poJSON.get("result"))) {
                if (!pbWthParent) {
                    restoreData();
                    poGRider.rollbackTrans();
                }
                return poJSON;
            }
            poJSON.put("result", "success");
            poJSON.put("message", "Save item record successfuly.");
        }
        return poJSON;
    }
    private JSONObject saveDetailWithROQ(){
        poJSON = new JSONObject();
        System.out.print("category_type = " + category_type);
        boolean allZero = true;
        for (Model_Inv_Stock_Request_Detail items : poModelDetailOthers) {
            if (Integer.parseInt(items.getQuantity().toString()) != 0) {
                allZero = false;  // If any product's quantity is not zero, set flag to false
                break;  // No need to check further if one non-zero quantity is found
            }
        }
        if (allZero) {
            restoreROQData();
            poJSON.put("result", "error");
            poJSON.put("message", "Quantities are currently set to 0. Update them to continue.");
            return poJSON;
        }
        
        // Proceed with saving remaining items
        roqSaveCount = 0;
        for (int lnCtr = 0; lnCtr < getOtherItemCount(); lnCtr++) {
            System.out.println("getItemCount() = " + getItemCount());
            poModelDetailOthers.get(lnCtr).setEditMode(EditMode.ADDNEW);
            poModelDetailOthers.get(lnCtr).setEntryNumber(lnCtr + 1);
            poJSON = poModelDetailOthers.get(lnCtr).saveRecord();


            if ("error".equals((String) poJSON.get("result"))) {
                if (!pbWthParent) {
                    restoreROQData();
                    poGRider.rollbackTrans();
                }
                return poJSON;
            }
           roqSaveCount++;

            poJSON.put("result", "success");
            poJSON.put("message", "Save item record successfuly.");
        
         }
        return poJSON;
    }
    private JSONObject saveInventoryTrans(){
        poJSON = new JSONObject();
        
        InventoryTrans loTrans = new InventoryTrans(poGRider, pbWthParent);
        loTrans.newTransaction();
        int pnCtr = 0;
        for (int lnCtr = 0; lnCtr < getItemCount(); lnCtr++) {
            double lnQuantity = Double.parseDouble(String.valueOf(getDetailModel(lnCtr).getQuantity()));
            double lnCancelld = Double.parseDouble(String.valueOf(getDetailModel(lnCtr).getCancelled()));
            if ((lnQuantity - lnCancelld) > 0){
                loTrans.setDetail(pnCtr, "sStockIDx", getDetailModel(lnCtr).getStockID());
                loTrans.setDetail(pnCtr, "nQuantity", (lnQuantity - lnCancelld));
                pnCtr++;
            }
        }
        
        return loTrans.BranchOrder(poModelMaster.getTransactionNumber(), poModelMaster.getTransaction(), EditMode.ADDNEW);
    }       
    
    /**
     *
     * @param fnRow
     */
    @Override
    public void RemoveModelDetail(int fnRow) {
        if (poModelDetail.size() >= 1) {
            poModelDetail.remove(fnRow);
            if (poModelDetail.isEmpty()) {
                AddModelDetail();
            }
        }
    }
    public JSONObject deleteRecord() {
        poJSON = new JSONObject();
        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE) {
            String lsSQLs = MiscUtil.addCondition(new Model_Inv_Stock_Request_Detail(poGRider).getSQL(), "a.sTransNox = " + SQLUtil.toSQL(poModelMaster.getTransactionNumber()));
            ResultSet loRS = poGRider.executeQuery(lsSQLs);
            backupRecords = new ArrayList<>();
            try {
                int lnCtr = 0;
                while (loRS.next()) {

                    backupRecords.add(new Model_Inv_Stock_Request_Detail(poGRider));
                    poJSON = backupRecords.get(lnCtr).openRecord(loRS.getString("sTransNox"), loRS.getString("sStockIDx"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                    lnCtr++;
                }
            } catch (SQLException ex) {
                Logger.getLogger(Inv_Request_SP.class.getName()).log(Level.SEVERE, null, ex);
            }
            Model_Inv_Stock_Request_Detail model = new Model_Inv_Stock_Request_Detail(poGRider);
            String lsSQL = "DELETE FROM " + model.getTable()+
                                " WHERE sTransNox = " + SQLUtil.toSQL(poModelMaster.getTransactionNumber());

            if (!lsSQL.equals("")){
//                 backupRecords.addAll(poModelDetail);
                if (poGRider.executeQuery(lsSQL, model.getTable(), poGRider.getBranchCode(), "") > 0) {
                    poJSON.put("result", "success");
                    poJSON.put("message", "Record deleted successfully.");
                } 
            }
        }
        
        return poJSON;
    }  
    
    // Method to restore records from backup
    public JSONObject restoreDeletedRecords() {
        poJSON =  new JSONObject();
        if (!backupRecords.isEmpty()) {
            if (!pbWthParent) {
                poGRider.beginTrans();
            }
            poModelDetail.clear();  // Clear the current details
            poModelDetail.addAll(backupRecords);  // Restore from backup
            poJSON = SaveDetail();
            
            if ("error".equals((String) poJSON.get("result"))) {
                if (!pbWthParent) {
                    poGRider.rollbackTrans();
                }
            }
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
            
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void setType(RequestControllerFactory.RequestType types) {
        type = types;
    }
    
    
    @Override
    public void setCategoryType(RequestControllerFactory.RequestCategoryType type) {
        category_type = type;
    }

    @Override
    public void cancelUpdate(){
        poJSON =  new JSONObject();
        if (!backupRecords.isEmpty()) {
            if (!pbWthParent) {
                poGRider.beginTrans();
            }
            for(int lnCtr1 = 0; lnCtr1 < backupRecords.size(); lnCtr1++){
                backupRecords.get(lnCtr1).setApproved(0);
            }
            
            poModelDetail.clear();  // Clear the current details
            poModelDetail.addAll(backupRecords);  // Restore from backup
            poJSON = SaveDetail();
            
            if ("error".equals((String) poJSON.get("result"))) {
                if (!pbWthParent) {
                    poGRider.rollbackTrans();
                }
            }
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
            
        }
//        poJSON.put("result", "success")
    }
    
    private void restoreData(){
        if (!backupRecords.isEmpty()) {
            if (!pbWthParent) {
                poGRider.beginTrans();
            }
            for(int lnCtr = 0; lnCtr < getItemCount() && lnCtr < backupRecords.size(); lnCtr++){
                Model_Inv_Stock_Request_Detail model = poModelDetail.get(lnCtr);
                if(model.getStockID().equalsIgnoreCase(backupRecords.get(lnCtr).getStockID())){
                    backupRecords.get(lnCtr).setApproved(model.getApproved());
                }
            }
            
            poModelDetail.clear();  // Clear the current details
            poModelDetail.addAll(backupRecords);  // Restore from backup
//            poJSON = SaveDetail();
            
            if ("error".equals((String) poJSON.get("result"))) {
                if (!pbWthParent) {
                    poGRider.rollbackTrans();
                }
            }
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
            
        }
    
    }
    
    private void restoreROQData(){
        if (!backupRecords.isEmpty()) {
            if (!pbWthParent) {
                poGRider.beginTrans();
            }
            for(int lnCtr = 0; lnCtr < getItemCount() && lnCtr < backupRecords.size(); lnCtr++){
                Model_Inv_Stock_Request_Detail model = poModelDetailOthers.get(lnCtr);
                if(model.getStockID().equalsIgnoreCase(backupRecords.get(lnCtr).getStockID())){
                    backupRecords.get(lnCtr).setApproved(model.getApproved());
                }
            }
            
            poModelDetailOthers.clear();  // Clear the current details
            poModelDetailOthers.addAll(backupRecords);  // Restore from backup
//            poJSON = SaveDetail();
            
            if ("error".equals((String) poJSON.get("result"))) {
                if (!pbWthParent) {
                    poGRider.rollbackTrans();
                }
            }
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
            
        }
    
    }
    @Override
    public JSONObject loadAllInventoryMinimumLevel(){
        poJSON = new JSONObject();
        try {
            String lsSQL = getSQL_Detail();
            lsSQL = MiscUtil.addCondition(lsSQL, "a.nQtyOnHnd < a.nMinLevel AND  b.sCategCd1 = '0001' AND b.sCategCd2 != '0007'");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            System.out.println(" " + lsSQL);
            poModelDetail =  new ArrayList<>();
            while (loRS.next()) {
                poModelDetail.add(new Model_Inv_Stock_Request_Detail(poGRider));
                poModelDetail.get(poModelDetail.size() - 1).newRecord();
                System.out.println(poModelMaster.getTransactionNumber());
                poModelDetail.get(poModelDetail.size() - 1).setTransactionNumber(poModelMaster.getTransactionNumber());
                poModelDetail.get(poModelDetail.size() - 1).setStockID((String) loRS.getString("sStockIDx"));
                poModelDetail.get(poModelDetail.size() - 1).setBarcode((String) loRS.getString("xBarCodex"));
                poModelDetail.get(poModelDetail.size() - 1).setDescription((String) loRS.getString("xDescript"));
                poModelDetail.get(poModelDetail.size() - 1).setCategoryName((String) loRS.getString("xCategr01"));
                poModelDetail.get(poModelDetail.size() - 1).setCategoryName2((String) loRS.getString("xCategr02"));
                poModelDetail.get(poModelDetail.size() - 1).setCategoryType((String) loRS.getString("xInvTypNm"));
                poModelDetail.get(poModelDetail.size() - 1).setBrandName((String) loRS.getString("xBrandNme"));
                poModelDetail.get(poModelDetail.size() - 1).setModelName((String) loRS.getString("xModelNme"));
                poModelDetail.get(poModelDetail.size() - 1).setColorName((String) loRS.getString("xColorNme"));
                poModelDetail.get(poModelDetail.size() - 1).setMeasureName((String) loRS.getString("xMeasurNm"));
                poModelDetail.get(poModelDetail.size() - 1).setClassify((String) loRS.getString("cClassify"));
                poModelDetail.get(poModelDetail.size() - 1).setQuantityOnHand(loRS.getInt("nQtyOnHnd"));
                poModelDetail.get(poModelDetail.size() - 1).setReservedOrder(loRS.getInt("nResvOrdr"));
                poModelDetail.get(poModelDetail.size() - 1).setBackOrder(loRS.getInt("nBackOrdr"));
                poModelDetail.get(poModelDetail.size() - 1).setAverageMonthlySalary(loRS.getInt("nAvgCostx"));
                poJSON = poModelDetail.get(poModelDetail.size() - 1).setMaximumLevel(loRS.getInt("nMaxLevel"));
                
                if ("error".equals((String) poJSON.get("result"))) {
                    if (!pbWthParent) {
                        poGRider.rollbackTrans();
                    }
                }
                poJSON.put("result", "success");
                poJSON.put("message", "Record successfully loaded to Detail.");
            }

            return poJSON;

        } catch (SQLException ex) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", ex.getMessage());

            return poJSON;
        }
    }
    private String getSQL(){
        return "SELECT " +
            "  a.sTransNox, " +
            "  a.sBranchCd, " +
            "  a.sCategrCd, " +
            "  a.dTransact, " +
            "  a.sReferNox, " +
            "  a.sRemarksx, " +
            "  a.sIssNotes, " +
            "  a.nCurrInvx, " +
            "  a.nEstInvxx, " +
            "  a.sApproved, " +
            "  a.dApproved, " +
            "  a.sAprvCode, " +
            "  a.nEntryNox, " +
            "  a.sSourceCd, " +
            "  a.sSourceNo, " +
            "  a.cConfirmd, " +
            "  a.cTranStat, " +
            "  a.dStartEnc, " +
            "  a.sModified, " +
            "  a.dModified, " +
            "  b.sBranchNm    xBranchNm, " +
            "  c.sDescript    xCategrNm, " +
            "  d.sDescript    xCategNm1, " +
            " f.sBarCodex " +
            "FROM Inv_Stock_Request_Master a " +
            "  LEFT JOIN Branch b " +
            "    ON a.sBranchCd = b.sBranchCd " +
            "  LEFT JOIN Category c " +
            "    ON a.sCategrCd = c.sCategrCd " +
            "  LEFT JOIN Category_Level2 d " +
            "    ON c.sCategrCd = d.sMainCatx " +
            "  LEFT JOIN Inv_Stock_Request_Detail e " +
            "	on  e.sTransNox = a.sTransNox " +
            "  LEFT JOIN Inventory f " +
            "	on f.sStockIDx = e.sStockIDx";
    }
    
    private String getSQL_Detail(){
        return "SELECT " +
                "  a.sStockIDx, " +
                "  a.sBranchCd, " +
                "  a.nQtyOnHnd, " +
                "  a.nMinLevel, " +
                "  a.nMaxLevel, " +
                "  a.nAvgMonSl, " +
                "  a.nAvgCostx, " +
                "  a.cClassify, " +
                "  a.nBackOrdr, " +
                "  a.nResvOrdr, " +
                "  b.sBarCodex xBarCodex, " +
                "  b.sDescript xDescript, " +
                "  c.sDescript xCategr01, " +
                "  d.sDescript xCategr02, " +
                "  e.sDescript xBrandNme, " +
                "  f.sDescript xModelNme, " +
                "  g.sDescript xColorNme, " +
                "  h.sMeasurNm xMeasurNm, " +
                "  i.sDescript xInvTypNm " +
                "FROM  Inv_Master a " +
                "  LEFT JOIN Inventory b " +
                "    ON a.sStockIDx = b.sStockIDx " +
                "  LEFT JOIN Category c " +
                "    ON b.sCategCd1 = c.sCategrCd " +
                "  LEFT JOIN Category_Level2 d " +
                "    ON b.sCategCd2 = d.sCategrCd " +
                "  LEFT JOIN Brand e " +
                "    ON b.sBrandIDx = e.sBrandIDx " +
                "  LEFT JOIN Model f " +
                "    ON b.sModelIDx = f.sModelIDx " +
                "  LEFT JOIN Color g " +
                "    ON b.sColorIDx = g.sColorIDx " +
                "  LEFT JOIN Measure h " +
                "    ON b.sMeasurID = h.sMeasurID " +
                "  LEFT JOIN Inv_Type i " +
                "    ON d.sInvTypCd = i.sInvTypCd" ;
    }
    

    @Override
    public JSONObject setDetailOthers(int fnRow, String fsCol, Object foData) {
        return poModelDetailOthers.get(fnRow).setValue(fsCol, foData);
    }

    @Override
    public JSONObject setDetailOthers(int fnRow, int fnCol, Object foData) {
        return poModelDetailOthers.get(fnRow).setValue(fnCol, foData);
    }
    @Override
    public ArrayList<Model_Inv_Stock_Request_Detail> getDetailModelOthers() {
        return poModelDetailOthers;
    }
 

    @Override
    public JSONObject AddDetailOthers(int fnRow) {
        JSONObject poJSON = new JSONObject();

        // Ensure the detail list (poModelDetailOthers) is initialized
//        if (poModelDetailOthers.isEmpty()) {
//            Model_Inv_Stock_Request_Detail initialDetail = new Model_Inv_Stock_Request_Detail(poGRider);
//            initialDetail.newRecord();
//            initialDetail.setTransactionNumber(poModelMaster.getTransactionNumber());
//            poModelDetailOthers.add(initialDetail);
//
//            poJSON.put("result", "success");
//            poJSON.put("message", "Initial empty detail record added.");
//            return poJSON;
//        }

        // Get the stock ID and quantity for the new detail
        Model_Inv_Stock_Request_Detail newDetail = poModelDetail.get(fnRow);
        String newStockID = newDetail.getStockID();
        int newQuantity = (int) newDetail.getQuantity();

        boolean replacedEmpty = false;
        boolean isDuplicate = false;

        // Check poModelDetailOthers for empty records or duplicates
        for (int lnCtr = 0; lnCtr < poModelDetailOthers.size(); lnCtr++) {
            Model_Inv_Stock_Request_Detail existingDetail = poModelDetailOthers.get(lnCtr);

            // Replace the first empty record
            if ((existingDetail.getStockID() == null || existingDetail.getStockID().isEmpty()) && !replacedEmpty) {
                poModelDetailOthers.set(lnCtr, newDetail); // Replace with new detail
                replacedEmpty = true;

                poJSON.put("result", "success");
                poJSON.put("message", "Replaced initial empty record with new detail.");
                return poJSON;
            }

            // Check for duplicates
            if (newStockID.equalsIgnoreCase(existingDetail.getStockID())) {
                if (newQuantity > 0) {
                    // Update the quantity
                    existingDetail.setQuantity(newQuantity);

                    poJSON.put("result", "success");
                    poJSON.put("message", "Inventory item already exists. Quantity updated.");
                } else {
                    // Remove the record if the new quantity is 0
                    poModelDetailOthers.remove(lnCtr);

                    poJSON.put("result", "success");
                    poJSON.put("message", "Inventory item removed due to zero quantity.");
                }
                isDuplicate = true;
                break;
            }
        }

        // If no empty record was replaced, no duplicate was found, and quantity > 0, add the new detail
        if (!replacedEmpty && !isDuplicate && newQuantity > 0) {
            poModelDetailOthers.add(newDetail);

            poJSON.put("result", "success");
            poJSON.put("message", "New inventory detail added.");
        }

        return poJSON;
    }





}
