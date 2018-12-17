package org.ucb.act.sar;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.io.formats.mdl.MolImport;
import chemaxon.marvin.space.drawing.Bond;
import chemaxon.reaction.Reactor;
import chemaxon.sss.SearchConstants;
import chemaxon.sss.search.MolSearch;
import chemaxon.sss.search.MolSearchOptions;
import chemaxon.standardizer.Standardizer;
import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;
import chemaxon.struc.RxnMolecule;
import chemaxon.util.MolHandler;
import com.chemaxon.mapper.AutoMapper;
import com.chemaxon.mapper.Mapper;
import com.chemaxon.search.mcs.MaxCommonSubstructure;
import com.chemaxon.search.mcs.McsSearchResult;
import org.ucb.act.utils.ChemAxonUtils;

import java.io.File;
import java.util.*;

/**
 * Created by Daniel on 10/24/2018.
 *
 *
 */
public class OverloadedMapper {

    public static void main(String[] args) throws Exception {
        ChemAxonUtils.license();
        Molecule star = MolImporter.importMol("C(C)(C)(C)(C)(C)");
        Molecule plus = MolImporter.importMol("C(C)(C)(C)(C)");
        MaxCommonSubstructure mcssearch = MaxCommonSubstructure.newInstance();
        mcssearch.setMolecules(star, plus);
        McsSearchResult res = mcssearch.nextResult();
        Molecule mcs = res.getAsMolecule();
        ChemAxonUtils.savePNGImage(mcs, "mcs.png");


        Molecule impossreact = MolImporter.importMol("C(C)(C)(C)(C)(C)(C)(C)(C)(C)(C)(C)C");
        //Molecule impossprod = MolImporter.importMol("C(C)(C)(C)(C)CO");
        Molecule impossprod = MolImporter.importMol("C(C)(C)(C)(C)");

        AutoMapper mapper = new AutoMapper();
        mapper.setMappingStyle(Mapper.MappingStyle.MATCHING);
        RxnMolecule dummy = new RxnMolecule();
        dummy.addComponent(impossreact, RxnMolecule.REACTANTS);
        dummy.addComponent(impossprod, RxnMolecule.PRODUCTS);
        mapper.map(dummy);
        for(MolAtom atom : impossreact.getAtomArray()){
            System.out.print(atom.getAtomMap() + "   ");
            System.out.println(atom.getBondCount());
        }
        System.out.println("-----");
        for(MolAtom atom : impossprod.getAtomArray()){
            System.out.print(atom.getAtomMap() + "   ");
            System.out.println(atom.getBondCount());
        }

       Molecule fluorophen = MolImporter.importMol("InChI=1S/C9H10FNO2/c10-7-3-1-6(2-4-7)5-8(11)9(12)13/h1-4,8H,5,11H2,(H,12,13)");
        //Molecule chlorophen = MolImporter.importMol("InChI=1S/C9H10FNO2/c10-7-3-1-6(2-4-7)5-8(11)9(12)13/h1-4,8H,5,11H2,(H,12,13)");
        //Molecule metachlorophen = MolImporter.importMol("InChI=1S/C9H10ClNO2/c10-7-3-1-2-6(4-7)5-8(11)9(12)13/h1-4,8H,5,11H2,(H,12,13)");

        String ro ="[H][#7]([H])-[#6:5]([H])-[#6:1]>>[#6:1]-[#6:5]=O";
        RxnMolecule baseEro = RxnMolecule.getReaction(MolImporter.importMol(ro));


        MolSearch searcher = new MolSearch();
        MolSearchOptions searchOptions = new MolSearchOptions(SearchConstants.SUBSTRUCTURE);
        searchOptions.setVagueBondLevel(SearchConstants.VAGUE_BOND_LEVEL4);
        searcher.setSearchOptions(searchOptions);
        MolHandler mh1 = new MolHandler("[N:8][C:9][C:10](=[O:11])[O:12]", true);
        Molecule query = mh1.getMolecule();
        //Standardizer st = new Standardizer("addexplicith");
        //st.standardize(query);
        searcher.setQuery(query);
        Set<Integer> ROIndices = new HashSet<>();
        Molecule[] subs = new Molecule[6];

        Molecule leu = MolImporter.importMol("InChI=1S/C6H13NO2/c1-4(2)3-5(7)6(8)9/h4-5H,3,7H2,1-2H3,(H,8,9)");
        Molecule val = MolImporter.importMol("InChI=1S/C5H11NO2/c1-3(2)4(6)5(7)8/h3-4H,6H2,1-2H3,(H,7,8)");
        Molecule ala = MolImporter.importMol("InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)");
        Molecule phe = MolImporter.importMol("InChI=1S/C9H11NO2/c10-8(9(11)12)6-7-4-2-1-3-5-7/h1-5,8H,6,10H2,(H,11,12)");
        Molecule met = MolImporter.importMol("InChI=1S/C5H11NO2S/c1-9-3-2-4(6)5(7)8/h4H,2-3,6H2,1H3,(H,7,8)");
        Molecule trp = MolImporter.importMol("InChI=1S/C11H12N2O2/c12-9(11(14)15)5-7-6-13-10-4-2-1-3-8(7)10/h1-4,6,9,13H,5,12H2,(H,14,15)");
        Molecule ile = MolImporter.importMol("InChI=1S/C6H13NO2/c1-3-4(2)5(7)6(8)9/h4-5H,3,7H2,1-2H3,(H,8,9)");

        //subs[0] = met;
        subs[0] = phe;
        subs[1] = val;
        subs[2] = ala;
        subs[3] = leu;
        subs[4] = ile;
        subs[5] = fluorophen;

        for(Molecule m : subs){
            m.aromatize();
        }
        //subs[2] = metachlorophen;

        File filedir = new File("output");
        if(!filedir.exists()) {
            filedir.mkdir();
        }
        for(File file : filedir.listFiles()){
            if(file.getPath().endsWith(".png")){
                file.delete();
            }
        }

        //Molecule[] labeledSubs = new Molecule[3];
        Set<Integer> ROMaps = new HashSet<>();
        for(int i = 0; i< subs.length; i++){
            searcher.setTarget(subs[i]);
            int[] matches = searcher.findAll()[0];
            for(int j = 0; j < matches.length; j++){
                if(matches[j] > 0){
                    MolAtom targetAt = subs[i].getAtomArray()[matches[j]];
                    MolAtom queryAt = query.getAtomArray()[j];
                    targetAt.setAtomMap(queryAt.getAtomMap());
                    ROMaps.add(queryAt.getAtomMap());
                }
            }
            ChemAxonUtils.savePNGImage(subs[i], "output/searched" + i + ".png");
        }
        Molecule ensemble = subs[0].clone();
        ChemAxonUtils.savePNGImage(ensemble, "output/startens.png");
        for(int i = 1; i < subs.length; i++){
            Molecule sub = subs[i];
            RxnMolecule subToEns = new RxnMolecule();
            String subsmiles = ChemAxonUtils.toSmiles(sub);
            String enssmiles = ChemAxonUtils.toSmiles(ensemble);
            subToEns = RxnMolecule.getReaction(MolImporter.importMol(subsmiles + ">>" + enssmiles));
            //AutoMapper autoMapper = new AutoMapper();
            //autoMapper.setMappingStyle(Mapper.MappingStyle.MATCHING);
            //autoMapper.map(subToEns);
            //autoMapper.map(ensToSub);
            ChemAxonUtils.savePNGImage(subToEns.getReactant(0), "output/substrate" + i + ".png");

            RxnMolecule extend = new RxnMolecule();


            //extend.addComponent(sub, RxnMolecule.PRODUCTS);

            Molecule toPrune = sub.clone();
            MaxCommonSubstructure keptsearcher = MaxCommonSubstructure.newInstance();
            keptsearcher.setMolecules(toPrune, ensemble);
            Molecule pruned = keptsearcher.nextResult().getAsMolecule();
            String prunedsmiles = ChemAxonUtils.toSmiles(pruned);
            /*


            Set<MolAtom> dying = new HashSet<>();
            Molecule numberedAnd
            for(MolAtom atom : toPrune.getAtomArray()) {
                if (atom.getAtomMap() == 0) {
                    dying.add(atom);
                }
            }
            for(MolAtom atom : dying){
                toPrune.removeAtom(atom);
            }*/
            //String prunesmiles = ChemAxonUtils.toSmiles(toPrune);
            //extend.addComponent(toPrune, RxnMolecule.REACTANTS);
            extend = RxnMolecule.getReaction(MolImporter.importMol(prunedsmiles + ">>" + subsmiles));
            ChemAxonUtils.savePNGImage(pruned, "output/pruned" + i + ".png");
            Reactor reactor = new Reactor();
            reactor.setReaction(extend);
            reactor.setReactants(new Molecule[]{ensemble});
            ChemAxonUtils.savePNGImage(extend, "output/extend" + i + ".png");
            Molecule[] prod = reactor.react();
            ensemble = prod[0];
            for(int j = 0; j < ensemble.getAtomCount(); j++){
                MolAtom at = ensemble.getAtom(j);
                if(!ROMaps.contains(at.getAtomMap())){
                    at.setAtomMap(0);
                }
            }

            for(int j = 0; j <ensemble.getAtomCount(); j++){
                MolAtom molAtom = ensemble.getAtom(j);
                assert(molAtom.getAtomMap() == 0 || ROMaps.contains(molAtom.getAtomMap()));
            }
            ChemAxonUtils.savePNGImage(ensemble, "output/ens" + i + ".png");

        }
        int k = 0;
        HashMap<MolAtom,Integer> atomids = new HashMap<>();
        for(int i = 0; i < ensemble.getAtomArray().length; i++){
            atomids.put(ensemble.getAtom(i), i);
        }
        for(MolBond bond : ensemble.getBondArray()){
            System.out.print(k + "     ");
            k++;
            System.out.print(bond.getAtom1().getAtno() + " " + bond.getAtom1().getAtomMap() + " " + atomids.get(bond.getAtom1()) + "        ");
            System.out.println(bond.getAtom2().getAtno() + " " + bond.getAtom2().getAtomMap() + " " + atomids.get(bond.getAtom2()));


        }
        ChemAxonUtils.saveSVGImage(ensemble, "ensemble.svg");

    }

}
