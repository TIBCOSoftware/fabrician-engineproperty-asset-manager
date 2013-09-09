// Copyright 2013 TIBCO Software Inc. All rights reserved.
package org.fabrician.assetmanager.engineproperty;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import com.datasynapse.fabric.admin.AdminManager;
import com.datasynapse.fabric.admin.ComponentAdmin;
import com.datasynapse.fabric.admin.EngineDaemonAdmin;
import com.datasynapse.fabric.admin.StackAdmin;
import com.datasynapse.fabric.admin.info.ComponentInfo;
import com.datasynapse.fabric.admin.info.FabricEngineDaemonInfo;
import com.datasynapse.fabric.admin.info.RuntimeContextVariableInfo;
import com.datasynapse.fabric.admin.info.StackInfo;
import com.datasynapse.fabric.asset.DefaultAssetManager;
import com.datasynapse.fabric.broker.FabricServerEvent;
import com.datasynapse.fabric.common.ActivationInfo;
import com.datasynapse.gridserver.admin.Property;


public class EngineProperty extends DefaultAssetManager{
    
   
    private static final long serialVersionUID = 2443519394918109351L;
    
    public EngineProperty() {}      
    public EnginePropertyConfig getPropertyConfig(){
        return (EnginePropertyConfig)getConfig();
    }

    @Override
    public void init() throws Exception {
        EngineDaemonAdmin eda = AdminManager.getEngineDaemonAdmin();
        EnginePropertyConfig config = getPropertyConfig();
        getLogger().log(Level.INFO, "Initializing Engine Daemon Property Asset Manager - Adding default Engine properties");
        try {
            getLogger().log(Level.FINE, "Adding default Engine property " + config.getExclusivityTag() + " with description " + config.getExclusivityTagDescription());
            eda.setDefaultProperty(config.getExclusivityTag(), config.getExclusivityTagDescription());
            getLogger().log(Level.FINE, "Adding default Engine property " + config.getPropertyList() + " with description " + config.getPropertyListDescription());
            eda.setDefaultProperty(config.getPropertyList(), config.getPropertyListDescription());
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Initialization of the Engine Daemon Property Asset Manager has failed with: " + e);
        }
        
    }
    
    @Override
    public void destroy() {
        EngineDaemonAdmin eda = AdminManager.getEngineDaemonAdmin();
        EnginePropertyConfig config = getPropertyConfig();
        if (config.getRemoveDefaultPropertiesOnExit().equalsIgnoreCase("true")) {
            getLogger().log(Level.INFO, "Destroying Engine Daemon Property Asset Manager - Removing default Engine properties that were added in initialization.");
            try {
                getLogger().log(Level.FINE, "Removing default Engine property " + config.getExclusivityTag() + " with description " + config.getExclusivityTagDescription());
                eda.removeDefaultProperty(config.getExclusivityTag());
                getLogger().log(Level.FINE, "Removing default Engine property " + config.getPropertyList() + " with description " + config.getPropertyListDescription());
                eda.removeDefaultProperty(config.getPropertyList());
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Destroying daemon property asset manager has failed with: " + e);
            }
        }        
    }

