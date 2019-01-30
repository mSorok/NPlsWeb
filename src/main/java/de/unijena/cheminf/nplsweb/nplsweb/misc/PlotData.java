package de.unijena.cheminf.nplsweb.nplsweb.misc;

import java.util.ArrayList;
import java.util.Hashtable;

public class PlotData {



    // X and Y for everything


    // X and Y for all
    public ArrayList<Double> npXnp = new ArrayList<>();
    public ArrayList<Double> npYnp = new ArrayList<>();


    public ArrayList<Double> npXsm = new ArrayList<>();
    public ArrayList<Double> npYsm = new ArrayList<>();

    // X and Y for kingdoms

    public ArrayList<Double> npXbacteria = new ArrayList<>();
    public ArrayList<Double> npYbacteria = new ArrayList<>();

    public ArrayList<Double> npXfungi = new ArrayList<>();
    public ArrayList<Double> npYfungi = new ArrayList<>();

    public ArrayList<Double> npXplants = new ArrayList<>();
    public ArrayList<Double> npYplants = new ArrayList<>();

    // X and Y by DB

    public ArrayList<Double> npXchebi = new ArrayList<>();
    public ArrayList<Double> npYchebi = new ArrayList<>();

    public ArrayList<Double> npXtcmdb = new ArrayList<>();
    public ArrayList<Double> npYtcmdb = new ArrayList<>();

    public ArrayList<Double> npXzincnp = new ArrayList<>();
    public ArrayList<Double> npYzincnp = new ArrayList<>();

    public ArrayList<Double> npXpubchem = new ArrayList<>();
    public ArrayList<Double> npYpubchem = new ArrayList<>();

    public ArrayList<Double> npXchembl = new ArrayList<>();
    public ArrayList<Double> npYchembl = new ArrayList<>();

    public ArrayList<Double> npXnpatlas = new ArrayList<>();
    public ArrayList<Double> npYnpatlas = new ArrayList<>();

    public ArrayList<Double> npXnubbe = new ArrayList<>();
    public ArrayList<Double> npYnubbe = new ArrayList<>();

    public ArrayList<Double> npXsancdb = new ArrayList<>();
    public ArrayList<Double> npYsancdb = new ArrayList<>();

    public ArrayList<Double> npXdrugbank = new ArrayList<>();
    public ArrayList<Double> npYdrugbank = new ArrayList<>();

    public ArrayList<Double> npXhmdb = new ArrayList<>();
    public ArrayList<Double> npYhmdb = new ArrayList<>();

    public ArrayList<Double> npXold2012 = new ArrayList<>();
    public ArrayList<Double> npYold2012 = new ArrayList<>();

    public ArrayList<Double> npXsupernatural = new ArrayList<>();
    public ArrayList<Double> npYsupernatural = new ArrayList<>();

    public ArrayList<Double> npXafrodb = new ArrayList<>();
    public ArrayList<Double> npYafrodb = new ArrayList<>();



    // X and Y by SIZE

    public ArrayList<Double> npXac100 = new ArrayList<>();
    public ArrayList<Double> npYac100 = new ArrayList<>();

    public ArrayList<Double> npXac100200 = new ArrayList<>();
    public ArrayList<Double> npYac100200 = new ArrayList<>();

    public ArrayList<Double> npXac200300 = new ArrayList<>();
    public ArrayList<Double> npYac200300 = new ArrayList<>();

    public ArrayList<Double> npXac300 = new ArrayList<>();
    public ArrayList<Double> npYac300 = new ArrayList<>();



    //************************************************$
    // DATA FOR EVERYTHING




    // scores

    public Hashtable<Double, Double> npScoresNPall = null; //All NP
    public Hashtable<Double, Double> npScoresSM = null; // All SM

    public Hashtable<Double, Double> npScoresCHEBI = null;
    public Hashtable<Double, Double> npScoresTCMDB = null;
    public Hashtable<Double, Double> npScoresZINCNP = null;
    public Hashtable<Double, Double> npScoresPUBCHEM = null;
    public Hashtable<Double, Double> npScoresCHEMBL = null;
    public Hashtable<Double, Double> npScoresNPATLAS = null;
    public Hashtable<Double, Double> npScoresNUBBE = null;
    public Hashtable<Double, Double> npScoresSANCDB = null;
    public Hashtable<Double, Double> npScoresDRUGBANK = null;
    public Hashtable<Double, Double> npScoresHMDB = null;
    public Hashtable<Double, Double> npScoresSUPERNATURAL = null;
    public Hashtable<Double, Double> npScoresAFRODB = null;
    public Hashtable<Double, Double> npScoresOLD2012 = null;

    public Hashtable<Double, Double> npScoresBACTERIA = null;
    public Hashtable<Double, Double> npScoresFUNGI = null;
    public Hashtable<Double, Double> npScoresPLANTS = null;


    public Hashtable<Double, Double> npScoresAC100 = null;
    public Hashtable<Double, Double> npScoresAC100200 = null;
    public Hashtable<Double, Double> npScoresAC200300 = null;
    public Hashtable<Double, Double> npScoresAC300 = null;






    public PlotData(){

    }





}
