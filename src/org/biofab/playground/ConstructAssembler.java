/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biofab.playground;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Enumeration;
import org.biofab.hibernate.HibernateUtil;
import org.biofab.model.Design;
import org.hibernate.Query;
import org.hibernate.Session;


import java.io.IOException;
import java.util.Hashtable;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;

import org.biofab.model.Part;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.seq.Feature;

import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.SimpleComment;
import org.biojavax.SimpleNote;
import org.biojavax.SimpleRichAnnotation;

import org.biofab.model.Part;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.RangeLocation;

import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.SimpleComment;
import org.biojavax.SimpleNote;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.bio.seq.*;

/**
 * This class first removes all DNAMolecules Features from the db
 * and then re-populates the Features for each Design
 * by matching Feature sequences against Design sequences
 *
 * @author juul
 */
public class ConstructAssembler
{
    protected Hashtable<String, Part>   _parts;
    protected Hashtable<String,String>  _idConversionTable;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        ConstructAssembler p = new ConstructAssembler();
    }

    public ConstructAssembler()
    {
        RichSequence        sequence;
        Part                part;
        Feature             feature = null;
        String[]            features;
        String              newID;
        String              componentID;
        Enumeration<String> keys;
        Statement statement = null;
        String responseString = null;

        this.populateDataStructures();
        keys = _idConversionTable.keys();

        while (keys.hasMoreElements())
        {
            Namespace ns = RichObjectFactory.getDefaultNamespace();
            sequence = null;
            newID = keys.nextElement();
            componentID = _idConversionTable.get(newID);

            features = componentID.split("\\.");

            String featureA = features[0];
            String featureB = "BFa_" + features[1];

            String preEOUSubseq = "tttagcttccttagctcctgaaaatctcgataactcaaaaaatacgcccggtagtgatcttatttcattatggtgaaagttggaacctcttacgtgccgatcaacgtctcattttcgccagatatc";
            String gfp = "ATGAGCAAAGGAGAAGAACTTTTCACTGGAGTTGTCCCAATTCTTGTTGAATTAGATGGTGATGTTAATGGGCACAAATTTTCTGTCCGTGGAGAGGGTGAAGGTGATGCTACAAACGGAAAACTCACCCTTAAATTTATTTGCACTACTGGAAAACTACCTGTTCCGTGGCCAACACTTGTCACTACTCTGACCTATGGTGTTCAATGCTTTTCCCGTTATCCGGATCACATGAAACGGCATGACTTTTTCAAGAGTGCCATGCCCGAAGGTTATGTACAGGAACGCACTATATCTTTCAAAGATGACGGGACCTACAAGACGCGTGCTGAAGTCAAGTTTGAAGGTGATACCCTTGTTAATCGTATCGAGTTAAAGGGTATTGATTTTAAAGAAGATGGAAACATTCTTGGACACAAACTCGAGTACAACTTTAACTCACACAATGTATACATCACGGCAGACAAACAAAAGAATGGAATCAAAGCTAACTTCAAAATTCGCCACAACGTTGAAGATGGTTCCGTTCAACTAGCAGACCATTATCAACAAAATACTCCAATTGGCGATGGCCCTGTCCTTTTACCAGACAACCATTACCTGTCGACACAATCTGTCCTTTCGAAAGATCCCAACGAAAAGCGTGACCACATGGTCCTTCTTGAGTTTGTAACTGCTGCTGGGATTACACATGGCATGGATGAGCTCTACAAA";
            String subseqA = "taaGGATCCaaactcgagtaaggatct";
            String dbITerminator = "ccaggcatcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgtttata";
            String subseqB = "CCTAGG";
            String p15A = "gatatattccgcttcctcgctcactgactcgctacgctcggtcgttcgactgcggcgagcggaaatggcttacgaacggggcggagatttcctggaagatgccaggaagatacttaacagggaagtgagagggccgcggcaaagccgtttttccataggctccgcccccctgacaagcatcacgaaatctgacgctcaaatcagtggtggcgaaacccgacaggactataaagataccaggcgtttccccctggcggctccctcgtgcgctctcctgttcctgcctttcggtttaccggtgtcattccgctgttatggccgcgtttgtctcattccacgcctgacactcagttccgggtaggcagttcgctccaagctggactgtatgcacgaaccccccgttcagtccgaccgctgcgccttatccggtaactatcgtcttgagtccaacccggaaagacatgcaaaagcaccactggcagcagccactggtaattgatttagaggagttagtcttgaagtcatgcgccggttaaggctaaactgaaaggacaagttttggtgactgcgctcctccaagccagttacctcggttcaaagagttggtagctcagagaaccttcgaaaaaccgccctgcaaggcggttttttcgttttcagagcaagagattacgcgcagaccaaaacgatctcaagaagatcatcttattaa";
            String subseqC = "tcagataaaatatttctagatttcagtgcaatttatctcttcaaatgtagcacctgaagtcagccccatacgatataagttgttactagt";
            String toTerminator = "gcttggattctcaccaataaaaaacgcccggcggcaaccgagcgttctgaacaaatccagatggagttctgaggtcattactggatctatcaacaggagtccaagc";
            String subseqD = "gagctcgatatcaaa";
            String cmR2 = "ttacgccccgccctgccactcatcgcagtactgttgtaattcattaagcattctgccgacatggaagccatcacaaacggcatgatgaacctgaatcgccagcggcatcagcaccttgtcgccttgcgtataatatttgcccatggtgaaaacgggggcgaagaagttgtccatattggccacgtttaaatcaaaactggtgaaactcacccagggattggctgagacgaaaaacatattctcaataaaccctttagggaaataggccaggttttcaccgtaacacgccacatcttgcgaatatatgtgtagaaactgccggaaatcgtcgtggtattcactccagagcgatgaaaacgtttcagtttgctcatggaaaacggtgtaacaagggtgaacactatcccatatcaccagctcaccgtctttcattgccatacgaaattccggatgagcattcatcaggcgggcaagaatgtgaataaaggccggataaaacttgtgcttatttttctttacggtctttaaaaaggccgtaatatccagctgaacggtctggttataggtacattgagcaactgactgaaatgcctcaaaatgttctttacgatgccattgggatatatcaacggtggtatatccagtgatttttttctccat";

            try
            {
                sequence = RichSequence.Tools.createRichSequence(newID, DNATools.createDNA(preEOUSubseq));
            }
            catch (IllegalSymbolException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

     //       sequence.setSeqVersion(1.0);
     //       sequence.setIdentifier(name);

            SimpleRichAnnotation sourceAnnotation = new SimpleRichAnnotation();
            sourceAnnotation.addNote(new SimpleNote(RichObjectFactory.getDefaultOntology().getOrCreateTerm("organism"),"Escherichia coli",0));
            sourceAnnotation.addNote(new SimpleNote(RichObjectFactory.getDefaultOntology().getOrCreateTerm("plasmid"),"VKM81",0));
            sourceAnnotation.addNote(new SimpleNote(RichObjectFactory.getDefaultOntology().getOrCreateTerm("strain"),"BW25113",0));
            sourceAnnotation.addNote(new SimpleNote(RichObjectFactory.getDefaultOntology().getOrCreateTerm("mol_type"),"other DNA",0));

            StrandedFeature.Template featureTemplate = new StrandedFeature.Template();
            featureTemplate.annotation = sourceAnnotation;
            featureTemplate.location = new RangeLocation(1,10);
            featureTemplate.source = "BIOFAB";
            featureTemplate.strand = StrandedFeature.POSITIVE;
            featureTemplate.type = "source";

            try
            {
              feature = sequence.createFeature(featureTemplate);
            }
            catch (Exception ex)
            {
              //ex.printStackTrace();
            }

            if(featureA != null && featureA.length() > 0)
            {
                featureA = featureA.toUpperCase();
                part = _parts.get(featureA);
                addPart(sequence, part, "promoter");
            }

            addNonAnnotatedSubsequence(sequence, "TTTG");

            if(featureB != null && featureB.length() > 0)
            {
                featureB = featureB.toUpperCase();
                part = _parts.get(featureB);
                addPart(sequence, part,"RBS");
            }

            addAnnotatedSubsequence(sequence, gfp, "CDS","gene","sfGFP");
            addNonAnnotatedSubsequence(sequence, subseqA);
            addAnnotatedSubsequence(sequence, dbITerminator, "terminator","label","dbI_terminator");
            addNonAnnotatedSubsequence(sequence, subseqB);
            addAnnotatedSubsequence(sequence, p15A, "rep_origin","label", "p15a");
            addNonAnnotatedSubsequence(sequence, subseqC);
            addAnnotatedSubsequence(sequence, toTerminator, "terminator","label", "To_terminator");
            addNonAnnotatedSubsequence(sequence, subseqD);
            addAnnotatedSubsequence(sequence, cmR2, "CDS","gene", "cmR2");

            feature.setLocation(new RangeLocation(1,sequence.length()));

            addComment(sequence, "The genetic constructs used here are taken or composed from available, well known genetic elements.  At this time BIOFAB staff have not yet taken care to define the precise functional boundaries of these genetic elements.  Thus, for example, a part labeled as a \"promoter\" may include sequences encoding all or part of a 5' UTR downstream of a transcription start site. And so on. Part of the mission of the BIOFAB is to define compatible sets of genetic objects with precise and composable boundaries. Such well engineered parts will be noted once available.");
            sequence.setCircular(true);
            System.out.println(newID + "," + sequence.seqString().toUpperCase());
        }

        System.out.println("Done");
    }

    protected void populateDataStructures()
    {
        _parts = new Hashtable<String, Part>();
        _parts.put("BFA_1", new Part("BFA_1", "J23101", "TTTACAGCTAGCTCAGTCCTAGGTATTATGCTAGC"));
        _parts.put("BFA_2", new Part("BFA_2", "J23109", "TTTACAGCTAGCTCAGTCCTAGGGACTGTGCTAGC"));
        _parts.put("BFA_3", new Part("BFA_3", "PLTETo1", "TCCCTATCAGTGATAGAGATTGACATCCCTATCAGTGATAGAGATACTGAGCACATCAGCAGGACGCACTGACC"));
        _parts.put("BFA_4", new Part("BFA_4", "galP1", "ATTCCACTAATTTATTCCATGTCACACTTTTCGCATCTTTGTTATGCTATGGTTATTTCATACCATAA"));
        _parts.put("BFA_5", new Part("BFA_5", "lacUV5", "CCCCAGGCTTTACACTTTATGCTTCCGGCTCGTATAATGTGTGGAATTGTGAG"));
        _parts.put("BFA_6", new Part("BFA_6", "pCat", "GGCACGTAAGAGGTTCCAACTTTCACCATAATGAAACA"));
        _parts.put("BFA_7", new Part("BFA_7", "pLlacO1", "ATAAATGTGAGCGGATAACAATTGACATTGTGAGCGGATAACAAGATACTGAGCACATCAGCAGGACGCACTGACC"));
        _parts.put("BFA_8", new Part("BFA_8", "pLux", "ACCTGTAGGATCGTACAGGTTTACGCAAGAAAATGGTTTGTTATAGTCGAATAAA"));
        _parts.put("BFA_9", new Part("BFA_9", "pT7A1", "ATTTAAAATTTATCAAAAAGAGTATTGACTTAAAGTCTAACCTATAGGATACTTACAGCCATCGAGAG"));
        _parts.put("BFA_10", new Part("BFA_10", "pTac", "TTGACAATTAATCATCCGGCTCGTATAATGTGTGGAATTGTGAG"));
        _parts.put("BFA_11", new Part("BFA_11", "pTet", "TAATTCCTAATTTTTGTTGACACTCTATCGTTGATAGAGTTATTTTACCACTCCCTATCAGTGATAGAGAAAA"));
        _parts.put("BFA_12", new Part("BFA_12", "pminor", "GTGACCCAATAATGTGGGATAACATTGAAAAGATTAAAGAAATATGGGAAAACTCTGGAAAATCCGGG"));
        _parts.put("BFA_13", new Part("BFA_13", "pTrp", "AATGAGCTGTTGACAATTAATCATCGAACTAGTTAACTAGTACGCA"));
        _parts.put("BFA_14", new Part("BFA_14", "Nopromoter (tac spacer)", "ATTAATCATCCG"));
        _parts.put("BFA_15", new Part("BFA_15", "Anderson_RBS", "TCTAGAGAAAGAGGGGACAAACTAGT"));
        _parts.put("BFA_16", new Part("BFA_16", "Bujard_RBS", "GAATTCATTAAAGAGGAGAAAGGTACC"));
        _parts.put("BFA_17", new Part("BFA_17", "B0030_RBS", "ATTAAAGAGGAGAAA"));
        _parts.put("BFA_18", new Part("BFA_18", "B0031_RBS", "TCACACAGGAAACC"));
        _parts.put("BFA_19", new Part("BFA_19", "B0032_RBS", "TCACACAGGAAAG"));
        _parts.put("BFA_20", new Part("BFA_20", "B0033_RBS", "TCACACAGGAC"));
        _parts.put("BFA_21", new Part("BFA_21", "B0034_RBS", "AAAGAGGAGAAA"));
        _parts.put("BFA_22", new Part("BFA_22", "GSG_RBS", "TAAGGAGGTGACAAT"));
        _parts.put("BFA_23", new Part("BFA_23", "GSGV_RBS", "GCTCTTTAACAATTTATCATAAGGAGGTGACAAT"));
        _parts.put("BFA_24", new Part("BFA_24", "Invitrogen_RBS", "AAAATTAAGAGGTATATATTA"));
        _parts.put("BFA_25", new Part("BFA_25", "JBEI_RBS", "GAATTCAAAAGATCTTTTAAGAAGGAGATATACAT"));
        _parts.put("BFA_26", new Part("BFA_26", "Plotkin_RBS", "TGGATCCAAGAAGGAGATATAACC"));
        _parts.put("BFA_27", new Part("BFA_27", "Alon_RBS", "GGATCCTCTAGATTTAAGAAGGAGATATACAT"));
        _parts.put("BFA_28", new Part("BFA_28", "DeadRBS", "CACCATACACTG"));

        _idConversionTable = new Hashtable<String, String>();
        _idConversionTable.put("pFAB21","BFa_1.15");
        _idConversionTable.put("pFAB22","BFa_1.16");
        _idConversionTable.put("pFAB23","BFa_1.17");
        _idConversionTable.put("pFAB24","BFa_1.19");
        _idConversionTable.put("pFAB25","BFa_1.21");
        _idConversionTable.put("pFAB26","BFa_1.22");
        _idConversionTable.put("pFAB27","BFa_1.23");
        _idConversionTable.put("pFAB28","BFa_1.24");
        _idConversionTable.put("pFAB29","BFa_1.25");
        _idConversionTable.put("pFAB30","BFa_1.26");
        _idConversionTable.put("pFAB31","BFa_1.27");
        _idConversionTable.put("pFAB32","BFa_1.28");
        _idConversionTable.put("pFAB33","BFa_2.15");
        _idConversionTable.put("pFAB34","BFa_2.16");
        _idConversionTable.put("pFAB35","BFa_2.17");
        _idConversionTable.put("pFAB36","BFa_2.19");
        _idConversionTable.put("pFAB37","BFa_2.21");
        _idConversionTable.put("pFAB38","BFa_2.22");
        _idConversionTable.put("pFAB39","BFa_2.23");
        _idConversionTable.put("pFAB40","BFa_2.24");
        _idConversionTable.put("pFAB41","BFa_2.25");
        _idConversionTable.put("pFAB42","BFa_2.26");
        _idConversionTable.put("pFAB43","BFa_2.27");
        _idConversionTable.put("pFAB44","BFa_2.28");
        _idConversionTable.put("pFAB45","BFa_3.15");
        _idConversionTable.put("pFAB46","BFa_3.16");
        _idConversionTable.put("pFAB47","BFa_3.17");
        _idConversionTable.put("pFAB48","BFa_3.19");
        _idConversionTable.put("pFAB49","BFa_3.21");
        _idConversionTable.put("pFAB50","BFa_3.22");
        _idConversionTable.put("pFAB51","BFa_3.23");
        _idConversionTable.put("pFAB52","BFa_3.24");
        _idConversionTable.put("pFAB53","BFa_3.25");
        _idConversionTable.put("pFAB54","BFa_3.26");
        _idConversionTable.put("pFAB55","BFa_3.27");
        _idConversionTable.put("pFAB56","BFa_3.28");
        _idConversionTable.put("pFAB57","BFa_4.15");
        _idConversionTable.put("pFAB58","BFa_4.16");
        _idConversionTable.put("pFAB59","BFa_4.17");
        _idConversionTable.put("pFAB60","BFa_4.19");
        _idConversionTable.put("pFAB61","BFa_4.21");
        _idConversionTable.put("pFAB62","BFa_4.22");
        _idConversionTable.put("pFAB63","BFa_4.23");
        _idConversionTable.put("pFAB64","BFa_4.24");
        _idConversionTable.put("pFAB65","BFa_4.25");
        _idConversionTable.put("pFAB66","BFa_4.26");
        _idConversionTable.put("pFAB67","BFa_4.27");
        _idConversionTable.put("pFAB68","BFa_4.28");
        _idConversionTable.put("pFAB69","BFa_5.15");
        _idConversionTable.put("pFAB70","BFa_5.16");
        _idConversionTable.put("pFAB71","BFa_5.17");
        _idConversionTable.put("pFAB72","BFa_5.19");
        _idConversionTable.put("pFAB73","BFa_5.21");
        _idConversionTable.put("pFAB74","BFa_5.22");
        _idConversionTable.put("pFAB75","BFa_5.23");
        _idConversionTable.put("pFAB76","BFa_5.24");
        _idConversionTable.put("pFAB77","BFa_5.25");
        _idConversionTable.put("pFAB78","BFa_5.26");
        _idConversionTable.put("pFAB79","BFa_5.27");
        _idConversionTable.put("pFAB80","BFa_5.28");
        _idConversionTable.put("pFAB81","BFa_7.15");
        _idConversionTable.put("pFAB82","BFa_7.16");
        _idConversionTable.put("pFAB83","BFa_7.17");
        _idConversionTable.put("pFAB84","BFa_7.19");
        _idConversionTable.put("pFAB85","BFa_7.21");
        _idConversionTable.put("pFAB86","BFa_7.22");
        _idConversionTable.put("pFAB87","BFa_7.23");
        _idConversionTable.put("pFAB88","BFa_7.24");
        _idConversionTable.put("pFAB89","BFa_7.25");
        _idConversionTable.put("pFAB90","BFa_7.26");
        _idConversionTable.put("pFAB91","BFa_7.27");
        _idConversionTable.put("pFAB92","BFa_7.28");
        _idConversionTable.put("pFAB93","BFa_8.15");
        _idConversionTable.put("pFAB94","BFa_8.16");
        _idConversionTable.put("pFAB95","BFa_8.17");
        _idConversionTable.put("pFAB96","BFa_8.19");
        _idConversionTable.put("pFAB97","BFa_8.21");
        _idConversionTable.put("pFAB98","BFa_8.22");
        _idConversionTable.put("pFAB99","BFa_8.23");
        _idConversionTable.put("pFAB100","BFa_8.24");
        _idConversionTable.put("pFAB101","BFa_8.25");
        _idConversionTable.put("pFAB102","BFa_8.26");
        _idConversionTable.put("pFAB103","BFa_8.27");
        _idConversionTable.put("pFAB104","BFa_8.28");
        _idConversionTable.put("pFAB105","BFa_9.15");
        _idConversionTable.put("pFAB106","BFa_9.16");
        _idConversionTable.put("pFAB107","BFa_9.17");
        _idConversionTable.put("pFAB108","BFa_9.19");
        _idConversionTable.put("pFAB109","BFa_9.21");
        _idConversionTable.put("pFAB110","BFa_9.22");
        _idConversionTable.put("pFAB111","BFa_9.23");
        _idConversionTable.put("pFAB112","BFa_9.24");
        _idConversionTable.put("pFAB113","BFa_9.25");
        _idConversionTable.put("pFAB114","BFa_9.26");
        _idConversionTable.put("pFAB115","BFa_9.27");
        _idConversionTable.put("pFAB116","BFa_9.28");
        _idConversionTable.put("pFAB117","BFa_10.15");
        _idConversionTable.put("pFAB118","BFa_10.16");
        _idConversionTable.put("pFAB119","BFa_10.17");
        _idConversionTable.put("pFAB120","BFa_10.19");
        _idConversionTable.put("pFAB121","BFa_10.21");
        _idConversionTable.put("pFAB122","BFa_10.22");
        _idConversionTable.put("pFAB123","BFa_10.23");
        _idConversionTable.put("pFAB124","BFa_10.24");
        _idConversionTable.put("pFAB125","BFa_10.25");
        _idConversionTable.put("pFAB126","BFa_10.26");
        _idConversionTable.put("pFAB127","BFa_10.27");
        _idConversionTable.put("pFAB128","BFa_10.28");
        _idConversionTable.put("pFAB129","BFa_11.15");
        _idConversionTable.put("pFAB130","BFa_11.16");
        _idConversionTable.put("pFAB131","BFa_11.17");
        _idConversionTable.put("pFAB132","BFa_11.19");
        _idConversionTable.put("pFAB133","BFa_11.21");
        _idConversionTable.put("pFAB134","BFa_11.22");
        _idConversionTable.put("pFAB135","BFa_11.23");
        _idConversionTable.put("pFAB136","BFa_11.24");
        _idConversionTable.put("pFAB137","BFa_11.25");
        _idConversionTable.put("pFAB138","BFa_11.26");
        _idConversionTable.put("pFAB139","BFa_11.27");
        _idConversionTable.put("pFAB140","BFa_11.28");
        _idConversionTable.put("pFAB141","BFa_12.15");
        _idConversionTable.put("pFAB142","BFa_12.16");
        _idConversionTable.put("pFAB143","BFa_12.17");
        _idConversionTable.put("pFAB144","BFa_12.19");
        _idConversionTable.put("pFAB145","BFa_12.21");
        _idConversionTable.put("pFAB146","BFa_12.22");
        _idConversionTable.put("pFAB147","BFa_12.23");
        _idConversionTable.put("pFAB148","BFa_12.24");
        _idConversionTable.put("pFAB149","BFa_12.25");
        _idConversionTable.put("pFAB150","BFa_12.26");
        _idConversionTable.put("pFAB151","BFa_12.27");
        _idConversionTable.put("pFAB152","BFa_12.28");
        _idConversionTable.put("pFAB153","BFa_13.15");
        _idConversionTable.put("pFAB154","BFa_13.16");
        _idConversionTable.put("pFAB155","BFa_13.17");
        _idConversionTable.put("pFAB156","BFa_13.19");
        _idConversionTable.put("pFAB157","BFa_13.21");
        _idConversionTable.put("pFAB158","BFa_13.22");
        _idConversionTable.put("pFAB159","BFa_13.23");
        _idConversionTable.put("pFAB160","BFa_13.24");
        _idConversionTable.put("pFAB161","BFa_13.25");
        _idConversionTable.put("pFAB162","BFa_13.26");
        _idConversionTable.put("pFAB163","BFa_13.27");
        _idConversionTable.put("pFAB164","BFa_13.28");
        _idConversionTable.put("pFAB165","BFa_14.15");
        _idConversionTable.put("pFAB166","BFa_14.16");
        _idConversionTable.put("pFAB167","BFa_14.17");
        _idConversionTable.put("pFAB168","BFa_14.19");
        _idConversionTable.put("pFAB169","BFa_14.21");
        _idConversionTable.put("pFAB170","BFa_14.22");
        _idConversionTable.put("pFAB171","BFa_14.23");
        _idConversionTable.put("pFAB172","BFa_14.24");
        _idConversionTable.put("pFAB173","BFa_14.25");
        _idConversionTable.put("pFAB174","BFa_14.26");
        _idConversionTable.put("pFAB175","BFa_14.27");
        _idConversionTable.put("pFAB176","BFa_14.28");
    }

    protected void addAnnotatedSubsequence(RichSequence sequence, String subSeq, String featureKey, String noteKey, String noteValue)
    {
        Edit edit;
        int start;
        int end;

        start = sequence.length() + 1;
        end = sequence.length() + subSeq.length();

        try
        {
            edit = new Edit(sequence.length() + 1, 0, DNATools.createDNA(subSeq.toUpperCase()));
            sequence.edit(edit);
        }
        catch (Exception e)
        {

        }

        SimpleRichAnnotation annotation = new SimpleRichAnnotation();
        annotation.addNote(new SimpleNote(RichObjectFactory.getDefaultOntology().getOrCreateTerm(noteKey),noteValue,0));

        StrandedFeature.Template featureTemplate = new StrandedFeature.Template();
        featureTemplate.annotation = annotation;
        featureTemplate.location = new RangeLocation(start,end);
        featureTemplate.source = "BIOFAB";
        featureTemplate.strand = StrandedFeature.POSITIVE;
        featureTemplate.type = featureKey;

        try
        {
          sequence.createFeature(featureTemplate);
        }
        catch (Exception ex)
        {
          //ex.printStackTrace();
        }
    }

    protected void addPart(RichSequence sequence, Part part, String featureKey)
    {
        Edit edit;
        int start;
        int end;

        start = sequence.length() + 1;
        end = sequence.length() + part.getSequence().length();

        try
        {
            edit = new Edit(sequence.length() + 1, 0, DNATools.createDNA(part.getSequence().toUpperCase()));
            sequence.edit(edit);
        }
        catch (Exception e)
        {

        }

        SimpleRichAnnotation annotation = new SimpleRichAnnotation();
        annotation.addNote(new SimpleNote(RichObjectFactory.getDefaultOntology().getOrCreateTerm("label"),part.getDescription(),0));
        //annotation.addNote(new SimpleNote(RichObjectFactory.getDefaultOntology().getOrCreateTerm("biofab_id"),part.getID(),0));

        StrandedFeature.Template featureTemplate = new StrandedFeature.Template();
        featureTemplate.annotation = annotation;
        featureTemplate.location = new RangeLocation(start,end);
        featureTemplate.source = "BIOFAB";
        featureTemplate.strand = StrandedFeature.POSITIVE;
        featureTemplate.type = featureKey;

        try
        {
          sequence.createFeature(featureTemplate);
        }
        catch (Exception ex)
        {
          //ex.printStackTrace();
        }
    }

    protected void addAnnotation(RichSequence sequence, String featureType, String noteKey, String noteValue, int start, int end)
    {
        SimpleRichAnnotation annotation = new SimpleRichAnnotation();
        annotation.addNote(new SimpleNote(RichObjectFactory.getDefaultOntology().getOrCreateTerm(noteKey),noteValue,0));

        StrandedFeature.Template featureTemplate = new StrandedFeature.Template();
        featureTemplate.annotation = annotation;
        featureTemplate.location = new RangeLocation(start,end);
        featureTemplate.source = "BIOFAB";
        featureTemplate.strand = StrandedFeature.POSITIVE;
        featureTemplate.type = featureType;

        try
        {
          sequence.createFeature(featureTemplate);
        }
        catch (Exception ex)
        {
          //ex.printStackTrace();
        }
    }

    protected void addNonAnnotatedSubsequence(RichSequence sequence, String seqString)
    {
        Edit edit;

        try
        {
            edit = new Edit(sequence.length() + 1, 0, DNATools.createDNA(seqString.toUpperCase()));
            sequence.edit(edit);
        }
        catch (Exception e)
        {

        }
    }

    protected void addComment(RichSequence seq, String comment)
    {
        seq.addComment(new SimpleComment(comment, 0));
    }
}