    @Override
    public void handleComponentInstanceAdded(ActivationInfo component) throws Exception {
        ComponentAdmin ca = AdminManager.getComponentAdmin();
        StackAdmin sa = AdminManager.getStackAdmin();
        EngineDaemonAdmin eda = AdminManager.getEngineDaemonAdmin();
        EnginePropertyConfig config = getPropertyConfig();
        final String undoModeRCV = "undoMode";
        final String autoStopStackRCV = "autoStopStack";
        String componentName = null;
        String ename = component.getEnabler();
        if (!config.getEnablerName().equalsIgnoreCase(ename)) {
            getLogger().log(Level.FINE, "Enabler "+ config.getEnablerName() + " does not match " + ename);
            // not an enabler we are interested in, so quit
            return;
        }
        componentName = component.getComponent();
        ComponentInfo aci = ca.getComponent(componentName);
        String exclusivity = null;
        String manifest = null;
        String undoMode = "false";
        String autoStop = "false";
        
        RuntimeContextVariableInfo[] rcvis = aci.getRuntimeContextVariables();              
        for(RuntimeContextVariableInfo rvar : rcvis){
            if(rvar.getName().equalsIgnoreCase(config.getExclusivityTagVariableName())){
                getLogger().log(Level.FINE, "ComponentInstanceAdded Event. Will modify "+ config.getExclusivityTag() + " property on daemon with the value " + rvar.getValue());
                exclusivity = rvar.getValue().toString();
            } else if(rvar.getName().equalsIgnoreCase(config.getPropertyTagVariableName())){
                getLogger().log(Level.FINE, "ComponentInstanceAdded Event. Will modify "+ config.getPropertyList() + " property on daemon with the value " + rvar.getValue());
                manifest = rvar.getValue().toString();
            } else if(rvar.getName().equalsIgnoreCase(autoStopStackRCV)){
                getLogger().log(Level.FINE, "ComponentInstanceAdded Event. autoStopStack = " + rvar.getValue());
                autoStop = rvar.getValue().toString();
            } else if(rvar.getName().equalsIgnoreCase(undoModeRCV)){
                getLogger().log(Level.FINE, "ComponentInstanceAdded Event. undoMode = " + rvar.getValue());
                undoMode = rvar.getValue().toString();
            }
        }
        
        // check we got both of our required properties
        if (exclusivity == null || manifest == null) {
            // we are missing one or more of our properties, so quit
            getLogger().log(Level.SEVERE, "Component Properties are missing, unable to process Engine properties update...");
            return;
        }
        String ipaddr = component.getProperty(ActivationInfo.IP_ADDRESS);    
        getLogger().log(Level.FINE, "Event IP address = " + ipaddr);
        // get the engine daemon
        FabricEngineDaemonInfo[] daemonsInfo = eda.getAllEngineDaemonInfo();
        for (int i = 0; i < daemonsInfo.length; i++) {
            Property[] daemonProperties = daemonsInfo[i].getProperties();
            //find IP address of the daemon
            String daemonIP = null;
            for (int j = 0; j < daemonProperties.length; j++) {
                if ("IP".equals(daemonProperties[j].getName())) {
                    getLogger().log(Level.FINE, "Got the Daemon IP address " + daemonProperties[j].getValue());
                    daemonIP = daemonProperties[j].getValue();
                    break;
                }
            }
            if (ipaddr.equals(daemonIP)) {
             // we found the right daemon, wish there was an easier way to tie events to the engines they come from..!
                long daemonID = daemonsInfo[i].getEngineId();
                if (daemonID != 0) {
                 // if the host has already been configured by the Enabler fetch the manifest
                    String oldManifest = null;
                    for (int j = 0; j < daemonProperties.length; j++) {
                        if (config.getPropertyList().equals(daemonProperties[j].getName())) {
                            oldManifest = daemonProperties[j].getValue().trim();
                            break;
                        }
                    }
                    if(undoMode.equalsIgnoreCase("false")) {
                        // for exclusivity we can just add or override, but for manifest we need to add ourselves to the 
                        // existing list if it is already present
                        getLogger().log(Level.INFO, "Adding daemon property " + config.getExclusivityTag() + " to " + ipaddr + " ID " + daemonID);
                        eda.setProperty(daemonID, config.getExclusivityTag(), exclusivity);
                        getLogger().log(Level.INFO, "Adding daemon property " + config.getPropertyList() + " to " + ipaddr + " ID " + daemonID);
                        if (oldManifest != null && !oldManifest.isEmpty()){
                            // there was a manifest already so we add to it - unless our manifest is already present
                            // this can happen if the broker crashed and we re-allocate, or someone runs the enabler twice 
                            // with the same manifest on the same host (this shouldn't happen of course!)
                            if (!oldManifest.contains(manifest)) {
                                manifest = oldManifest + "," + manifest;
                            } else {
                                // if we are in the list already don't change the list
                                manifest = oldManifest;
                            }
                        }
                        eda.setProperty(daemonID, config.getPropertyList(), manifest);
                        // once we have set the properties on the daemon
                        // we can stop the stack if it is not needed
                        if (autoStop.equalsIgnoreCase("true")){
                            sa.setStackMode(FabricServerEvent.STACK_NAME, StackInfo.MODE_STOPPED);
                        }
                        break;
                    } else {
                        // Removing properties is more complex, if there was only one applied manifest we can just blat them 
                        // otherwise we need to leave the Exclusivity value and remove our manifest from the list
                        if (!oldManifest.contains(",")) {
                            getLogger().log(Level.INFO, "Removing daemon property " + config.getExclusivityTag() + " from " + ipaddr + " ID " + daemonID);
                            eda.removeProperty(daemonID, config.getExclusivityTag());
                            getLogger().log(Level.INFO, "Removing daemon property " + config.getPropertyList() + " from " + ipaddr + " ID " + daemonID);
                            eda.removeProperty(daemonID, config.getPropertyList());
                        } else {
                            getLogger().log(Level.INFO, "Removing daemon property " + config.getPropertyList() + " from " + ipaddr + " ID " + daemonID);
                            // find our manifest in the list
                            List<String> manifestList = Arrays.asList(oldManifest.split(","));
                            StringBuilder sb = new StringBuilder();
                            for(String manifestItem: manifestList){
                                if (manifestItem.equalsIgnoreCase(manifest)){
                                    continue;
                                }                                   
                                if(sb.length() > 0){
                                    sb.append(',');
                                }
                                sb.append(manifestItem);
                            }
                            eda.setProperty(daemonID, config.getPropertyList(), sb.toString());
                        }
                        break;
                    }
                } else {
                    getLogger().log(Level.SEVERE, "Unable to get FabricEngineDaemonId");
                    // we found our daemon and couldn't get the ID, so quit
                    return;
                }
            }
        }
    }

