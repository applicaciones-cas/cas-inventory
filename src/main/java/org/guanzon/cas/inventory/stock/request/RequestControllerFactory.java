/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.guanzon.cas.inventory.stock.request;

import org.guanzon.appdriver.base.GRider;
import org.guanzon.cas.inventory.stock.Inv_Request_Appliances;
import org.guanzon.cas.inventory.stock.Inv_Request_Auto;
import org.guanzon.cas.inventory.stock.Inv_Request_General;
import org.guanzon.cas.inventory.stock.Inv_Request_MC;
import org.guanzon.cas.inventory.stock.Inv_Request_MC1;
import org.guanzon.cas.inventory.stock.Inv_Request_MCSpareparts;
import org.guanzon.cas.inventory.stock.Inv_Request_MP;
import org.guanzon.cas.inventory.stock.Inv_Request_MP1;
import org.guanzon.cas.inventory.stock.Inv_Request_MPAccesories;
import org.guanzon.cas.inventory.stock.Inv_Request_SP;
import org.guanzon.cas.inventory.stock.Inv_Request_SP_Auto;
import org.guanzon.cas.inventory.stock.Inv_Request_Vehicle;
import org.guanzon.cas.inventory.stock.request.approval.Inv_Request_General_Approval;
import org.guanzon.cas.inventory.stock.request.approval.Inv_Request_MC_Approval;
import org.guanzon.cas.inventory.stock.request.approval.Inv_Request_MP_Approval;
import org.guanzon.cas.inventory.stock.request.approval.Inv_Request_SP_Approval;
import org.guanzon.cas.inventory.stock.request.issuance.Inv_Request_General_Issuance;
import org.guanzon.cas.inventory.stock.request.issuance.Inv_Request_MC_Issuance;
import org.guanzon.cas.inventory.stock.request.issuance.Inv_Request_MP_Issuance;
import org.guanzon.cas.inventory.stock.request.issuance.Inv_Request_SP_Issuance;
import org.guanzon.cas.inventory.stock.request.purchase.Inv_Request_General_Purchase;
import org.guanzon.cas.inventory.stock.request.purchase.Inv_Request_MC_Purchase;
import org.guanzon.cas.inventory.stock.request.purchase.Inv_Request_MP_Purchase;
import org.guanzon.cas.inventory.stock.request.purchase.Inv_Request_SP_Purchase;
/**
 *
 * @author User
 */
public class RequestControllerFactory {
    public enum RequestType {
        AUTO,
        SP_AUTO,
        ASSET,
        MC,
        MCSpareparts,
        MPUnits,
        MPAccesories,
        MPAppliance,
        SP,
        SUPPLIES,
        GENERAL,
        Vehicle
    }
    public enum RequestCategoryType {
        WITH_ROQ,
        WITHOUT_ROQ
    }
    public static RequestController make(RequestType foType, GRider oApp, boolean fbVal){
        System.out.println("foType = " + foType);
        switch (foType) {
            case MC:
                return (RequestController) new Inv_Request_MC1(oApp, fbVal);
            case MPUnits:
                return (RequestController) new Inv_Request_MP1(oApp, fbVal);
            case SP:
                return (RequestController) new Inv_Request_SP(oApp, fbVal);
            case GENERAL:
                return (RequestController) new Inv_Request_General(oApp, fbVal);
            case AUTO:
                return (RequestController) new Inv_Request_Auto(oApp, fbVal);
            case SP_AUTO:
                return (RequestController) new Inv_Request_SP_Auto(oApp, fbVal);
            case MPAccesories:
                return (RequestController) new Inv_Request_MPAccesories(oApp, fbVal);
            case MPAppliance:
                return (RequestController) new Inv_Request_Appliances(oApp, fbVal);
            case MCSpareparts:
                return (RequestController) new Inv_Request_MCSpareparts(oApp, fbVal);
            case Vehicle:
                return (RequestController) new Inv_Request_Vehicle(oApp, fbVal);
            default:
                return null;
        }
    }
    
    public static RequestCancelController makeCancel(RequestType foType, GRider oApp, boolean fbVal) {
        switch (foType) {
            case MC:
                return (RequestCancelController) new Inv_Request_MC(oApp, fbVal);
            case MPUnits:
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
            case MPUnits:
                return (RequestApprovalController) new Inv_Request_MP_Approval(oApp, fbVal);
            case SP:
                return (RequestApprovalController) new Inv_Request_SP_Approval(oApp, fbVal);
            case GENERAL:
                return (RequestApprovalController) new Inv_Request_General_Approval(oApp, fbVal);
            default:
                return null;
        }
    }
    
    public static RequestIssuanceController makeIssuance(RequestType foType, GRider oApp, boolean fbVal) {
        switch (foType) {
            case MC:
                return (RequestIssuanceController) new Inv_Request_MC_Issuance(oApp, fbVal);
            case MPUnits:
                return (RequestIssuanceController) new Inv_Request_MP_Issuance(oApp, fbVal);
            case SP:
                return (RequestIssuanceController) new Inv_Request_SP_Issuance(oApp, fbVal);
            case GENERAL:
                return (RequestIssuanceController) new Inv_Request_General_Issuance(oApp, fbVal);
            default:
                return null;
        }
    }
    
    public static RequestPurchaseController maakePurchase(RequestType foType, GRider oApp, boolean fbVal) {
        switch (foType) {
            case MC:
                return (RequestPurchaseController) new Inv_Request_MC_Purchase(oApp, fbVal);
            case MPUnits:
                return (RequestPurchaseController) new Inv_Request_MP_Purchase(oApp, fbVal);
            case SP:
                return (RequestPurchaseController) new Inv_Request_SP_Purchase(oApp, fbVal);
            case GENERAL:
                return (RequestPurchaseController) new Inv_Request_General_Purchase(oApp, fbVal);
            default:
                return null;
        }
    }
    
}
