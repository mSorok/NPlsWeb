package de.unijena.cheminf.nplsweb.nplsweb.reader;

import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

public interface IReader {

    Hashtable<String, IAtomContainer> readMoleculesFromFile(File file);
}
