/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rmj.parameters;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.constants.RecordStatus;
import org.rmj.appdriver.GCrypt;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.iface.GEntity;
import org.rmj.appdriver.iface.GRecord;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.integsys.pojo.UnitBrand;

/**
 *
 * @author kalyptus
 */
public class Brand implements GRecord{
   private boolean pbWithParnt = false;
   private String psBranchCD = "";
   private String psUserIDxx = "";
   private String psWarnMsg = "";
   private String psErrMsgx = "";
   private GRider poGRider = null;

   public UnitBrand newRecord() {
      UnitBrand  loOcc = new UnitBrand();
      Connection loCon = null;

      if(pbWithParnt){
         loCon = poGRider.getConnection();
         if(loCon == null)
            loCon = poGRider.doConnect();
      }
      else
         loCon = poGRider.doConnect();

//      if(poGRider.getConnection() == null)
//         loCon = poGRider.doConnect();
//      else
//         loCon = poGRider.getConnection();

      loOcc.setBrandID(MiscUtil.getNextCode(loOcc.getTable(), "sBrandIDx", false, loCon, ""));

      if(!pbWithParnt)
         MiscUtil.close(loCon);

      return loOcc;
   }

   public UnitBrand openRecord(String fstransNox) {
      UnitBrand  loOcc = new UnitBrand();
      Connection loCon = null;

      //If with parent then get the connection from parent else create a new connection
      if(pbWithParnt){
         loCon = poGRider.getConnection();
         if(loCon == null)
            loCon = poGRider.doConnect();
      }
      else
         loCon = poGRider.doConnect();

//      if(poGRider.getConnection() == null)
//         loCon = poGRider.doConnect();
//      else
//         loCon = poGRider.getConnection();

      //retrieve the record
      String lsSQL = MiscUtil.addCondition(getSQ_Master(), "sBrandIDx = " + SQLUtil.toSQL(fstransNox));
      Statement loStmt = null;
      ResultSet loRS = null;
      try {
         loStmt = loCon.createStatement();
         loRS = loStmt.executeQuery(lsSQL);

         if(!loRS.next())
             setMessage("No Old Record Found!");
         else{
            //load each column to the entity
            for(int lnCol=1; lnCol<=loRS.getMetaData().getColumnCount(); lnCol++){
                loOcc.setValue(lnCol, loRS.getObject(lnCol));
            }
         }

      } catch (SQLException ex) {
         Logger.getLogger(Brand.class.getName()).log(Level.SEVERE, null, ex);
         setErrMsg(ex.getMessage());
      }
      finally{
         MiscUtil.close(loRS);
         MiscUtil.close(loStmt);
         if(!pbWithParnt)
            MiscUtil.close(loCon);
      }

      return loOcc;
   }

   public UnitBrand saveRecord(Object foEntity, String fsTransNox) {
      String lsSQL = "";
      UnitBrand loOldEnt = null;
      UnitBrand loNewEnt = null;
      UnitBrand loResult = null;

      // Check for the value of foEntity
      if (!(foEntity instanceof UnitBrand)) {
          setErrMsg("Invalid Entity Passed as Parameter");
          return loResult;
      }

      // Typecast the Entity to this object
      loNewEnt = (UnitBrand) foEntity;

      //test for the validity of the different fields here
      if (loNewEnt.getBrandName().equals("")) {
          setMessage("Invalid Brand Description Detected");
          return loResult;
      }

      //TODO: Test the user rights in these area...

      //Set the value of sModified and dModified here
      //GCrypt loCrypt = new GCrypt();
      //loNewEnt.setModifiedBy(loCrypt.encrypt(psUserIDxx));
      loNewEnt.setModifiedBy(psUserIDxx);
      loNewEnt.setDateModified(poGRider.getServerDate());

      //Generate the SQL Statement
      if (fsTransNox.equals("")) {
         //TODO: Get new id for this record...
         Connection loCon = null;
         if(pbWithParnt){
            loCon = poGRider.getConnection();
            if(loCon == null)
               loCon = poGRider.doConnect();
         }
         else
            loCon = poGRider.doConnect();

         loNewEnt.setValue(1, MiscUtil.getNextCode(loNewEnt.getTable(), "sBrandIDx", false, loCon, ""));

         if(!pbWithParnt)
            MiscUtil.close(loCon);

         //Generate the INSERT statement
          lsSQL = MiscUtil.makeSQL((GEntity)loNewEnt);
      } else {
          //Reload previous record
          loOldEnt = openRecord(fsTransNox);
          //Generate the UPDATE statement
          lsSQL = MiscUtil.makeSQL((GEntity)loNewEnt, (GEntity)loOldEnt, "sBrandIDx = " + SQLUtil.toSQL(loNewEnt.getValue(1)));
      }

      //No changes has been made
      if (lsSQL.equals("")) {
          setMessage("Record is not updated!");
          return loResult;
      }

      if(!pbWithParnt)
         poGRider.beginTrans();

      if(poGRider.executeQuery(lsSQL.toString(), loNewEnt.getTable(), "", "") == 0){
         if(!poGRider.getErrMsg().isEmpty())
            setErrMsg(poGRider.getErrMsg());
         else
            setMessage("No record updated");
      }
      else
         loResult = loNewEnt;

      if(!pbWithParnt){
         if(getErrMsg().isEmpty())
            poGRider.commitTrans();
         else
            poGRider.rollbackTrans();
      }

      return loResult;
   }

