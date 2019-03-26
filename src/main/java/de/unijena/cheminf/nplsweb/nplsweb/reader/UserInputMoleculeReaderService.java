package de.unijena.cheminf.nplsweb.nplsweb.reader;


import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Calendar;

@Service
public class UserInputMoleculeReaderService {



    public boolean verifySMILES(String smiles){



        if(smiles.equals("")){
            return false;
        }


        SmilesParser sp  = new SmilesParser(SilentChemObjectBuilder.getInstance());
        System.out.println(smiles);
        try {
            IAtomContainer m   = sp.parseSmiles(smiles);

        } catch (InvalidSmilesException e) {
            System.out.println(e.toString());
            return false;
        }




        return true;
    }



    public String transformToSMI(String smiles){

        String localSmiFile = "upload-dir/pasted_molecule_"+ Calendar.getInstance().getTime().getTime()+".smi";


        smiles = smiles.replaceAll("\\r|\\n", "");


        smiles = smiles + " "+"UI_"+Calendar.getInstance().getTime().getTime();




        try {


            File f = new File(localSmiFile);
            f.createNewFile();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(f)));

            bw.write(smiles);
            bw.newLine();

            bw.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return localSmiFile;
    }


    public String transformToMOL(String jsonMol){
        String localMolFile = "upload-dir/sketched_molecule_"+ Calendar.getInstance().getTime().getTime()+".mol";
        System.out.println(localMolFile);

        jsonMol = jsonMol.replace("\"", "");
        jsonMol = jsonMol.replace("\\n", "@");

        String [] lines = jsonMol.split("@");
        System.out.println(lines[0]);

        System.out.println(lines);

        try {




            File f = new File(localMolFile);
            //f.getParentFile().mkdirs();
            f.createNewFile();


            FileOutputStream fos = new FileOutputStream(f);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (int i = 0; i < lines.length; i++) {
                bw.write(lines[i]);
                bw.newLine();
            }

            bw.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return  localMolFile;


    }


}
