package mnm.mods.libupd8;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import net.minecraft.launchwrapper.Launch;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid="libupd8", name="libupd8", version="1.0")
public class LibraryUpdate {
	
	private File dir = new File(System.getProperty("user.dir"));
	private File version;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private Logger logger = LogManager.getLogger("libupd8");
	private static final String VERSION_URL = "https://s3.amazonaws.com/Minecraft.Download/versions/%s/%s.json";

	private JsonObject object;
	private boolean updated = false;
	
	private List<String> launchArgs;
	
	@EventHandler
	public void start(FMLPreInitializationEvent event){
		// Run this "mod" in a thread
		new Thread("LibUpd8"){
			@Override
			public void run(){
				LibraryUpdate.this.start();
				this.interrupt();
			}
		}.start();
	}
	
	private void start(){
		try {
			// Get mcversion from fml.properties
			Properties p = new Properties();
			p.load(getClass().getResourceAsStream("/fmlversion.properties"));
			String rver = p.getProperty("fmlbuild.mcversion");
			if(rver == null){
				logger.warn("Couldn't determine Minecraft version");
				return;
			}	
			JsonElement remotej = getRemoteJson(rver);
			
			// Get the launched version from the blackboard
			this.launchArgs = (List<String>) Launch.blackboard.get("ArgumentList");
			
			int ind = launchArgs.indexOf("--version");
			String lver = launchArgs.get(ind+1);
			
			if(lver == null){
				logger.warn("Couldn't determine launched version");
				return;
			}
			JsonElement localj = getLocalJson(lver);
			this.object = localj.getAsJsonObject();
			
			// Start comparing libraries
			List<Library> remotelibs = getLibraries(remotej.getAsJsonObject().get("libraries").getAsJsonArray());
			List<Library> locallibs = getLibraries(localj.getAsJsonObject().get("libraries").getAsJsonArray());
			compareLibraries(locallibs, remotelibs);
			if(updated){
				logger.info("Changes will take effect the next time you open your launcher.");
				saveJson();
			}else
				logger.info("No library updates were found.");
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private void saveJson() throws IOException {
		String json = gson.toJson(this.object);
		FileUtils.writeStringToFile(this.version, json);
	}

	private JsonElement getRemoteJson(String version) throws IOException {
		URL url = new URL(String.format(VERSION_URL, version, version));
		InputStream is = url.openStream();
		try{
			return gson.fromJson(new InputStreamReader(is), JsonElement.class);
		}finally{
			is.close();
		}
	}
	
	private JsonElement getLocalJson(String version) throws IOException {
		this.version = new File(dir, String.format("versions/%s/%s.json", version, version));
		return gson.fromJson(new FileReader(this.version), JsonElement.class);
	}
	
	private List<Library> getLibraries(JsonArray array){
		List<Library> list = Lists.newArrayList();
		for(JsonElement library : array)
			list.add(new Library(library.getAsJsonObject()));
		return list;
	}
	
	private void compareLibraries(List<Library> locals, List<Library> remotes){
		for(Library remote : remotes){
			Library local = getLibrary(locals, remote);
			if(local == null){
				addLibrary(remote);
				updated = true;
			}
			else if(!local.version.equals(remote.version) && checkForUpdate(local.version, remote.version)){
				updateLibrary(local, remote);
				updated = true;
			}
		}
	}
	
	private void addLibrary(Library remote) {
		logger.info(String.format("Adding missing library \"%s\"", remote.name));
		this.object.get("libraries").getAsJsonArray().add(remote.getJsonObject());
	}

	private void updateLibrary(Library local, Library remote){
		logger.info(String.format("Updating library \"%s:%s\" from %s to %s", remote.group, remote.name, local.version, remote.version));
	
		JsonArray array = this.object.get("libraries").getAsJsonArray();
		for(JsonElement element : array){
			JsonObject obj = element.getAsJsonObject();
			if(new Library(obj).equals(local)){
				obj.remove("name");
				obj.addProperty("name", remote.getFormattedName());
			}
		}
	}
	
	private boolean checkForUpdate(String local, String remote){
		String[] locala = local.split("\\.");
		String[] remotea = remote.split("\\.");
		try{
			for(int i = 0; i < Math.min(locala.length, remotea.length); i++){
				int l = Integer.parseInt(locala[i]), r = Integer.parseInt(remotea[i]);
				if(l > r) // 1.3 -> 1.2, no
					return false;
				if(l < r) // 1.2 -> 1.3, yes
					return true;
			}
			return locala.length < remotea.length; // 1.2 -> 1.2.1, yes
		}catch(NumberFormatException e){
			return false; // has letters, ignore
		}
	}

	private Library getLibrary(List<Library> list, Library lib){
		for(Library library : list){
			if(lib.equals(library))
				return library;
		}
		return null;
	}

}
