package org.ucb.c5.inventoryModel;

//import org.ucb.semiprotocol.model.*;
import org.ucb.semiprotocolModel.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Updates inventory from semiprotocols. Does not do manual modifications such as shipments or abort scenarios.
 * @author Daniel Blank
 */
public class Updater {


    public void initiate(){
    }

    //Anita assumes that setContainedBy will MOVE the item OUT of its current location and INTO its next location.
    public void run(Inventory inv, Semiprotocol sem) throws Exception {
        for (Task task : sem.getSteps()) { //iterate over tasks in the semiprotocol
            switch (task.getOperation()) {
                case transfer:
                    Transfer transfer = (Transfer) task;
                    String src = transfer.getSource();
                    String dst = transfer.getDest();
                    double vol = transfer.getVolume();
                    String[] srcLoc = src.split("/");
                    String[] dstLoc = dst.split("/"); //If source or dest is a well in a plate or a tube in a strip, it will be formatted /plate1:3,3 for well index (3, 3) in plate 1. Else it is just a container name.
                    if(srcLoc.length == 1){ //no slash, means an individual tube name
                        Tube source = (Tube) inv.getFromLabel(src);
                        if (source == null){
                            break;
                        }
                        source.removeVol(vol);
                    }
                    else{
                        String toDrawFrom = srcLoc[srcLoc.length - 1]; //parse strip/plate syntax
                        String[] nameAndIndices = toDrawFrom.split(":");
                        String[] indicesStrings = nameAndIndices[1].split(",");
                        int[] indices = new int[]{Integer.parseInt(indicesStrings[0]), Integer.parseInt(indicesStrings[1])};
                        if(transfer.getSourceType().equals("Strip")){
                            //Strip stripSrc = (Strip) inv.getFromLabel(nameAndIndices[0]);
                            // stripSrc.removeVol(vol, indices[1]);
                        }
                        else{
                            Plate plateSrc = (Plate) inv.getFromLabel(nameAndIndices[0]);
                            plateSrc.removeVol(vol, indices[0], indices[1] );
                        }
                    }
                    
                    if(dstLoc.length == 1){ //repeat for destination
                        Tube dest = (Tube) inv.getFromLabel(dst);
                        if (dest == null){
                            break;
                        }
                        dest.addVol(vol);
                    }
                    else{
                        String toAddTo = dstLoc[dstLoc.length - 1];
                        String[] nameAndIndices = toAddTo.split(":");
                        String[] indicesStrings = nameAndIndices[1].split(",");
                        int[] indices = new int[]{Integer.parseInt(indicesStrings[0]), Integer.parseInt(indicesStrings[1])};
                        if(transfer.getDestType().equals("Strip")){
                            //Strip stripDst = (Strip) inv.getFromLabel(nameAndIndices[0]);
                            //stripDst.addVol(vol, indices[1]);
                        } else{
                            Plate plateDst = (Plate) inv.getFromLabel(nameAndIndices[0]);
                            plateDst.addVol(vol, indices[0], indices[1] );  
                        }
                    }

                    break;
                case addContainer:
                    AddContainer addContainer = (AddContainer) task;
                    Container added = null;
                    ContainerType contType = addContainer.getTubetype();
                    if(addContainer.getLocation() == null || addContainer.getLocation().charAt(0) != '/') {
                        if (contType == ContainerType.cell_aliquot || contType == ContainerType.column_tube || contType == ContainerType.column || contType == ContainerType.oligo_stockTube || contType == ContainerType.culture_flask){
                            break;
                        }

                        String id = inv.addAnything(addContainer.getName(), addContainer.getTubetype(), true);//container put in default location by inventory, likely to be stored later
                        if((contType == ContainerType.eppendorfTube || contType == ContainerType.pcrTube) && addContainer.getStartingVol() != 0){
                            ((Tube) inv.getFromID(id)).addVol(addContainer.getStartingVol());
                        }

                    }
                    else{
                        String[] loc = addContainer.getLocation().split(":"); //loc formatted as "/rack1:3,3" for position (3, 3) in rack1

                        Container toPutIn = inv.getFromLabel(loc[0].substring(1));
                        String[] pos = loc[1].split(",");
                        int[] posAsInts = new int[]{Integer.parseInt(pos[0]), Integer.parseInt(pos[1])};
                        if (contType == ContainerType.cell_aliquot || contType == ContainerType.column_tube || contType == ContainerType.column || contType == ContainerType.oligo_stockTube || contType == ContainerType.culture_flask){
                            break;
                        }  
                        String id = inv.addAnything(addContainer.getName(), addContainer.getTubetype(), false);
                        added = inv.getFromID(id);
                        if(addContainer.getTubetype() == ContainerType.Strip){
                            ((PcrRack) toPutIn).addStripToPos(added, posAsInts[0]); //add strip
                        }
                        else {
                            if(addContainer.getStartingVol() != 0){
                                ((Tube) added).addVol(addContainer.getStartingVol());
                            }
                            ((Rack) toPutIn).addTubeToPos(added, posAsInts);
                        }

                        }
                    break;
                case dispense:
                    Dispense dispense = (Dispense) task; //handled like half a transfer
                    String toDisp = dispense.getDstContainer();
                    double volDispensed = dispense.getVolume();
                    String[] dispLoc = toDisp.split("/");
                    if(dispLoc.length == 1){
                        if (inv.getFromLabel(toDisp) == null){
                            break;
                        }
                        Tube dispTube = (Tube) inv.getFromLabel(toDisp);
                        dispTube.addVol(volDispensed);
                    }
                    else{
                        String toDispTo = dispLoc[dispLoc.length - 1];
                        String[] nameAndIndices = toDispTo.split(":");
                        String[] indicesStrings = nameAndIndices[1].split(",");
                        int[] indices = new int[]{Integer.parseInt(indicesStrings[0]), Integer.parseInt(indicesStrings[1])};
                        if(nameAndIndices[0].contains("Strip")){
                            //Strip stripDisp = (Strip) inv.getFromLabel(nameAndIndices[0]);
                            //stripDisp.addVol(volDispensed, indices[1]);
                        }
                        else{
                            Plate plateDisp = (Plate) inv.getFromLabel(nameAndIndices[0]);
                            plateDisp.addVol(volDispensed, indices[0], indices[1] );
                        }
                    }
                    break;
                case Dispose:
                    Dispose dispose = (Dispose) task;
                    Container trashed = inv.getFromLabel(dispose.getDisposed());
                    //ANITA IS DEBUGGING
                    if (trashed != null) { //does not dispose of a container that already does not exist
                        inv.deleteContainer(trashed);
                    }
                    break;
                case Store:
                    //Store store = (Store) task;
                    //Container stored = inv.getFromLabel(store.getStored());
                    //stored.setContainedBy(inv.getFromLabel(store.getLocation()));
                    break;
            }

            }
        //FileSystem.serialize(inv.sortedInventoryKeys(), inv.getInvContents(), inv.getSerialNumbers()); //persist altered inventory to filesystem, per semiprotocol.

    }


