package io.wispforest.affinity.fixers;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceWriteReadFix;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.stream.Stream;

public class ItemTransferNodeFix extends ChoiceWriteReadFix {

    public ItemTransferNodeFix(Schema outputSchema) {
        super(outputSchema, false, "Item transfer node entry origin fixer", TypeReferences.BLOCK_ENTITY, "affinity:item_transfer_node");
    }

    @Override
    protected <T> Dynamic<T> transform(Dynamic<T> data) {
        var entries = data.get("Entries").result();
        var newEntries = new ArrayList<Dynamic<T>>();
        entries.ifPresent(dynamic -> {
            dynamic.asStream().forEach(entryDynamic -> {
                var pos = entryDynamic.get("OriginNode").result();
                if (pos.isPresent()) {
                    var blockPos = BlockPos.fromLong(pos.get().asLong(0));
                    newEntries.add(entryDynamic.set("OriginNode", data.createList(Stream.of(data.createInt(blockPos.getX()), data.createInt(blockPos.getY()), data.createInt(blockPos.getZ())))));
                } else {
                    newEntries.add(entryDynamic);
                }
            });
        });

        var links = data.get("Links").result();
        var newLinks = new ArrayList<Dynamic<T>>();
        links.ifPresent(dynamic -> {
            dynamic.asStream().forEach(linkLong -> {
                var blockPos = BlockPos.fromLong(linkLong.asLong(0));
                newLinks.add(data.createList(Stream.of(data.createInt(blockPos.getX()), data.createInt(blockPos.getY()), data.createInt(blockPos.getZ()))));
            });
        });

        var result = data;
        if (!newEntries.isEmpty()) result = data.set("Entries", data.createList(newEntries.stream()));
        if (!newLinks.isEmpty()) result = data.set("Links", data.createList(newLinks.stream()));

        return result;
    }
}
