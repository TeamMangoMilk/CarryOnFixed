package mangomilk.carryon;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import mangomilk.carryon.networking.PacketBase;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class CarryOnNeoForgeClient {

    public CarryOnNeoForgeClient(ModContainer container) {

    }

    public static void sendPacketToServer(PacketBase packet) {
        PacketDistributor.sendToServer(packet);
    }

}