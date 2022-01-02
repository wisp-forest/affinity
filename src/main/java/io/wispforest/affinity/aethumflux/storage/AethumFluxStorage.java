package io.wispforest.affinity.aethumflux.storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.NbtCompound;

@SuppressWarnings("UnstableApiUsage")
public class AethumFluxStorage extends SnapshotParticipant<Long> implements AethumFluxContainer {

    private final CommitCallback commitCallback;

    private long capacity;
    private long maxInsert;
    private long maxExtract;

    private long flux = 0;

    public AethumFluxStorage(CommitCallback callback) {
        this.capacity = 0;
        this.maxInsert = 0;
        this.maxExtract = 0;
        this.commitCallback = callback;
    }

    public AethumFluxStorage(long capacity, long maxInsert, long maxExtract, CommitCallback callback) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.commitCallback = callback;
    }

    // -------
    // Storage
    // -------

    @Override
    public long flux() {
        return flux;
    }

    @Override
    public long fluxCapacity() {
        return capacity;
    }

    // ---------
    // Insertion
    // ---------

    @Override
    public long insert(long max, TransactionContext transaction) {
        StoragePreconditions.notNegative(max);

        long transfer = Math.min(maxInsert, Math.min(max, capacity - flux));

        if (transfer > 0) {
            updateSnapshots(transaction);
            this.flux += transfer;
            return transfer;
        }

        return 0;
    }

    @Override
    public boolean canInsert() {
        return maxInsert > 0;
    }

    @Override
    public long maxInsert() {
        return maxInsert;
    }

    // ----------
    // Extraction
    // ----------

    @Override
    public long extract(long max, TransactionContext transaction) {
        StoragePreconditions.notNegative(max);

        long transfer = Math.min(maxExtract, Math.min(max, flux));

        if (transfer > 0) {
            updateSnapshots(transaction);
            this.flux -= transfer;
            return transfer;
        }

        return 0;
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }

    @Override
    public long maxExtract() {
        return maxExtract;
    }

    // ------------
    // Transactions
    // ------------

    @Override
    protected Long createSnapshot() {
        return flux;
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        this.flux = snapshot;
    }

    @Override
    protected void onFinalCommit() {
        this.commitCallback.onTransactionCommitted();
    }

    @FunctionalInterface
    public interface CommitCallback {
        void onTransactionCommitted();
    }

    // -------------
    // Serialization
    // -------------

    public void writeNbt(NbtCompound nbt) {
        nbt.putLong("AethumFlux", flux);
    }

    public void readNbt(NbtCompound nbt) {
        this.flux = nbt.getLong("AethumFlux");
    }

    // -------
    // Setters
    // -------

    /**
     * @return {@code true} if the flux stored in this storage changed
     */
    public boolean setFlux(long flux) {
        boolean changed = this.flux != flux;
        this.flux = flux;
        return changed;
    }

    public void setFluxCapacity(long capacity) {
        this.capacity = capacity;
    }

    public void setMaxInsert(long maxInsert) {
        this.maxInsert = maxInsert;
    }

    public void setMaxExtract(long maxExtract) {
        this.maxExtract = maxExtract;
    }
}
