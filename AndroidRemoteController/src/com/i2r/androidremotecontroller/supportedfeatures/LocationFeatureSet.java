package com.i2r.androidremotecontroller.supportedfeatures;

import java.util.List;


import ARC.Constants;
import android.location.LocationManager;
import android.location.LocationProvider;


/**
 * This class models a feature set for location
 * information about this android device that
 * can be relayed to the controller PC.
 * @author Josh Noel
 * @see {@link FeatureSet}
 * @see {@link Feature}
 */
public class LocationFeatureSet extends FeatureSet {

	
	public static final String[] POWER_REQUIREMENTS = {
		"power-requirement-none", "power-requirement-low",
		"power-requirement-medium", "power-requirement-high"
	};
	
	
	
	public static final String PROXIMITY_ALERT = "proximity-alert";
	public static final String PROXIMITY_ALERT_LATITUDE = "proximity-alert-latitude";
	public static final String PROXIMITY_ALERT_LONGITUDE = "proximity-alert-longitude";
	public static final String PROXIMITY_ALERT_RADIUS = "proximity-alert-radius";
	public static final String PROXIMITY_ALERT_EXPIRATION = "proximity-alert-expiration";
	
	
	public static final String PROVIDER = "location-provider";
	public static final String PROVIDER_PREFIX = "location-provider-";
	public static final String ACCURACY_SUFFIX = "-accuracy";
	public static final String POWER_COST_SUFFIX = "-power-cost";
	public static final String MONETARY_COST_SUFFIX = "-has-monetary-cost";
	public static final String CELL_SUFFIX = "-requires-cell-network";
	public static final String DATA_SUFFIX = "-requires-data-network";
	public static final String SATELLITE_SUFFIX = "-requires-satellite";
	public static final String SUPPORTS_ALTITUDE_SUFFIX = "-supports-altitude";
	public static final String SUPPORTS_BEARING_SUFFIX = "-supports-bearing";
	public static final String SUPPORTS_SPEED_SUFFIX = "-supports-speed";
	
	
	/**
	 * Constructor<br>
	 * @param manager - the {@link LocationManager} to
	 * extract supported features information from.
	 * @see {@link LocationFeatureSet}
	 */
	public LocationFeatureSet(LocationManager manager){
		
		
		if(manager != null){
			
			List<String> providers = manager.getAllProviders();
			
			addSet(PROVIDER, Constants.Args.ARG_STRING_NONE, Constants.DataTypes.STRING, providers);
			
			for(String provider : providers){
				
				try{
					LocationProvider p = manager.getProvider(provider);
					String name = PROVIDER_PREFIX + p.getName();
					
					addProperty(name + ACCURACY_SUFFIX, 
							String.valueOf(p.getAccuracy()), Constants.DataTypes.INTEGER);

					addProperty(name + POWER_COST_SUFFIX,
							POWER_REQUIREMENTS[p.getPowerRequirement()], Constants.DataTypes.STRING);
					
					addProperty(name + MONETARY_COST_SUFFIX,
							Boolean.toString(p.hasMonetaryCost()), Constants.DataTypes.STRING);
					
					addProperty(name + CELL_SUFFIX,
							Boolean.toString(p.requiresCell()), Constants.DataTypes.STRING);
					
					addProperty(name + DATA_SUFFIX,
							Boolean.toString(p.requiresNetwork()), Constants.DataTypes.STRING);
					
					addProperty(name + SATELLITE_SUFFIX,
							Boolean.toString(p.requiresSatellite()), Constants.DataTypes.STRING);
					
					addProperty(name + SUPPORTS_ALTITUDE_SUFFIX,
							Boolean.toString(p.supportsAltitude()), Constants.DataTypes.STRING);
					
					addProperty(name + SUPPORTS_BEARING_SUFFIX,
							Boolean.toString(p.supportsBearing()), Constants.DataTypes.STRING);
					
					addProperty(name + SUPPORTS_SPEED_SUFFIX,
							Boolean.toString(p.supportsSpeed()), Constants.DataTypes.STRING);
					
				} catch (Exception e){
					// move on to next provider
				}
			}
			
			
			addSwitch(PROXIMITY_ALERT, FALSE);
			
			addSingleVariant(PROXIMITY_ALERT_EXPIRATION,
					Constants.DataTypes.INTEGER);
			
			addSingleVariant(LocationFeatureSet.PROXIMITY_ALERT_LATITUDE,
					Constants.DataTypes.DOUBLE);
			
			addSingleVariant(LocationFeatureSet.PROXIMITY_ALERT_LONGITUDE,
					Constants.DataTypes.DOUBLE);
			
			addSingleVariant(LocationFeatureSet.PROXIMITY_ALERT_RADIUS,
					Constants.DataTypes.DOUBLE);
			
		}
	}
	
	
	
} // end of LocationFeatureSet class
