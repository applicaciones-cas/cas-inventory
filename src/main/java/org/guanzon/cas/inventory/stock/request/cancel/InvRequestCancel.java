package org.guanzon.cas.inventory.stock.request.cancel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GTranDet;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Req_Cancel_Detail;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Req_Cancel_Master;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inventory.stock.Inv_Request;
import org.guanzon.cas.inventory.stock.request.RequestControllerFactory;
import org.json.simple.JSONObject;

/**
 *
 * @author Unclejo
 */
public class InvRequestCancel implements GTranDet {

    GRider poGRider;
    boolean pbWthParent;
    int pnEditMode;
    String psTranStatus;

    private boolean p_bWithUI;
    Inv_Request poRequest;
    Model_Inv_Stock_Req_Cancel_Master poModelMaster;
    ArrayList<Model_Inv_Stock_Req_Cancel_Master> poMasterList;
    ArrayList<Model_Inv_Stock_Req_Cancel_Detail> poModelDetail;
    private List<Model_Inv_Stock_Req_Cancel_Detail> backupRecords = new ArrayList<>();

    RequestControllerFactory.RequestType type;
    RequestControllerFactory.RequestCategoryType category_type;
    JSONObject poJSON;
    private boolean pbIsHistory = false;

    public void isHistory(boolean fbValue) {
        pbIsHistory = fbValue;
    }

    public void setWithUI(boolean fbValue) {
        p_bWithUI = fbValue;
    }

    public InvRequestCancel(GRider foGRider, boolean fbWthParent) {
        poGRider = foGRider;
        pbWthParent = fbWthParent;
        poModelMaster = new Model_Inv_Stock_Req_Cancel_Master(foGRider);
        poModelDetail = new ArrayList<>();
        poModelDetail.add(new Model_Inv_Stock_Req_Cancel_Detail(foGRider));
        poRequest = new Inv_Request(foGRider, fbWthParent);
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

        poJSON = new JSONObject();

        poRequest = new Inv_Request(poGRider, pbWthParent);
        poModelDetail = new ArrayList<>();
        poModelDetail.add(new Model_Inv_Stock_Req_Cancel_Detail(poGRider));
        poModelDetail.get(0).newRecord();
        poJSON = poModelDetail.get(0).setTransactionNumber(poModelMaster.getTransactionNumber());
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record to load.");
            return poJSON;

        }

