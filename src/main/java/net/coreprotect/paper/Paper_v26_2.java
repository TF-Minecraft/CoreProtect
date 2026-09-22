package net.coreprotect.paper;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import net.coreprotect.utility.ErrorReporter;

public class Paper_v26_2 extends Paper_v26_1 {

    @Override
    public boolean isAttached(Block block, Block scanBlock, BlockData blockData, int scanMin) {
        try {
            Class<?> speleothem = Class.forName("org.bukkit.block.data.type.Speleothem");
            if (speleothem.isInstance(blockData)) {
                BlockFace blockFace = (BlockFace) speleothem.getMethod("getVerticalDirection").invoke(blockData);
                return scanBlock.getRelative(blockFace.getOppositeFace()).getLocation().equals(block.getLocation());
            }
        }
        catch (ClassNotFoundException ignored) {
            // This block type was added after the 1.21.10 compile baseline.
        }
        catch (ReflectiveOperationException exception) {
            ErrorReporter.report(exception);
        }

        return true;
    }

}
