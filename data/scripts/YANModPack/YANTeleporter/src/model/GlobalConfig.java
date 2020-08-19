package YANModPack.YANTeleporter.src.model;

import java.util.Map;

import YANModPack.YANTeleporter.src.model.entity.GroupTeleport;
import YANModPack.YANTeleporter.src.model.entity.SoloTeleport;

public class GlobalConfig
{
	private boolean debug;
	private Map<String, SoloTeleport> soloTeleports;
	private Map<String, GroupTeleport> groupTeleports;
	
	public void afterDeserialize(TeleporterConfig config)
	{
		for (SoloTeleport teleport : soloTeleports.values())
		{
			teleport.afterDeserialize(config);
		}
		
		for (GroupTeleport teleport : groupTeleports.values())
		{
			teleport.afterDeserialize(config);
		}
	}
	
	public boolean getDebug()
	{
		return debug;
	}
	
	public Map<String, SoloTeleport> getSoloTeleports()
	{
		return soloTeleports;
	}
	
	public Map<String, GroupTeleport> getGroupTeleports()
	{
		return groupTeleports;
	}
}