        poJSON.put("result", "success");
        poJSON.put("message", "initialized new record.");
        pnEditMode = EditMode.ADDNEW;
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
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        pnEditMode = EditMode.READY;
        return poJSON;

    }

    @Override
    public JSONObject updateTransaction() {
        poJSON = new JSONObject();
        if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode.");
            return poJSON;
        }
        
        if ("error".equals((String) isProcessed("update").get("result"))) {
            return poJSON;
        }
        pnEditMode = EditMode.UPDATE;
        poJSON.put("result", "success");
        poJSON.put("message", "Update mode success.");
        return poJSON;
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
        if ("error".equals((String) poJSON.get("result"))) {
            restoreData();
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }
            return poJSON;
        }
        poModelMaster.setEntryNumber(poModelDetail.size());
        poJSON = poModelMaster.saveRecord();
        if ("success".equals((String) poJSON.get("result"))) {
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
        } else {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }
            restoreData();
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Save Transaction.");
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
        if (poModelMaster.getEditMode() == EditMode.READY
                || poModelMaster.getEditMode() == EditMode.UPDATE) {

            if (poModelMaster.getTransactionStatus().equalsIgnoreCase(TransactionStatus.STATE_CLOSED)) {
                poJSON.put("result", "success");
                poJSON.put("message", "Unable to close proccesed transaction.");
                return poJSON;

            }
            if ("error".equals((String) isProcessed("post").get("result"))) {
                return poJSON;
            }

            poJSON = poModelMaster.setModifiedBy(poGRider.getUserID());
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            poJSON = poModelMaster.setModifiedDate(poGRider.getServerDate());
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            poJSON = poModelMaster.setTransactionStatus(TransactionStatus.STATE_CLOSED);
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
//            poJSON = transactRequest();
            if (getItemCount() >= 1) {
                for (int lnCtr = 0; lnCtr <= getItemCount() - 1; lnCtr++) {
                    poModelDetail.get(lnCtr).setTransactionNumber(poModelMaster.getTransactionNumber());
                    poModelDetail.get(lnCtr).setEntryNumber(lnCtr + 1);

                    int lnQty = Integer.parseInt(poModelDetail.get(lnCtr).getQuantity().toString());
                    poJSON = updateRequestDetail(poModelDetail.get(lnCtr).getStockID(), lnQty);
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                    poJSON.put("result", "success");
                    poJSON.put("message", "Record save successfully");

                }
                poJSON = poRequest.openTransaction(poModelMaster.getOrderNumber());
                if ("error".equals((String) poJSON.get("result"))) {
                    if (!pbWthParent) {
                        poGRider.rollbackTrans();
                    }
                    return poJSON;
                }
                if (getItemCount() == poRequest.getItemCount()) {
                    poRequest.getMasterModel().setTransactionStatus(TransactionStatus.STATE_CANCELLED);
                    poJSON = poRequest.getMasterModel().saveRecord();
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to Save empty Transaction.");
                return poJSON;
            }
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            poModelMaster.setEntryNumber(poModelDetail.size());

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

            poJSON = poModelMaster.setModifiedBy(poGRider.getUserID());
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            poJSON = poModelMaster.setModifiedDate(poGRider.getServerDate());
            if ("error".equals((String) poJSON.get("result"))) {
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
//            poJSON = transactRequest();
            if (getItemCount() >= 1) {
                for (int lnCtr = 0; lnCtr <= getItemCount() - 1; lnCtr++) {
                    poModelDetail.get(lnCtr).setTransactionNumber(poModelMaster.getTransactionNumber());
                    poModelDetail.get(lnCtr).setEntryNumber(lnCtr + 1);

                    int lnQty = Integer.parseInt(poModelDetail.get(lnCtr).getQuantity().toString());
                    poJSON = updateRequestDetail(poModelDetail.get(lnCtr).getStockID(), lnQty);
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                    poJSON.put("result", "success");
                    poJSON.put("message", "Record save successfully");

                }
                poJSON = poRequest.openTransaction(poModelMaster.getOrderNumber());
                if ("error".equals((String) poJSON.get("result"))) {
                    if (!pbWthParent) {
                        poGRider.rollbackTrans();
                    }
                    return poJSON;
                }
                if (getItemCount() == poRequest.getItemCount()) {
                    poRequest.getMasterModel().setTransactionStatus(TransactionStatus.STATE_CANCELLED);
                    poJSON = poRequest.getMasterModel().saveRecord();
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to Save empty Transaction.");
                return poJSON;
            }
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            poModelMaster.setEntryNumber(poModelDetail.size());

            poJSON = poModelMaster.saveRecord();
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

            poJSON = poModelMaster.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    private JSONObject isProcessed(String lsMessage) {
        poJSON = new JSONObject();
        if (poModelMaster.getTransactionStatus().equalsIgnoreCase(TransactionStatus.STATE_POSTED)
                || poModelMaster.getTransactionStatus().equalsIgnoreCase(TransactionStatus.STATE_CANCELLED)
                || poModelMaster.getTransactionStatus().equalsIgnoreCase(TransactionStatus.STATE_VOID)) {

            poJSON.put("result", "error");
            poJSON.put("message", "Unable to " + lsMessage + " proccesed transaction.");
            return poJSON;
        }
        poJSON.put("result", "success");
        poJSON.put("message", "Okay.");
        return poJSON;
    }

    @Override
    public int getItemCount() {
        return poModelDetail.size();
    }

    @Override
    public Model_Inv_Stock_Req_Cancel_Detail getDetailModel(int fnRow) {
        return poModelDetail.get(fnRow);
    }
//    @Override
    public ArrayList<Model_Inv_Stock_Req_Cancel_Detail> getDetailModel() {
        return poModelDetail;
    }

    @Override
    public JSONObject setDetail(int fnRow, int fnCol, Object foData) {
        poJSON = new JSONObject();
        if (fnCol == 5) {

            int lnQty = Integer.parseInt(String.valueOf(foData));
            int nIssueQty = Integer.parseInt((String.valueOf(poModelDetail.get(fnRow).getIssueQuantity()).isEmpty() ? "0" : String.valueOf(poModelDetail.get(fnRow).getIssueQuantity())));
            int nCancelld = Integer.parseInt((String.valueOf(poModelDetail.get(fnRow).getQuantity()).isEmpty() ? "0" : String.valueOf(poModelDetail.get(fnRow).getQuantity())));
            int nOrderQty = Integer.parseInt((String.valueOf(poModelDetail.get(fnRow).getOrderQuantity()).isEmpty() ? "0" : String.valueOf(poModelDetail.get(fnRow).getOrderQuantity())));
            int nQuantityRequest = Integer.parseInt((String.valueOf(poModelDetail.get(fnRow).getQuantityRequest()).isEmpty() ? "0" : String.valueOf(poModelDetail.get(fnRow).getQuantityRequest())));

            // Recompute 'nUnserved' quantity based on nQuantityRequest
            int nUnserved = nQuantityRequest - (nIssueQty + nCancelld + nOrderQty);

            // Ensure lnQty is capped by nQuantityRequest
            //check lnQty input value is lesser than or equal to unserve qty
            if ((lnQty) < nQuantityRequest) {
            //if true save the qty value of foData
                System.out.println("fnCol <= " + foData);

                nUnserved = computeUnserved(nQuantityRequest, nIssueQty, lnQty, nOrderQty);
            } else {
                lnQty = nQuantityRequest; // Prevent lnQty from exceeding the total request
                nUnserved = computeUnserved(nQuantityRequest, nIssueQty, lnQty, nOrderQty);
                System.out.println("fnCol >= " + nUnserved);
                poJSON.put("result", "error");
                poJSON.put("message", "Quantity exceeds unprocessed quantity. The quantity has been set to an unprocessed value.");

            }

            poModelDetail.get(fnRow).setUnserve(nUnserved);
            poModelDetail.get(fnRow).setValue(fnCol, lnQty);
            return poJSON;
        }
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

            case 3: //sBarCodex/sStockID
                if (poModelMaster.getOrderNumber().isEmpty()) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No order was discovered. Please search for order first.");
                    return poJSON;
                }
                String lsSQL = new Model_Inv_Stock_Request_Detail(poGRider).getSQL();

                lsSQL = MiscUtil.addCondition(lsSQL, "a.sTransNox = " + SQLUtil.toSQL(poModelMaster.getOrderNumber()));
                if (fbByCode) {
                    lsSQL = MiscUtil.addCondition(lsSQL, "b.sBarCodex LIKE " + SQLUtil.toSQL("%" + fsValue + "%"));
                } else {
                    lsSQL = MiscUtil.addCondition(lsSQL, "b.sDescript LIKE " + SQLUtil.toSQL("%" + fsValue + "%"));
                }
                
                lsSQL = lsSQL + " AND (a.nQuantity - (a.nIssueQty + a.nCancelld + a.nOrderQty)) > 0";
                System.out.println("searchDetail = " + lsSQL);
                if (p_bWithUI) {
                    poJSON = ShowDialogFX.Search(poGRider,
                            lsSQL,
                            fsValue,
                            "Stock ID»Barcode»Name",
                            "sTransNox»xBarCodex»xDescript",
                            "a.sStockIDx»b.sBarCodex»b.sDescript",
                            fbByCode ? 1 : 2);

                    if (poJSON != null) {
                        poJSON = poRequest.OpenModelDetailByStockID((String) poJSON.get("sTransNox"), (String) poJSON.get("sStockIDx"));

                        if (poJSON != null) {
                            for (int lnCtr = 0; lnCtr < poModelDetail.size(); lnCtr++) {
                                if (poModelDetail.get(lnCtr).getStockID().equalsIgnoreCase((String) poRequest.getDetailModel(0).getStockID())) {
                                    poJSON = new JSONObject();
                                    poJSON.put("result", "error");
                                    poJSON.put("message", "Inventory item request already added.");
                                    return poJSON;
                                }
                            }
                            setDetail(fnRow, "nEntryNox", (int) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getEntryNumber());
                            setDetail(fnRow, "sOrderNox", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getTransactionNumber());
                            setDetail(fnRow, "sStockIDx", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getStockID());
                            setDetail(fnRow, "sNotesxxx", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getNotes());
                            setDetail(fnRow, "xBarCodex", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getBarcode());
                            setDetail(fnRow, "xDescript", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getDescription());
                            setDetail(fnRow, "xCategr01", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getCategoryName());
                            setDetail(fnRow, "xCategr02", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getCategoryName2());
                            setDetail(fnRow, "xInvTypNm", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getCategoryType());
                            setDetail(fnRow, "cClassify", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getClassify());
                            setDetail(fnRow, "xQuantity", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getQuantity());
                            setDetail(fnRow, "nQtyOnHnd", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getQuantityOnHand());
                            setDetail(fnRow, "nResvOrdr", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getReservedOrder());
                            setDetail(fnRow, "nBackOrdr", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getBackOrder());
                            setDetail(fnRow, "nOnTranst", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getOnTransit());
                            setDetail(fnRow, "nAvgMonSl", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getAverageMonthlySalary());
                            setDetail(fnRow, "nIssueQty", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getIssueQuantity());
                            setDetail(fnRow, "nOrderQty", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getOrderQuantity());
                            setDetail(fnRow, "xBrandNme", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getBrandName());
                            setDetail(fnRow, "xModelNme", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getModelName());
                            setDetail(fnRow, "xModelDsc", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getModelDescription());
                            setDetail(fnRow, "xColorNme", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getColorName());
                            setDetail(fnRow, "xMeasurNm", poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getMeasureName());
                            int nUnserved = Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getQuantity()))
                                    - (Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getIssueQuantity()))
                                    + Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getCancelled()))
                                    + Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getOrderQuantity())));
                            setDetail(fnRow, "nUnserved", nUnserved);
                            setDetail(fnRow, "nQuantity", (int) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getCancelled());

                            poJSON.put("result", "success");
                            poJSON.put("message", "Record successfully loaded.");
                            return poJSON;
                        } else {

                            poJSON.put("result", "error");
                            poJSON.put("message", "No record loaded to update.");
                            return poJSON;
                        }

                    } else {
                        poJSON.put("result", "error");
                        poJSON.put("message", "No record loaded to update.");
                        return poJSON;
                    }
                }

                //use for testing 
                lsSQL += " LIMIT 1";
                System.out.println(lsSQL);
                lsSQL = lsSQL + " AND (a.nQuantity - (a.nIssueQty + a.nCancelld + a.nOrderQty)) > 0";
                ResultSet loRS = poGRider.executeQuery(lsSQL);
                String lsStockId = "";
                try {
                    if (!loRS.next()) {
                        MiscUtil.close(loRS);
                        poJSON.put("result", "error");
                        poJSON.put("message", "No record loaded.");
                        return poJSON;
                    }

                    lsSQL = loRS.getString("sTransNox");
                    lsStockId = loRS.getString("sStockIDx");
                    MiscUtil.close(loRS);
                } catch (SQLException ex) {
                    Logger.getLogger(Inv_Request_Cancel.class.getName()).log(Level.SEVERE, null, ex);
                }
                poJSON = poRequest.OpenModelDetailByStockID(lsSQL, lsStockId);
                if (poJSON != null) {
                    for (int lnCtr = 0; lnCtr < poModelDetail.size(); lnCtr++) {
                        if (poModelDetail.get(lnCtr).getStockID().equalsIgnoreCase((String) poRequest.getDetailModel(0).getStockID())) {
                            poJSON = new JSONObject();
                            poJSON.put("result", "error");
                            poJSON.put("message", "Inventory item request already added.");
                            return poJSON;
                        }
                    }

                    setDetail(fnRow, "sOrderNox", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getTransactionNumber());
                    setDetail(fnRow, "sStockIDx", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getStockID());
                    setDetail(fnRow, "nQuantity", (int) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getCancelled());
                    setDetail(fnRow, "sNotesxxx", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getNotes());
                    setDetail(fnRow, "xBarCodex", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getBarcode());
                    setDetail(fnRow, "xDescript", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getDescription());
                    setDetail(fnRow, "xCategr01", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getCategoryName());
                    setDetail(fnRow, "xCategr02", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getCategoryName2());
                    setDetail(fnRow, "xInvTypNm", (String) poRequest.getDetailModel().get(poRequest.getDetailModel().size() - 1).getCategoryType());
                } else {

                    poJSON.put("result", "error");
                    poJSON.put("message", "No record loaded to update.");
                    return poJSON;
                }
        }
        return poJSON;
    }

    @Override
    public JSONObject searchWithCondition(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public JSONObject searchTransaction(String fsColumn, String fsValue, boolean fbByCode) {
//        if (pnEditMode == EditMode.UNKNOWN || pnEditMode == EditMode.READY) {
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

        String lsSQL = MiscUtil.addCondition(getSQL_Master(), " a.sTransNox LIKE "
                + SQLUtil.toSQL(fsValue + "%") + " AND LEFT(a.sTransNox,4) = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        switch (type) {
            case AUTO:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0003' AND f.sCategCd2 != '0007'");
            case MC:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0001' AND f.sCategCd2 != '0007'");
                break;
            case MPUnits:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0002'");
                break;
            case SP:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0001' AND f.sCategCd2 = '0007'");
            case SP_AUTO:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0003' AND f.sCategCd2 = '0007'");
                break;
            case GENERAL:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0004' AND d.sMainCatx = '0004'");
                break;
            default:
                break;
        }
        lsSQL = MiscUtil.addCondition(lsSQL, lsCondition) + " GROUP BY a.sTransNox ASC";
        poJSON = new JSONObject();

        System.out.println(lsSQL);
        if (p_bWithUI) {
            poJSON = ShowDialogFX.Search(poGRider,
                    lsSQL,
                    fsValue,
                    "Transaction No»Date»Remarks",
                    "sTransNox»dTransact»sRemarksx",
                    "a.sTransNox»a.dTransact»a.sRemarksx",
                    fbByCode ? 0 : 1);

            System.out.println(poJSON);
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
         lsSQL = MiscUtil.addCondition(getSQL_Master(), " a.sTransNox = "
                + SQLUtil.toSQL(fsValue) + " AND LEFT(a.sTransNox,4) = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        switch (type) {
            case AUTO:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0003' AND f.sCategCd2 != '0007'");
            case MC:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0001' AND f.sCategCd2 != '0007'");
                break;
            case MPUnits:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0002'");
                break;
            case SP:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0001' AND f.sCategCd2 = '0007'");
            case SP_AUTO:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0003' AND f.sCategCd2 = '0007'");
                break;
            case GENERAL:
                lsSQL = MiscUtil.addCondition(lsSQL, " f.sCategCd1 = '0004' AND d.sMainCatx = '0004'");
                break;
            default:
                break;
        }
        lsSQL = MiscUtil.addCondition(lsSQL, lsCondition) + " GROUP BY a.sTransNox ASC";
        lsSQL += " LIMIT 1";
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            if (!loRS.next()) {
                MiscUtil.close(loRS);
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;
            }

            lsSQL = loRS.getString("sTransNox");
            MiscUtil.close(loRS);
        } catch (SQLException ex) {
            Logger.getLogger(Inv_Request_Cancel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return openTransaction(lsSQL);
//        } 
//        else {
//            return BrowseRequest(fsColumn, fsValue, fbByCode);
//        }
    }

    @Override
    public JSONObject searchMaster(String fsCol, String string1, boolean bln) {
        return searchMaster(poModelMaster.getColumn(fsCol), string1, bln);
    }

    @Override
    public JSONObject searchMaster(int fnCol, String fsValue, boolean bln) {
        poJSON = new JSONObject();
        switch (fnCol) {
            case 5:
                poRequest.setType(type);
                poRequest.setCategoryType(category_type);
                poRequest.setTransactionStatus("01");
                poRequest.setWithUI(p_bWithUI);
                poRequest.isHistory(pbIsHistory);
                poJSON = poRequest.searchTransaction("sTransNox", fsValue, bln);
                if (poJSON == null || "error".equals((String) poJSON.get("result"))) {
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "No record loaded.");
                    return poJSON;
                }

                setMaster("sBranchCd", poRequest.getMasterModel().getBranchCode());
                setMaster("xBranchNm", poRequest.getMasterModel().getBranchName());
                setMaster("sCategrCd", poRequest.getMasterModel().getCategoryCode());
                setMaster("xCategrNm", poRequest.getMasterModel().getCategoryName());
                setMaster("sOrderNox", poRequest.getMasterModel().getTransactionNumber());
                setMaster("dStartEnc", poRequest.getMasterModel().getStartEncDate());
                System.out.println("getTransactionNumber = " + poRequest.getMasterModel().getTransactionNumber());
                poModelDetail.clear();
                poJSON = AddModelDetail();
                break;
            default:
                throw new AssertionError();
        }
        return poJSON;
    }

    @Override
    public Model_Inv_Stock_Req_Cancel_Master getMasterModel() {
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

    private JSONObject transactRequest() {
        poJSON = new JSONObject();
        if (getItemCount() >= 1) {
// Group by order number 
            Map<String, List<Model_Inv_Stock_Req_Cancel_Detail>> orderGroups = new HashMap<>();
            for (int lnCtr = 0; lnCtr <= getItemCount() - 1; lnCtr++) {
                String orderNumber = poModelDetail.get(lnCtr).getOrderNumber();
// Add the detail to the appropriate group
                orderGroups.putIfAbsent(orderNumber, new ArrayList<>());  // Create list if absent
                orderGroups.get(orderNumber).add(poModelDetail.get(lnCtr));  // Add detail to the list of that order number
                poModelDetail.get(lnCtr).setTransactionNumber(poModelMaster.getTransactionNumber());
                poModelDetail.get(lnCtr).setEntryNumber(lnCtr + 1);
                int lnQty = Integer.parseInt(poModelDetail.get(lnCtr).getQuantity().toString());
                poJSON = updateRequestDetail(poModelDetail.get(lnCtr).getStockID(), lnQty);
                if ("error".equals((String) poJSON.get("result"))) {
                    if (!pbWthParent) {
                        poGRider.rollbackTrans();
                    }
                    return poJSON;
                }
                poJSON.put("result", "success");
                poJSON.put("message", "Record save successfully");

            }
            for (String orderNumber : orderGroups.keySet()) {
                boolean allUnservedZero = true;
                for (Model_Inv_Stock_Req_Cancel_Detail detail : orderGroups.get(orderNumber)) {
                    int lnUnserve = Integer.parseInt(detail.getUnserve().toString());
                    if (lnUnserve > 0) {
                        allUnservedZero = false;  // If any 'nUnserved' is not zero, set flag to false
                        break;
                    }
                }
                System.out.println("Order Number: " + orderNumber);
                poJSON = poRequest.openTransaction(orderNumber);
                if ("error".equals((String) poJSON.get("result"))) {
                    if (!pbWthParent) {
                        poGRider.rollbackTrans();
                    }
                    return poJSON;
                }
                int detailCount = orderGroups.get(orderNumber).size();  // Get the count of details for this order number
                System.out.println("Order Number: " + orderNumber + " | Count of details: " + detailCount);

                //close transaction if cancel detail is equal to request detail count
                //also if all request quantity is serve
                if (detailCount == poRequest.getItemCount() && allUnservedZero) {
                    poJSON = poRequest.cancelTransaction(orderNumber);
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }
//                     
            }

        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Save empty Transaction.");
            return poJSON;
        }
        return poJSON;
    }

    public JSONObject OpenModelDetail(String fsTransNo) {
        poJSON = new JSONObject();
        try {
            String lsSQL = MiscUtil.addCondition(new Model_Inv_Stock_Req_Cancel_Detail(poGRider).getSQL(), "a.sTransNox = " + SQLUtil.toSQL(fsTransNo)) +
                    " GROUP BY A.sTransNox";
            ResultSet loRS = poGRider.executeQuery(lsSQL);

            System.out.println("searchTransaction = " + lsSQL);
            int lnctr = 0;
            if (MiscUtil.RecordCount(loRS) > 0) {
                poModelDetail = new ArrayList<>();
                while (loRS.next()) {
                    poModelDetail.add(new Model_Inv_Stock_Req_Cancel_Detail(poGRider));
                    poJSON = poModelDetail.get(poModelDetail.size() - 1).openRecord(loRS.getString("sTransNox"), loRS.getString("sStockIDx"));

                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                    poModelDetail.get(poModelDetail.size() - 1).setTransactionNumber(poModelMaster.getTransactionNumber());
                    int nUnserved = Integer.parseInt(String.valueOf(poModelDetail.get(poModelDetail.size() - 1).getQuantityRequest()))
                            - (Integer.parseInt(String.valueOf(poModelDetail.get(poModelDetail.size() - 1).getIssueQuantity()))
                            + Integer.parseInt(String.valueOf(poModelDetail.get(poModelDetail.size() - 1).getQuantity()))
                            + Integer.parseInt(String.valueOf(poModelDetail.get(poModelDetail.size() - 1).getOrderQuantity())));
                    poModelDetail.get(poModelDetail.size() - 1).setUnserve(nUnserved);
                    pnEditMode = EditMode.UPDATE;
                    lnctr++;
                    poJSON.put("result", "success");
                    poJSON.put("message", "Record loaded successfully.");
                }

                System.out.println("lnctr = " + lnctr);

            } else {
                poModelDetail = new ArrayList<>();
                AddModelDetail();
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
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
        if (poModelDetail.isEmpty()) {
            poModelDetail.add(new Model_Inv_Stock_Req_Cancel_Detail(poGRider));
            poModelDetail.get(0).newRecord();
            poModelDetail.get(0).setEntryNumber(poModelDetail.size());
            poModelDetail.get(0).setTransactionNumber(poModelMaster.getTransactionNumber());
            poJSON.put("result", "success");
            poJSON.put("message", "Inventory request add record.");

        } else {
            poModelDetail.add(new Model_Inv_Stock_Req_Cancel_Detail(poGRider));
            poModelDetail.get(poModelDetail.size() - 1).newRecord();
            poModelDetail.get(poModelDetail.size() - 1).setEntryNumber(poModelDetail.size());
            poModelDetail.get(poModelDetail.size() - 1).setTransactionNumber(poModelMaster.getTransactionNumber());

            poJSON.put("result", "success");
            poJSON.put("message", "Inventory request add record.");
        }
        System.out.println(poModelDetail.size());

        return poJSON;
    }

    public void RemoveModelDetail(int fnRow) {
        if (poModelDetail.size() >= 1) {
            poModelDetail.remove(fnRow);
            if (poModelDetail.isEmpty()) {
                AddModelDetail();
            }
        }
    }

    /**
     * @return This function process for inventory stock request. This function
     * use for browsing Inventory Stock Request fetching inventory request and
     * set data for Inventory Stock Request Cancel
     */
    public JSONObject BrowseRequest(String fsColumn, String fsValue, boolean fbByCode) {
        poJSON = new JSONObject();
        poRequest.setType(type);
        poRequest.setCategoryType(category_type);
        poRequest.setTransactionStatus("01");
        poRequest.setWithUI(p_bWithUI);
        poRequest.isHistory(pbIsHistory);
        poJSON = poRequest.searchTransaction(fsColumn, fsValue, fbByCode);
        if (poJSON == null || "error".equals((String) poJSON.get("result"))) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }

        setMaster("sBranchCd", poRequest.getMasterModel().getBranchCode());
        setMaster("xBranchNm", poRequest.getMasterModel().getBranchName());
        setMaster("sCategrCd", poRequest.getMasterModel().getCategoryCode());
        setMaster("xCategrNm", poRequest.getMasterModel().getCategoryName());
        setMaster("sOrderNox", poRequest.getMasterModel().getTransactionNumber());
        setMaster("dStartEnc", poRequest.getMasterModel().getStartEncDate());
        System.out.println("getTransactionNumber = " + poRequest.getMasterModel().getTransactionNumber());
        if (p_bWithUI) {
            if (ShowMessageFX.YesNo("Do you want to display all request item from this transaction " + poRequest.getMasterModel().getTransactionNumber() + "?", "Computerized Acounting System", "Inventory Stock Request Cancel")) {
                poModelDetail = new ArrayList<>();

                for (int lnCtr = 0; lnCtr < poRequest.getDetailModel().size(); lnCtr++) {
                    boolean isExist = false;
                    for (int lnCtr1 = 0; lnCtr1 < poModelDetail.size(); lnCtr1++) {
                        if (poModelDetail.get(lnCtr1).getOrderNumber().equalsIgnoreCase((String) poRequest.getDetailModel().get(lnCtr).getTransactionNumber())
                                && poModelDetail.get(lnCtr1).getStockID().equalsIgnoreCase((String) poRequest.getDetailModel().get(lnCtr).getStockID())) {
                            isExist = true;
                            break;
                        }
                    }
                    if (isExist) {
                        continue;
                    }
                    poModelDetail.add(new Model_Inv_Stock_Req_Cancel_Detail(poGRider));
                    poModelDetail.get(lnCtr).newRecord();

                    setDetail(lnCtr, "nEntryNox", (int) poRequest.getDetailModel().get(lnCtr).getEntryNumber());
                    setDetail(lnCtr, "sOrderNox", (String) poRequest.getDetailModel().get(lnCtr).getTransactionNumber());
                    setDetail(lnCtr, "sStockIDx", (String) poRequest.getDetailModel().get(lnCtr).getStockID());
                    setDetail(lnCtr, "sNotesxxx", (String) poRequest.getDetailModel().get(lnCtr).getNotes());
                    setDetail(lnCtr, "xBarCodex", (String) poRequest.getDetailModel().get(lnCtr).getBarcode());
                    setDetail(lnCtr, "xDescript", (String) poRequest.getDetailModel().get(lnCtr).getDescription());
                    setDetail(lnCtr, "xCategr01", (String) poRequest.getDetailModel().get(lnCtr).getCategoryName());
                    setDetail(lnCtr, "xCategr02", (String) poRequest.getDetailModel().get(lnCtr).getCategoryName2());
                    setDetail(lnCtr, "xInvTypNm", (String) poRequest.getDetailModel().get(lnCtr).getCategoryType());
                    setDetail(lnCtr, "cClassify", poRequest.getDetailModel().get(lnCtr).getClassify());
                    setDetail(lnCtr, "xQuantity", poRequest.getDetailModel().get(lnCtr).getQuantity());
                    setDetail(lnCtr, "nQtyOnHnd", poRequest.getDetailModel().get(lnCtr).getQuantityOnHand());
                    setDetail(lnCtr, "nResvOrdr", poRequest.getDetailModel().get(lnCtr).getReservedOrder());
                    setDetail(lnCtr, "nBackOrdr", poRequest.getDetailModel().get(lnCtr).getBackOrder());
                    setDetail(lnCtr, "nOnTranst", poRequest.getDetailModel().get(lnCtr).getOnTransit());
                    setDetail(lnCtr, "nAvgMonSl", poRequest.getDetailModel().get(lnCtr).getAverageMonthlySalary());
                    setDetail(lnCtr, "nIssueQty", poRequest.getDetailModel().get(lnCtr).getIssueQuantity());
                    setDetail(lnCtr, "nOrderQty", poRequest.getDetailModel().get(lnCtr).getOrderQuantity());
                    setDetail(lnCtr, "xBrandNme", poRequest.getDetailModel().get(lnCtr).getBrandName());
                    setDetail(lnCtr, "xModelNme", poRequest.getDetailModel().get(lnCtr).getModelName());
                    setDetail(lnCtr, "xModelDsc", poRequest.getDetailModel().get(lnCtr).getModelDescription());
                    setDetail(lnCtr, "xColorNme", poRequest.getDetailModel().get(lnCtr).getColorName());
                    setDetail(lnCtr, "xMeasurNm", poRequest.getDetailModel().get(lnCtr).getMeasureName());
                    int nUnserved = Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(lnCtr).getQuantity()))
                            - (Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(lnCtr).getIssueQuantity()))
                            + Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(lnCtr).getCancelled()))
                            + Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(lnCtr).getOrderQuantity())));
                    setDetail(lnCtr, "nUnserved", 0);
                    setDetail(lnCtr, "nQuantity", (int) poRequest.getDetailModel().get(lnCtr).getCancelled());
                    poJSON.put("result", "success");
                    poJSON.put("message", "Item added.");
