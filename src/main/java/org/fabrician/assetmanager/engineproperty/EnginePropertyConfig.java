/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 * 
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code. 
 * In most instances, the license terms are contained in a file named license.txt.
 */
package org.fabrician.assetmanager.engineproperty;

import java.util.logging.Level;


import com.datasynapse.fabric.asset.AssetManager;
import com.datasynapse.fabric.asset.AssetManagerConfig;


public class EnginePropertyConfig implements AssetManagerConfig {
       
    private String enablerName = null;
    private String exclusivityTagVariableName = null;
    private String exclusivityTag = null;
    private String exclusivityTagDescription = null;
    private String propertyTagVariableName = null;
    private String propertyList = null;
    private String propertyListDescription = null;
    private String removeDefaultPropertiesOnExit = null;
    
    public EnginePropertyConfig() {}
    
    public void setEnablerName(String enablerName){
        AssetManager.getLogger().log(Level.FINER, "Set enablerName to "+ enablerName);
        this.enablerName = enablerName;
    }
    
    public String getEnablerName() {
        return enablerName;
    }
    
    public void setExclusivityTagVariableName(String exclusivityTagVariableName) {
        AssetManager.getLogger().log(Level.FINER, "Set exclusivityTagVariableName to "+exclusivityTagVariableName);
        this.exclusivityTagVariableName = exclusivityTagVariableName;
    }

    public String getExclusivityTagVariableName() {
        return this.exclusivityTagVariableName;
    }

    public void setExclusivityTag(String exclusivityTag) {
        AssetManager.getLogger().log(Level.FINER, "Set exclusivityTag to "+exclusivityTag);
        this.exclusivityTag = exclusivityTag;
    }

    public String getExclusivityTag() {
        return this.exclusivityTag;
    }

    public void setPropertyTagVariableName(String propertyTagVariableName) {
        AssetManager.getLogger().log(Level.FINER, " Set propertyTagVariableName to "+propertyTagVariableName);
        this.propertyTagVariableName = propertyTagVariableName;
    }

    public String getPropertyTagVariableName() {
        return this.propertyTagVariableName;
    }

    public void setPropertyList(String propertyList) {
        AssetManager.getLogger().log(Level.FINER, "Set propertyList to "+propertyList);
        this.propertyList = propertyList;
    }

    public String getPropertyList() {
        return this.propertyList;
    }

    public String getPropertyListDescription() {
        return propertyListDescription;
    }

    public void setPropertyListDescription(String propertyListDescription) {
        AssetManager.getLogger().log(Level.FINER, "Set propertyListDescription to "+propertyListDescription);
        this.propertyListDescription = propertyListDescription;
    }

    public String getExclusivityTagDescription() {
        return exclusivityTagDescription;
    }

    public void setExclusivityTagDescription(String exclusivityTagDescription) {
        AssetManager.getLogger().log(Level.FINER, "Set exclusivityTagDescription to "+exclusivityTagDescription);
        this.exclusivityTagDescription = exclusivityTagDescription;
    }

    public String getRemoveDefaultPropertiesOnExit() {
        return removeDefaultPropertiesOnExit;
    }

    public void setRemoveDefaultPropertiesOnExit(String removeDefaultPropertiesOnExit) {
        AssetManager.getLogger().log(Level.FINER, " Set removeDefaultPropertiesOnExit to "+removeDefaultPropertiesOnExit);
        this.removeDefaultPropertiesOnExit = removeDefaultPropertiesOnExit;
    }
    
}
