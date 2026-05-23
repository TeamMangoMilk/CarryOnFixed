package mangomilk.carryon.carry;

/*
public class CarryOnDataSyncHandler implements AttachmentSyncHandler<CarryOnData> {
    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, CarryOnData carryOnData, boolean b) {
        CarryOnData.STREAM_CODEC.encode(registryFriendlyByteBuf, carryOnData);
    }

    @Override
    public @Nullable CarryOnData read(IAttachmentHolder iAttachmentHolder, RegistryFriendlyByteBuf registryFriendlyByteBuf, @Nullable CarryOnData carryOnData) {
        return CarryOnData.STREAM_CODEC.decode(registryFriendlyByteBuf);
    }

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        if (to.connection == null)
            return false;
        return AttachmentSyncHandler.super.sendToPlayer(holder, to);
    }
}
 */