//                    fnRow++;

                }
            } else {
                setDetail(poModelDetail.size() - 1, "sOrderNox", (String) poRequest.getMasterModel().getTransactionNumber());
            }

        } else {
            poModelDetail = new ArrayList<>();
//            This code assume that user click no in ShowMessageFX;
//            poJSON = AddModelDetail();

//            This code assume that user click yes in ShowMessageFX;
//            set all request detail to request detail cancel;
            for (int lnCtr = 0; lnCtr <= poRequest.getDetailModel().size() - 1; lnCtr++) {
                poModelDetail.add(new Model_Inv_Stock_Req_Cancel_Detail(poGRider));
                poModelDetail.get(lnCtr).newRecord();

                setDetail(lnCtr, "nEntryNox", (int) poRequest.getDetailModel().get(lnCtr).getEntryNumber());
                setDetail(lnCtr, "sOrderNox", (String) poRequest.getDetailModel().get(lnCtr).getTransactionNumber());
                setDetail(lnCtr, "sStockIDx", (String) poRequest.getDetailModel().get(lnCtr).getStockID());
                setDetail(lnCtr, "sNotesxxx", (String) poRequest.getDetailModel().get(lnCtr).getNotes());
                setDetail(lnCtr, "xBarCodex", (String) poRequest.getDetailModel().get(lnCtr).getBarcode());
                setDetail(lnCtr, "xDescript", (String) poRequest.getDetailModel().get(lnCtr).getDescription());
                setDetail(lnCtr, "xCategr01", (String) poRequest.getDetailModel().get(lnCtr).getCategoryName());
                setDetail(lnCtr, "xCategr02", (String) poRequest.getDetailModel().get(lnCtr).getCategoryName2());
                setDetail(lnCtr, "xInvTypNm", (String) poRequest.getDetailModel().get(lnCtr).getCategoryType());
                setDetail(lnCtr, "cClassify", poRequest.getDetailModel().get(lnCtr).getClassify());
                setDetail(lnCtr, "xQuantity", poRequest.getDetailModel().get(lnCtr).getQuantity());
                setDetail(lnCtr, "nQtyOnHnd", poRequest.getDetailModel().get(lnCtr).getQuantityOnHand());
                setDetail(lnCtr, "nResvOrdr", poRequest.getDetailModel().get(lnCtr).getReservedOrder());
                setDetail(lnCtr, "nBackOrdr", poRequest.getDetailModel().get(lnCtr).getBackOrder());
                setDetail(lnCtr, "nOnTranst", poRequest.getDetailModel().get(lnCtr).getOnTransit());
                setDetail(lnCtr, "nAvgMonSl", poRequest.getDetailModel().get(lnCtr).getAverageMonthlySalary());
                setDetail(lnCtr, "nIssueQty", poRequest.getDetailModel().get(lnCtr).getIssueQuantity());
                setDetail(lnCtr, "nOrderQty", poRequest.getDetailModel().get(lnCtr).getOrderQuantity());
                setDetail(lnCtr, "xBrandNme", poRequest.getDetailModel().get(lnCtr).getBrandName());
                setDetail(lnCtr, "xModelNme", poRequest.getDetailModel().get(lnCtr).getModelName());
                setDetail(lnCtr, "xModelDsc", poRequest.getDetailModel().get(lnCtr).getModelDescription());
                setDetail(lnCtr, "xColorNme", poRequest.getDetailModel().get(lnCtr).getColorName());
                setDetail(lnCtr, "xMeasurNm", poRequest.getDetailModel().get(lnCtr).getMeasureName());
                int nUnserved = Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(lnCtr).getQuantity()))
                        - (Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(lnCtr).getIssueQuantity()))
                        + Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(lnCtr).getCancelled()))
                        + Integer.parseInt(String.valueOf(poRequest.getDetailModel().get(lnCtr).getOrderQuantity())));
                setDetail(lnCtr, "nUnserved", nUnserved);
                setDetail(lnCtr, "nQuantity", (int) poRequest.getDetailModel().get(lnCtr).getCancelled());

            }
        }

        return poJSON;
    }

    private JSONObject updateRequestDetail(String lsStockID, int fnQty) {
        poRequest = new Inv_Request(poGRider, false);
        poRequest.setType(type);
        poRequest.setCategoryType(category_type);
        poJSON = poRequest.OpenModelDetailByStockID(poModelMaster.getOrderNumber(), lsStockID);
        System.out.println("poJSON = " + poJSON);
        if ("error".equals((String) poJSON.get("result"))) {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }
            return poJSON;
        }

        for (int lnctr = 0; lnctr < poRequest.getDetailModel().size(); lnctr++) {
            Model_Inv_Stock_Request_Detail poModel = poRequest.getDetailModel(lnctr);
            if (poModel.getStockID().equals(lsStockID)) {
                poModel.setCancelled(fnQty);
                poJSON = poModel.saveRecord();

                if ("error".equals((String) poJSON.get("result"))) {
                    if (!pbWthParent) {
                        poGRider.rollbackTrans();
                    }
                }

                poJSON.put("result", "success");
                poJSON.put("message", "Record save successfully");
            }
        }

        return poJSON;
    }

    public String getSQL_Master() {
        return "SELECT"
                + "   a.sTransNox,"
                + "   a.sBranchCd,"
                + "   a.sCategrCd,"
                + "   a.dTransact,"
                + "   a.sOrderNox,"
                + "   a.sRemarksx,"
                + "   a.sApproved,"
                + "   a.dApproved,"
                + "   a.sAprvCode,"
                + "   a.nEntryNox,"
                + "   a.cTranStat,"
                + "   a.dStartEnc,"
                + "   a.sModified,"
                + "   a.dModified,"
                + "   b.sBranchNm    xBranchNm,"
                + "   c.sDescript    xCategrNm,"
                + "   d.sDescript    xCategNm1,"
                + "   f.sBarCodex "
                + " FROM Inv_Stock_Req_Cancel_Master a"
                + "   LEFT JOIN Branch b"
                + "     ON a.sBranchCd = b.sBranchCd"
                + "   LEFT JOIN Category c"
                + "     ON a.sCategrCd = c.sCategrCd"
                + "   LEFT JOIN Category_Level2 d"
                + "     ON c.sCategrCd = d.sMainCatx"
                + "   LEFT JOIN Inv_Stock_Req_Cancel_Detail e"
                + "     ON e.sTransNox = a.sTransNox"
                + "   LEFT JOIN Inventory f"
                + "     ON f.sStockIDx = e.sStockIDx";
    }

