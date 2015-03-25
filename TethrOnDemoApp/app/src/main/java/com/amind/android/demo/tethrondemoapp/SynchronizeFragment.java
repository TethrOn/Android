package com.amind.android.demo.tethrondemoapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.HashMap;
import com.amind.amdcom.AMDCom;
import com.amind.amdcom.infra.model.AMDCEvent;
import com.amind.amdcom.infra.model.AMDCEventListener;
import com.amind.amdcom.infra.model.AMDCEvents;
import com.amind.amdcom.infra.utils.AMDCCDMManager;
import com.amind.amdcom.infra.utils.AMDCSettings;
import com.amind.amdcom.infra.utils.constants.StrConsts;
import com.amind.amdcom.sync.AMDCSynchronizationManager;
import android.app.ProgressDialog;


/**
 * This fragment handles synchronization of data from the
 * TethrOn server to the local device. The steps of the initial
 * synchronization are as follows:
 * 1) The device sends a registration call to the server.
 * 2) On the very first sync request, the server will ask the client
 *    to download and install a new database, and CDM definitions. This
 *    will come as part of a successful registration event, as an event
 *    parameter CDM_UPGRADE_REQUIRED
 * 3) The client kicks off an asynchronous job to download the new metadata
 *    files, and waits for an event CDM_UPGRADE_DONE
 * 4) Once the event is received, the client kicks off synchronization.
 * 5) A SYNC_DONE event is fired when synchronization is completed.
 *
 * During synchronization, the client can catch and act on option events. These
 * events can let a client know the current stage of synchronization and other
 * activities that are going on in the background. In this case, we catch some
 * of these events to let the user know the progress of the current sync process.
 */
public class SynchronizeFragment extends Fragment implements AMDCEventListener {

    private ProgressDialog progress;
    private int numberOfSyncParts;
    private int currentProgress;