    @Override
    public void handleComponentInstanceRemoved(ActivationInfo component) throws Exception {
        ComponentAdmin ca = AdminManager.getComponentAdmin();
        EngineDaemonAdmin eda = AdminManager.getEngineDaemonAdmin();
        EnginePropertyConfig config = getPropertyConfig();
        final String removePropertiesOnShutdownRCV = "removePropertiesOnShutdown";
        String componentName = null;
        String ename = component.getEnabler();
        if (!config.getEnablerName().equalsIgnoreCase(ename)) {
            getLogger().log(Level.FINE, "Enabler "+ config.getEnablerName() + " does not match " + ename);
            // not an enabler we are interested in, so quit
            return;
        }
        componentName = component.getComponent();
        ComponentInfo aci = ca.getComponent(componentName);
        //String exclusivity = null;
        String manifest = null;
        String removeOnShutdown = "false";
        
        RuntimeContextVariableInfo[] rcvis = aci.getRuntimeContextVariables();              
        for(RuntimeContextVariableInfo rvar : rcvis){
            /*if(rvar.getName().equalsIgnoreCase(config.getExclusivityTagVariableName())){
                getLogger().log(Level.INFO, "ComponentInstanceRemoved Event. Will modify "+ config.getExclusivityTag() + " property on daemon with the value " + rvar.getValue());
                exclusivity = rvar.getValue().toString();
            } else */ if(rvar.getName().equalsIgnoreCase(config.getPropertyTagVariableName())){
                getLogger().log(Level.INFO, "ComponentInstanceRemoved Event. Will modify "+ config.getPropertyList() + " property on daemon with the value " + rvar.getValue());
                manifest = rvar.getValue().toString();
            } else if(rvar.getName().equalsIgnoreCase(removePropertiesOnShutdownRCV)){
                getLogger().log(Level.INFO, "ComponentInstanceRemoved Event. removePropertiesOnShutdown = " + rvar.getValue());
                removeOnShutdown = rvar.getValue().toString();
            }
        }
        
        // check we got both of our required properties
//        if (exclusivity == null || manifest == null) {
//            // we are missing one or more of our properties, so quit
//            getLogger().log(Level.SEVERE, "Component Properties are missing, unable to process Engine properties update...");
//            return;
//        }
        String ipaddr = component.getProperty(ActivationInfo.IP_ADDRESS);    
        getLogger().log(Level.INFO, "Event IP address = " + ipaddr);
        // get the engine daemon
        FabricEngineDaemonInfo[] daemonsInfo = eda.getAllEngineDaemonInfo();
        for (int i = 0; i < daemonsInfo.length; i++) {
            Property[] daemonProperties = daemonsInfo[i].getProperties();
            //find IP address of the daemon
            String daemonIP = null;
            for (int j = 0; j < daemonProperties.length; j++) {
                if ("IP".equals(daemonProperties[j].getName())) {
                    getLogger().log(Level.INFO, "Got the Daemon IP address " + daemonProperties[j].getValue());
                    daemonIP = daemonProperties[j].getValue();
                    break;
                }
            }
            if (ipaddr.equals(daemonIP)) {
             // we found the right daemon, wish there was an easier way to tie events to the engines they come from..!
                long daemonID = daemonsInfo[i].getEngineId();
                if (daemonID != 0) {
                 // if the host has already been configured by the Enabler fetch the manifest
                    String oldManifest = null;
                    for (int j = 0; j < daemonProperties.length; j++) {
                        if (config.getPropertyList().equals(daemonProperties[j].getName())) {
                            oldManifest = daemonProperties[j].getValue().trim();
                            break;
                        }
                    }
                    if (removeOnShutdown.equalsIgnoreCase("true")){
                        // Removing properties is more complex, if there was only one applied manifest we can just blat them 
                        // otherwise we need to leave the Exclusivity value and remove our manifest from the list
                        if (!oldManifest.contains(",")) {
                            getLogger().log(Level.INFO, "Removing daemon property " + config.getExclusivityTag() + " from " + ipaddr + " ID " + daemonID);
                            eda.removeProperty(daemonID, config.getExclusivityTag());
                            getLogger().log(Level.INFO, "Removing daemon property " + config.getPropertyList() + " from " + ipaddr + " ID " + daemonID);
                            eda.removeProperty(daemonID, config.getPropertyList());
                        } else {
                            if (manifest == null) {
                                getLogger().log(Level.SEVERE, "Component Property " + config.getPropertyTagVariableName() + " is missing. Unable to process Engine properties update...");
                                return;
                            }
                            getLogger().log(Level.INFO, "Removing daemon property " + config.getPropertyList() + " from " + ipaddr + " ID " + daemonID);
                            // find our manifest in the list
                            List<String> manifestList = Arrays.asList(oldManifest.split(","));
                            StringBuilder sb = new StringBuilder();
                            for(String manifestItem: manifestList){
                                if (manifestItem.equalsIgnoreCase(manifest)){
                                    continue;
                                }                                   
                                if(sb.length() > 0){
                                    sb.append(',');
                                }
                                sb.append(manifestItem);
                            }
                            eda.setProperty(daemonID, config.getPropertyList(), sb.toString());
                        }
                        break;
                    } else {
                        break;
                    }
                } else {
                    getLogger().log(Level.SEVERE, "Unable to get FabricEngineDaemonId");
                    // we found our daemon and couldn't get the ID, so quit
                    return;
                }
            }
        }
    }
}
