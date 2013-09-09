[fabrician.org](http://fabrician.org/)
==========================================================================
Engine Property Asset Manager Guide
==========================================================================

Introduction
--------------------------------------
The Engine Property Asset Manager allows an Enabler to set two properties on an Engine Daemon.  One property is used to indicate if other components
using the same enabler are allowed to run on the host where the property is set. For example if an OS level change was applied by the enabler and as a result of that 
no other Enablers that want to modify the OS should run on that host. The second property is whatever value you want the engines tagged with to
be used in conjunction with a Resource Preference.  

One scenario where these properties are useful are with enablers that support host configuration.  The properties allow users of this asset manager
to create resource preference rules based on the properties.  Users (via resource preference rules) can require or prohibit components from a 
host that have the properties set to specific values.  For instance, a JRuby enabler running a Puppet component can tag the engine daemon with which manifest
was applied.  You can then create a resource preference rule that only allows a component to run on an engine where that manifest was applied.  


Supported Platforms
--------------------------------------
* Silver Fabric 5
* Linux, Windows


XML Deployment Descriptor
--------------------------------------
The following is an XML Deployment Descriptor example.

```XML
<enginedaemonproperty class="org.fabrician.assetmanager.daemonproperty.EngineProperty">
    <property name="enabled" value="True"/>
    <property name="description" value="Engine Daemon Property Asset Manager"/>
    <property name="name" value="Silver Fabric Engine Daemon Properties Asset Manager for Puppet"/>
    <config class="org.fabrician.assetmanager.daemonproperty.EngineDaemonPropertyConfig">
        <property name="enablerName" value="Ruby 1.9.3"/>  
        <property name="exclusivityTagVariableName" value="PuppetRequireExclusivity"/>      
        <property name="exclusivityTag" value="PuppetExclusive"/>        
        <property name="exclusivityTagDescription" value="Indicates if more than one Puppet Manifest can be applied to this host"/>        
        <property name="propertyTagVariableName" value="PuppetManifest"/>
        <property name="propertyList" value="PuppetManifestList"/>
        <property name="propertyListDescription" value="Shows the list of Puppet Manifests applied to this host"/>
        <property name="removeDefaultPropertiesOnExit" value="true"/>
    </config>
</enginedaemonproperty>
```
Configuration property descriptions:

* **enablerName** - the enabler that this asset manager is being used for.
* **exclusivityTagVariableName** - the name of the component runtime context variable that was created (RequireExclusivity).
* **exclusivityTag** - the name of the engine property. The name needs to be unique because engine properties are global.
* **exclusivityTagDescription** - the description of the engine property.
* **propertyTagVariableName** - the name of the component runtime context variable that was created (PropertyList).
* **propertyList** - the name of the engine property. 
* **propertyListDescription** - the description of the engine property.
* **removeDefaultPropertiesOnExit** - delete the engine properties if the asset manager is disabled.


Packaging
--------------------------------------
The Asset Manager jar is created by building the maven project. The build depends on the SilverFabricSDK jar file that is distributed with TIBCO Silver Fabric. 
The SilverFabricSDK.jar file needs to be referenced in the maven pom.xml or it can be placed in the project root directory.

```bash
mvn package
```

Deployment
--------------------------------------
To deploy the Asset Manager, copy the JAR created in the packaging step along with the XML descriptor to the 
SF_HOME/webapps/livecluster/WEB-INF/assets directory of the Silver Fabric Broker. The Asset Manager will automatically be
detected and loaded. You can verify successful deployment by ensuring that the Asset Manager appears on the Admin > Assets page in the 
Silver Fabric Administration Tool. That page may also be used to Enable and Disable Asset Managers as well as edit Asset
Managers that have an AssetManagerConfig implementation. 

To segregate Asset Managers, you may add the Asset Manager JARs and XML file to a first-level subdirectory of the SF_HOME/webapps/livecluster/WEB-INF/assets directory.


Component Runtime Context Variables
--------------------------------------
The following are the Component Runtime Context Variables used by the asset manager.  'RequireExclusivity' and 'PropertyList' are mandatory for activating components.  
The others are optional. 

* **RequireExclusivity** - Are other enablers of the same type allowed to run on this host. 
    * Type: String
    * value: true or false
* **PropertyList** - A list of comma separated properties that you want to tag the engine daemon with. The property name needs to be unique because engine properties are global. 
    * Type: String
    * value: property1, property2
* **removePropertiesOnShutdown** - Remove the properties that were created when the component is deactivated.
    * Type: String
    * value: true or false
* **undoMode** - Allows for removal of properties created by another component to be removed by an activating component. 
    * Type: String
    * value: true or false    
* **autoStopStack** - Used to stop the stack after properties are set on the engine daemon, if the stack is not needed. 
    * Type: String
    * value: true or false