//    @Override
    public void setType(RequestControllerFactory.RequestType types) {
        type = types;
    }

//    @Override
    public void setCategoryType(RequestControllerFactory.RequestCategoryType type) {
        category_type = type;
    }

    private int computeUnserved(int qtyReq, int qtyIssue, int qty, int qtyOrder) {
        int nUnserved = qtyReq - (qtyIssue + qty + qtyOrder);
        return nUnserved;
    }

    public JSONObject deleteRecord() {
        poJSON = new JSONObject();
        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE) {
            String lsSQLs = MiscUtil.addCondition(new Model_Inv_Stock_Req_Cancel_Detail(poGRider).getSQL(), "a.sTransNox = " + SQLUtil.toSQL(poModelMaster.getTransactionNumber()));
            ResultSet loRS = poGRider.executeQuery(lsSQLs);
            backupRecords = new ArrayList<>();
            int lnctr = 0;
            try {
                while (loRS.next()) {

                    backupRecords.add(new Model_Inv_Stock_Req_Cancel_Detail(poGRider));
                    poJSON = backupRecords.get(lnctr).openRecord(loRS.getString("sTransNox"), loRS.getString("sStockIDx"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                    lnctr++;
                }
            } catch (SQLException ex) {
                Logger.getLogger(InvRequestCancel.class.getName()).log(Level.SEVERE, null, ex);
            }
            Model_Inv_Stock_Req_Cancel_Detail model = new Model_Inv_Stock_Req_Cancel_Detail(poGRider);
            String lsSQL = "DELETE FROM " + model.getTable()
                    + " WHERE sTransNox = " + SQLUtil.toSQL(poModelMaster.getTransactionNumber());

            if (!lsSQL.equals("")) {
                if (poGRider.executeQuery(lsSQL, model.getTable(), poGRider.getBranchCode(), "") > 0) {
                    poJSON.put("result", "success");
                    poJSON.put("message", "Record deleted successfully.");
                }
            }
        }

        return poJSON;
    }

    private JSONObject SaveDetail() {
        poJSON = new JSONObject();
        // Check if there are items to process
        boolean hasZero = false;
        if (getItemCount() >= 1) {
            // Loop in reverse order to avoid index shifting when removing elements

            for (int lnCtr = getItemCount() - 1; lnCtr >= 0; lnCtr--) {
            // Check if StockID or Barcode is empty

                int lnQty = Integer.parseInt(poModelDetail.get(lnCtr).getQuantity().toString());
                if (poModelDetail.get(lnCtr).getStockID().isEmpty() || poModelDetail.get(lnCtr).getBarcode().isEmpty()) {
                    // Remove the empty record
                    poModelDetail.remove(lnCtr);
                } else {
                    //check if one non-zero quantity is found
                    if (lnQty == 0) {
                        hasZero = true;  // If any product's quantity is not zero, set flag to false
                        break;  // No need to check further if one non-zero quantity is found
                    }

                }
            }
            if (hasZero) {
                poJSON.put("result", "error");
                poJSON.put("message", "Quantities are currently set to 0. Update them to continue.");
                return poJSON;
            }
            System.out.println(getItemCount());
            for (int lnCtr = 0; lnCtr <= getItemCount() - 1; lnCtr++) {
                System.out.println("getStockID() = " + poModelDetail.get(lnCtr).getStockID());
                poModelDetail.get(lnCtr).setTransactionNumber(poModelMaster.getTransactionNumber());
                poModelDetail.get(lnCtr).setEntryNumber(lnCtr + 1);
                poModelDetail.get(lnCtr).setEditMode(EditMode.ADDNEW);
                poJSON = poModelDetail.get(lnCtr).saveRecord();
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }

            }

        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "No record of item detected.");
            return poJSON;
        }

        return poJSON;
    }

    public void cancelUpdate() {
        poJSON = new JSONObject();
        if (!backupRecords.isEmpty()) {
            if (!pbWthParent) {
                poGRider.beginTrans();
            }
            for (int lnCtr1 = 0; lnCtr1 < backupRecords.size(); lnCtr1++) {
                backupRecords.get(lnCtr1).setQuantity(0);
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
    }

    private void restoreData() {
        if (!backupRecords.isEmpty()) {
            for (int lnCtr = 0; lnCtr < getItemCount() && lnCtr < backupRecords.size(); lnCtr++) {
                Model_Inv_Stock_Req_Cancel_Detail model = poModelDetail.get(lnCtr);
                if (model.getStockID().equalsIgnoreCase(backupRecords.get(lnCtr).getStockID())) {
                    backupRecords.get(lnCtr).setQuantity(Integer.parseInt(model.getQuantity().toString()));
                }
            }
            poModelDetail.clear();  // Clear the current details
            poModelDetail.addAll(backupRecords);  // Restore from backup

        }

    }
}
