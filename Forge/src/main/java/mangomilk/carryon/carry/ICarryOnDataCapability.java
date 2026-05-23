package mangomilk.carryon.carry;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import mangomilk.carryon.common.carry.CarryOnData;

@AutoRegisterCapability
public interface ICarryOnDataCapability  {

    CarryOnData getCarryData();

    void setCarryData(CarryOnData data);

}
