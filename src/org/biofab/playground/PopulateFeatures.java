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
import org.biofab.hibernate.HibernateUtil;
import org.biofab.model.DNAMolecule;
import org.biofab.model.Feature;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * This class first removes all DNAMolecules Features from the db
 * and then re-populates the Features for each DNAMolecule
 * by matching Feature sequences against DNAMolecule sequences
 *
 * @author juul
 */
public class PopulateFeatures {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PopulateFeatures p = new PopulateFeatures();
        
    }

    Session session;

    public PopulateFeatures() {
        session = HibernateUtil.getSessionFactory().openSession();

        System.out.println("Running HQL query.");

        List features = query("from Feature");
        List results = query("from DNAMolecule");
        
        Iterator it = results.iterator();
        while(it.hasNext()) {
            DNAMolecule d = (DNAMolecule) it.next();
            if(d.getSeq() == null) {
                continue;
            }
            // Skip N-containing sequences
            if(checkNs(d.getSeq())) {
                System.out.println("Found N");
                continue;
            }

//            System.out.println("SEQ: " + d.getSeq() + "\n\n\n");

//            printFeatures(d);

            clearFeatures(d);

            createFeatures(d, features);
        }



        session.close();
    }

    private void clearFeatures(DNAMolecule d) {
        d.getFeatures().clear();
        session.beginTransaction();
        session.update(d);
        session.getTransaction().commit();
    }

    private void printFeatures(DNAMolecule d) {
        Set<Feature> fs = (Set<Feature>) d.getFeatures();
        Iterator it =fs.iterator();
        while(it.hasNext()) {
            Feature f = (Feature) it.next();
            System.out.println("f.getSeq() = " + f.getSeq());
        }
    }

    private List<Feature> createFeatures(DNAMolecule d, List allFeatures) {
        ArrayList<Feature> features = new ArrayList<Feature>();

        Iterator it = allFeatures.iterator();
        while(it.hasNext()) {
            Feature f = (Feature) it.next();



            createFeature(d, f);

        }

        return features;
    }

    private void createFeature(DNAMolecule d, Feature f) {
        Pattern p = Pattern.compile(f.getSeq(), Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(d.getSeq());
        while(m.find()) {
            int from = m.start();
            int to = m.start();
            d.getFeatures().add(f);
            session.beginTransaction();
            session.update(d);
            session.getTransaction().commit();

        }
    }

    private boolean checkNs(String seq) {
        Pattern p = Pattern.compile("N", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(seq);
        if(m.find()) {
            return true;
        }
        return false;
    }

    private List query(String hql) {
        Query q = session.createQuery(hql);
        session.beginTransaction();
        List resultList = q.list();
        session.getTransaction().commit();
        return resultList;
    }

}
