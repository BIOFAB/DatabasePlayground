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
import org.biofab.model.Design;
import org.biofab.model.Feature;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * This class first removes all Design Features from the db
 * and then re-populates the Features for each Design
 * by matching Feature sequences against Design sequences
 *
 */

public class PopulateFeatures
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        PopulateFeatures p = new PopulateFeatures();
    }

    Session session;

    public PopulateFeatures()
    {
        session = HibernateUtil.getSessionFactory().openSession();

        System.out.println("Running HQL query.");

        List features = query("from Feature");
        List designs = query("from Design");
        
        Iterator it = designs.iterator();
        
        while(it.hasNext())
        {
            Design design = (Design)it.next();
            
            if(design.getDna_sequence() == null)
            {
                continue;
            }
            
            // Skip N-containing sequences
            if(checkNs(design.getDna_sequence()))
            {
                System.out.println("Found N");
                continue;
            }

            clearFeatures(design);
            createFeatures(design, features);
        }

        session.close();
    }

    private void clearFeatures(Design design)
    {
        design.getFeatures().clear();
        session.beginTransaction();
        session.update(design);
        session.getTransaction().commit();
    }

    private void printFeatures(Design design)
    {
        Set<Feature> fs = (Set<Feature>) design.getFeatures();
        Iterator it =fs.iterator();
        
        while(it.hasNext())
        {
            Feature feature = (Feature) it.next();
            System.out.println("feature.getDna_sequence() = " + feature.getDna_sequence());
        }
    }

    private List<Feature> createFeatures(Design design, List allFeatures)
    {
        ArrayList<Feature> features = new ArrayList<Feature>();

        Iterator it = allFeatures.iterator();
        
        while(it.hasNext())
        {
            Feature f = (Feature) it.next();
            createFeature(design, f);
        }

        return features;
    }

    private void createFeature(Design design, Feature feature)
    {
        Pattern p = Pattern.compile(feature.getDna_sequence(), Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(design.getDna_sequence());
        
        while(m.find())
        {
            int from = m.start();
            int to = m.start();
            design.getFeatures().add(feature);
            session.beginTransaction();
            session.update(design);
            session.getTransaction().commit();

        }
    }

    private boolean checkNs(String seq)
    {
        Pattern p = Pattern.compile("N", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(seq);
        
        if(m.find())
        {
            return true;
        }
        
        return false;
    }

    private List query(String hql)
    {
        Query q = session.createQuery(hql);
        session.beginTransaction();
        List resultList = q.list();
        session.getTransaction().commit();
        return resultList;
    }

}
