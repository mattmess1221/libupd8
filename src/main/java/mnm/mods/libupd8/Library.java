package mnm.mods.libupd8;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Library {
	private JsonObject object;
	
	public String group;
	public String name;
	public String version;
	
	public Library(JsonObject object){
		this.object = object;
		String[] ver = this.object.get("name").getAsString().split(":");
		group = ver[0];
		name = ver[1];
		version = ver[2];
	}
	
	public String getFormattedName(){
		return String.format("%s:%s:%s", group, name, version);
	}
	
	public JsonObject getJsonObject(){
		return object;
	}
	
	/**
	 * Determines if a library is equal to this one.  Ignores versions.
	 */
	@Override
	public boolean equals(Object object){
		if(object.getClass() != getClass())
			return false;
		Library library = (Library) object;
		return library.group.equals(group) && library.name.equals(name);
	}
}