    public static void main(String[] args) throws Exception{
        Inventory inventory = new Inventory(1, 2, 0);
        //inventory.load();
        Updater updater = new Updater();
        List<Task> instructions = new ArrayList<>();
        Task addRack = new AddContainer(ContainerType.EppRack, "rack1", null, true);
        Task addFirstContainer = new AddContainer(ContainerType.eppendorfTube, "eppen1", "/rack1:2,2", true);
        Task addSecondContainer = new AddContainer(ContainerType.eppendorfTube, "eppen2", "/rack1:2,3", true);
        Task dispenseToSecond = new Dispense(Reagent.water, "eppen2", 50);
        Task transferToFirst = new Transfer("eppen2", "eppen1", 8, Boolean.FALSE, "eppendorf", "eppendorf");
        Task killSecond = new Dispose("eppen2", "biohazard");
        Task killRack = new Dispose("rack1", "biohazard");
        Task storeRack = new Store("rack1", DestinationType.Thermocycler, "Thermocycler_0");

        instructions.add(addRack);
        instructions.add(addFirstContainer);
        instructions.add(addSecondContainer);
        instructions.add(dispenseToSecond);
        instructions.add(transferToFirst);
        //instructions.add(killSecond);
        //instructions.add(storeRack);
        instructions.add(killRack);
        instructions.add(killSecond);
        Semiprotocol semiprotocol = new Semiprotocol(instructions);
        updater.run(inventory, semiprotocol);
    }


}