   public boolean deleteRecord(String fsTransNox) {
      UnitBrand  loOcc = openRecord(fsTransNox);
      boolean lbResult = false;

      if(loOcc == null){
         setMessage("No record found!");
         return lbResult;
      }


      //TODO: Test the user rights in these area...

      StringBuilder lsSQL = new StringBuilder();
      lsSQL.append("DELETE FROM " + loOcc.getTable());
      lsSQL.append(" WHERE sBrandIDx = " + SQLUtil.toSQL(fsTransNox));

      if(!pbWithParnt)
         poGRider.beginTrans();

      if(poGRider.executeQuery(lsSQL.toString(), loOcc.getTable(), "", "") == 0){
         if(!poGRider.getErrMsg().isEmpty())
            setErrMsg(poGRider.getErrMsg());
         else
            setMessage("No record deleted");
      }
      else
         lbResult = true;

      if(!pbWithParnt){
         if(getErrMsg().isEmpty())
            poGRider.commitTrans();
         else
            poGRider.rollbackTrans();
      }

      return lbResult;
   }

   public boolean deactivateRecord(String fsTransNox) {
      UnitBrand  loOcc = openRecord(fsTransNox);
      boolean lbResult = false;

      if(loOcc == null){
         setMessage("No record found!");
         return lbResult;
      }

      if(loOcc.getRecdStat().equalsIgnoreCase(RecordStatus.INACTIVE)){
         setMessage("Current record is inactive!");
         return lbResult;
      }

      //TODO: Test the user rights in these area...

      //GCrypt loCrypt = new GCrypt();
      StringBuilder lsSQL = new StringBuilder();
      lsSQL.append("UPDATE " + loOcc.getTable() + " SET ");
      lsSQL.append("  cRecdStat = " + SQLUtil.toSQL(RecordStatus.INACTIVE));
      lsSQL.append(", sModified = " + SQLUtil.toSQL(psUserIDxx));
      lsSQL.append(", dModified = " + SQLUtil.toSQL(poGRider.getServerDate()));
      lsSQL.append(" WHERE sBrandIDx = " + SQLUtil.toSQL(fsTransNox));

      if(!pbWithParnt)
         poGRider.beginTrans();

      if(poGRider.executeQuery(lsSQL.toString(), loOcc.getTable(), "", "") == 0){
         if(!poGRider.getErrMsg().isEmpty())
            setErrMsg(poGRider.getErrMsg());
         else
            setMessage("No record updated");
      }
      else
         lbResult = true;

      if(!pbWithParnt){
         if(getErrMsg().isEmpty())
            poGRider.commitTrans();
         else
            poGRider.rollbackTrans();
      }

      return lbResult;
   }

   public boolean activateRecord(String fsTransNox) {
      UnitBrand  loOcc = openRecord(fsTransNox);
      boolean lbResult = false;

      if(loOcc == null){
         setMessage("No record found!");
         return lbResult;
      }

      if(loOcc.getRecdStat().equalsIgnoreCase(RecordStatus.ACTIVE)){
         setMessage("Current record is active!");
         return lbResult;
      }

      //TODO: Test the user rights in these area...

      //GCrypt loCrypt = new GCrypt();
      StringBuilder lsSQL = new StringBuilder();
      lsSQL.append("UPDATE " + loOcc.getTable() + " SET ");
      lsSQL.append("  cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
      lsSQL.append(", sModified = " + SQLUtil.toSQL(psUserIDxx));
      lsSQL.append(", dModified = " + SQLUtil.toSQL(poGRider.getServerDate()));
      lsSQL.append(" WHERE sBrandIDx = " + SQLUtil.toSQL(fsTransNox));

      if(!pbWithParnt)
         poGRider.beginTrans();

      if(poGRider.executeQuery(lsSQL.toString(), loOcc.getTable(), "", "") == 0){
         if(!poGRider.getErrMsg().isEmpty())
            setErrMsg(poGRider.getErrMsg());
         else
            setMessage("No record updated");
      }
      else
         lbResult = true;

      if(!pbWithParnt){
         if(getErrMsg().isEmpty())
            poGRider.commitTrans();
         else
            poGRider.rollbackTrans();
      }

      return lbResult;
   }

   public String getMessage() {
      return psWarnMsg;
   }

   public void setMessage(String fsMessage) {
      this.psWarnMsg = fsMessage;
   }

   public String getErrMsg() {
      return psErrMsgx;
   }

   public void setErrMsg(String fsErrMsg) {
      this.psErrMsgx = fsErrMsg;
   }

   public void setBranch(String foBranchCD) {
      this.psBranchCD = foBranchCD;
   }

   public void setWithParent(boolean fbWithParent) {
      this.pbWithParnt = fbWithParent;
   }

   public String getSQ_Master() {
      return (MiscUtil.makeSelect(new UnitBrand()));
   }

   // Added methods here
   public void setGRider(GRider foGRider) {
      this.poGRider = foGRider;
      this.psUserIDxx = foGRider.getUserID();
      if(psBranchCD.isEmpty())
         psBranchCD = poGRider.getBranchCode();
   }
}
