package io.wispforest.affinity.blockentity;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

@SuppressWarnings("UnstableApiUsage")
public interface AethumFluxContainer {

    /**
     * @return The amount of flux stored in this container
     */
    long flux();

    /**
     * @return The maximum amount of flux this container can hold
     */
    long fluxCapacity();

    // Insertion

    /**
     * Tries to insert {@code max} amount of flux into this container
     *
     * @param max         The maximum amount of the insert
     * @param transaction The transaction context this operation happens in
     * @return The amount of flux that was inserted
     */
    long insert(long max, TransactionContext transaction);

    /**
     * @return {@code true} if this container supports flux insertion
     */
    boolean canInsert();

    /**
     * @return The maximum amount of flux this container can insert per operation
     */
    long maxInsert();

    // Extraction

    /**
     * Tries to extract {@code max} amount of flux from this container
     *
     * @param max         The maximum amount of the extract
     * @param transaction The transaction context this operation happens in
     * @return The amount of flux that was extracted
     */
    long extract(long max, TransactionContext transaction);

    /**
     * @return {@code true} if this container supports energy extraction
     */
    boolean canExtract();

    /**
     * @return The maximum amount of flux this container can extract per operation
     */
    long maxExtract();

}
