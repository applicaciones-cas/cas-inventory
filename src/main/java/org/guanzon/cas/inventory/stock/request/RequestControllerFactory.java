/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.guanzon.cas.inventory.stock.request;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.cas.inventory.stock.Inv_Request_General;
import org.guanzon.cas.inventory.stock.Inv_Request_MC;
import org.guanzon.cas.inventory.stock.Inv_Request_Without_ROQ;
import org.guanzon.cas.inventory.stock.Inv_Request_MP;
import org.guanzon.cas.inventory.stock.Inv_Request_SP;
import org.guanzon.cas.inventory.stock.Inv_Request_SP_Without_ROQ;
import org.guanzon.cas.inventory.stock.request.approval.Inv_Request_MC_Approval;
import org.guanzon.cas.inventory.stock.request.approval.Inv_Request_SP_Approval;
import org.guanzon.cas.inventory.stock.request.issuance.Inv_Request_SP_Issuance;
import org.guanzon.cas.inventory.stock.request.purchase.Inv_Request_SP_Purchase;
/**
 *
 * @author User
 */
public class RequestControllerFactory {
    public enum RequestType {
        ASSET,
        MC,
        MP,
        SP,
        SUPPLIES,
        GENERAL
    }
    public enum RequestCategoryType {
        WITH_ROQ,
        WITHOUT_ROQ
    }
    public static RequestController make(RequestType foType, GRider oApp, boolean fbVal){
        System.out.println("foType = " + foType);
        switch (foType) {
            case MC:
                return (RequestController) new Inv_Request_MC(oApp, fbVal);
            case MP:
                return (RequestController) new Inv_Request_MP(oApp, fbVal);
            case SP:
                return (RequestController) new Inv_Request_SP(oApp, fbVal);
            case GENERAL:
                return (RequestController) new Inv_Request_General(oApp, fbVal);
            default:
                return null;
        }
    }
    
    public static RequestCancelController makeCancel(RequestType foType, GRider oApp, boolean fbVal) {
        switch (foType) {
            case MC:
                return (RequestCancelController) new Inv_Request_MC(oApp, fbVal);
            case MP:
                return (RequestCancelController) new Inv_Request_MC(oApp, fbVal);
            case SP:
                return (RequestCancelController) new Inv_Request_SP(oApp, fbVal);
            default:
                return null;
        }
    }
    public static RequestApprovalController makeApproval(RequestType foType, GRider oApp, boolean fbVal) {
        switch (foType) {
            case MC:
                return (RequestApprovalController) new Inv_Request_MC_Approval(oApp, fbVal);
            case MP:
                return (RequestApprovalController) new Inv_Request_MC(oApp, fbVal);
            case SP:
                return (RequestApprovalController) new Inv_Request_SP_Approval(oApp, fbVal);
            default:
                return null;
        }
    }
    
    public static RequestIssuanceController makeIssuance(RequestType foType, GRider oApp, boolean fbVal) {
        switch (foType) {
            case MC:
                return (RequestIssuanceController) new Inv_Request_MC_Approval(oApp, fbVal);
            case MP:
                return (RequestIssuanceController) new Inv_Request_MC(oApp, fbVal);
            case SP:
                return (RequestIssuanceController) new Inv_Request_SP_Issuance(oApp, fbVal);
            default:
                return null;
        }
    }
    public static RequestPurchaseController maakePurchase(RequestType foType, GRider oApp, boolean fbVal) {
        switch (foType) {
            case MC:
                return (RequestPurchaseController) new Inv_Request_MC_Approval(oApp, fbVal);
            case MP:
                return (RequestPurchaseController) new Inv_Request_MC(oApp, fbVal);
            case SP:
                return (RequestPurchaseController) new Inv_Request_SP_Purchase(oApp, fbVal);
            default:
                return null;
        }
    }
    
    
}
