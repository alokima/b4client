package com.ehelpy.brihaspati4.authenticate ;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import com.ehelpy.brihaspati4.voip.B4services;
import com.ehelpy.brihaspati4.indexmanager.IndexManagement;
import com.ehelpy.brihaspati4.comnmgr.CommunicationManager;
import com.ehelpy.brihaspati4.indexmanager.IndexManagementUtilityMethods;
import com.ehelpy.brihaspati4.overlaymgmt.OverlayManagement;
import com.ehelpy.brihaspati4.routingmgmt.RMThreadPrimary;
import com.ehelpy.brihaspati4.routingmgmt.RTManager;
import com.ehelpy.brihaspati4.routingmgmt.SysOutCtrl;
import com.ehelpy.brihaspati4.routingmgmt.UpdateIP;
import com.ehelpy.brihaspati4.sms.sms_methods;
import com.ehelpy.brihaspati4.sms.sms_retrival_thread;
import com.ehelpy.brihaspati4.sms.sms_send_rec_management;
import com.ehelpy.brihaspati4.DFS.DistFileSys;
import com.ehelpy.brihaspati4.DFS.Save_Retrive_data_Structures;

public class ClientMain extends Thread {
    private static X509Certificate client_cert = null;
    private static X509Certificate server_cert = null;
    private static boolean flagset = false;
    private static int CtrlConsoleOut=0;

    public static void main(String args[]) throws Exception
    {
        // @SuppressWarnings("unused")

        // Create a singleton global object and set run status as true.
        // GlobalObject will keep status of various threads and run status. This will be used
        // for proper closure of threads when closing the application.

        GlobalObject globj= GlobalObject.getGlobalObject();
        globj.setRunStatus(true);

        // Configuration object created and object referece is saved in
        // GlobalObject.
        Config conf= Config.getConfigObject();

        // Config initialization from configuration file done during call of the
        // constructor of Config.
        // Config_object will keep the data after reading from configuration file.
        // On each change, the data should also be written back to config file.
        // It implies, in each write api, write to config file on disk is to be implemented.
        // Debug level (CtrlConsoleOut) is read from Config object which in
        // turn is to be read from configuration file.
        // Can be modified in GUI, which will update it in the configuration file.

        CtrlConsoleOut = conf.getCtrlConsoleOut();

        /* Commented - to be removed when the branch is to be finally merged to
         * the master.*/ 
        	SysOutCtrl.SysoutSet("iptable initiated"+CommunicationManager.myIpTable);
                UpdateIP IPUpdate = new UpdateIP();
                IPUpdate.start();
                IPUpdate.setName("IPUpdate");
                SysOutCtrl.SysoutSet("Thread Id : "+IPUpdate.getName(), 1);
        /* */

        boolean timeflg=dateTimeCheck.checkDate();
        /* Date and time is to be checked. It should be same as on standard time
        * server or greater than equal to last logout date time value.  If the returns
        * value false (in case the above conditions fails) then exit the user from the
        * system with advise to user to correct the system date and time. otherwise
        * start the services.
        */

        // Start the singleton object for UI
        // UIObject ui = UIObject.getUIObject();

        if (!timeflg) {
            String msg = "Please reset your system time and try again." ;
            Gui.showMessageDialogBox(msg);
            System.exit(0);
        }
        else {
            try {
                flagset = ReadVerifyCert.verifyCert();
                // check if there is valid certificate is present in the keystore of of the client.
                // In case of invalid certificate, new certificate generation and identity verification should be done.

                client_cert = ReadVerifyCert.returnClientCert();
                server_cert = ReadVerifyCert.returnServerCert();
                // debug_level.debug(0,"clientcertsaved is =" + client_cert );
                // debug_level.debug(0,"servercertsaved is =" + server_cert );
                String email_id=emailid.getemaild();
                debug_level.debug(0,"My Email-Id is =" + email_id );

            }
            catch (CertificateException e) {
                // In case of exception during the certificate verification, the stacktrace is to be printed
                // for debugging purpose and program should terminate.
                // We can have an log submission mail of development team for identifying the issue and improving
                // the product. The emailing of this stacktrace can be added later.

                e.printStackTrace();
                System.exit(0);
            }
        }
        if(flagset) {
            // get singleton object for DHTRouter, RTManager, DHTable,
            // SpillOverTable, ComnMgr, ProxyRouter, MulticastMgr, MediaBridge,
            // IndexingMgr, KeyCache, SearchEngine, ContentCache,
            // Broadcast-RWRouter.
            // DHTRouter dhtr = DHTRouter.getDHTRouter();
            // RTManager rtmgr = RTManager.getRTManager();
            // DHTable dhtable = DHTable.getDHTable();

            // debug_level.debug(0,"The private key of client is  =" + ReadVerifyCert.getKeyPair() );
            // sms_methods.choose_loc();
            // sms_send_rec_management.empty_cache_folder();
            // sms_send_rec_management.empty_rec_folder();
            IndexManagementUtilityMethods.Ip_txt_empty();
            // call objects and methods from classes of - communication
            //            CommunicationManager cm= CommunicationManager.getCM(); //todo
            CommunicationManager cm= new CommunicationManager();
            cm.start();
            // Communication manager thread started. The thread will have buffers to keep incoming messages
            // which can be read by various modules (RTManager to update Routing Tables in all the DHT
            // layers,)

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                //RTManager rm = RTManager.getRTMgr(); //TODO
                //rm.start();//TODO
                RTManager.initiateRT();
                Save_Retrive_data_Structures.Save_nodeFileChunkMap();
                Save_Retrive_data_Structures.Save_nodefilemap();
                Save_Retrive_data_Structures.Save_root_Fileinfo_Map();
                Save_Retrive_data_Structures.Save_shared_Fileinfo_Map();
                Save_Retrive_data_Structures.Retrive_nodeFileChunkMap();
                Save_Retrive_data_Structures.Retrive_nodefilemap();
                Save_Retrive_data_Structures.Retrive_root_Fileinfo_Map();
                Save_Retrive_data_Structures.Retrive_shared_Fileinfo_Map();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            OverlayManagement.nodeStartUp();
            //    OverlayManagement olay = OverlayManagement.getInstance(); //ToDo
            OverlayManagement olay = new OverlayManagement();
            olay.start();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            // call objects of routing  mangement

            RMThreadPrimary.update_rt();

            System.out.println("Starting IndexManagement thread");

            // call objects and methods from classes of - index management
            IndexManagement indmgt= new IndexManagement();
            indmgt.start();

            /*   sms_retrival_thread sms= new sms_retrival_thread();
               sms.start();

               DistFileSys dfs = new DistFileSys();
               dfs.start();
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e1) {
                   // TODO Auto-generated catch block
                   e1.printStackTrace();
               }
            */
            B4services.service();
            // start user specific services
            // user specific DFS mount service,
            // call objects and methods from classes of - routing and overlay mangement
            // All generic services Interface
            // VOIP call, storage services, messaging service
            // }
        }
        while(globj.getRunStatus()) {
            Thread.sleep(30000);
        }
    }
}
