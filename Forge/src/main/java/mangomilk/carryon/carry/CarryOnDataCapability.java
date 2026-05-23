package mangomilk.carryon.carry;

import mangomilk.carryon.common.carry.CarryOnData;

public class CarryOnDataCapability implements ICarryOnDataCapability {

    private CarryOnData data;

    public CarryOnDataCapability() {
        this.data = CarryOnData.empty();
    }

    @Override
    public CarryOnData getCarryData() {
        return data;
    }

    @Override
    public void setCarryData(CarryOnData data) {
        this.data = data;
    }
}
