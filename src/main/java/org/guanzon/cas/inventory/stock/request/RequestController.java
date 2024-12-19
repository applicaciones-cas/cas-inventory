/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.guanzon.cas.inventory.stock.request;

import java.util.ArrayList;
import org.guanzon.appdriver.iface.GTranDet;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inventory.models.Model_Inv_Stock_Request_Master;
import org.json.simple.JSONObject;

/**
 *
 * @author User
 */
public interface RequestController extends GTranDet  {
    
    /**
     *
     * @param fbValue
     */
    void setWithUI(boolean fbValue);
    /**
     *
     * @return
     */
    @Override
    int getItemCount();
    /**
     *
     * @return
     */
    
    int getOtherItemCount();

    /**
     *
     * @param fnRow
     * @return
     */
    @Override
    Model_Inv_Stock_Request_Detail getDetailModel(int fnRow);

    /**
     *
     * @param fnRow
     * @param fnCol
     * @param foData
     * @return
     */
    @Override
    JSONObject setDetail(int fnRow, int fnCol, Object foData);

    /**
     *
     * @param fnRow
     * @param fsCol
     * @param foData
     * @return
     */
    @Override
    JSONObject setDetail(int fnRow, String fsCol, Object foData);

    /**
     *
     * @param fnRow
     * @param fsCol
     * @param fsValue
     * @param bln
     * @return
     */
    @Override
    JSONObject searchDetail(int fnRow, String fsCol, String fsValue, boolean bln);

    /**
     *
     * @param fnRow
     * @param fnCol
     * @param fsValue
     * @param bln
     * @return
     */
    @Override
    JSONObject searchDetail(int fnRow, int fnCol, String fsValue, boolean bln);

    /**
     *
     * @return
     */
    @Override
    JSONObject newTransaction();

    /**
     *
     * @param fsValue
     * @return
     */
    @Override
    JSONObject openTransaction(String fsValue);

    /**
     *
     * @return
     */
    @Override
    JSONObject updateTransaction();

    /**
     *
     * @return
     */
    @Override
    JSONObject saveTransaction();

    /**
     *
     * @param fsValue
     * @return
     */
    @Override
    JSONObject deleteTransaction(String fsValue);

    /**
     *
     * @param fsValue
     * @return
     */
    @Override
    JSONObject closeTransaction(String fsValue);

    /**
     *
     * @param fsValue
     * @return
     */
    @Override
    JSONObject postTransaction(String fsValue);

    /**
     *
     * @param fsValue
     * @return
     */
    @Override
    JSONObject voidTransaction(String fsValue);

    /**
     *
     * @param fsValue
     * @return
     */
    @Override
    JSONObject cancelTransaction(String fsValue);

    /**
     *
     * @param fsValue
     * @return
     */
    @Override
    JSONObject searchWithCondition(String fsValue);

    /**
     *
     * @param fsCol
     * @param fsValue
     * @param bln
     * @return
     */
    @Override
    JSONObject searchTransaction(String fsCol, String fsValue, boolean bln);

    /**
     *
     * @param fsCol
     * @param fsValue
     * @param bln
     * @return
     */
    @Override
    JSONObject searchMaster(String fsCol, String fsValue, boolean bln);

    /**
     *
     * @param fnCol
     * @param fsValue
     * @param bln
     * @return
     */
    @Override
    JSONObject searchMaster(int fnCol, String fsValue, boolean bln);

    /**
     *
     * @return
     */
    @Override
    Model_Inv_Stock_Request_Master getMasterModel();
    
    /**
     *
     * @param fnCol
     * @param foData
     * @return
     */
    @Override
    JSONObject setMaster(int fnCol, Object foData);
    
    /**
     *
     * @param fsValue
     * @param foData
     * @return
     */
    @Override
    JSONObject setMaster(String fsValue, Object foData);
    
    /**
     *
     * @return
     */
    @Override
    int getEditMode();
    
    /**
     *
     * @param fsValue
     */
    @Override
    void setTransactionStatus(String fsValue);
    
    
    
    JSONObject OpenModelDetail(String fsTransNo);
    JSONObject SearchDetailRequest(String fsTransNo, String fsStockID);
    JSONObject OpenModelDetailByStockID(String fsTransNo, String fsStockID);
//    JSONObject deleteRecord(int fnEntryNox);
    JSONObject AddModelDetail();
    void RemoveModelDetail(int fnRow);
    void setType(RequestControllerFactory.RequestType type);
    void setCategoryType(RequestControllerFactory.RequestCategoryType type);
    
    ArrayList<Model_Inv_Stock_Request_Detail> getDetailModel();
    ArrayList<Model_Inv_Stock_Request_Detail> getDetailModelOthers();
    void cancelUpdate();
//    void checkType();
    
    //use for request with roq
    JSONObject loadAllInventoryMinimumLevel();
    
    /**
     *
     * @param fnRow
     * @param fsCol
     * @param foData
     * @return
     */
    JSONObject setDetailOthers(int fnRow, String fsCol, Object foData);

    /**
     *
     * @param fnRow
     * @param fsCol
     * @param fsValue
     * @param bln
     * @return
     */
    JSONObject setDetailOthers(int fnRow, int fnCol, Object foData);
    JSONObject AddDetailOthers(int fnRow);
    
    void isHistory(boolean fbValue);
    
}