    public SynchronizeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_synchronize, container, false);
    }

    /**
     *
     * Instantiate TethrOn synchronization manager, register for events,
     * and begin synchronization.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        // Set up initial progress bar
        progress = new ProgressDialog(getActivity());
        progress.setMessage(getString(R.string.preparing_sync));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();

        super.onActivityCreated(savedInstanceState);

        // Obtain TethrOn Synchronization manager instance for sync.
        AMDCSynchronizationManager syncManager = AMDCom.instance().getSyncManager();

        //Register for actionable sync events we want to listen to
        syncManager.addEventListener(this, AMDCEvents.SYNC_DONE, null);
        syncManager.addEventListener(this, AMDCEvents.REGISTERDEVICEOK, null);
        syncManager.addEventListener(this, AMDCEvents.REGISTERDEVICEFAILED, null);
        AMDCom.instance().addEventListener(this, AMDCEvents.CDM_UPGRADE_DONE, null);

        // Register for events for part download progress bar
        AMDCom.instance().addEventListener(this, AMDCEvents.PARTCOUNT_REQUEST_COMPLETED, null);
        AMDCom.instance().addEventListener(this, AMDCEvents.MULTIPART_REQUEST_COMPLETED, null);

        //Register for events for part saving progress bar
        AMDCom.instance().addEventListener(this, AMDCEvents.SYNC_WILLBEGINPARTPROCESSING, null);
        AMDCom.instance().addEventListener(this, AMDCEvents.SYNC_COMPLETEDPARTPROCESSING, null);

        //Kick off Sync process immediately instead of scheduling it for a certain time
        syncManager.synchronizeInBackground();

        Toast.makeText(this.getActivity(), getString(R.string.starting_sync), Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * This method will get called from TethrOn for any of the events we registered
     * for above. The event name and various event parameters will be passed in the
     * amdcEvent.
     */
    @Override
    public void onEvent(AMDCEvent amdcEvent, Object o) {

        if (amdcEvent.getName().equals(AMDCEvents.SYNC_DONE)) {
            // event from Sync will come in on a different thread. We must
            // run any UI activity on the UI thread, so we pass the control
            // to the UI thread below.
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(SynchronizeFragment.this.getActivity(), getString(R.string.sync_completed), Toast.LENGTH_LONG).show();
                    progress.dismiss();
                    getActivity().findViewById(R.id.ll_syncDone).setVisibility(View.VISIBLE);
                }
            });
        } else if (amdcEvent.getName().equals(AMDCEvents.REGISTERDEVICEOK)) {

            /*
            If a new CDM/Database has been provisioned on the server,
            we will receive this information upon registration. This information
            will be saved in the settings. When registration succeeds, we must
            check the settings, and see if we must retrieve and install new CDM
            definitions and database before continuing with synchronization.
             */
            AMDCSettings settings = AMDCom.instance().getSettings();
            Integer needsToInstallNewCDM = (Integer) settings.getProperty(StrConsts.SettingsConst.Property.NEEDTOINSTALLNEWCDM);
            if (needsToInstallNewCDM != null
                    && needsToInstallNewCDM.equals(AMDCSettings.CDMUpgradeStages.cdmNeedsToBeDownloaded)) {

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(SynchronizeFragment.this.getActivity(), getString(R.string.upgrading_CDM), Toast.LENGTH_SHORT).show();
                        setupCreds();
                        AMDCCDMManager.instance().upgradeCDM();
                        return;
                    }
                });
            }
        } else if (amdcEvent.getName().equals(AMDCEvents.REGISTERDEVICEFAILED)) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    progress.dismiss();
                    Toast.makeText(SynchronizeFragment.this.getActivity(), getString(R.string.registration_failed), Toast.LENGTH_LONG).show();
                }
            });


        } else if (amdcEvent.getName().equals(AMDCEvents.CDM_UPGRADE_DONE)) {
            /*
            This event means that the CDM and the new database have been installed
            successfully. At this point, we can continue with synchronization.
             */
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    //Run Sync immediately. Implementer may ask user if they want to wait for a better connection.
                    Toast.makeText(SynchronizeFragment.this.getActivity(), getString(R.string.cdm_upgrade_completed), Toast.LENGTH_LONG).show();
                    setupCreds();
                    AMDCom.instance().getSyncManager().synchronizeInBackground();
                    return;
                }
            });
        } else if (amdcEvent.getName().equals(AMDCEvents.PARTCOUNT_REQUEST_COMPLETED)) {
            /*
            This event is received in the base of a multi-part synchronization. In the case
            of a multi-part sync, the server splits the result set into many parts. The parts
            are downloaded to the client in a multi-threaded fashion, and once all parts are
            received, they are then inserted into the DB. This event also contains the total
            number of parts the server plans to send.
             */
            this.numberOfSyncParts = (Integer) amdcEvent.getEventProperty(AMDCEvents.Params.PARTCOUNT);
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    progress.dismiss();
                    progress = new ProgressDialog(getActivity());
                    progress.setMessage(getString(R.string.downloading_parts));
                    progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progress.setProgress(0);
                    progress.setMax(100);
                    progress.show();
                }
            });

        } else if (amdcEvent.getName().equals(AMDCEvents.MULTIPART_REQUEST_COMPLETED)) {
            /*
            This event gets triggered when a single part is done downloading. We update the
            progress bar to reflect the current progress of the total download.
             */
            int partNum = (Integer) amdcEvent.getEventProperty(AMDCEvents.Params.PARTNUMBER);
            int prog = (partNum * 100) / numberOfSyncParts;
            // due to multithreading, the parts may complete downloading out of order,
            // so the check below ensures the progress bar does not go backwards
            if (prog > currentProgress)
                progress.setProgress(prog);
            currentProgress = prog;
        } else if (amdcEvent.getName().equals(AMDCEvents.SYNC_WILLBEGINPARTPROCESSING)) {
            /*
            This event gets triggered when all the parts have been downloaded,
            and the insertion into the DB will begin.
             */
            currentProgress = 0;
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    progress.setMessage(getString(R.string.applying_parts));
                    progress.setProgress(0);
                    progress.setMax(100);
                }
            });
        } else if (amdcEvent.getName().equals(AMDCEvents.SYNC_COMPLETEDPARTPROCESSING)) {
            /*
            This event gets triggered when a single part has been successfully inserted into
            the database.
             */
            int partNum = (Integer) amdcEvent.getEventProperty(AMDCEvents.Params.PARTNUMBER);
            int prog = (partNum * 100) / numberOfSyncParts;
            if (prog > currentProgress)
                progress.setProgress(prog);
            currentProgress = prog;
        }
    }

    /**
     * For the purposes of this demo, we are hard-coding the credentials to the
     * backend services. In a real-world scenario, the user would be asked to
     * enter these credentials, and they would be saved in a secure storage
     * on the device.
     */
    private void setupCreds() {

        AMDCSettings settings = AMDCom.instance().getSettings();

        settings.setProperty(StrConsts.SettingsConst.Property.APPID, "Default Application");

        HashMap<String, String> backendDef = new HashMap<String, String>();

        backendDef.put(StrConsts.SettingsConst.Property.BACKENDCODE, "SalesforcePassword");
        backendDef.put(StrConsts.SettingsConst.Property.BACKENDSRC, "SFDC");
        settings.addBackend("SFDC", backendDef);

        settings.saveSettings();

    }


}
